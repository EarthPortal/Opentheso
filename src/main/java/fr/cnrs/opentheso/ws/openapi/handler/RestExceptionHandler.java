package fr.cnrs.opentheso.ws.openapi.handler;

import fr.cnrs.opentheso.ws.openapi.exception.*;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;

@RestControllerAdvice
@Order(1)
public class RestExceptionHandler {

    @ExceptionHandler(ThesaurusNotFoundException.class)
    public ProblemDetail handleThesaurusNotFound(ThesaurusNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND); // 404
        problem.setTitle("Thesaurus Not Found");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("errorCode", "THESAURUS_NOT_FOUND");
        return problem;
    }

    @ExceptionHandler(ConceptAlreadyExistsException.class)
    public ProblemDetail handleConceptNotFound(ConceptAlreadyExistsException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT); // 404
        problem.setTitle("Concept Conflit");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("errorCode", "CONCEPT_ALREADY_EXISTS");
        return problem;
    }

    @ExceptionHandler(LabelAlreadyExistsException.class)
    public ProblemDetail handleLabelConflict(LabelAlreadyExistsException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT); // 404
        problem.setTitle("Label Conflit");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("errorCode", "LABEL_ALREADY_EXISTS");
        return problem;
    }

    @ExceptionHandler(NotationAlreadyExistsException.class)
    public ProblemDetail handleNotationConflict(NotationAlreadyExistsException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT); // 409
        problem.setTitle("Notation Conflict");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("errorCode", "NOTATION_ALREADY_EXISTS");
        return problem;
    }

    @ExceptionHandler(ApiKeyMissingException.class)
    public ProblemDetail handleMissingApiKey(ApiKeyMissingException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("errorCode", "API_KEY_MISSING");
        return problem;
    }

    @ExceptionHandler(ApiKeyInvalidException.class)
    public ProblemDetail handleInvalidApiKey(ApiKeyInvalidException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(
                ex.getState() == ApiKeyState.EXPIRED ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN
        );
        problem.setTitle("Unauthorized");
        problem.setDetail("Clé API " + ex.getState().name().toLowerCase());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("errorCode", "API_KEY_" + ex.getState().name());
        return problem;
    }

    @ExceptionHandler(UserCantWriteOnThesaurusException.class)
    public ProblemDetail handleFailPermission(UserCantWriteOnThesaurusException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail("The user does not have write access to this thesaurus.");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("errorCode", "No right");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("Une erreur interne est survenue");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", OffsetDateTime.now());
        return problem;
    }
}