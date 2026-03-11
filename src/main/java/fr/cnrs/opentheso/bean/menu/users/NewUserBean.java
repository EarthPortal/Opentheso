package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.entites.Roles;
import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.services.*;

import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;
import fr.cnrs.opentheso.services.utils.BaseUrl;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.primefaces.PrimeFaces;
import org.springframework.security.crypto.password.PasswordEncoder;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "newUserBean")
public class NewUserBean implements Serializable {

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;
    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final UserService userService;
    private final MyProjectBean myProjectBean;
    private final SuperAdminBean superAdminBean;
    private final ThesaurusService thesaurusService;
    private final UserRoleGroupService userRoleGroupService;
    private final MailService mailService;
    private final BaseUrl baseUrl;

    private NodeUser nodeUser;
    private boolean limitOnThesaurus;
    private String passWord1, passWord2, selectedProject, selectedRole, name;
    private List<UserGroupLabel> nodeAllProjects;
    private List<Roles> nodeAllRoles;
    private List<NodeIdValue> listThesaurusOfProject;
    private List<String> selectedThesaurus;
    private String creationMode = "EMAIL"; // DIRECT ou EMAIL


    public void init(String selectedProject) {
        nodeUser = new NodeUser();
        passWord1 = null;
        passWord2 = null;
        this.selectedProject = selectedProject;
        selectedRole = null;
        limitOnThesaurus = false;
        listThesaurusOfProject = null;
        selectedThesaurus = null;
        creationMode = "EMAIL"; // par défaut
    }   
    
    public void initForSuperAdmin() {
        nodeUser = new NodeUser();
        passWord1 = null;
        passWord2 = null;
        selectedRole = null;
        listThesaurusOfProject = null;
        selectedThesaurus = null;
        limitOnThesaurus = false;
        creationMode = "EMAIL"; // par défaut

        nodeAllProjects = userRoleGroupService.findAllUserRoleGroup();
        nodeAllProjects.sort(Comparator.comparing(UserGroupLabel::getLabel, String.CASE_INSENSITIVE_ORDER));
        nodeAllRoles = userRoleGroupService.getAllRoles();

        selectedProject = CollectionUtils.isNotEmpty(nodeAllProjects) ? nodeAllProjects.get(0).getId().toString() : null;
    }
    
    public void toggleLimitThesaurus(){
        if(!limitOnThesaurus) return;
        /// récupérer la liste des thésaurus d'un projet
        int idProject = -1;
        try {
            idProject = Integer.parseInt(selectedProject);
        } catch (Exception e) {
            return;
        }
        if(idProject == -1) return;
        listThesaurusOfProject = thesaurusService.getThesaurusOfProject(idProject, workLanguage);
    }
    
    public void addUser(boolean bySuperAdmin){
        if(!checkUserDatas()) return;

        User userCreated;

        if("EMAIL".equals(creationMode)) {
            userCreated = saveUserByEmail();
        } else {
            userCreated = saveUserDirect();
        }

        if(userCreated == null) return;

        saveRole(userCreated);
        MessageUtils.showInformationMessage("Utilisateur créé avec succès !!!");

        if (bySuperAdmin) {
            superAdminBean.init();
            initForSuperAdmin();
        } else {
            myProjectBean.setLists();
        }

        PrimeFaces.current().executeScript("PF('newUserForProject').hide();");
    }

    private User saveUserDirect() {
        var user = User.builder()
                .mail(nodeUser.getMail())
                .username(nodeUser.getName())
                .institution(nodeUser.getInstitution())
                .password(passwordEncoder.encode(passWord1))
                .isSuperAdmin(nodeUser.isSuperAdmin())
                .alertMail(nodeUser.isAlertMail())
                .isServiceAccount(nodeUser.isServiceAccount())
                .active(true)
                .verified(true)
                .keyNeverExpire(false)
                .passToModify(false)
                .rgpdConsent(true)
                .build();

        return userService.saveUser(user);
    }

