package org.reddot15.be_stockmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
	@NotBlank(message = "Username is required.")
	String email;

	@NotBlank(message = "Password is required.")
	String password;
}
