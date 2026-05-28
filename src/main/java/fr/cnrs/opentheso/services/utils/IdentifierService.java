package fr.cnrs.opentheso.services.utils;

import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.utils.ToolsHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class IdentifierService {
    private final ConceptRepository conceptRepository;

    public IdentifierService(ConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    public String getAlphaNumericId() {
        String id = ToolsHelper.getNewId(15, false, false);
        while (isIdExiste(id)) {
            id = ToolsHelper.getNewId(15, false, false);
        }
        return id;
    }

    public String getNumericConceptId() {
        var idNumerique = conceptRepository.getNextConceptNumericId();
        if (idNumerique == null) {
            throw new IllegalStateException("Impossible de récupérer un ID depuis la séquence concept__id_seq");
        }

        String idConcept = String.valueOf(idNumerique);
        while (!conceptRepository.findByIdConcept(idConcept).isEmpty()) {
            idConcept = String.valueOf(++idNumerique);
        }

        return idConcept;
    }
    private boolean isIdExiste(String idConcept) {

        log.debug("Vérifier l'existence de l'id concept {}", idConcept);
        var concepts = conceptRepository.findByIdConcept(idConcept);
        log.debug("Le concept id {} existe ? {}", idConcept, CollectionUtils.isNotEmpty(concepts));
        return CollectionUtils.isNotEmpty(concepts);
    }
}
