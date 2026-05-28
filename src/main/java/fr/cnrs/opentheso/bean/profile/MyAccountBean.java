package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.services.ApiKeyService;
import fr.cnrs.opentheso.services.UserService;
import fr.cnrs.opentheso.services.security.CryptoService;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import fr.cnrs.opentheso.utils.SimpleCrypto;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean pour la gestion des actions de compte utilisateur telles que la mise à jour des informations de profil et des clés API.
 * Cette classe est à portée de session et gère les paramètres du compte utilisateur.
 *
 * @author miledrousset
 */
@Slf4j
@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "myAccountBean")
public class MyAccountBean implements Serializable {

    private final CurrentUser currentUser;
    private final UserService userService;
    private final ApiKeyService apiKeyService;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;

    private NodeUser nodeUser;
    private String passWord1, passWord2, displayedKey;
    private List<NodeUserRoleGroup> allMyRoleProject;
    private String apiKeyPlain;
    private String apiKey;


    public void loadDataPage(){

        log.debug("Chargement des données nécessaire au fonctionnement de l'écran utilisateur");
        nodeUser = userService.getUser(currentUser.getNodeUser().getIdUser());
        displayedKey = StringUtils.isEmpty(nodeUser.getApiKey()) ? null : new String(new char[64]).replace("\0", "*");
        passWord1 = null;
        passWord2 = null;
        apiKeyPlain = null;
        apiKey = nodeUser.getApiKey();
        nodeUser.setApiKey(null);
    }

    public void updateKey() {

        // 1. Génération de la clé API (32 bytes = très sécurisé)
        apiKeyPlain = SimpleCrypto.generateRandomApiKey(32);

        // 2. Chiffrement
        String apiKeyEncrypted = cryptoService.encrypt(apiKeyPlain);

        // 3. on enregistre la clé cryptée dans la base
        if (apiKeyService.saveApiKey(apiKeyEncrypted, nodeUser.getIdUser())) {
            MessageUtils.showInformationMessage("La clé a bien été enregistrée.");
        } else {
            MessageUtils.showErrorMessage("Erreur de sauvegarde de la clé.");
        }
        /*
        if(!arkeoUserService.updateApiKey(apiKeyEncrypted, userId)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "La génération de la clé a échoué");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // 4. on envoie la clé en clair une seule fois
        this.apiKey = apiKeyPlain;
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Génération de la clé réussie");
        FacesContext.getCurrentInstance().addMessage(null, msg);


    /*    displayedKey = apiKeyService.generateApiKey("ot_", 64);
        nodeUser.setApiKey(displayedKey);

        if (apiKeyService.saveApiKey(MD5Password.getEncodedPassword(displayedKey), nodeUser.getIdUser())) {
            MessageUtils.showInformationMessage("La clé a bien été enregistrée.");
        } else {
            MessageUtils.showErrorMessage("Erreur de sauvegarde de la clé.");
        }*/

    }

    public void updateUserName() {

        if (StringUtils.isEmpty(nodeUser.getName())) {
            MessageUtils.showErrorMessage("Le pseudo est obligatoire !!!");
            return;
        }

        if (userService.updateUserInformation(currentUser.getNodeUser().getIdUser(), nodeUser.getName(), null, null, null)) {
            MessageUtils.showInformationMessage("Pseudo changé avec succès");
            PrimeFaces.current().ajax().update("containerIndex");
        } else {
            MessageUtils.showErrorMessage("Erreur pendant la modification du pseudo");
        }
    }

    public void updateAlertEmail() {

        if (userService.updateUserInformation(currentUser.getNodeUser().getIdUser(), null, null, null, nodeUser.isAlertMail())) {
            MessageUtils.showInformationMessage("Alerte changée avec succès");
            PrimeFaces.current().ajax().update("containerIndex");
        } else {
            MessageUtils.showErrorMessage("Erreur pendant la modification de l'alerte email");
        }
    }

    public void updateEmail() {

        if (StringUtils.isEmpty(nodeUser.getMail())) {
            MessageUtils.showErrorMessage("Un Email est obligatoire !!!");
            return;
        }

        if (userService.updateUserInformation(currentUser.getNodeUser().getIdUser(), null, null, nodeUser.getMail(), null)) {
            MessageUtils.showInformationMessage("Email changé avec succès");
            PrimeFaces.current().ajax().update("containerIndex");
        } else {
            MessageUtils.showErrorMessage("Erreur pendant la modification du mot de passe");
        }
    }

    public boolean isKeyExpired() {
        if (ObjectUtils.isEmpty(nodeUser.getApiKeyExpireDate())) return false;
        return LocalDate.now().isAfter(nodeUser.getApiKeyExpireDate());
    }
}

