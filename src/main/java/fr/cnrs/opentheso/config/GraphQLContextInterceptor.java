package fr.cnrs.opentheso.config;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GraphQLContextInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {

        // récupérer la clé API depuis le header
        String apiKey = request.getHeaders().getFirst("X-API-KEY");
        // on continue sans clé si elle n’est pas fournie
        if (apiKey == null || apiKey.isBlank()) {
            return chain.next(request);
        }

        // injecter dans le contexte GraphQL
        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(Map.of("X-API-KEY", apiKey)).build()
        );

        return chain.next(request);
    }
}