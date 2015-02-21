package ru.mirea.diff.proj;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.zip.ZipFile;

public final class ZipProjectSource implements ProjectSource {

    private final Path path;
    private final Date created;

    public ZipProjectSource(Path path) {
        this.path = path;
        try {
            FileTime lastModified = Files.getLastModifiedTime(path);
            this.created = new Date(lastModified.toMillis());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public String getName() {
        return path.getFileName().toString();
    }

    public Date getCreated() {
        return created;
    }

    public void readProject(FileConsumer consumer) throws IOException {
        ZipFile zip = new ZipFile(path.toFile());
        zip.stream().filter(e -> !e.isDirectory() && e.getName().toLowerCase().endsWith(".java")).forEach(e -> {
            try {
                try (InputStream is = new BufferedInputStream(zip.getInputStream(e))) {
                    consumer.accept(e.getName(), is);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    public String toString() {
        return getName();
    }
}
