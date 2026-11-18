package software.ulpgc.katas;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.ulpgc.katas.adapters.ApiCharacterReader;
import software.ulpgc.katas.adapters.JsonCharacterWriter;
import software.ulpgc.katas.domain.model.Character;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiCharacterReaderAndWriterIntegrationTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldFetchAndWriteUtf8JsonWithExpectedCharactersAndMetadataOn200(@TempDir Path tempDir) throws IOException {
        String sampleResponse = """
                {
                  "results": [
                    { "id": 1, "name": "Rick Sánchez", "species": "Human", "image": "https://example.com/rick.jpg" },
                    { "id": 2, "name": "Morty Smith", "species": "Human", "image": "https://example.com/morty.jpg" }
                  ]
                }
                """;
        URI uri = startServer(200, sampleResponse, 0);

        ApiCharacterReader reader = new ApiCharacterReader(uri, Duration.ofSeconds(2));
        Path outputFile = tempDir.resolve("characters_report.json");
        JsonCharacterWriter writer = new JsonCharacterWriter(outputFile.toFile());

        List<Character> characters = reader.readCharacters();
        writer.writeCharacters(characters);

        String actualJson = Files.readString(outputFile, StandardCharsets.UTF_8);
        JsonObject parsed = new Gson().fromJson(actualJson, JsonObject.class);

        JsonObject metadata = parsed.getAsJsonObject("metadata");
        assertEquals(2, metadata.get("totalCharacters").getAsInt());
        assertEquals("Rick and Morty API", metadata.get("apiSource").getAsString());

        JsonArray parsedCharacters = parsed.getAsJsonArray("characters");
        assertEquals(2, parsedCharacters.size());
        assertEquals("Rick Sánchez", parsedCharacters.get(0).getAsJsonObject().get("name").getAsString());
        assertEquals("Morty Smith", parsedCharacters.get(1).getAsJsonObject().get("name").getAsString());
    }

    @Test
    void shouldThrowIllegalStateExceptionOnHttpError404() throws IOException {
        URI uri = startServer(404, "{\"error\":\"Not Found\"}", 0);
        ApiCharacterReader reader = new ApiCharacterReader(uri, Duration.ofSeconds(2));

        IllegalStateException ex = assertThrows(IllegalStateException.class, reader::readCharacters);
        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    void shouldThrowJsonSyntaxExceptionOnInvalidJsonBody() throws IOException {
        URI uri = startServer(200, "{not-valid-json", 0);
        ApiCharacterReader reader = new ApiCharacterReader(uri, Duration.ofSeconds(2));

        assertThrows(JsonSyntaxException.class, reader::readCharacters);
    }

    @Test
    void shouldFailWithTimeoutCauseWhenServerExceedsTimeout() throws IOException {
        URI uri = startServer(200, "{\"results\":[]}", 500);
        ApiCharacterReader reader = new ApiCharacterReader(uri, Duration.ofMillis(100));

        RuntimeException ex = assertThrows(RuntimeException.class, reader::readCharacters);
        assertInstanceOf(HttpTimeoutException.class, ex.getCause());
    }

    @Test
    void shouldFailWithConnectionErrorWhenServerIsUnreachable() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        URI unreachableUri = URI.create("http://127.0.0.1:" + closedPort + "/api/character");
        ApiCharacterReader reader = new ApiCharacterReader(unreachableUri, Duration.ofSeconds(1));

        RuntimeException ex = assertThrows(RuntimeException.class, reader::readCharacters);
        assertInstanceOf(IOException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("Error de conexión"));
    }

    private URI startServer(int statusCode, String body, long delayMillis) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/character", exchange -> {
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/character");
    }
}
