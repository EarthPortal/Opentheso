package fr.cnrs.opentheso.ws.openapi.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cnrs.opentheso.models.concept.ConceptNote;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jfr.Description;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v2")
@Tag(name = "Api v2")

public class ReconciliationController {

    private final RestRDFHelper restRDFHelper;
    private final ObjectMapper mapper = new ObjectMapper();
    private final LevenshteinDistance levenshtein = new LevenshteinDistance();

    public ReconciliationController(RestRDFHelper restRDFHelper) {
        this.restRDFHelper = restRDFHelper;
    }

    // =========================================================
    // 1. METADATA (IMPORTANT OPENREFINE ENTRY POINT)
    // =========================================================
    @GetMapping("/{thesaurus}/{lang}/reconcile")
    @Operation(
            summary = "OpenRefine - Reconcile",
            description = "Pour la reconciliation avec OpenRefine."
    )
    public Map<String, Object> metadata(@PathVariable String thesaurus, @PathVariable String lang) {
        return Map.of(
                "name", "Opentheso Reconciliation Service",
                "versions", List.of("1.0"),

                "identifierSpace", "http://localhost:8099/",
                "schemaSpace", "http://localhost:8099/schema/",

                "defaultTypes", List.of(
                        Map.of("id", "concept", "name", "Concept")
                ),

                // IMPORTANT : URL TEMPLATE multi-thésaurus
                "view", Map.of(
                        "url", "http://localhost:8099/?idc={{id}}&idt=" + thesaurus
                ),

                "suggest", Map.of(
                        "entity", Map.of(
                                "service_url", "http://localhost:8099/api/v2/" + thesaurus + "/" + lang,
                                "service_path", "/suggest/entity"
                        ),
                        "property", Map.of(
                                "service_url", "http://localhost:8099/api/v2",
                                "service_path", "/suggest/properties"
                        )
                ),

                "extend", Map.of(
                        "propose_properties", Map.of(
                                "service_url", "http://localhost:8099/api/v2",
                                "service_path", "/propose_properties"
                        )
                ),

                "preview", Map.of(
                        "url",
                        "http://localhost:8099/api/v2/"
                                + "preview/"
                                + thesaurus + "/"
                                + "{{id}}",
                        "height", 120,
                        "width", 400
                ),

                // IMPORTANT : propriétés utilisables dans OpenRefine UI
                "properties", List.of(
                        Map.of("id", "thesaurus", "name", "Thesaurus"),
                        Map.of("id", "lang", "name", "Language")
                )
        );
    }

    // =========================================================
    // 2. RECONCILIATION CORE
    // =========================================================
    @PostMapping(
            value = "/{thesaurus}/{lang}/reconcile",
            consumes = "application/x-www-form-urlencoded"
    )
    @Operation(
            summary = "OpenRefine - Reconcile",
            description = "Pour la reconciliation avec OpenRefine."
    )
    public Map<String, Object> reconcile(
            @PathVariable String thesaurus,
            @PathVariable String lang,
            @RequestParam String queries
    ) throws Exception {

        JsonNode root = mapper.readTree(queries);
        Map<String, Object> response = new LinkedHashMap<>();

        root.fieldNames().forEachRemaining(key -> {

            JsonNode q = root.get(key);
            String query = q.path("query").asText("");

            List<NodeAutoCompletion> data =
                    restRDFHelper.searchAutoCompletionWS(query, lang, null, thesaurus, true);

            response.put(key, buildResult(data, query, thesaurus, false));
        });

        return response;
    }

