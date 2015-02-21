package ru.mirea.diff.proj;

import java.io.IOException;
import java.util.Date;

public interface ProjectSource {

    String getName();

    Date getCreated();

    void readProject(FileConsumer consumer) throws IOException;
}
