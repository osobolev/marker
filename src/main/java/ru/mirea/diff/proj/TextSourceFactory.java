package ru.mirea.diff.proj;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public final class TextSourceFactory implements SourceFactory {

    private static final Pattern SPACE = Pattern.compile("\\s");

    public List<Source> parse(String fileName, InputStream is) {
        List<String> lines = new ArrayList<>();
        Scanner scanner = new Scanner(is, "UTF-8");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = SPACE.matcher(line).replaceAll("").toLowerCase();
            lines.add(line);
        }
        return Collections.singletonList(new Source(fileName, lines));
    }
}
