package software.ulpgc.katas;

import software.ulpgc.katas.adapters.ApiUserReader;
import software.ulpgc.katas.adapters.JsonCorporateDirectoryWriter;
import software.ulpgc.katas.domain.model.User;
import software.ulpgc.katas.ports.UserReader;
import software.ulpgc.katas.ports.UserWriter;

import java.io.File;
import java.util.List;

public class Main {
    static void main() {
        UserReader reader = new ApiUserReader();
        List<User> users = reader.readUsers();
        for (User user : users) {
            System.out.println(user);
        }
        UserWriter writer = new JsonCorporateDirectoryWriter(new File("corporate_directory.json"));
        writer.writeUsers(users);
        System.out.println("Process completed: JSON report created successfully.");
    }
}
