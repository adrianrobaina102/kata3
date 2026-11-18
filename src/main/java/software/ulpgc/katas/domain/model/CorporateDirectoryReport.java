package software.ulpgc.katas.domain.model;

import java.util.List;

public record CorporateDirectoryReport(DirectoryMetadata metadata, List<User> users) {
}
