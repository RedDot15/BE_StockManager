package org.reddot15.be_stockmanager.dto.response;

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

	Integer amount;

	String earliestExpiry;

	Double vat;
}
