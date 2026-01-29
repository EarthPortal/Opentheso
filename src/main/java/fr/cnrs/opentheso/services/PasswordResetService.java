package fr.cnrs.opentheso.services;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.PasswordResetToken;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.PasswordResetTokenRepository;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRATION_MINUTES = 30;



    @Autowired
    private HttpServletRequest request;

/*
    // Récupération du token depuis l'URL
    @PostConstruct
    public void init() {
        // ici, token devrait être injecté automatiquement par f:viewParam
        if (token != null) {
            try {
                passwordResetService.validateToken(token);
            } catch (Exception e) {
                message = "Lien invalide ou expiré.";
            }
        }
    }*/

    public String getBaseUrl() {
        String scheme = request.getScheme(); // http ou https
        String serverName = request.getServerName(); // ex: localhost
        int serverPort = request.getServerPort(); // ex: 8099
        String contextPath = request.getContextPath(); // ex: /

        // si port standard, on peut l’omettre
        String portPart = (serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort;

        return scheme + "://" + serverName + portPart + contextPath;
    }

    public void requestPasswordReset(String email) {
        String baseUrl = getBaseUrl();
        userRepository.findByMail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setUser(user);
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES));
            prt.setUsed(false);

            tokenRepository.save(prt);

            // Construire dynamiquement le lien
            String resetLink = baseUrl + "/reset-password.xhtml?token=" + token;

            mailService.sendMail(user.getMail(), "Réinitialisation mot de passe",
                    "Cliquez sur ce lien pour réinitialiser votre mot de passe (30 min) : " + resetLink);
        });
    }

    public User validateToken(String token) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (prt.getUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré ou déjà utilisé");
        }

        return prt.getUser();
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (prt.getUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré ou déjà utilisé");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPassToModify(false);
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
    }
}

