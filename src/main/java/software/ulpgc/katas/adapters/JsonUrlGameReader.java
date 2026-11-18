package software.ulpgc.katas.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import software.ulpgc.katas.domain.model.Genre;
import software.ulpgc.katas.domain.model.Platform;
import software.ulpgc.katas.domain.model.VideoGame;
import software.ulpgc.katas.ports.GameReader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JsonUrlGameReader implements GameReader {
    private final URI uri;
    private final Duration timeout;

    public JsonUrlGameReader(URI uri, Duration timeout) {
        this.uri = uri;
        this.timeout = timeout;
    }

    @Override
    public List<VideoGame> read() {
        return read(uri);
    }

    private List<VideoGame> read(URI uri) {
        return read(HttpRequest.newBuilder().GET().uri(uri).timeout(timeout).build());
    }

    private List<VideoGame> read(HttpRequest build) {
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(timeout).build()) {
            HttpResponse<String> response = client.send(build, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Error HTTP: código " + response.statusCode());
            }
            return read(response);
        } catch (HttpTimeoutException e) {
            throw new RuntimeException("Tiempo de espera agotado: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Petición interrumpida", e);
        }
    }

    private List<VideoGame> read(HttpResponse<String> send) {
        JsonObject root = new Gson().fromJson(send.body(), JsonObject.class);
        if (root == null || !root.has("games") || !root.get("games").isJsonArray()) {
            throw new JsonSyntaxException("La respuesta no contiene un array 'games' válido");
        }
        return read(root.getAsJsonArray("games"));
    }

    private List<VideoGame> read(JsonArray games) {
        List<VideoGame> videogames = new ArrayList<>();
        for (JsonElement game : games) {
            videogames.add(read(game));
        }
        return videogames;
    }

    private VideoGame read(JsonElement game) {
        return read(new Gson().fromJson(game, GameDto.class));
    }

    private VideoGame read(GameDto dto) {
        return new VideoGame(
                dto.title,
                Genre.valueOf(dto.genre),
                dto.releaseYear,
                Platform.valueOf(dto.platform)
        );
    }
}
