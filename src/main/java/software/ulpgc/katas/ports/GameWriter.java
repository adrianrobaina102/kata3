package software.ulpgc.katas.ports;

import software.ulpgc.katas.domain.model.VideoGame;

import java.io.IOException;
import java.util.List;

public interface GameWriter {
    void write(List<VideoGame> games) throws IOException;
}
