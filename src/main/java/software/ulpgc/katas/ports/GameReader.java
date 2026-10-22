package software.ulpgc.katas.ports;

import software.ulpgc.katas.domain.model.VideoGame;

import java.util.List;

public interface GameReader {
    List<VideoGame> read();
}