    // =========================================================
// 3. EXTEND permet de récuperer les informations pour un concept reconcilié
// =========================================================
    @PostMapping(
            value = "/{thesaurus}/{lang}/reconcile",
            params = "extend",
            consumes = "application/x-www-form-urlencoded"
    )
    @Operation(
            summary = "OpenRefine - Extend (stable version)",
            description = "Enrichissement stable des concepts reconciliés (ordre indépendant de l'UI)."
    )
    public Map<String, Object> extend(
            @PathVariable String thesaurus,
            @PathVariable String lang,
            @RequestParam String extend
    ) throws Exception {

        JsonNode json = mapper.readTree(extend);

        // =========================
        // IDS
        // =========================
        List<String> ids = new ArrayList<>();
        JsonNode idsNode = json.get("ids");

        if (idsNode != null && idsNode.isArray()) {
            idsNode.forEach(n -> ids.add(n.asText()));
        }

        // =========================
        // PROPERTIES (RAW FROM OPENREFINE)
        // =========================
        List<String> requestedProps = new ArrayList<>();
        JsonNode propsNode = json.get("properties");

        if (propsNode != null && propsNode.isArray()) {
            propsNode.forEach(p -> {
                JsonNode idNode = p.get("id");
                if (idNode != null) {
                    requestedProps.add(idNode.asText());
                }
            });
        }

        // =========================
        // CANONICAL ORDER (IMPORTANT FIX)
        // =========================
        List<String> canonicalOrder = List.of(
                "prefLabel",
                "description",
                "aliases",
                "ark",
                "uri"
        );

        List<String> props = canonicalOrder.stream()
                .filter(requestedProps::contains)
                .toList();

        // =========================
        // RESULT
        // =========================
        Map<String, Object> rows = new LinkedHashMap<>();

        for (String id : ids) {

            NodeFullConcept concept =
                    restRDFHelper.getNodeFullConcept(thesaurus, id, lang);

            Map<String, Object> row = new LinkedHashMap<>();

            for (String prop : props) {

                List<Map<String, String>> values = new ArrayList<>();

                switch (prop) {

                    case "prefLabel" -> {
                        String label = concept.getPrefLabel() != null
                                ? concept.getPrefLabel().getLabel()
                                : "";

                        values.add(Map.of("str", label));
                    }

                    case "description" -> {
                        String def = safeNote(concept.getDefinitions());
                        values.add(Map.of("str", def != null ? def : ""));
                    }

                    case "aliases" -> {

                        if (concept.getAltLabels() != null && !concept.getAltLabels().isEmpty()) {

                            for (var a : concept.getAltLabels()) {
                                if (a != null && isValid(a.getLabel())) {
                                    values.add(Map.of("str", a.getLabel()));
                                }
                            }
                        }

                        if (values.isEmpty()) {
                            values.add(Map.of("str", ""));
                        }
                    }

                    case "ark" -> {
                        values.add(Map.of(
                                "str",
                                isValid(concept.getPermanentId())
                                        ? concept.getPermanentId()
                                        : ""
                        ));
                    }

                    case "uri" -> {

                        String uri = isValid(concept.getPermanentId())
                                ? concept.getPermanentId()
                                : "http://localhost:8099/?idc="
                                  + id
                                  + "&idt="
                                  + thesaurus;

                        values.add(Map.of("str", uri));
                    }

                    default -> values.add(Map.of("str", ""));
                }

                // IMPORTANT : toujours écrire la clé (pas conditionnel)
                row.put(prop, values);
            }

            // IMPORTANT : garantir complétude même si OpenRefine change l’ordre
            for (String p : canonicalOrder) {
                row.putIfAbsent(p, List.of(Map.of("str", "")));
            }

            rows.put(id, row);
        }

        // =========================
        // META (FIXED ORDER TOO)
        // =========================
        List<Map<String, Object>> meta = List.of(
                Map.of("id", "prefLabel", "name", "Preferred label"),
                Map.of("id", "description", "name", "Definition"),
                Map.of("id", "aliases", "name", "Alternative labels"),
                Map.of("id", "ark", "name", "ARK Identifier"),
                Map.of("id", "uri", "name", "URI")
        );

        return Map.of(
                "meta", meta,
                "rows", rows
        );
    }
    private boolean isValid(String s) {
        return s != null && !s.isBlank();
    }

