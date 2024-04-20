package dev.necr.velocitycore.dependency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Skidded from <a href="https://github.com/Alviannn/DependencyHelper">github.com/Alviannn/DependencyHelper</a>
 */
@Getter
@RequiredArgsConstructor
public class DependencyManager {

    private URLClassLoader urlClassLoader;

    public DependencyManager(Class<?> clazz) {
        this.urlClassLoader = (URLClassLoader) clazz.getClassLoader();
    }

    /**
     * downloads a dependency file
     *
     * @param fileName the file name
     * @param fileUrl  the file url
     * @param dirPath  the directory path
     * @throws IOException if the download fails
     */
    public void download(String fileName, String fileUrl, Path dirPath) throws IOException {
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path filePath = dirPath.resolve(fileName);
        if (!Files.exists(filePath)) {
            try (InputStream in = new URL(fileUrl).openStream()) {
                Files.copy(in, filePath);
            } catch (Exception e) {
                throw new IOException("Failed to download " + fileName);
            }
        }
    }

    /**
     * downloads the dependencies
     *
     * @param dependencies the dependencies
     * @param dirPath      the directory path
     * @throws IOException if the download fails
     */
    public void download(Map<String, String> dependencies, Path dirPath) throws IOException {
        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            download(entry.getKey(), entry.getValue(), dirPath);
        }
    }

    /**
     * loads the dependencies
     *
     * @param dirPath the directory path
     * @throws IOException if the load fails
     */
    public void loadDir(Path dirPath) throws IOException {
        if (!Files.isDirectory(dirPath)) {
            return;
        }

        List<URL> urls = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            for (Path path : directoryStream) {
                if (!path.toString().endsWith(".jar")) {
                    continue;
                }
                urls.add(path.toUri().toURL());
            }
        }

        urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), urlClassLoader);
    }

}