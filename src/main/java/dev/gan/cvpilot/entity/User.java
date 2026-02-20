package dev.gan.cvpilot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private Long id;

    private String email;

    private String googleId;

    @Builder.Default
    private String plan = "FREE";

    private Instant planExpiresAt;

    @Builder.Default
    private int analysesToday = 0;

    private LocalDate analysesDate;

    private Instant createdAt;

    private Instant updatedAt;
}
