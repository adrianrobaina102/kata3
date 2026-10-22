package software.ulpgc.katas.ports;

public interface ImageDownloader {
    void downloadImage(String url, String destinationFile);
}
