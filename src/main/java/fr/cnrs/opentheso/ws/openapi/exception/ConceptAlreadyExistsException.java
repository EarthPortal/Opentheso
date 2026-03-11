package fr.cnrs.opentheso.ws.openapi.exception;

public class ConceptAlreadyExistsException extends RuntimeException{
    public ConceptAlreadyExistsException(String id) {
        super("The concept " + id + " already exists");
    }
}
