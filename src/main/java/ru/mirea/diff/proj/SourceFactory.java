package ru.mirea.diff.proj;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface SourceFactory {

    List<Source> parse(String fileName, InputStream is) throws IOException;
}
