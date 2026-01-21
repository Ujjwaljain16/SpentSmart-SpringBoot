package com.expenseTracker.demo.dto.request;

import com.expenseTracker.demo.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = Constants.Validation.CATEGORY_NAME_MAX_LENGTH, message = "Category name is too long")
    private String name;

    @Size(max = Constants.Validation.DESCRIPTION_MAX_LENGTH, message = "Description is too long")
    private String description;

    @Size(min = 7, max = 7, message = "Color code must be in format #RRGGBB")
    private String colorCode;
}