    // =========================================================
    // 4. SUGGEST ENTITY
    // =========================================================
    @GetMapping("/{thesaurus}/{lang}/suggest/entity")
    @Operation(
            summary = "OpenRefine - Reconcile",
            description = "Pour la reconciliation avec OpenRefine."
    )
    public Map<String, Object> suggestEntity(
            @PathVariable String thesaurus,
            @PathVariable String lang,
            @RequestParam String prefix
    ) {

        List<NodeAutoCompletion> data =
                restRDFHelper.searchAutoCompletionWS(prefix, lang, null, thesaurus, false);

        List<Map<String, Object>> result = new ArrayList<>();

        if (data != null) {
            for (NodeAutoCompletion c : data) {
                result.add(buildConcept(c, prefix, thesaurus, true));
            }
        }

        result.sort((a, b) ->
                Integer.compare((int) b.get("score"), (int) a.get("score"))
        );

        return Map.of("result", result);
    }

    // =========================================================
    // 5. PROPERTIES
    // =========================================================
    @GetMapping("/suggest/properties")
    public Map<String, Object> suggestProperties() {

        return Map.of(
                "result", List.of(

                        Map.of(
                                "id", "prefLabel",
                                "name", "Preferred label"
                        ),

                        Map.of(
                                "id", "description",
                                "name", "Description"
                        ),

                        Map.of(
                                "id", "aliases",
                                "name", "Alternative labels"
                        ),

                        Map.of(
                                "id", "ark",
                                "name", "ARK Identifier"
                        ),

                        Map.of(
                                "id", "uri",
                                "name", "URI"
                        )
                )
        );
    }

    @GetMapping("/propose_properties")
    public Map<String, Object> propose() {

        return Map.of(

                "type",
                Map.of(
                        "id", "concept",
                        "name", "Concept"
                ),

                "properties", List.of(

                        Map.of(
                                "id", "prefLabel",
                                "name", "Preferred label"
                        ),

                        Map.of(
                                "id", "description",
                                "name", "Description"
                        ),

                        Map.of(
                                "id", "aliases",
                                "name", "Alternative labels"
                        ),

                        Map.of(
                                "id", "ark",
                                "name", "ARK Identifier"
                        ),

                        Map.of(
                                "id", "uri",
                                "name", "URI"
                        )
                )
        );
    }


