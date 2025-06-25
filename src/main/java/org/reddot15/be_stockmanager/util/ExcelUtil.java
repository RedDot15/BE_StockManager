package org.reddot15.be_stockmanager.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reddot15.be_stockmanager.entity.Product;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelUtil {
    public static ByteArrayInputStream productsToExcel(List<Product> products) throws IOException {
        String[] COLUMNs = {"ID", "Name", "Vendor ID", "Category", "Import Price", "Sale Price", "Amount", "Earliest Expiry", "VAT"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");

            // Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUMNs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUMNs[col]);
            }

            int rowIdx = 1;
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
                row.createCell(8).setCellValue(product.getVat()); // Corrected index for VAT
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
