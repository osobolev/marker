package ru.mirea.diff.proj;

import java.util.List;

public final class Source {

    final String name;
    public final List<String> lines;

    public Source(String name, List<String> lines) {
        this.name = name;
        this.lines = lines;
    }

    @Override
    public String toString() {
        return name;
    }
}
