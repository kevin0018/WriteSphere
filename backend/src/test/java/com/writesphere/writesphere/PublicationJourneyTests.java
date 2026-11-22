package com.writesphere.writesphere;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PublicationJourneyTests {
    private static final String PASSWORD = "long-test-passphrase";
    private final String fixture = "test-" + UUID.randomUUID().toString().substring(0, 12);
    @LocalServerPort int port;
    @Autowired JdbcClient jdbc;
    @Autowired JsonMapper json;

    @AfterEach
    void removeFixtures() {
        String emailPattern = fixture + "-%@example.org";
        jdbc.sql("""
                DELETE FROM reports WHERE work_id IN (
                    SELECT w.id FROM works w JOIN accounts a ON a.id = w.author_id WHERE a.email LIKE ?
                )
                """).param(emailPattern).update();
        jdbc.sql("""
                DELETE FROM bookmarks WHERE work_id IN (
                    SELECT w.id FROM works w JOIN accounts a ON a.id = w.author_id WHERE a.email LIKE ?
                )
                """).param(emailPattern).update();
        jdbc.sql("DELETE FROM works WHERE author_id IN (SELECT id FROM accounts WHERE email LIKE ?)")
                .param(emailPattern).update();
        jdbc.sql("DELETE FROM accounts WHERE email LIKE ?").param(emailPattern).update();
    }

    @Test
    void passkeyEnrollmentRequiresTheAccountPasswordAndLoginOptionsRequireCsrf() throws Exception {
        String path = "/api/session/passkeys";
        var anonymous = new Client();
        anonymous.send(anonymous.request(path + "/login/options").POST(HttpRequest.BodyPublishers.noBody()), 403);
        var challenge = anonymous.change("POST", path + "/login/options", null, 200);
        assertThat(challenge.get("challenge").asText()).isNotBlank();
        assertThat(challenge.get("userVerification").asText()).isEqualTo("required");
        assertThat(challenge.get("allowCredentials").size()).isZero();
        anonymous.change("POST", path + "/register/options", Map.of("password", PASSWORD), 401);

        var author = member("author");
        author.change("POST", path + "/register/options", Map.of("password", "wrong-password"), 401);
        var options = author.change("POST", path + "/register/options", Map.of("password", PASSWORD), 200);
        assertThat(options.get("user").get("name").asText()).isEqualTo(email("author"));
        assertThat(options.get("authenticatorSelection").get("userVerification").asText()).isEqualTo("required");
        assertThat(author.get(path, 200).size()).isZero();
    }

    @Test
    void withdrawImmediatelyRemovesTheWorkFromEveryPublicReadAndThePrivateBookshelf() throws Exception {
        var author = member("author");
        var reader = member("reader");
        var anonymous = new Client();
        var draft = author.change("POST", "/api/editor/works", Map.of("genre", "POEM"), 201);
        String id = draft.get("id").asText();
        String editor = "/api/editor/works/" + id;
        String publicLink = "/api/public/works/" + id;
        String bookmark = "/api/bookshelf/" + id;
        reader.get(editor, 404);
        anonymous.get(publicLink, 404);
        author.change("POST", editor + "/publish", Map.of("version", 0), 400);

        String body = "Verso largo\n\nÚltimo verso\n";
        var revised = author.change("PUT", editor, revision(0, "Primer título", body), 200);
        author.change("PUT", editor, revision(0, "Copia antigua", body), 409);
        var published = author.change("POST", editor + "/publish", Map.of("version", version(revised)), 200);
        var detail = anonymous.get(publicLink, 200);
        assertThat(detail.get("body").asText()).isEqualTo(body);
        assertThat(detail.toString()).doesNotContain("@example.org", "password");
        Instant firstPublication = Instant.parse(detail.get("publishedAt").asText());
        String profile = "/api/public/authors/" + detail.get("authorId").asText();

        var corrected = author.change("PUT", editor, revision(version(published), "Otro título", body), 200);
        assertThat(Instant.parse(anonymous.get(publicLink, 200).get("publishedAt").asText()))
                .isEqualTo(firstPublication);

        reader.change("PUT", bookmark, null, 204);
        reader.change("PUT", bookmark, null, 204);
        assertThat(ids(reader.get("/api/bookshelf", 200))).containsExactly(id);
        assertThat(ids(author.get("/api/bookshelf", 200))).isEmpty();
        anonymous.get("/api/bookshelf", 401);
        reader.change("DELETE", bookmark, null, 204);
        reader.change("DELETE", bookmark, null, 204);
        assertThat(ids(reader.get("/api/bookshelf", 200))).isEmpty();
        reader.change("PUT", bookmark, null, 204);

        author.change("POST", editor + "/withdraw", Map.of("version", version(corrected)), 200);
        anonymous.get(publicLink, 404);
        assertThat(ids(anonymous.get("/api/public/works?genre=POEM", 200).get("items"))).doesNotContain(id);
        assertThat(ids(anonymous.get(profile, 200).get("works").get("items"))).isEmpty();
        assertThat(ids(reader.get("/api/bookshelf", 200))).isEmpty();
        reader.change("PUT", bookmark, null, 404);
    }

    @Test
    void moderatorCanDismissAndHideWithAnAuditTrailAndTheAuthorCannotRepublish() throws Exception {
        var author = member("author");
        var reader = member("reader");
        var moderator = register("moderator");
        jdbc.sql("UPDATE accounts SET role = 'MODERATOR' WHERE email = ?").param(email("moderator")).update();
        moderator.login(email("moderator"));
        String id = author.change("POST", "/api/editor/works", Map.of("genre", "POEM"), 201).get("id").asText();
        String editor = "/api/editor/works/" + id;
        var revised = author.change("PUT", editor, revision(0, "La obra reportada", "Su texto"), 200);
        author.change("POST", editor + "/publish", Map.of("version", version(revised)), 200);
        String publicLink = "/api/public/works/" + id;
        String report = "/api/works/" + id + "/reports";
        String queue = "/api/moderation/reports";

        reader.get(queue, 403);
        reader.change("POST", report, Map.of("reason", " "), 400);
        reader.change("POST", report, Map.of("reason", "Primer motivo"), 204);
        reader.change("POST", report, Map.of("reason", "Primer motivo"), 204);
        String first = reportIdFor(moderator.get(queue, 200), id);
        reader.change("POST", queue + "/" + first + "/resolve", Map.of("decision", "HIDE"), 403);
        moderator.change("POST", queue + "/" + first + "/resolve", Map.of("decision", "DISMISS"), 204);
        reader.get(publicLink, 200);
        reader.change("POST", report, Map.of("reason", "Segundo motivo"), 204);
        String second = reportIdFor(moderator.get(queue, 200), id);
        reader.change("PUT", "/api/bookshelf/" + id, null, 204);
        moderator.change("POST", queue + "/" + second + "/resolve", Map.of("decision", "HIDE"), 204);

        reader.get(publicLink, 404);
        assertThat(ids(reader.get("/api/bookshelf", 200))).isEmpty();
        var hidden = author.get(editor, 200);
        assertThat(hidden.get("state").asText()).isEqualTo("HIDDEN");
        author.change("POST", editor + "/publish", Map.of("version", version(hidden)), 409);
        author.change("POST", editor + "/withdraw", Map.of("version", version(hidden)), 409);
        moderator.change("POST", queue + "/" + second + "/resolve", Map.of("decision", "DISMISS"), 409);

        var resolution = jdbc.sql("SELECT decision, resolved_at, moderator_id FROM reports WHERE id = ?")
                .param(UUID.fromString(second)).query().singleRow();
        assertThat(resolution.get("decision")).isEqualTo("HIDE");
        assertThat(resolution.get("resolved_at")).isNotNull();
        assertThat(resolution.get("moderator_id")).isNotNull();
    }

    @Test
    void profileCursorIsStableWhenPublicationsShareTheSameTimestamp() throws Exception {
        member("author");
        UUID author = jdbc.sql("SELECT id FROM accounts WHERE email = ?")
                .param(email("author")).query(UUID.class).single();
        OffsetDateTime sameTime = OffsetDateTime.parse("2026-11-22T12:00:00Z");
        for (int index = 1; index <= 13; index++) {
            jdbc.sql("""
                    INSERT INTO works (id, author_id, genre, title, body, state, version, created_at, updated_at, published_at)
                    VALUES (?, ?, 'POEM', 'Título', 'Verso', 'PUBLISHED', 0, ?, ?, ?)
                    """).params(UUID.nameUUIDFromBytes((fixture + index).getBytes(StandardCharsets.UTF_8)),
                    author, sameTime, sameTime, sameTime).update();
        }
        var anonymous = new Client();
        String profile = "/api/public/authors/" + author;
        var first = anonymous.get(profile, 200).get("works");
        var next = first.get("next");
        var last = anonymous.get(profile + "?before=" + next.get("publishedAt").asText()
                + "&beforeId=" + next.get("id").asText(), 200).get("works");
        assertThat(first.get("items").size()).isEqualTo(12);
        assertThat(last.get("items").size()).isEqualTo(1);
        assertThat(ids(first.get("items"))).doesNotContainAnyElementsOf(ids(last.get("items")));
        assertThat(last.get("next").isNull()).isTrue();
    }

    private Client member(String role) throws Exception {
        var client = register(role);
        client.login(email(role));
        return client;
    }

    private Client register(String role) throws Exception {
        var client = new Client();
        client.change("POST", "/api/accounts", Map.of("email", email(role), "penName", fixture + "-" + role,
                "password", PASSWORD, "role", "MODERATOR"), 201);
        return client;
    }

    private String email(String role) {
        return fixture + "-" + role + "@example.org";
    }

    private static long version(JsonNode work) {
        return work.get("version").asLong();
    }

    private static Map<String, Object> revision(long version, String title, String body) {
        return Map.of("genre", "POEM", "version", version, "title", title, "body", body);
    }

    private static List<String> ids(JsonNode array) {
        return StreamSupport.stream(array.spliterator(), false).map(item -> item.get("id").asText()).toList();
    }

    private static String reportIdFor(JsonNode array, String workId) {
        var matches = StreamSupport.stream(array.spliterator(), false)
                .filter(item -> item.get("workId").asText().equals(workId)).toList();
        assertThat(matches).hasSize(1);
        return matches.getFirst().get("id").asText();
    }

    private final class Client {
        private final HttpClient http = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();

        JsonNode get(String path, int status) throws Exception {
            return send(request(path).GET(), status);
        }

        JsonNode change(String method, String path, Map<String, ?> data, int status) throws Exception {
            var csrf = get("/api/csrf", 200);
            var builder = request(path).header(csrf.get("header").asText(), csrf.get("token").asText());
            if (data == null) return send(builder.method(method, HttpRequest.BodyPublishers.noBody()), status);
            return send(builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(data))), status);
        }

        void login(String email) throws Exception {
            var csrf = get("/api/csrf", 200);
            send(request("/api/session/login").header(csrf.get("header").asText(), csrf.get("token").asText())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("email=" + email + "&password=" + PASSWORD)), 204);
        }

        private HttpRequest.Builder request(String path) {
            return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        }

        private JsonNode send(HttpRequest.Builder request, int status) throws Exception {
            var response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).as("%s returned %s", request.build().uri(), response.body())
                    .isEqualTo(status);
            return response.body().isBlank() ? null : json.readTree(response.body());
        }
    }
}
