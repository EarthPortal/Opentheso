package fr.cnrs.opentheso.ws.graphql.dto;

public record MutationResponse(
        boolean success,
        String message,
        String idConcept,
        String idThesaurus
) {}
