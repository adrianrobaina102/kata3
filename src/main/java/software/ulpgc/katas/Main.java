package software.ulpgc.katas;

import software.ulpgc.katas.adapters.ApiUserReader;
import software.ulpgc.katas.adapters.JsonCorporateDirectoryWriter;
import software.ulpgc.katas.domain.model.CorporateDirectoryReport;
import software.ulpgc.katas.domain.model.User;
import software.ulpgc.katas.domain.service.CorporateDirectoryTransformer;
import software.ulpgc.katas.ports.UserReader;
import software.ulpgc.katas.ports.UserWriter;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class Main {
    private static final String DEFAULT_URL = "https://jsonplaceholder.typicode.com/users";
    private static final String DEFAULT_OUTPUT = "corporate_directory.json";

    public static void main(String[] args) {
        URI serviceUri = URI.create(args.length > 0 ? args[0] : DEFAULT_URL);
        File outputFile = new File(args.length > 1 ? args[1] : DEFAULT_OUTPUT);

        UserReader reader = new ApiUserReader(serviceUri, Duration.ofSeconds(30));
        CorporateDirectoryTransformer transformer = new CorporateDirectoryTransformer();
        UserWriter writer = new JsonCorporateDirectoryWriter(outputFile);

        List<User> users = reader.readUsers();
        for (User user : users) {
            System.out.println(user);
        }
        CorporateDirectoryReport report = transformer.transform(users);
        writer.writeReport(report);
        System.out.println("Process completed: JSON report created successfully.");
    }
}
