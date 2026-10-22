package software.ulpgc.katas.adapters;

import software.ulpgc.katas.ports.ImageDownloader;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class NativeImageDownloader implements ImageDownloader {
    @Override
    public void downloadImage(String url, String destinationFile) {
        try {
            try(InputStream in = new URI(url).toURL().openStream())
            {
                Files.copy(in, Paths.get(destinationFile), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
