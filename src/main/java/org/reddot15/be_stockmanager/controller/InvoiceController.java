package org.reddot15.be_stockmanager.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.reddot15.be_stockmanager.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.reddot15.be_stockmanager.helper.ResponseBuilder.buildResponse;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    InvoiceService invoiceService;

    @PostMapping("")
    public ResponseEntity<ResponseObject> uploadInvoicesCSV(@RequestParam("file") MultipartFile file) {
        List<Invoice> importedInvoices = invoiceService.importInvoicesFromCSV(file);
        return buildResponse(HttpStatus.OK,
                "CSV file uploaded and " + importedInvoices.size() + " invoices imported successfully.",
                importedInvoices);
    }

    @GetMapping(value = "")
    public ResponseEntity<ResponseObject> getAll(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "nextPageToken", required = false) String nextPageToken) {
        return buildResponse(HttpStatus.OK, "Get invoices successfully.", invoiceService.getAll(limit, nextPageToken));
    }
}