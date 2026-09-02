package fr.cnrs.opentheso.services.security;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserRightsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service central de résolution des droits utilisateur.
 * Ordre de priorité : SuperAdmin (global) > rôle only_on (exception sur un thésaurus précis)
 * > rôle de groupe (hérité du projet auquel appartient le thésaurus).
 */
@Service
public class PermissionService {

    private final UserRepository userRepo;
    private final UserRoleGroupRepository roleGroupRepo;
    private final UserRoleOnlyOnRepository roleOnlyOnRepo;
    private final UserGroupThesaurusRepository groupThesoRepo;
    private final UserRightsRepository userRightsRepo;

    @Autowired
    public PermissionService(UserRepository userRepo,
                             UserRoleGroupRepository roleGroupRepo,
                             UserRoleOnlyOnRepository roleOnlyOnRepo,
                             UserGroupThesaurusRepository groupThesoRepo,
                             UserRightsRepository userRightsRepo) {
        this.userRepo = userRepo;
        this.roleGroupRepo = roleGroupRepo;
        this.roleOnlyOnRepo = roleOnlyOnRepo;
        this.groupThesoRepo = groupThesoRepo;
        this.userRightsRepo = userRightsRepo;
    }

    public Integer findRoleIdOnTheso(int idUser, String idTheso) {
        return userRightsRepo.findRoleIdOnTheso(idUser, idTheso);
    }

    /**
     * Résout le RoleType effectif d'un utilisateur sur un thésaurus.
     * Retourne Optional.empty() si l'utilisateur n'a aucun droit sur ce thésaurus.
     */
    public Optional<RoleType> getEffectiveRole(int idUser, String idTheso) {
        Integer idRole = findRoleIdOnTheso(idUser, idTheso);
        if (idRole == null) {
            return Optional.empty();
        }
        return Optional.of(RoleType.fromId(idRole));
    }

    // =========================================================
    //  RÉSOLUTION DU RÔLE EFFECTIF (version "pas à pas" en Java)
    // =========================================================

    /**
     * Résout le rôle effectif d'un utilisateur sur un thésaurus donné,
     * en appliquant la priorité SuperAdmin > only_on > groupe.
     * Fait plusieurs requêtes : à utiliser quand on n'a pas déjà les données en mémoire
     * (préférer UserRightsContext en session pour l'affichage JSF répété).
     */
 /*   public Optional<RoleType> getEffectiveRole(int idUser, String idTheso) {
        User user = userRepo.findById(idUser)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + idUser));

        // 1. SuperAdmin global : bypass total
        if (user.getIsSuperAdmin()) {
            return Optional.of(RoleType.SUPER_ADMIN);
        }

        // 2. Exception précise sur ce thésaurus (prévaut sur le rôle groupe)
        Optional<RoleType> specific = roleOnlyOnRepo.findByUserAndTheso(idUser, idTheso)
                .map(r -> RoleType.fromId(r.getIdRole()));
        if (specific.isPresent()) {
            return specific;
        }

        // 3. Sinon, rôle hérité du groupe propriétaire du thésaurus
        Integer idGroup = groupThesoRepo.findGroupIdByTheso(idTheso);
        if (idGroup == null) {
            return Optional.empty();
        }

        return roleGroupRepo.findByUserAndGroup(idUser, idGroup)
                .map(r -> RoleType.fromId(r.getIdRole()));
    }
*/
    // =========================================================
    //  RÉSOLUTION EN UNE SEULE REQUÊTE SQL (contrôle ponctuel)
    // =========================================================

    /**
     * Variante optimisée : une seule requête SQL native qui applique déjà
     * la priorité SuperAdmin > only_on > groupe côté base de données.
     * Recommandé pour un contrôle isolé (endpoint REST, action sensible, batch),
     * plutôt qu'à chaque rendu JSF où UserRightsContext en session est préférable.
     */
/*    public Optional<RoleType> getRoleOnThesaurus(int idUser, String idTheso) {
        Integer idRole = userRightsRepo.findRoleIdOnTheso(idUser, idTheso);
        return idRole == null ? Optional.empty() : Optional.of(RoleType.fromId(idRole));
    }

    public boolean hasAnyRightOn(int idUser, String idTheso) {
        return userRightsRepo.findRoleIdOnTheso(idUser, idTheso) != null;
    }

    // =========================================================
    //  VÉRIFICATIONS MÉTIER DE HAUT NIVEAU
    // =========================================================

    /** Peut modifier le contenu d'un thésaurus (Manager ou plus). */
/*    public boolean canEditThesaurus(int idUser, String idTheso) {
        return getEffectiveRole(idUser, idTheso)
                .map(r -> r.isAtLeast(RoleType.MANAGER))
                .orElse(false);
    }*/

    /** Peut proposer des candidats, notes, traductions (Contributeur ou plus). */
/*    public boolean canProposeCandidate(int idUser, String idTheso) {
        return getEffectiveRole(idUser, idTheso)
                .map(r -> r.isAtLeast(RoleType.CONTRIBUTOR))
                .orElse(false);
    }*/

    /** Est administrateur du projet/groupe entier (droits complets sur le groupe). */
/*    public boolean canAdministerProject(int idUser, int idGroup) {
        User user = userRepo.findById(idUser)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + idUser));

        if (user.getIsSuperAdmin()) {
            return true;
        }

        return roleGroupRepo.findByUserAndGroup(idUser, idGroup)
                .map(r -> RoleType.fromId(r.getIdRole()) == RoleType.ADMIN)
                .orElse(false);
    }*/

    /** Vérifie si l'utilisateur est SuperAdmin (global, transversal). */
/*    public boolean isSuperAdmin(int idUser) {
        return userRepo.findById(idUser)
                .map(User::getIsSuperAdmin)
                .orElse(false);
    }*/
}