package software.ulpgc.katas.adapters;

import com.google.gson.GsonBuilder;
import software.ulpgc.katas.ports.CharacterWriter;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import software.ulpgc.katas.domain.model.Character;
public class JsonCharacterWriter implements CharacterWriter {
    private final File file;
    public JsonCharacterWriter(File file) {
        this.file = file;
    }
    @Override
    public void writeCharacters(List<Character> characters) {
        ReportDto report = new ReportDto(characters);
        try(Writer writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(report, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static class MetadataDto
    {
        public int totalCharacters;
        public String generatedAt;
        public String apiSource;

        public MetadataDto(int totalCharacters) {
            this.totalCharacters = totalCharacters;
            this.generatedAt = LocalDate.now().toString();
            this.apiSource = "Rick and Morty API";
        }
    }
    private static class ReportDto{
        public MetadataDto metadata;
        public List<Character> characters;
        public ReportDto(List<Character> characters) {
            this.characters = characters;
            this.metadata = new MetadataDto(characters.size());
        }
    }
}
