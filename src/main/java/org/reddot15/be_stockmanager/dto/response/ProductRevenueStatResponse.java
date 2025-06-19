package org.reddot15.be_stockmanager.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRevenueStatResponse {
	String id;

	String name;

	String vendorName;

	String categoryName;

	Integer amount;

	Double totalRevenue;
}
