package fr.cnrs.opentheso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
public class AppConfig {

    private boolean keycloakEnabled;

    /**
     * Identifiant du client OIDC tel que déclaré dans Keycloak.
     * Sert à lire les rôles du client dans le claim resource_access.<client-id>.roles
     * (recommandation du SSO Gaia Data ; à défaut, fallback sur realm_access.roles).
     */
    private String clientId;

    public boolean isKeycloakEnabled() {
        return keycloakEnabled;
    }

    public void setKeycloakEnabled(boolean keycloakEnabled) {
        this.keycloakEnabled = keycloakEnabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}