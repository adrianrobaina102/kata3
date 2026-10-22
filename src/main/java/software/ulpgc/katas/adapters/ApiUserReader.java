package software.ulpgc.katas.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.katas.domain.model.User;
import software.ulpgc.katas.ports.UserReader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ApiUserReader implements UserReader {
    @Override
    public List<User> readUsers() {
        try {
            return readUsers(new URI("https://jsonplaceholder.typicode.com/users"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> readUsers(URI uri) {
        try {
            return readUsers(HttpRequest.newBuilder().GET().uri(uri).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> readUsers(HttpRequest build) {
        try(HttpClient client = HttpClient.newHttpClient()) {
            return readUsers(client.send(build, HttpResponse.BodyHandlers.ofString()));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> readUsers(HttpResponse<String> send) {
        return readUsers(new Gson().fromJson(send.body(), JsonArray.class));
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
