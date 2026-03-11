package fr.cnrs.opentheso.ws.openapi.exception;

import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;

public class ApiKeyInvalidException extends RuntimeException {
    private final ApiKeyState state;
    public ApiKeyInvalidException(ApiKeyState state) {
        super("Clé API invalide ou expirée");
        this.state = state;
    }
    public ApiKeyState getState() { return state; }
}