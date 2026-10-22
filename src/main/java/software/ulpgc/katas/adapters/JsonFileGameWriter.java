package software.ulpgc.katas.adapters;

import com.google.gson.GsonBuilder;
import software.ulpgc.katas.domain.model.VideoGame;
import software.ulpgc.katas.ports.GameWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Random;

public class JsonFileGameWriter implements GameWriter {
    private final File file;

    public JsonFileGameWriter(File file) {
        this.file = file;
    }

    @Override
    public void write(List<VideoGame> games) throws IOException {
        ReportOutputDto reportOutputDto = new ReportOutputDto(games);
        try(Writer writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(reportOutputDto, writer);
        }
    }
    private static class MetadaDto
    {
        public String reportTitle = "Title Report";
        public int totalGamesProcessed;
        public int estimatedGlobalSales;

        public MetadaDto(int totalGamesProcessed)
        {
            this.totalGamesProcessed = totalGamesProcessed;
            this.estimatedGlobalSales = new Random().nextInt(100000);
        }
    }
    private static class ReportOutputDto
    {
        public List<VideoGame> games;
        public MetadaDto metada;
        public ReportOutputDto(List<VideoGame> games)
        {
            this.games = games;
            this.metada = new MetadaDto(games.size());
        }
    }
}
