package com.expenseTracker.demo.dto.request;

import com.expenseTracker.demo.util.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = Constants.Validation.EMAIL_MAX_LENGTH, message = "Email is too long")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = Constants.Validation.PASSWORD_MIN_LENGTH,
          max = Constants.Validation.PASSWORD_MAX_LENGTH,
          message = "Password must be between 8 and 100 characters")
    private String password;

    @Size(max = Constants.Validation.NAME_MAX_LENGTH, message = "Full name is too long")
    private String fullName;
}
