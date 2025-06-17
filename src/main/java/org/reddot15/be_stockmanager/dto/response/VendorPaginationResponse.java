package org.reddot15.be_stockmanager.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.reddot15.be_stockmanager.dto.response.VendorResponse;

import java.util.List;

@Getter
@Setter
@Builder
public class VendorPaginationResponse {
    private List<VendorResponse> vendors;
    private String nextPageToken; // Base64 encoded lastEvaluatedKey
    private boolean hasMore; // Convenience flag for client
}