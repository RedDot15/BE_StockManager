package org.reddot15.be_stockmanager.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VendorRevenueStatResponse {
	String id;

	String name;

	Double totalRevenue;
}
