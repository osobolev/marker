package ru.mirea.diff.proj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Project {

    public final String name;
    public final List<Source> sources;

    public Project(String name, List<Source> sources) {
        this.name = name;
        this.sources = sources;
    }

    public static Project fromSource(ProjectSource source, SourceFactory factory) throws IOException {
        List<Source> sources = new ArrayList<>();
        source.readProject((name, is) -> {
            List<Source> fileSources = factory.parse(name, is);
            sources.addAll(fileSources);
        });
        return new Project(source.getName(), sources);
    }

    @Override
    public String toString() {
        return name;
    }
}
