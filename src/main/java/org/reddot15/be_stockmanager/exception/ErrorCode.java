package org.reddot15.be_stockmanager.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    // General
    UNCATEGORIZED(HttpStatus.BAD_REQUEST, 70,"Bad request."),
    // Player
    USER_DUPLICATE(HttpStatus.CONFLICT, 69,"User already exists."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 64,"User not found."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, 60,"Wrong password."),
    INVALID_USER_QUERY(HttpStatus.BAD_REQUEST, 70,"Invalid user query."),
    // Authentication
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, 61,"Unauthenticated error."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, 63,"You do not have permission to perform this operation."),
    // Role
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, 64,"Role not found."),
    // Permission
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, 64,"Permission not found."),
    // Validation
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 60, "Request invalid."),
    // Vendor
    VENDOR_DUPLICATE(HttpStatus.CONFLICT, 69,"Vendor already exists."),
    VENDOR_NOT_FOUND(HttpStatus.NOT_FOUND, 64,"Vendor not found."),
    // Product
    PRODUCT_DUPLICATE(HttpStatus.CONFLICT, 69,"Product already exists."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, 64,"Product not found."),
    PRODUCT_MISMATCH(HttpStatus.BAD_REQUEST, 60,"Product mismatch."),
    // Invoice
    INVOICE_NOT_FOUND(HttpStatus.NOT_FOUND, 64,"Invoice not found."),
    // Pagination
    INVALID_PAGINATION_TOKEN(HttpStatus.BAD_REQUEST, 60, "Invalid pagination token."),
    SERIALIZE_PAGINATION_TOKEN_FAILED(HttpStatus.BAD_REQUEST, 70, "Serialize pagination token failed."),
    // File
    EMPTY_FILE(HttpStatus.BAD_REQUEST, 60, "Empty file."),
    FILE_PARSE_FAILED(HttpStatus.BAD_REQUEST, 60, "File parse failed."),
    FILE_EXPORT_FAILED(HttpStatus.NOT_FOUND, 64, "Fail to export data to Excel file."),
    // Record
    INVALID_RECORD(HttpStatus.BAD_REQUEST, 60, "Invalid record."),
    //
    UNSUPPORT_DYNAMODB_TYPE(HttpStatus.BAD_REQUEST, 60, "Unsupported DynamoDB type."),
    ;

    HttpStatus httpStatus;
    Integer code;
    String message;
}
