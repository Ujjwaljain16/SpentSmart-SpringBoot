package com.expenseTracker.demo.util;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final class Roles {
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
    }

    public static final class Validation {
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int PASSWORD_MAX_LENGTH = 100;
        public static final int EMAIL_MAX_LENGTH = 255;
        public static final int NAME_MAX_LENGTH = 100;
        public static final int DESCRIPTION_MAX_LENGTH = 500;
        public static final int CATEGORY_NAME_MAX_LENGTH = 50;
        public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    }

    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_SORT_FIELD = "createdAt";
        public static final String DEFAULT_SORT_DIRECTION = "DESC";
    }

    public static final class Cache {
        public static final String ANALYTICS_MONTHLY = "analytics:monthly";
        public static final String ANALYTICS_CATEGORY = "analytics:category";
        public static final String CATEGORIES_USER = "categories:user";
        public static final int TTL_ANALYTICS_MINUTES = 60;
        public static final int TTL_CATEGORIES_MINUTES = 15;
    }

    public static final class RateLimit {
        public static final int ANALYTICS_REQUESTS_PER_MINUTE = 10;
        public static final int GENERAL_REQUESTS_PER_MINUTE = 100;
        public static final int AUTH_REQUESTS_PER_MINUTE = 5;
    }

    public static final class FileUpload {
        public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png"};
        public static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf"};
        public static final String UPLOAD_DIR = "uploads";
    }

    public static final class Security {
        public static final long JWT_EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours
        public static final String JWT_HEADER = "Authorization";
        public static final String JWT_PREFIX = "Bearer ";
        public static final int BCRYPT_STRENGTH = 12;
    }

    public static final class ErrorMessages {
        public static final String USER_NOT_FOUND = "User not found";
        public static final String EXPENSE_NOT_FOUND = "Expense not found";
        public static final String CATEGORY_NOT_FOUND = "Category not found";
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access to resource";
        public static final String EMAIL_ALREADY_EXISTS = "Email already registered";
        public static final String INVALID_CREDENTIALS = "Invalid email or password";
        public static final String RATE_LIMIT_EXCEEDED = "Rate limit exceeded. Please try again later";
        public static final String FILE_TOO_LARGE = "File size exceeds maximum allowed size";
        public static final String INVALID_FILE_TYPE = "Invalid file type";
    }
}
