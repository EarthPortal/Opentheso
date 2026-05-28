package fr.cnrs.opentheso.models.skos;

//import fr.cnrs.opentheso.bean.importexport.newcsvimport.SkosConceptImageDto;
//import fr.cnrs.opentheso.entites.Gps;
import lombok.Data;

import java.util.*;

/**
 * DTO orienté RDF / SKOS pour représenter un concept/thésaurus pour les données à modifier.
 */
@Data
public class SkosConceptUpdateDto {

    /* =========================
       LABELS SKOS
       ========================= */

    /** prefLabel : un seul par langue */
    private Map<String, String> prefLabels = new HashMap<>();
    /** altLabel : plusieurs valeurs possibles par langue */
    private Map<String, List<String>> altLabels = new HashMap<>();
    /** hiddenLabel : plusieurs valeurs possibles par langue */
    private Map<String, List<String>> hiddenLabels = new HashMap<>();

    public String getPrefLabel(String lang) {
        return prefLabels.get(lang);
    }
/*
    public void setPrefLabel(String lang, String value) {
        if (value != null && !value.isBlank()) {
            prefLabels.put(lang, value.trim());
        }
    }

    public void addAltLabel(String lang, String value) {
        if (value == null || value.isBlank()) return;
        List<String> list = altLabels.computeIfAbsent(lang, k -> new ArrayList<>());
        String[] parts = value.split("##");
        for (String p : parts) {
            if (!p.isBlank()) list.add(p.trim());
        }
    }

    public void addHiddenLabel(String lang, String value) {
        if (value == null || value.isBlank()) return;
        List<String> list = hiddenLabels.computeIfAbsent(lang, k -> new ArrayList<>());
        String[] parts = value.split("##");
        for (String p : parts) {
            if (!p.isBlank()) list.add(p.trim());
        }
    }*/
}