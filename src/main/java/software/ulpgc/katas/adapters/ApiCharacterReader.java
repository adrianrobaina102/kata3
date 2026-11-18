package software.ulpgc.katas.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import software.ulpgc.katas.domain.model.Character;
import software.ulpgc.katas.ports.CharacterReader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ApiCharacterReader implements CharacterReader {
    private final URI uri;
    private final Duration timeout;

    public ApiCharacterReader(URI uri, Duration timeout) {
        this.uri = uri;
        this.timeout = timeout;
    }

    @Override
    public List<Character> readCharacters() {
        return readCharacters(uri);
    }

    private List<Character> readCharacters(URI uri) {
        return readCharacters(HttpRequest.newBuilder().GET().uri(uri).timeout(timeout).build());
    }

    private List<Character> readCharacters(HttpRequest build) {
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(timeout).build()) {
            HttpResponse<String> response = client.send(build, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Error HTTP: código " + response.statusCode());
            }
            return readCharacters(response);
        } catch (HttpTimeoutException e) {
            throw new RuntimeException("Tiempo de espera agotado: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Petición interrumpida", e);
        }
    }

    private List<Character> readCharacters(HttpResponse<String> send) {
        ApiResponseDto apiResponseDto = new Gson().fromJson(send.body(), ApiResponseDto.class);
        if (apiResponseDto == null || apiResponseDto.results == null) {
            throw new JsonSyntaxException("La respuesta JSON no contiene un campo 'results' válido");
        }
        return readCharacters(apiResponseDto);
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
