package com.expenseTracker.demo.entity;

import com.expenseTracker.demo.util.Constants;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories", 
    indexes = {
        @Index(name = "idx_category_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_category_name", columnNames = {"user_id", "name"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = Constants.Validation.CATEGORY_NAME_MAX_LENGTH)
    private String name;

    @Column(length = Constants.Validation.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(name = "color_code", length = 7)
    private String colorCode;
}
