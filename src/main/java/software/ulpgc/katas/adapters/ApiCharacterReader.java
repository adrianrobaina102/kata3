package software.ulpgc.katas.adapters;

import com.google.gson.Gson;
import software.ulpgc.katas.domain.model.Character;
import software.ulpgc.katas.ports.CharacterReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ApiCharacterReader implements CharacterReader {
    @Override
    public List<Character> readCharacters() {
        try {
            return readCharacters(new URI("https://rickandmortyapi.com/api/character"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Character> readCharacters(URI uri) {
        try {
            return readCharacters(HttpRequest.newBuilder().GET().uri(uri).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Character> readCharacters(HttpRequest build) throws IOException {
        try(HttpClient  client = HttpClient.newHttpClient()) {
            return readCharacters(client.send(build, HttpResponse.BodyHandlers.ofString()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Character> readCharacters(HttpResponse<String> send) {
        return readCharacters(new Gson().fromJson(send.body(), ApiResponseDto.class));
    }

    private List<Character> readCharacters(ApiResponseDto apiResponseDto) {
        List<Character> characters = new ArrayList<>();
        for (CharacterDto characterDto : apiResponseDto.results) {
            characters.add(readCharacters(characterDto));
        }
        return characters;
    }

    private Character readCharacters(CharacterDto dto) {
        return new Character(
                dto.id,
                dto.name,
                dto.species,
                dto.image
        );
    }
}
