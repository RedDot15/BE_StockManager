package org.reddot15.be_stockmanager.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public class ExcelUtil {
    public static <T> Path exportToExcel(
            String fileNamePrefix,
            List<String> headers,
            Function<Map<String, AttributeValue>, PaginatedResult<T>> queryFunction,
            BiConsumer<Row, T> rowMapper
    ) {
        // Create a temporary file on the local disk to write the Excel data to.
        Path tempFile = createTempFile(fileNamePrefix);

        // Use SXSSFWorkbook for streaming large datasets.
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {

            SXSSFSheet sheet = workbook.createSheet(StringUtils.capitalize(fileNamePrefix));
            // Auto-size columns for better readability
            sheet.trackAllColumnsForAutoSizing();

            // Create and style the header row.
            createHeaderRow(sheet, headers);

            int rowIdx = 1; // Start data from the second row.
            Map<String, AttributeValue> exclusiveStartKey = null;

            // Paginate through the data source and write to the sheet.
            do {
                // Fetch a chunk of records from DynamoDB
                PaginatedResult<T> pageResult = queryFunction.apply(exclusiveStartKey);
                List<T> items = pageResult.getItems();

                // Write this chunk of records to the Excel sheet
                for (T item : items) {
                    Row row = sheet.createRow(rowIdx++);
                    // Use the provided rowMapper to populate the cells.
                    rowMapper.accept(row, item);
                }

                exclusiveStartKey = pageResult.getLastEvaluatedKey();

            } while (exclusiveStartKey != null && !exclusiveStartKey.isEmpty());

            // Auto-size columns after all data is written
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook content to the file output stream
            workbook.write(fos);
        } catch (IOException e) {
            log.error("Failed to write data to Excel file: " + e.getMessage());
            // Clean up the created temp file on failure
            cleanupTempFile(tempFile);
            throw new AppException(ErrorCode.FILE_EXPORT_FAILED);
        }

        // Return
        return tempFile;
    }

    // Creates the header row in the given sheet.
    private static void createHeaderRow(SXSSFSheet sheet, List<String> headers) {
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < headers.size(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(headers.get(col));
            // Optionally, add styling to the header here.
        }
    }

    // Creates a temporary file for the export.
    private static Path createTempFile(String prefix) {
        try {
            return Files.createTempFile(prefix + "-", ".xlsx");
        } catch (IOException e) {
            log.error("Failed to create temporary file for export: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_CREATION_FAILED);
        }
    }

    // Deletes the temporary file in case of an error.
    private static void cleanupTempFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to clean up temporary file: {}", path, e);
        }
    }
}
