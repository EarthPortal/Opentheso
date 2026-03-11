package fr.cnrs.opentheso.ws.openapi.exception;

public class ThesaurusNotFoundException extends RuntimeException {
    public ThesaurusNotFoundException(String id) {
        super("The thesaurus " + id + " does not exist");
    }
}
