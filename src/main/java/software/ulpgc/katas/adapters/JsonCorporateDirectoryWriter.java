package software.ulpgc.katas.adapters;

import com.google.gson.GsonBuilder;
import software.ulpgc.katas.domain.model.CorporateDirectoryReport;
import software.ulpgc.katas.ports.UserWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class JsonCorporateDirectoryWriter implements UserWriter {
    private final File file;

    public JsonCorporateDirectoryWriter(File file) {
        this.file = Objects.requireNonNull(file, "file must not be null");
    }

    @Override
    public void writeReport(CorporateDirectoryReport report) {
        Objects.requireNonNull(report, "report must not be null");
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(report, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
