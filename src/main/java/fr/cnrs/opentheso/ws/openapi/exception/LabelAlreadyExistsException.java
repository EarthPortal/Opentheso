package fr.cnrs.opentheso.ws.openapi.exception;

public class LabelAlreadyExistsException extends RuntimeException{
    public LabelAlreadyExistsException(String label) {
        super("The Label " + label + " already exists");
    }
}
