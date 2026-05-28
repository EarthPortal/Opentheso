package fr.cnrs.opentheso.services.mappers;

import fr.cnrs.opentheso.models.skosapi.SKOSProperty;

import java.util.Map;

/**
 * Mapper minimal pour les relations d'alignements SKOS.
 * Retourne la propriété interne (int) correspondant à chaque type.
 */
public class SkosAlignmentMapper {

    /** Map : nom de la relation → propriété interne (int) */
    private static final Map<String, Integer> alignmentPropertyMap = Map.of(
            "exactMatch", 1,
            "closeMatch", 2,
            "broadMatch", 3,
            "relatedMatch", 4,
            "narrowMatch", 5
    );

    /**
     * Retourne la propriété interne correspondant à un type de relation SKOS
     * @param relationName nom de la relation (ex: "exactMatch")
     * @return propriété interne (ex: 1) ou null si non défini
     */
    public static Integer toAlignmentProperty(String relationName) {
        return alignmentPropertyMap.get(relationName);
    }
}
