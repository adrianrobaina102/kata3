package software.ulpgc.katas.adapters;

import com.google.gson.*;
import software.ulpgc.katas.domain.model.User;
import software.ulpgc.katas.ports.UserReader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ApiUserReader implements UserReader {
    private final URI uri;
    private final Duration timeout;

    public ApiUserReader(URI uri, Duration timeout) {
        this.uri = uri;
        this.timeout = timeout;
    }

    @Override
    public List<User> readUsers() {
        return readUsers(uri);
    }

    private List<User> readUsers(URI uri) {
        return readUsers(HttpRequest.newBuilder().GET().uri(uri).timeout(timeout).build());
    }

    private List<User> readUsers(HttpRequest build) {
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(timeout).build()) {
            HttpResponse<String> response = client.send(build, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Error HTTP: código " + response.statusCode());
            }
            return readUsers(response);
        } catch (HttpTimeoutException e) {
            throw new RuntimeException("Tiempo de espera agotado: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Petición interrumpida", e);
        }
    }

    private List<User> readUsers(HttpResponse<String> send) {
        String body = send.body();
        if (body == null || body.isBlank()) {
            throw new JsonSyntaxException("La respuesta está vacía");
        }
        JsonArray root = new Gson().fromJson(body, JsonArray.class);
        if (root == null) {
            throw new JsonSyntaxException("La respuesta no es un array JSON válido");
        }
        return readUsers(root);
    }

    private List<User> readUsers(JsonArray asJsonArray) {
        List<User> users = new ArrayList<>();
        for (JsonElement element : asJsonArray) {
            users.add(readUsers(element));
        }
        return users;
    }

    private User readUsers(JsonElement element) {
        return readUsers(new Gson().fromJson(element, UserDto.class));
    }

    private User readUsers(UserDto dto) {
        return new User(
                dto.id,
                dto.name,
                dto.username,
                dto.email);
    }
}
