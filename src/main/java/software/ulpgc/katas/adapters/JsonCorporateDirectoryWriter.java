package software.ulpgc.katas.adapters;

import com.google.gson.GsonBuilder;
import software.ulpgc.katas.domain.model.User;
import software.ulpgc.katas.ports.UserWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class JsonCorporateDirectoryWriter implements UserWriter {
    private final File file;
    public JsonCorporateDirectoryWriter(File file) {
        this.file = file;
    }
    @Override
    public void writeUsers(List<User> users) {
        CorporateReportDto report = new CorporateReportDto(users);
        try(Writer writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(report, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static class DirectoryMetadataDto
    {
        public int totalUsers;
        public String reportDate;
        public String purpose;

        public DirectoryMetadataDto(int size) {
            this.totalUsers = size;
            this.reportDate = LocalDate.now().toString();
            this.purpose = "Corporate Directory Report";
        }
    }
    private static class CorporateReportDto
    {
        public List<User> users;
        public DirectoryMetadataDto metadata;
        public CorporateReportDto(List<User> users)
        {
            this.users = users;
            this.metadata = new DirectoryMetadataDto(users.size());
        }
    }
}
