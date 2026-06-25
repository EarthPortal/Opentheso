package fr.cnrs.opentheso.ws.openapi.v2;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.services.UserService;
import fr.cnrs.opentheso.services.security.SsoTokenService;
import fr.cnrs.opentheso.ws.openapi.exception.ApiKeyInvalidException;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//@CrossOrigin(origins = "*") // à restreindre en production
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
@Tag(name = "Api v2")
public class ApiAuthController {

    private final UserService userService;
    private final SsoTokenService ssoTokenService; // service à créer (voir plus bas)

    @PostMapping(
            value = "/token",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Génère un token SSO",
            description = "Permet à une application tierce de générer un token temporaire " +
                    "pour connecter un utilisateur sans passer par l'écran de login."
    )
    public ResponseEntity<Map<String, String>> generateSsoToken(
            HttpServletRequest requestHeader
    ) {
        // Récupérer l'utilisateur authentifié via l'interceptor existant (X-API-Key)
        User user = (User) requestHeader.getAttribute("authenticatedUser");
        if (user == null) {
            throw new ApiKeyInvalidException(ApiKeyState.INVALID);
        }

        // Générer et stocker le token SSO en base (expire dans 5 minutes)
        String token = ssoTokenService.createToken(user);

        // URL de redirection vers JSF sans login
        String redirectUrl = "/index.xhtml?ssoToken=" + token;

        return ResponseEntity.ok(Map.of(
                "token", token,
                "redirectUrl", redirectUrl,
                "expiresIn", "300"
        ));
    }
}