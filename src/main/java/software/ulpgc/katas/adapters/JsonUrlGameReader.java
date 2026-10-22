package software.ulpgc.katas.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.katas.domain.model.Genre;
import software.ulpgc.katas.domain.model.Platform;
import software.ulpgc.katas.domain.model.VideoGame;
import software.ulpgc.katas.ports.GameReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class JsonUrlGameReader implements GameReader {
    @Override
    public List<VideoGame> read() {
        try {
            return read(new URI("https://raw.githubusercontent.com/adrianrobaina102/kata3/refs/heads/develop/videogames.csv"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<VideoGame> read(URI uri) {
        try {
            return read(HttpRequest.newBuilder().GET().uri(uri).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<VideoGame> read(HttpRequest build) {
        try(HttpClient client = HttpClient.newHttpClient()) {
            return read(client.send(build, HttpResponse.BodyHandlers.ofString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<VideoGame> read(HttpResponse<String> send) {
        return read(new Gson().fromJson(send.body(), JsonObject.class).get("games").getAsJsonArray());
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
