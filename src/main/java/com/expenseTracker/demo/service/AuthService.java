package com.expenseTracker.demo.service;

import com.expenseTracker.demo.dto.request.LoginRequest;
import com.expenseTracker.demo.dto.request.RegisterRequest;
import com.expenseTracker.demo.dto.response.AuthResponse;
import com.expenseTracker.demo.entity.Category;
import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.repository.CategoryRepository;
import com.expenseTracker.demo.repository.UserRepository;
import com.expenseTracker.demo.security.JwtTokenProvider;
import com.expenseTracker.demo.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(Constants.ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        createDefaultCategories(user);

        String token = jwtTokenProvider.generateToken(user, user.getId());

        return AuthResponse.of(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(user, user.getId());

        return AuthResponse.of(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }

    private void createDefaultCategories(User user) {
        List<String[]> defaultCategories = Arrays.asList(
                new String[]{"Food", "Food and dining expenses", "#FF5733"},
                new String[]{"Transport", "Transportation and travel", "#3498DB"},
                new String[]{"Shopping", "Shopping and retail", "#9B59B6"},
                new String[]{"Entertainment", "Entertainment and leisure", "#E74C3C"},
                new String[]{"Bills", "Utilities and bills", "#F39C12"},
                new String[]{"Healthcare", "Medical and healthcare", "#1ABC9C"},
                new String[]{"Education", "Education and learning", "#34495E"},
                new String[]{"Other", "Miscellaneous expenses", "#95A5A6"}
        );

        for (String[] categoryData : defaultCategories) {
            Category category = Category.builder()
                    .user(user)
                    .name(categoryData[0])
                    .description(categoryData[1])
                    .colorCode(categoryData[2])
                    .build();
            categoryRepository.save(category);
        }
    }
}
