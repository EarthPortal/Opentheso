package fr.cnrs.opentheso.services.security;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;


@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRoleGroupRepository userRoleGroupRepository;
    private final UserRoleOnlyOnRepository userRoleOnlyOnRepository;

    public Optional<Integer> getUserRoleOnThesaurus(int userId, String idThesaurus) {

        Optional<Integer> groupIdOpt =
                userRoleGroupRepository.findGroupIdByUserIdAndThesaurus(userId, idThesaurus);

        if (groupIdOpt.isEmpty()) {
            groupIdOpt = userRoleOnlyOnRepository.findGroupIdByUserIdAndThesaurusId(userId, idThesaurus);
            if (groupIdOpt.isEmpty()) {
                return Optional.empty();
            }
        }

        Integer groupId = groupIdOpt.get();

        // priorité only_on
        Optional<Integer> roleOnlyOn =
                userRoleOnlyOnRepository.findRoleFromOnlyOn(userId, groupId, idThesaurus);

        if (roleOnlyOn.isPresent()) {
            return roleOnlyOn;
        }

        // fallback group
        return userRoleOnlyOnRepository.findRoleFromGroup(userId, groupId, idThesaurus);
    }

    public boolean canUserWrite(int userId, String idThesaurus) {
        return getUserRoleOnThesaurus(userId, idThesaurus)
                .map(role -> role <= 2) // à adapter
                .orElse(false);
    }
}