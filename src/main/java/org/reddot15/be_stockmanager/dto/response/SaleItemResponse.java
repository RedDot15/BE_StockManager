package org.reddot15.be_stockmanager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleItemResponse {
	String productId;

	String vendorId;

	String categoryName;

	Integer amount;

	Double price;

	Double vat;
}
