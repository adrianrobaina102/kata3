package software.ulpgc.katas;

import software.ulpgc.katas.adapters.JsonFileGameWriter;
import software.ulpgc.katas.adapters.JsonUrlGameReader;
import software.ulpgc.katas.domain.model.VideoGame;
import software.ulpgc.katas.ports.GameReader;
import software.ulpgc.katas.ports.GameWriter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        URI serviceUri = URI.create("https://raw.githubusercontent.com/adrianrobaina102/kata3/refs/heads/develop/videogames.csv");
        GameReader reader = new JsonUrlGameReader(serviceUri, Duration.ofSeconds(30));
        List<VideoGame> games = reader.read();
        GameWriter writer = new JsonFileGameWriter(new File("games.json"));
        writer.write(games);
    }
}
