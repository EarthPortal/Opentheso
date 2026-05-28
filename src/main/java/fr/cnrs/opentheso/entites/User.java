package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "mail", nullable = false, unique = true)
    private String mail;

    @Column(name = "passtomodify", nullable = false)
    private Boolean passToModify = false;

    @Column(name = "alertmail", nullable = false)
    private Boolean alertMail = false;

    @Column(name = "issuperadmin", nullable = false)
    private Boolean isSuperAdmin = false;

    @Column(name = "apikey")
    private String apiKey;

    @Column(name = "key_never_expire", nullable = false)
    private Boolean keyNeverExpire = false;

    @Column(name = "key_expires_at")
    @Temporal(TemporalType.DATE)
    private LocalDate keyExpiresAt;

    @Column(name = "isservice_account", nullable = false)
    private Boolean isServiceAccount = false;

    @Column(name = "key_description")
    private String keyDescription;

    @Column(name = "institution")
    private String institution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "deletion_scheduled_at")
    private LocalDateTime deletionScheduledAt;

    @Column(name = "rgpd_consent", nullable = false)
    private Boolean rgpdConsent = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
