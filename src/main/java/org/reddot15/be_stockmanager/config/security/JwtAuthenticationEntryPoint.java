package org.reddot15.be_stockmanager.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(
			HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException {
		// Get error code
		ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
		// Set response
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		// new ResponseObject
		ResponseObject responseObject = new ResponseObject(errorCode.getCode(), errorCode.getMessage(), null);
		// Mapper
		ObjectMapper objectMapper = new ObjectMapper();
		// Write response
		response.getWriter().write(objectMapper.writeValueAsString(responseObject));
		response.flushBuffer();
	}
}
