package org.reddot15.be_stockmanager.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class ExcelUtil {
    public static Path productsToExcel(
            Function<Map<String, AttributeValue>, PaginatedResult<Product>> queryFunction
    ) throws IOException {
        String[] COLUMNs = {"ID", "Name", "Vendor ID", "Category", "Import Price", "Sale Price", "Amount", "Earliest Expiry", "VAT"};

        Path tempFile;
        try {
            // Create a temporary file on the local disk to write the Excel data to.
            tempFile = Files.createTempFile("products-export-", ".xlsx");
        } catch (IOException e) {
            log.error("Failed to create temporary file for export: " + e.getMessage());
            throw new AppException(ErrorCode.FILE_CREATION_FAILED);
        }

        // Use SXSSFWorkbook for streaming large datasets.
        // The constructor argument is the "window size" - the number of rows kept in memory.
        // Once the window is full, older rows are flushed to the temporary file on disk.
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {

            SXSSFSheet sheet = workbook.createSheet("Products");
            // Auto-size columns for better readability
            for (int i = 0; i < COLUMNs.length; i++) {
                sheet.trackAllColumnsForAutoSizing();
            }

            // --- Header ---
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
                PaginatedResult<Product> pageResult = queryFunction.apply(exclusiveStartKey);

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
    }
}
