package software.ulpgc.katas;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.ulpgc.katas.adapters.ApiUserReader;
import software.ulpgc.katas.adapters.JsonCorporateDirectoryWriter;
import software.ulpgc.katas.domain.model.CorporateDirectoryReport;
import software.ulpgc.katas.domain.model.User;
import software.ulpgc.katas.domain.service.CorporateDirectoryTransformer;

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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiUserReaderAndWriterIntegrationTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldFetchTransformAndWriteExpectedUtf8JsonOn200(@TempDir Path tempDir) throws IOException {
        String sampleResponse = """
                [
                  {
                    "id": 2,
                    "name": "Álvaro Pérez",
                    "username": "aperez",
                    "email": "alvaro@example.com"
                  },
                  {
                    "id": 1,
                    "name": "Leanne Graham",
                    "username": "Bret",
                    "email": "Sincere@april.biz"
                  }
                ]
                """;
        URI uri = startServer(200, sampleResponse, 0);

        ApiUserReader reader = new ApiUserReader(uri, Duration.ofSeconds(2));
        CorporateDirectoryTransformer transformer = new CorporateDirectoryTransformer();
        Path outputFile = tempDir.resolve("corporate_directory.json");
        JsonCorporateDirectoryWriter writer = new JsonCorporateDirectoryWriter(outputFile.toFile());

        List<User> users = reader.readUsers();
        CorporateDirectoryReport report = transformer.transform(users, LocalDate.of(2026, 10, 22));
        writer.writeReport(report);

        String actualJson = Files.readString(outputFile, StandardCharsets.UTF_8);
        String expectedJson = """
                {
                  "metadata": {
                    "totalUsers": 2,
                    "reportDate": "2026-10-22",
                    "purpose": "Corporate Directory Report"
                  },
                  "users": [
                    {
                      "id": 1,
                      "name": "Leanne Graham",
                      "username": "Bret",
                      "email": "Sincere@april.biz"
                    },
                    {
                      "id": 2,
                      "name": "Álvaro Pérez",
                      "username": "aperez",
                      "email": "alvaro@example.com"
                    }
                  ]
                }""";
        assertEquals(expectedJson, actualJson.replace("\r\n", "\n"));
    }

    @Test
    void shouldThrowIllegalStateExceptionOnHttpError404() throws IOException {
        URI uri = startServer(404, "{\"error\":\"Not Found\"}", 0);
        ApiUserReader reader = new ApiUserReader(uri, Duration.ofSeconds(2));

        IllegalStateException ex = assertThrows(IllegalStateException.class, reader::readUsers);
        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    void shouldThrowJsonSyntaxExceptionOnInvalidJsonBody() throws IOException {
        URI uri = startServer(200, "{not-valid-json", 0);
        ApiUserReader reader = new ApiUserReader(uri, Duration.ofSeconds(2));

        assertThrows(JsonSyntaxException.class, reader::readUsers);
    }

    @Test
    void shouldFailWithTimeoutCauseWhenServerExceedsTimeout() throws IOException {
        URI uri = startServer(200, "[]", 500);
        ApiUserReader reader = new ApiUserReader(uri, Duration.ofMillis(100));

        RuntimeException ex = assertThrows(RuntimeException.class, reader::readUsers);
        assertInstanceOf(HttpTimeoutException.class, ex.getCause());
    }

    @Test
    void shouldFailWithConnectionErrorWhenServerIsUnreachable() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        URI unreachableUri = URI.create("http://127.0.0.1:" + closedPort + "/users");
        ApiUserReader reader = new ApiUserReader(unreachableUri, Duration.ofSeconds(1));

        RuntimeException ex = assertThrows(RuntimeException.class, reader::readUsers);
        assertInstanceOf(IOException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("Error de conexión"));
    }

    private URI startServer(int statusCode, String body, long delayMillis) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/users", exchange -> {
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
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/users");
    }
}
