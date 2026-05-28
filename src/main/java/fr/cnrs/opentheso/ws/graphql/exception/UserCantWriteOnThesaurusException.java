package fr.cnrs.opentheso.ws.graphql.exception;


public class UserCantWriteOnThesaurusException extends RuntimeException {
    public UserCantWriteOnThesaurusException() {
        super("The user does not have write access to this thesaurus.");
    }
}