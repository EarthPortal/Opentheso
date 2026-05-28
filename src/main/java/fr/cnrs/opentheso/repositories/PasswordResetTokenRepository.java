package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("""
                UPDATE PasswordResetToken t
                SET t.used = true
                WHERE t.user.id = :userId
                AND t.used = false
            """)
    void invalidateAllActiveTokensForUser(Integer userId);

    void deleteByUserId(Integer userId);
}
