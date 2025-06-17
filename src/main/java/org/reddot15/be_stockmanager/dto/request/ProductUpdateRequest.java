package org.reddot15.be_stockmanager.dto.request;

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
public class ProductUpdateRequest {
	@NotBlank(message = "Vendor ID is required.")
	String vendorId;

	@NotBlank(message = "Name is required.")
	String name;

	@NotBlank(message = "Category name is required.")
	String categoryName;

	@NotNull(message = "Import price is required.")
	Double importPrice;

	@NotNull(message = "Sale price is required.")
	Double salePrice;

	@NotNull(message = "Amount is required.")
	Double amount;

	@NotBlank(message = "Earliest expiry is required.")
	String earliestExpiry;

	@NotNull(message = "VAT is required.")
	Double vat;
}