    @GetMapping(value = "/preview/{thesaurus}/{id}", produces = "text/html")
    @Operation(
            summary = "OpenRefine - Reconcile",
            description = "Pour la reconciliation avec OpenRefine."
    )
    public ResponseEntity<String> preview(
            @PathVariable String thesaurus,
            @PathVariable String id
    ) {

        NodeFullConcept c =
                restRDFHelper.getNodeFullConcept(thesaurus, id, "fr");

        String html =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<style>" +
                        "body { font-family: Arial; font-size: 12px; margin: 6px; }" +
                        "h4 { font-size: 13px; margin: 0 0 4px 0; }" +
                        "p { font-size: 12px; margin: 2px 0; color: #444; }" +
                        "hr { margin: 6px 0; border: none; border-top: 1px solid #ddd; }" +
                        "small { font-size: 11px; color: #666; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<h4>" + safe(c.getPrefLabel().getLabel()) + "</h4>" +
                        "<p>" + safeNote(c.getDefinitions()) + "</p>" +
                        "<hr/>" +
                        "<small>ID: " + id + "</small>" +
                        "</body></html>";

        return ResponseEntity.ok()
                .header("Content-Security-Policy", "frame-ancestors 'self' *")
                .body(html);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
    private String safeNote(List<ConceptNote> notes) {
        if(notes == null || notes.isEmpty()) { return ""; }
        return notes.get(0).getLabel();
    }


    // =========================================================
    // 6. RESULT BUILDER
    // =========================================================
    private Map<String, Object> buildResult(
            List<NodeAutoCompletion> data,
            String query,
            String thesaurus,
            boolean suggestMode
    ) {

        List<Map<String, Object>> results = new ArrayList<>();

        if (data != null) {
            for (NodeAutoCompletion c : data) {
                results.add(buildConcept(c, query, thesaurus, suggestMode));
            }
        }

        return Map.of("result", results);
    }

    private Map<String, Object> buildConcept(
            NodeAutoCompletion c,
            String query,
            String thesaurus,
            boolean suggestMode
    ) {

        int score = computeScore(c.getPrefLabel(), query, suggestMode);

        Map<String, Object> m = new LinkedHashMap<>();

        // =========================
        // CORE
        // =========================
        m.put("id", c.getIdConcept());
        m.put("name", c.getPrefLabel());
        m.put("score", score);
        m.put("match", score >= 95);

        m.put("type", List.of(
                Map.of("id", "concept", "name", "Concept")
        ));

        // =========================
        // STABLE URI (IMPORTANT)
        // =========================
        m.put("uri",
                "http://localhost:8099/resource/"
                        + thesaurus + "/"
                        + c.getIdConcept()
        );

        // optional ARK
        if (c.getIdArk() != null && !c.getIdArk().isBlank()) {
            m.put("persistentIdentifier", c.getIdArk());
        }

        // =========================
        // DESCRIPTION (tooltip OpenRefine)
        // =========================
        if (c.getDefinition() != null && !c.getDefinition().isBlank()) {
            m.put("description", c.getDefinition());
        }

        // =========================
        // ALIASES
        // =========================
        if (c.getAltLabelValue() != null && !c.getAltLabelValue().isBlank()) {

            List<String> aliases = Arrays.stream(c.getAltLabelValue().split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            if (!aliases.isEmpty()) {
                m.put("aliases", aliases);
            }
        }

        // =========================
        // PREVIEW (IMPORTANT FIX)
        // =========================
        m.put("preview", Map.of(
                "url",
                "http://localhost:8099/api/v2/preview/"
                        + thesaurus + "/"
                        + c.getIdConcept(),
                "height", 120,
                "width", 400
        ));

        return m;
    }

    // =========================================================
    // 7. SCORE ENGINE
    // =========================================================
    private int computeScore(String label, String query, boolean suggestMode) {

        String l = normalize(label);
        String q = normalize(query);

        if (l.isEmpty() || q.isEmpty()) return 0;

        if (l.equals(q)) return 100;

        if (!suggestMode) {
            int max = Math.max(l.length(), q.length());
            int dist = levenshtein.apply(l, q);

            double sim = 1.0 - ((double) dist / max);
            return (int) (sim * 100);
        }

        // =========================
        // SUGGEST MODE (REBALANCED)
        // =========================

        int score;

        // 1. base discrimination forte
        if (l.equals(q)) {
            return 100;
        }

        if (l.startsWith(q)) {
            score = 90;
        } else if (l.contains(q)) {
            score = 60;
        } else {
            score = 25;
        }

        // 2. boost exact prefix length match (important)
        if (l.startsWith(q) && l.length() == q.length()) {
            score += 5;
        }

        // 3. penalties faibles MAIS pas cumulatives fortes
        if (l.equals(q + "s")) {
            score -= 8;
        }

        if (l.matches(q + "\\d+")) {
            score -= 12;
        }

        // 4. Levenshtein léger (ne doit PAS écraser le score)
        int dist = levenshtein.apply(l, q);
        score -= Math.min(dist, 10);

        // 5. NORMALISATION (IMPORTANT)
        return Math.max(0, Math.min(score, 100));
    }

    private String normalize(String s) {
        return (s == null) ? "" :
                s.toLowerCase()
                .trim()
                .replaceAll("[_\\-]", " ")
                .replaceAll("\\s+", " ");
    }
}