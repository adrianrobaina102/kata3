package software.ulpgc.katas;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.ulpgc.katas.adapters.JsonFileGameWriter;
import software.ulpgc.katas.adapters.JsonUrlGameReader;
import software.ulpgc.katas.domain.model.VideoGame;

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

class JsonUrlGameReaderAndWriterIntegrationTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldFetchAndWriteUtf8JsonWithExpectedGamesAndMetadataOn200(@TempDir Path tempDir) throws IOException {
        String sampleResponse = """
                {
                  "games": [
                    { "title": "Hollow Knight: Edición", "genre": "Metroidvania", "releaseYear": 2017, "platform": "PC" },
                    { "title": "Hades II", "genre": "Roguelite", "releaseYear": 2024, "platform": "Multiplatform" }
                  ]
                }
                """;
        URI uri = startServer(200, sampleResponse, 0);

        JsonUrlGameReader reader = new JsonUrlGameReader(uri, Duration.ofSeconds(2));
        Path outputFile = tempDir.resolve("games.json");
        JsonFileGameWriter writer = new JsonFileGameWriter(outputFile.toFile());

        List<VideoGame> games = reader.read();
        writer.write(games);

        String actualJson = Files.readString(outputFile, StandardCharsets.UTF_8);
        JsonObject parsed = new Gson().fromJson(actualJson, JsonObject.class);

        JsonObject metada = parsed.getAsJsonObject("metada");
        assertEquals("Title Report", metada.get("reportTitle").getAsString());
        assertEquals(2, metada.get("totalGamesProcessed").getAsInt());

        JsonArray parsedGames = parsed.getAsJsonArray("games");
        assertEquals(2, parsedGames.size());
        assertEquals("Hollow Knight: Edición", parsedGames.get(0).getAsJsonObject().get("title").getAsString());
        assertEquals("Hades II", parsedGames.get(1).getAsJsonObject().get("title").getAsString());
    }

    @Test
    void shouldThrowIllegalStateExceptionOnHttpError404() throws IOException {
        URI uri = startServer(404, "{\"error\":\"Not Found\"}", 0);
        JsonUrlGameReader reader = new JsonUrlGameReader(uri, Duration.ofSeconds(2));

        IllegalStateException ex = assertThrows(IllegalStateException.class, reader::read);
        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    void shouldThrowJsonSyntaxExceptionOnInvalidJsonBody() throws IOException {
        URI uri = startServer(200, "{not-valid-json", 0);
        JsonUrlGameReader reader = new JsonUrlGameReader(uri, Duration.ofSeconds(2));

        assertThrows(JsonSyntaxException.class, reader::read);
    }

    @Test
    void shouldFailWithTimeoutCauseWhenServerExceedsTimeout() throws IOException {
        URI uri = startServer(200, "{\"games\":[]}", 500);
        JsonUrlGameReader reader = new JsonUrlGameReader(uri, Duration.ofMillis(100));

        RuntimeException ex = assertThrows(RuntimeException.class, reader::read);
        assertInstanceOf(HttpTimeoutException.class, ex.getCause());
    }

    @Test
    void shouldFailWithConnectionErrorWhenServerIsUnreachable() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        URI unreachableUri = URI.create("http://127.0.0.1:" + closedPort + "/games");
        JsonUrlGameReader reader = new JsonUrlGameReader(unreachableUri, Duration.ofSeconds(1));

        RuntimeException ex = assertThrows(RuntimeException.class, reader::read);
        assertInstanceOf(IOException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("Error de conexión"));
    }

    private URI startServer(int statusCode, String body, long delayMillis) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/games", exchange -> {
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
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/games");
    }
}
