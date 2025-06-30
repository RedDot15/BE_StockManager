package org.reddot15.be_stockmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.SaleItem;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class CSVUtil {
    public static <T> List<T> processCSVFile(MultipartFile file, Function<CSVRecord, T> recordProcessor) {
        // File empty exception
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_FILE);
        }

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            // Stream records and apply the provided processor function
            return csvParser.getRecords().stream()
                    .map(recordProcessor)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error parsing or processing CSV file: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_PARSE_FAILED);
        }
    }
}
