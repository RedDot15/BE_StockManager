package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.dto.response.CategoryRevenueStatResponse;
import org.reddot15.be_stockmanager.dto.response.ProductRevenueStatResponse;
import org.reddot15.be_stockmanager.dto.response.VendorRevenueStatResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.SaleItem;
import org.reddot15.be_stockmanager.entity.Vendor;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.repository.InvoiceRepository;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.repository.VendorRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class RevenueStatService {
    InvoiceRepository invoiceRepository;
    VendorRepository vendorRepository;
    ProductRepository productRepository;

    @PreAuthorize("hasAuthority('VIEW_FINANCIAL_STATISTIC')")
    public List<VendorRevenueStatResponse> getRevenueStatsByVendor(String startDate, String endDate) {
        // Retrieve invoices within the specified date range using the repository
        List<Invoice> invoices = invoiceRepository.findInvoicesByCreatedAtBetween(startDate, endDate);

        // Grouping sale items by vendor
        Map<String, Double> vendorRevenueMap = invoices.stream()
                .flatMap(invoice -> invoice.getSales() != null ? invoice.getSales().stream() : null)
                .collect(Collectors.groupingBy( // Group by vendorId
                        SaleItem::getVendorId,
                        Collectors.summingDouble(saleItem -> saleItem.getAmount() * saleItem.getPrice()) // Sum (amount * price) for each vendor
                ));

        // Convert the map to a list of VendorRevenueStat DTOs
        return vendorRevenueMap.entrySet().stream()
                .map(entry -> {
                    // Get entity
                    Vendor entity = vendorRepository.findVendorById(entry.getKey())
                            .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
                    // Return
                    return VendorRevenueStatResponse.builder()
                            .id(entity.getEntityId())
                            .name(entity.getName())
                            .totalRevenue(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('VIEW_FINANCIAL_STATISTIC')")
    public List<ProductRevenueStatResponse> getRevenueStatsByProduct(String startDate, String endDate) {
        // Retrieve invoices within the specified date range using the repository
        List<Invoice> invoices = invoiceRepository.findInvoicesByCreatedAtBetween(startDate, endDate);

        // Grouping sale items by product
        Map<String, Double> productRevenueMap = invoices.stream()
                .flatMap(invoice -> invoice.getSales() != null ? invoice.getSales().stream() : null)
                .collect(Collectors.groupingBy( // Group by productId
                        SaleItem::getProductId,
                        Collectors.summingDouble(saleItem -> saleItem.getAmount() * saleItem.getPrice())
                ));

        // Convert the map to a list of ProductRevenueStat DTOs
        return productRevenueMap.entrySet().stream()
                .map(entry -> {
                    // Get entity
                    Product productEntity = productRepository.findProductById(entry.getKey())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                    Vendor vendorEntity = vendorRepository.findVendorById(productEntity.getVendorId())
                            .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
                    // Return
                    return ProductRevenueStatResponse.builder()
                            .id(productEntity.getEntityId())
                            .name(productEntity.getName())
                            .vendorName(vendorEntity.getName())
                            .categoryName(productEntity.getCategoryName())
                            .amount(productEntity.getAmount())
                            .totalRevenue(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('VIEW_FINANCIAL_STATISTIC')")
    public List<CategoryRevenueStatResponse> getRevenueStatsByCategory(String startDate, String endDate) {
        // Retrieve invoices within the specified date range using the repository
        List<Invoice> invoices = invoiceRepository.findInvoicesByCreatedAtBetween(startDate, endDate);

        // Grouping sale items by category
        Map<String, Double> categoryRevenueMap = invoices.stream()
                .flatMap(invoice -> invoice.getSales() != null ? invoice.getSales().stream() : null)
                .collect(Collectors.groupingBy( // Group by productId
                        SaleItem::getCategoryName,
                        Collectors.summingDouble(saleItem -> saleItem.getAmount() * saleItem.getPrice())
                ));

        // Convert the map to a list of CategoryRevenueStat DTOs
        return categoryRevenueMap.entrySet().stream()
                .map(entry -> CategoryRevenueStatResponse.builder()
                        .name(entry.getKey())
                        .totalRevenue(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
    }
}