package software.ulpgc.katas.ports;

import software.ulpgc.katas.domain.model.Character;

import java.util.List;

public interface CharacterReader {
    List<Character> readCharacters();
}
