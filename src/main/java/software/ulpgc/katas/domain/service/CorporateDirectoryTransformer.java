package software.ulpgc.katas.domain.service;

import software.ulpgc.katas.domain.model.CorporateDirectoryReport;
import software.ulpgc.katas.domain.model.DirectoryMetadata;
import software.ulpgc.katas.domain.model.User;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class CorporateDirectoryTransformer {
    public CorporateDirectoryReport transform(List<User> users) {
        List<User> validUsers  = users.stream().filter(e -> e.email() != null && !e.email().isBlank())
                .sorted(Comparator.comparingInt(User::id))
                .toList();
        DirectoryMetadata metadata = new DirectoryMetadata(
                validUsers.size(), LocalDate.now().toString(), "Corporate Directory Report");
        return new CorporateDirectoryReport(metadata, validUsers);
    }
    public CorporateDirectoryReport transform(List<User> users, LocalDate reportDate) {
        List<User> validUsers  = users.stream().filter(e -> e.email() != null && !e.email().isBlank())
                .sorted(Comparator.comparingInt(User::id))
                .toList();
        DirectoryMetadata metadata = new DirectoryMetadata(
                validUsers.size(), reportDate.toString(), "Corporate Directory Report");
        return new CorporateDirectoryReport(metadata, validUsers);
    }
}
