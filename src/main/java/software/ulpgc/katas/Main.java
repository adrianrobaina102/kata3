package software.ulpgc.katas;

import software.ulpgc.katas.adapters.ApiCharacterReader;
import software.ulpgc.katas.adapters.JsonCharacterWriter;
import software.ulpgc.katas.adapters.NativeImageDownloader;
import software.ulpgc.katas.domain.model.Character;
import software.ulpgc.katas.ports.CharacterReader;
import software.ulpgc.katas.ports.CharacterWriter;
import software.ulpgc.katas.ports.ImageDownloader;

import java.io.File;
import java.util.List;

public class Main {
    static void main() {
        CharacterReader reader = new ApiCharacterReader();
        List<Character> characters = reader.readCharacters();
        ImageDownloader downloader = new NativeImageDownloader();
        new File("photos").mkdirs();
        for (Character character : characters) {
            System.out.println(character);
            downloader.downloadImage(character.imageUrl(), "photos/" + character.name().replace(" ", "_") + ".jpg");
        }
        CharacterWriter writer = new JsonCharacterWriter(new File("characters_report.json"));
        writer.writeCharacters(characters);

    }
}
