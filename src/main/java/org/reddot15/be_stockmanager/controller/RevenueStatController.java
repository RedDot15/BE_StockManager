package org.reddot15.be_stockmanager.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.CategoryRevenueStatResponse;
import org.reddot15.be_stockmanager.dto.response.ProductRevenueStatResponse;
import org.reddot15.be_stockmanager.dto.response.VendorRevenueStatResponse;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.reddot15.be_stockmanager.service.RevenueStatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.reddot15.be_stockmanager.helper.ResponseBuilder.buildResponse;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/stats") // Base path for all statistics endpoints
public class RevenueStatController {
    RevenueStatService revenueStatService;

    @GetMapping("/vendor-revenue")
    public ResponseEntity<ResponseObject> getVendorRevenueStats(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        List<VendorRevenueStatResponse> stats = revenueStatService.getRevenueStatsByVendor(startDate, endDate);
        return buildResponse(HttpStatus.OK, "Get revenue stats by vendor successfully.", stats);
    }

    @GetMapping("/product-revenue")
    public ResponseEntity<ResponseObject> getProductRevenueStats(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        List<ProductRevenueStatResponse> stats = revenueStatService.getRevenueStatsByProduct(startDate, endDate);
        return buildResponse(HttpStatus.OK, "Get revenue stats by product successfully.", stats);
    }

    @GetMapping("/category-revenue")
    public ResponseEntity<ResponseObject> getCategoryRevenueStats(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        List<CategoryRevenueStatResponse> stats = revenueStatService.getRevenueStatsByCategory(startDate, endDate);
        return buildResponse(HttpStatus.OK, "Get revenue stats by product successfully.", stats);
    }
}