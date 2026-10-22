package software.ulpgc.katas;

import software.ulpgc.katas.adapters.JsonFileGameWriter;
import software.ulpgc.katas.adapters.JsonUrlGameReader;
import software.ulpgc.katas.domain.model.VideoGame;
import software.ulpgc.katas.ports.GameReader;
import software.ulpgc.katas.ports.GameWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    static void main() throws IOException {
        GameReader reader = new JsonUrlGameReader();
        List<VideoGame> games = reader.read();
        GameWriter writer = new JsonFileGameWriter(new File("games.json"));
        writer.write(games);
    }
}
