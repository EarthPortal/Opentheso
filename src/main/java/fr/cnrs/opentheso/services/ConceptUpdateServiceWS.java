package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.skos.SkosConceptDto;
import fr.cnrs.opentheso.models.skos.SkosConceptUpdateDto;
import fr.cnrs.opentheso.repositories.*;
import fr.cnrs.opentheso.services.mappers.SkosConceptMapper;
import fr.cnrs.opentheso.services.mappers.SkosConceptUpdateMapper;
import fr.cnrs.opentheso.services.security.CryptoService;
import fr.cnrs.opentheso.services.utils.IdentifierService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ConceptUpdateServiceWS {


    private final ConceptRepository conceptRepository;

    private final SkosConceptMapper skosConceptMapper;

    private final TermRepository termRepository;

    private final NonPreferredTermService nonPreferredTermService;

    private final NoteService noteService;
    private final SkosConceptUpdateMapper skosConceptUpdateMapper;

    public boolean exists(String idConcept, String idThesaurus) {
        if (StringUtils.isBlank(idConcept) || StringUtils.isBlank(idThesaurus)) {
            return false;
        }
        return conceptRepository.existsByIdConceptAndIdThesaurus(idConcept, idThesaurus);
    }

    @Transactional
    public SkosConceptUpdateDto updateConcept(SkosConceptUpdateDto dto, String idThesaurus, String idConcept, Integer userId) {

        skosConceptUpdateMapper.finalizeMapping(dto);

        // 2️⃣ Labels (PATCH-like)
        updatePrefLabels(idConcept, idThesaurus, dto.getPrefLabels(), userId);
   //     updateMultiLabels(idConcept, idThesaurus, dto.getAltLabels(), true, userId);
   //     updateMultiLabels(idConcept, idThesaurus, dto.getHiddenLabels(), false, userId);

        // 3️⃣ Notes (PATCH-like)
    //    updateNotes(idConcept, idThesaurus, dto.getNotes(), userId);

        // 4️⃣ Relations (à compléter selon besoin)
    //    updateRelations(idConcept, idThesaurus, dto.getRelations(), userId);

        // 5️⃣ Images et GPS (remplacement)
    //    imageService.replaceImages(idConcept, idThesaurus, dto.getImages(), userId);
    //    gpsService.replaceGps(idConcept, idThesaurus, dto.getGpsPoints());

        // 6️⃣ Mise à jour date modification
    //    conceptRepository.updateModifiedDate(idConcept, idThesaurus, new Date(), userId);

        return dto;
    }

    private void updatePrefLabels(String idConcept,
                                  String idThesaurus,
                                  Map<String, String> updatedLabels,
                                  Integer userId) {

 /*       Map<String, String> existingLabels = termRepository.getPrefLabels(idConcept, idThesaurus);

        syncMapByLang(
                existingLabels,
                updatedLabels,
                entry -> termRepository.saveOrUpdatePrefLabel(idConcept, idThesaurus, entry.getKey(), entry.getValue(), userId),
                lang -> termRepository.deletePrefLabel(idConcept, idThesaurus, lang)
        );*/
    }

    private void updateMultiLabels(String idConcept,
                                   String idThesaurus,
                                   Map<String, List<String>> updatedLabels,
                                   boolean isAltLabel,
                                   Integer userId) {

/*        if (updatedLabels == null) return;

        Map<String, List<String>> existingLabels = nonPreferredTermService.getLabelsByConcept(idConcept, idThesaurus, isAltLabel);

        // DELETE par langue
        for (String lang : existingLabels.keySet()) {
            if (!updatedLabels.containsKey(lang)) {
                nonPreferredTermService.deleteLabels(idConcept, idThesaurus, lang, isAltLabel);
            }
        }

        // ADD / UPDATE par langue
        for (Map.Entry<String, List<String>> entry : updatedLabels.entrySet()) {
            String lang = entry.getKey();
            List<String> newValues = entry.getValue().stream()
                    .filter(v -> v != null && !v.isBlank())
                    .map(String::trim)
                    .toList();

            List<String> oldValues = existingLabels.getOrDefault(lang, List.of());

            // DELETE valeurs supprimées
            oldValues.stream()
                    .filter(v -> !newValues.contains(v))
                    .forEach(v -> nonPreferredTermService.deleteLabel(idConcept, idThesaurus, lang, v, isAltLabel));

            // ADD nouvelles valeurs
            newValues.stream()
                    .filter(v -> !oldValues.contains(v))
                    .forEach(v -> nonPreferredTermService.addLabel(idConcept, idThesaurus, lang, v, isAltLabel, userId));
        }*/
    }

    private void updateNotes(String idConcept,
                             String idThesaurus,
                             Map<String, Map<String, String>> updatedNotes,
                             Integer userId) {

 /*       if (updatedNotes == null) return;

        Map<String, Map<String, String>> existingNotes = noteService.getNotesByConcept(idConcept, idThesaurus);

        // DELETE types de notes absents dans le DTO
        for (String type : existingNotes.keySet()) {
            if (!updatedNotes.containsKey(type)) {
                noteService.deleteNotesByType(idConcept, idThesaurus, type);
            }
        }

        // ADD / UPDATE
        for (Map.Entry<String, Map<String, String>> typeEntry : updatedNotes.entrySet()) {
            String type = typeEntry.getKey();
            Map<String, String> newLangMap = typeEntry.getValue();
            Map<String, String> oldLangMap = existingNotes.getOrDefault(type, Map.of());

            syncMapByLang(
                    oldLangMap,
                    newLangMap,
                    entry -> noteService.addOrUpdateNote(idConcept, idThesaurus, entry.getKey(), type, entry.getValue(), userId),
                    lang -> noteService.deleteNote(idConcept, idThesaurus, type, lang)
            );
        }*/
    }

    private void syncMapByLang(Map<String, String> existing,
                               Map<String, String> updates,
                               Consumer<Map.Entry<String, String>> onAddOrUpdate,
                               Consumer<String> onDelete) {

        if (updates == null) updates = Map.of();

        // DELETE : langues absentes
        for (String lang : existing.keySet()) {
            if (!updates.containsKey(lang)) {
                onDelete.accept(lang);
            }
        }

        // ADD / UPDATE
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String lang = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().trim() : null;

            if (value == null || value.isBlank()) {
                if (existing.containsKey(lang)) onDelete.accept(lang);
            } else {
                if (!value.equals(existing.get(lang))) {
                    onAddOrUpdate.accept(entry);
                }
            }
        }
    }

    // TODO : Implémenter updateRelations selon logique PATCH-like des relations
    private void updateRelations(String idConcept,
                                 String idThesaurus,
                                 Map<String, List<String>> updatedRelations,
                                 Integer userId) {
        // À compléter : similaire à updateMultiLabels, en comparant l’existant et le DTO
    }
}