package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.ProductMapper;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.repository.VendorRepository;
import org.reddot15.be_stockmanager.util.CSVUtil;
import org.reddot15.be_stockmanager.util.ExcelUtil;
import org.reddot15.be_stockmanager.util.QueryConditionalBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class ProductService {
	ProductRepository productRepository;
	ProductMapper productMapper;
	VendorRepository vendorRepository;

	@PreAuthorize("hasAuthority('IMPORT_PRODUCT')")
	public List<ProductResponse> importProductFromCSV(MultipartFile file) {
		// File empty exception
		if (file.isEmpty()) {
			throw new AppException(ErrorCode.EMPTY_FILE);
		}

		// Initialize result variable
		List<Product> importedProducts = new ArrayList<>();
		// Parse file
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
			 CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
			// For each record
			for (CSVRecord csvRecord : csvParser.getRecords()) {
				try {
					// Process Record
					processCsvRecord(csvRecord, importedProducts);
				} catch (IllegalArgumentException | DateTimeParseException e) {
					// Invalid record exception
					log.error("Invalid record: {}", csvRecord.toString(), e);
					throw new AppException(ErrorCode.INVALID_RECORD);
				}
			}
		} catch (IOException e) {
			throw new AppException(ErrorCode.FILE_PARSE_FAILED);
		}

		return importedProducts.stream()
				.map(productMapper::toResponse)
				.toList();
	}

	private void processCsvRecord(CSVRecord csvRecord, List<Product> importedProducts) {
		// Get product ID
		String productId = csvRecord.get("entity_id");
		// Get exists product
		Optional<Product> foundOptionalProduct = productRepository.findProductById(productId);
		// Map imported product from CSV
		Product productToSave = CSVUtil.createProductFromCsvRecord(csvRecord);

		// If not exists
		if (foundOptionalProduct.isEmpty()) {
			// Add the new product
			importedProducts.add(productRepository.saveProduct(productToSave));
		} else {
			// Update exists product
			Product existingProduct = foundOptionalProduct.get();
			productMapper.updateExistingProduct(existingProduct, productToSave);
			importedProducts.add(productRepository.saveProduct(existingProduct));
		}
	}

	@PreAuthorize("hasAuthority('CREATE_PRODUCT')")
	public ProductResponse create(ProductCreateRequest request) {
		// Duplicate exception
		Optional<Product> optionalProduct = productRepository.findProductById(request.getEntityId());
		if (optionalProduct.isPresent())
			throw new AppException(ErrorCode.PRODUCT_DUPLICATE);
		// Vendor not found exception
		if (vendorRepository.findVendorById(request.getVendorId()).isEmpty())
			throw new AppException(ErrorCode.VENDOR_NOT_FOUND);
		// Mappping
		Product entity = productMapper.toEntity(request);
		// Save
		return productMapper.toResponse(productRepository.saveProduct(entity));
	}

	@PreAuthorize("hasAuthority('VIEW_PRODUCT')")
	public DDBPageResponse<ProductResponse> getAll(
			String keyword,
			String categoryName,
			Double minPrice,
			Double maxPrice,
			Integer limit,
			String nextPageToken) {
		// Default limit if not provided - This remains in the service as business logic
		if (limit == null || limit <= 0) {
			limit = 10;
		}

		// Delegate the full pagination logic to the repository
		DDBPageResponse<Product> productPage = productRepository.findAllPaginatedProducts(
				keyword,
				categoryName,
				minPrice,
				maxPrice,
				limit,
				nextPageToken);

		// Mapping to response DTOs - This remains in the service as presentation logic
		List<ProductResponse> productResponses = productPage.getItems().stream()
				.map(productMapper::toResponse)
				.toList();

		// Return the paginated response with DTOs
		return DDBPageResponse.<ProductResponse>builder()
				.items(productResponses)
				.nextPageToken(productPage.getNextPageToken())
				.hasMore(productPage.isHasMore())
				.build();
	}

	public Path exportProductsToExcel(
			String keyword,
			String categoryName,
			Double minPrice,
			Double maxPrice) {
		// --- Database Query Logic (unchanged) ---
		final boolean useGsiQuery = categoryName != null && !categoryName.isBlank();
		Expression filterExpression = null;
		if (keyword != null && !keyword.isBlank()) {
			filterExpression = Expression.builder()
					.expression("(contains(#name, :keyword) OR contains(#vendorId, :keyword))")
					.putExpressionName("#name", "name")
					.putExpressionName("#vendorId", "vendor_id")
					.putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
					.build();
		}

		String index;
		QueryConditional queryConditional;
		if (useGsiQuery) {
			index = "category_name-sale_price-gsi";
			queryConditional = QueryConditionalBuilder.build(categoryName, minPrice, maxPrice);
		} else {
			index = "pk-sale_price-lsi";
			queryConditional = QueryConditionalBuilder.build("Products", minPrice, maxPrice);
		}

		String[] COLUMNs = {"ID", "Name", "Vendor ID", "Category", "Import Price", "Sale Price", "Amount", "Earliest Expiry", "VAT"};

		Path tempFile;
		try {
			// Create a temporary file on the local disk to write the Excel data to.
			tempFile = Files.createTempFile("products-export-", ".xlsx");
		} catch (IOException e) {
			log.error("Failed to create temporary file for export: " + e.getMessage());
			throw new AppException(ErrorCode.FILE_CREATION_FAILED); // Assuming a new error code
		}

		// Use SXSSFWorkbook for streaming large datasets.
		// The constructor argument is the "window size" - the number of rows kept in memory.
		// Once the window is full, older rows are flushed to the temporary file on disk.
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
			 FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {

			SXSSFSheet sheet = workbook.createSheet("Products");
			// It's good practice to auto-size columns for better readability
			for (int i = 0; i < COLUMNs.length; i++) {
				sheet.trackAllColumnsForAutoSizing();
			}

			// --- Header (unchanged) ---
			Row headerRow = sheet.createRow(0);
			for (int col = 0; col < COLUMNs.length; col++) {
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(COLUMNs[col]);
			}

			int rowIdx = 1;
			Map<String, AttributeValue> exclusiveStartKey = null;

			// --- Paginated Database Read and Streaming Write ---
			do {
				// Fetch a chunk of records from DynamoDB
				PaginatedResult<Product> pageResult = productRepository.findByPk(
						index,
						queryConditional,
						10000, // Fetch a reasonable number of items per chunk
						exclusiveStartKey,
						filterExpression);

				List<Product> products = pageResult.getItems();

				// Write this chunk of records to the Excel sheet
				for (Product product : products) {
					Row row = sheet.createRow(rowIdx++);
					row.createCell(0).setCellValue(product.getEntityId());
					row.createCell(1).setCellValue(product.getName());
					row.createCell(2).setCellValue(product.getVendorId());
					row.createCell(3).setCellValue(product.getCategoryName());
					row.createCell(4).setCellValue(product.getImportPrice());
					row.createCell(5).setCellValue(product.getSalePrice());
					row.createCell(6).setCellValue(product.getAmount());
					row.createCell(7).setCellValue(product.getEarliestExpiry());
					row.createCell(8).setCellValue(product.getVat());
				}

				exclusiveStartKey = pageResult.getLastEvaluatedKey();

			} while (exclusiveStartKey != null && !exclusiveStartKey.isEmpty());

			// Auto-size columns after all data is written
			for (int i = 0; i < COLUMNs.length; i++) {
				sheet.autoSizeColumn(i);
			}

			// Write the workbook content to the file output stream
			workbook.write(fos);

		} catch (IOException e) {
			log.error("Failed to write data to Excel file: " + e.getMessage());
			// Clean up the created temp file on failure
			try {
				Files.deleteIfExists(tempFile);
			} catch (IOException cleanupException) {
				log.error("Failed to clean up temporary file: " + tempFile, cleanupException);
			}
			throw new AppException(ErrorCode.FILE_EXPORT_FAILED);
		}

		// Return the path to the completed file
		return tempFile;
	}

	@PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
	public ProductResponse update(String productId, ProductUpdateRequest request) {
		// Check exists
		Product entity = productRepository.findProductById(productId)
				.orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
		// Vendor not found exception
		if (vendorRepository.findVendorById(request.getVendorId()).isEmpty())
			throw new AppException(ErrorCode.VENDOR_NOT_FOUND);
		// Updating
		productMapper.updateEntity(entity, request);
		// Save
		return productMapper.toResponse(productRepository.saveProduct(entity));
	}

	@PreAuthorize("hasAuthority('DELETE_PRODUCT')")
	public String delete(String productId) {
		// Check exists
		if (productRepository.findProductById(productId).isEmpty())
			throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
		// Delete
		productRepository.deleteProductById(productId);
		// Return ID
		return productId;
	}
}
