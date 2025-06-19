package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.dto.response.CategoryRevenueStatResponse;
import org.reddot15.be_stockmanager.dto.response.ProductRevenueStatResponse;
import org.reddot15.be_stockmanager.dto.response.VendorRevenueStatResponse;
import org.reddot15.be_stockmanager.dto.response.pagination.PageResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.SaleItem;
import org.reddot15.be_stockmanager.entity.Vendor;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.repository.InvoiceRepository;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.repository.VendorRepository;
import org.reddot15.be_stockmanager.util.ListPaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
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
    public PageResponse<VendorRevenueStatResponse> getRevenueStatsByVendor(
            String startDate,
            String endDate,
            Integer pageNumber,
            Integer pageSize
    ) {
        List<VendorRevenueStatResponse> allStats = getRevenueStats(
                startDate,
                endDate,
                SaleItem::getVendorId,
                entry -> {
                    Vendor entity = vendorRepository.findVendorById(entry.getKey())
                            .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
                    return VendorRevenueStatResponse.builder()
                            .id(entity.getEntityId())
                            .name(entity.getName())
                            .totalRevenue(entry.getValue())
                            .build();
                },
                Comparator.comparing(VendorRevenueStatResponse::getId)
        );

        return ListPaginationUtil.paginateList(allStats, pageNumber, pageSize);
    }

    @PreAuthorize("hasAuthority('VIEW_FINANCIAL_STATISTIC')")
    public PageResponse<ProductRevenueStatResponse> getRevenueStatsByProduct(
            String startDate,
            String endDate,
            Integer pageNumber,
            Integer pageSize
    ) {
        List<ProductRevenueStatResponse> allStats = getRevenueStats(
                startDate,
                endDate,
                SaleItem::getProductId,
                entry -> {
                    Product productEntity = productRepository.findProductById(entry.getKey())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                    Vendor vendorEntity = vendorRepository.findVendorById(productEntity.getVendorId())
                            .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
                    return ProductRevenueStatResponse.builder()
                            .id(productEntity.getEntityId())
                            .name(productEntity.getName())
                            .vendorName(vendorEntity.getName())
                            .categoryName(productEntity.getCategoryName())
                            .amount(productEntity.getAmount())
                            .totalRevenue(entry.getValue())
                            .build();
                },
                Comparator.comparing(ProductRevenueStatResponse::getId) // Assuming ProductRevenueStatResponse also has an ID
        );

        return ListPaginationUtil.paginateList(allStats, pageNumber, pageSize);
    }

    @PreAuthorize("hasAuthority('VIEW_FINANCIAL_STATISTIC')")
    public PageResponse<CategoryRevenueStatResponse> getRevenueStatsByCategory(
            String startDate,
            String endDate,
            Integer pageNumber,
            Integer pageSize
    ) {
        List<CategoryRevenueStatResponse> allStats = getRevenueStats(
                startDate,
                endDate,
                SaleItem::getCategoryName,
                entry -> CategoryRevenueStatResponse.builder()
                        .name(entry.getKey())
                        .totalRevenue(entry.getValue())
                        .build(),
                Comparator.comparing(CategoryRevenueStatResponse::getName) // Assuming CategoryRevenueStatResponse is sorted by name
        );

        return ListPaginationUtil.paginateList(allStats, pageNumber, pageSize);
    }

    // Generic method to calculate revenue stats
    private <T> List<T> getRevenueStats(
            String startDate,
            String endDate,
            Function<SaleItem, String> groupBy,
            Function<Map.Entry<String, Double>, T> mapper,
            Comparator<T> comparator) {

        List<Invoice> invoices = invoiceRepository.findInvoicesByCreatedAtBetween(startDate, endDate);

        Map<String, Double> revenueMap = invoices.stream()
                .flatMap(invoice -> invoice.getSales() != null ? invoice.getSales().stream() : null)
                .filter(Objects::nonNull) // Filter out null SaleItems if any from the flatMap
                .collect(Collectors.groupingBy(
                        groupBy,
                        Collectors.summingDouble(saleItem -> saleItem.getAmount() * saleItem.getPrice())
                ));

        return revenueMap.entrySet().stream()
                .map(mapper)
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}