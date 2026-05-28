package fr.cnrs.opentheso.services.mappers;

import java.util.Map;

public final class SkosRelationMapper {

    private SkosRelationMapper() {} // empêche l'instanciation

    private static final Map<String, String> SKOS_TO_DB_RELATIONS = Map.of(
            "broader", "BT",
            "narrower", "NT",
            "related", "RT",
            "broaderGeneric", "BTG",
            "broaderPartitive", "BTP",
            "broaderInstantial", "BTI",
            "narrowerGeneric", "NTG",
            "narrowerPartitive", "NTP",
            "narrowerInstantial", "NTI"
    );

    public static String toDbRelation(String skosRelation) {
        return SKOS_TO_DB_RELATIONS.get(skosRelation);
    }

    /**
     * Map des inverses pour relations asymétriques.
     * Exemple : BT (Broader Term) ↔ NT (Narrower Term)
     */
    public static final Map<String, String> inverseRelationMap = Map.of(
            "BT", "NT",
            "NT", "BT",
            "BTI", "NTI",
            "NTI", "BTI",
            "BTG", "NTG",
            "NTG", "BTG",
            "BTP", "NTP",
            "NTP", "BTP"
    );
}