package org.reddot15.be_stockmanager.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.entity.Permission;
import org.reddot15.be_stockmanager.entity.Role;
import org.reddot15.be_stockmanager.entity.User;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.repository.PermissionRepository;
import org.reddot15.be_stockmanager.repository.RoleRepository;
import org.reddot15.be_stockmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class TokenService {
	NimbusJwtDecoder nimbusJwtDecoder;
	RedisAuthService redisAuthService;
	UserRepository userRepository;
	RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;

	@NonFinal
	@Value("${jwt.signer-key}")
	String SIGNER_KEY;

	@NonFinal
	@Value("${jwt.valid-duration}")
	Long VALID_DURATION;

	@NonFinal
	@Value("${jwt.refreshable-duration}")
	Long REFRESHABLE_DURATION;

	public String generateToken(User user, Boolean isRefreshToken, String jti) {
		// Define Header
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
		// Calculate expiration time based on token type
		Long duration = isRefreshToken ? REFRESHABLE_DURATION : VALID_DURATION;
		Date expirationTime = Date.from(Instant.now().plus(duration, ChronoUnit.SECONDS));
		// Define Body: ClaimSet
		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
				.subject(user.getEmail())
				.issuer("reddot15.com")
				.issueTime(new Date())
				.expirationTime(expirationTime)
				.jwtID(isRefreshToken ? jti : null);

		if (!isRefreshToken) {
			claimsBuilder
					.claim("rid", jti)
					.claim("scope", buildScope(user.getRoleIds()))
					.claim("uid", user.getEntityId());
		}

		JWTClaimsSet jwtClaimsSet = claimsBuilder.build();
		// Define Body: Payload
		Payload payload = new Payload(jwtClaimsSet.toJSONObject());
		// Define JWSObject
		JWSObject jwsObject = new JWSObject(header, payload);
		// Sign JWSObject & Return
		try {
			jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
			return jwsObject.serialize();
		} catch (JOSEException e) {
			log.error("Cannot create token", e);
			throw new RuntimeException(e);
		}
	}

	public Jwt verifyToken(String token, Boolean isRefreshToken) {
		try {
			// Decode jwt (function include integrity verify & expiry verify)
			Jwt jwt = nimbusJwtDecoder.decode(token);
			// Validate token based on type
			String tokenId = isRefreshToken ? jwt.getClaim("jti") : jwt.getClaim("rid");
			if (Objects.isNull(tokenId) || redisAuthService.getInvalidatedTokenExpirationKey(tokenId) != null) {
				throw new JwtException("Invalid token");
			}
			if (userRepository.findUserByEmail(jwt.getSubject()).isEmpty()) throw new JwtException("Invalid user");
			// Return jwt
			return jwt;
		} catch (JwtException e) {
			log.error("JWT decoding failed: {}", e.getMessage());
			throw e; // Re-throw to let Spring Security handle it
		}
	}

	private String buildScope(List<String> roleIds){
		// Result string
		StringJoiner stringJoiner = new StringJoiner(" ");
		// Role exists?:
		if (!CollectionUtils.isEmpty(roleIds))
			// For each role:
			roleIds.forEach(roleId -> {
				// Get role
				Role role = roleRepository.findRoleById(roleId)
						.orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
				// Add role
				stringJoiner.add("ROLE_" + role.getName());
				// Permission exists?:
				if (!CollectionUtils.isEmpty(role.getPermissionIds()))
					// For each permission:
					role.getPermissionIds().forEach(permissionId -> {
						// Get permission
						Permission permission = permissionRepository.findPermissionById(permissionId)
										.orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
						// Add permission
						stringJoiner.add(permission.getName());
					});
			});
		// Return
		return stringJoiner.toString();
	}
}
