package ru.mirea.diff.proj;

import java.io.IOException;
import java.io.InputStream;

public interface FileConsumer {

    void accept(String name, InputStream is) throws IOException;
}
