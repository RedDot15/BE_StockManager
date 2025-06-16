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
public class VendorCreateRequest {
	@NotBlank(message = "ID is required.")
	String entityId;

	@NotBlank(message = "Name is required.")
	String name;
}
