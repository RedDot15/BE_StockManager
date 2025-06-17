package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.dto.request.AuthenticationRequest;
import org.reddot15.be_stockmanager.dto.request.RefreshRequest;
import org.reddot15.be_stockmanager.dto.response.AuthenticationResponse;
import org.reddot15.be_stockmanager.dto.response.RefreshResponse;
import org.reddot15.be_stockmanager.entity.User;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class AuthenticationService {
	PasswordEncoder passwordEncoder;
	UserRepository userRepository;
	RedisAuthService redisAuthService;
	TokenService tokenService;

	@NonFinal
	@Value("${jwt.refreshable-duration}")
	Long REFRESHABLE_DURATION;

	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		// Fetch
		User user = userRepository
				.findUserByEmail(request.getEmail())
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
		// Authenticate
		boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
		if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
		// Generate token
		String uuid = UUID.randomUUID().toString();
		String refreshToken = tokenService.generateToken(user, true, uuid);
		String accessToken = tokenService.generateToken(user, false, uuid);
		// Return token
		return AuthenticationResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
	}

	public RefreshResponse refresh(RefreshRequest request) {
		// Verify token
		try {
			Jwt jwt = tokenService.verifyToken(request.getRefreshToken(), true);
			// Get token information
			User userEntity = userRepository
					.findUserByEmail(jwt.getSubject())
					.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
			String jti = jwt.getClaim("jti");
			Date expiryTime = Date.from(jwt.getClaim("exp"));
			// Build & Save invalid token
			redisAuthService.saveInvalidatedTokenExpirationKey(jti, expiryTime.toInstant().toEpochMilli());
			// Generate new token
			String uuid = UUID.randomUUID().toString();
			String refreshToken = tokenService.generateToken(userEntity, true, uuid);
			String accessToken = tokenService.generateToken(userEntity, false, uuid);
			// Return token
			return RefreshResponse.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.build();
		} catch (JwtException e) {
			throw new AppException(ErrorCode.UNAUTHENTICATED);
		}
	}

	public void logout() {
		// Get Jwt token from Context
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Jwt jwt = (Jwt) authentication.getPrincipal();
		// Get token information
		String jti = jwt.getClaim("rid");
		Instant expiryTime = Instant.from(jwt.getClaim("iat")).plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS);
		// Save invalid token
		redisAuthService.saveInvalidatedTokenExpirationKey(jti, expiryTime.toEpochMilli());
	}
}