    private User saveUserByEmail() {
        // Création de l'utilisateur en base
        var user = User.builder()
                .mail(nodeUser.getMail())
                .username(nodeUser.getName())
                .institution(nodeUser.getInstitution())
                .isSuperAdmin(nodeUser.isSuperAdmin())
                .alertMail(nodeUser.isAlertMail())
                .isServiceAccount(nodeUser.isServiceAccount())
                .active(false)           // compte inactif
                .verified(false)
                .keyNeverExpire(false)
                .passToModify(true)      // mot de passe à définir
                .rgpdConsent(true)
                .build();

        var userCreated = userService.saveUser(user);
        if (userCreated == null) {
            MessageUtils.showErrorMessage("Erreur pendant la création");
            return null;
        }

        // Utilisation du PasswordResetService pour créer le token et envoyer le mail
        try {
            passwordResetService.requestPasswordReset(userCreated.getMail(), true);
        } catch (Exception e) {
            MessageUtils.showWarnMessage(
                    "Utilisateur créé mais le serveur mail est indisponible. Vous pourrez renvoyer le mail plus tard."
            );
        }

        MessageUtils.showInformationMessage("Un mail a été envoyé pour définir le mot de passe et activer le compte");
        return userCreated;
    }

    private boolean checkUserDatas() {

        if(ObjectUtils.isEmpty(nodeUser)) {
            MessageUtils.showErrorMessage("Aucun utilisateur à ajouter !!!");
            return false;
        }

        if(StringUtils.isEmpty(nodeUser.getName())) {
            MessageUtils.showErrorMessage("Le pseudo est obligatoire");
            return false;
        }

        if(StringUtils.isEmpty(nodeUser.getMail())) {
            MessageUtils.showErrorMessage("Email obligatoire");
            return false;
        }

        if(userService.getUserByMail(nodeUser.getMail()) != null) {
            MessageUtils.showErrorMessage("Email existe déjà");
            return false;
        }

        nodeUser.setName(nodeUser.getName().trim());

        if("DIRECT".equals(creationMode)) {

            if(StringUtils.isEmpty(passWord1) || StringUtils.isEmpty(passWord2)) {
                MessageUtils.showErrorMessage("Mot de passe obligatoire");
                return false;
            }

            if(!passWord1.equals(passWord2)) {
                MessageUtils.showErrorMessage("Mot de passe non identique");
                return false;
            }

            if (!passWord1.matches(".*[A-Z].*") ||
                    !passWord1.matches(".*[a-z].*") ||
                    !passWord1.matches(".*\\d.*") ||
                    !passWord1.matches(".*[^A-Za-z\\d].*")) {

                MessageUtils.showErrorMessage(
                        "Mot de passe invalide (majuscule, minuscule, chiffre, spécial requis)"
                );
                return false;
            }
        }

        if(StringUtils.isEmpty(selectedRole)) {
            nodeUser.setSuperAdmin(false);
            selectedProject = null;
        } else {
            try {
                nodeUser.setSuperAdmin(Integer.parseInt(selectedRole) == 1);
            } catch (Exception e) {
                MessageUtils.showErrorMessage("Role non reconnu");
                return false;
            }
        }

        return true;
    }

    public void updatePasswordFields() {
        if("EMAIL".equals(creationMode)) {
            passWord1 = null;
            passWord2 = null;
        }
    }

    private void saveRole(User userCreated) {

        if(StringUtils.isNotEmpty(selectedProject) && StringUtils.isNotEmpty(selectedRole)){
            if (limitOnThesaurus) {
                userRoleGroupService.addUserRoleOnTheso(userCreated.getId(), Integer.parseInt(selectedRole),
                        Integer.parseInt(selectedProject), selectedThesaurus);
            } else {
                userRoleGroupService.addUserRoleOnGroup(userCreated.getId(), Integer.parseInt(selectedRole),
                        Integer.parseInt(selectedProject));
            }
        }
    }
}
