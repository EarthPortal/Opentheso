package fr.cnrs.opentheso.ws.openapi.exception;

public class NotationAlreadyExistsException extends RuntimeException {
    public NotationAlreadyExistsException(String notation, String idThesaurus) {
        super("Notation " + notation + " already exists in the thesaurus " + idThesaurus);
    }
}