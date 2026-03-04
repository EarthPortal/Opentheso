package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.services.utils.BaseUrl;
import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.PasswordResetToken;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.PasswordResetTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final BaseUrl baseUrl;

    private static final int TOKEN_EXPIRATION_MINUTES = 30;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                MailService mailService,
                                PasswordEncoder passwordEncoder,
                                BaseUrl baseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.baseUrl = baseUrl;
    }

    /**
     * Demande de réinitialisation (mot de passe oublié)
     */
    @Transactional
    public void requestPasswordReset(String email, boolean isActivation) {

        userRepository.findByMail(email).ifPresent(user -> {

            // Invalide tous les anciens tokens actifs
            tokenRepository.invalidateAllActiveTokensForUser(user.getId());

            String token = UUID.randomUUID().toString().replace("-", "");

            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setUser(user);
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES));
            prt.setUsed(false);

            tokenRepository.save(prt);

            String resetLink = baseUrl.getBaseUrl()
                    + "/reset-password.xhtml?token=" + token;

            String subject;
            String body;

            if(isActivation) {
                subject = "Activation de votre compte Opentheso";
                body = String.format(
                        "Bonjour,<br/><br/>" +
                                "Un compte a été créé pour vous sur <strong>Opentheso</strong>.<br/>" +
                                "Pour activer votre compte et définir votre mot de passe, cliquez sur le lien ci-dessous (valable 30 minutes) :<br/><br/>" +
                                "<a href=\"%s\">Activer mon compte</a><br/><br/>" +
                                "Si vous n’êtes pas à l’origine de cette demande, vous pouvez ignorer ce mail.<br/><br/>" +
                                "Cordialement,<br/>L'équipe Opentheso",
                        resetLink
                );
            } else {
                subject = "Réinitialisation de votre mot de passe Opentheso";
                body = String.format(
                        "Bonjour,<br><br>" +
                                "Vous avez fait une demande de réinitialisation de votre mot de passe sur Opentheso.<br>" +
                                "Pour définir un nouveau mot de passe, cliquez sur le lien suivant : " +
                                "<a href=\"%s\">Réinitialiser mon mot de passe</a><br>" +
                                "Ce lien est valable %d minutes.<br><br>" +
                                "Si vous n’êtes pas à l’origine de cette demande, merci d’ignorer ce message.<br>" +
                                "Celui-ci a été généré automatiquement, merci de ne pas y répondre.",
                        resetLink, TOKEN_EXPIRATION_MINUTES
                );
            }

            mailService.sendMail(user.getMail(), subject, body);
        });
    }

    public User validateToken(String token) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (prt.getUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré ou déjà utilisé");
        }

        User user = prt.getUser();

        // Cas activation : le compte doit être inactif et mot de passe à définir
        if (!user.getActive() && !user.getPassToModify()) {
            throw new IllegalArgumentException("Token invalide pour activation");
        }

        return user;
    }

    /**
     * Reset du mot de passe (utilisé pour :
     * - mot de passe oublié
     * - activation initiale
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (prt.getUsed()
                || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré ou déjà utilisé");
        }

        User user = prt.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPassToModify(false);
        user.setActive(true);  // active si c'était activation initiale
        user.setUpdatedAt(LocalDateTime.now());

        prt.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(prt);
    }
}