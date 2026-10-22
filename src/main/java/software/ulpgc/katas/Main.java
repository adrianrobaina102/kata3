package software.ulpgc.katas;

import software.ulpgc.katas.adapters.ApiCharacterReader;
import software.ulpgc.katas.adapters.CharacterDto;
import software.ulpgc.katas.domain.model.Character;
import software.ulpgc.katas.ports.CharacterReader;

import java.util.List;

public class Main {
    static void main() {
        CharacterReader reader = new ApiCharacterReader();
        List<Character> characters = reader.readCharacters();
        for (Character character : characters) {
            System.out.println(character);
        }
    }
}
