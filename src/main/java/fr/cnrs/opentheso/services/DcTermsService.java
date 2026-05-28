package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DcTermsService {

    private final ConceptDcTermRepository conceptDcTermRepository;

    public void updateConceptDcTerms(String idConcept,
                                     String idThesaurus,
                                     String contributorName) {

        conceptDcTermRepository.save(
                ConceptDcTerm.builder()
                        .name(DCMIResource.CONTRIBUTOR)
                        .value(contributorName)
                        .idConcept(idConcept)
                        .idThesaurus(idThesaurus)
                        .build()
        );
    }
}