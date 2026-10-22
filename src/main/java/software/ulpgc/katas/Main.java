package software.ulpgc.katas;

import software.ulpgc.katas.adapters.JsonUrlGameReader;
import software.ulpgc.katas.ports.GameReader;

public class Main {
    static void main() {
        GameReader reader = new JsonUrlGameReader();
        System.out.println(reader.read());
    }
}
