package org.reddot15.be_stockmanager.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
	String entityId;

	String vendorId;

	String name;

	String categoryName;

	Double importPrice;

	Double salePrice;

	Double amount;

	String earliestExpiry;

	Double vat;
}
