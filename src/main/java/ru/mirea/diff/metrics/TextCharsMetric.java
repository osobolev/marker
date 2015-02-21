package ru.mirea.diff.metrics;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import ru.mirea.diff.proj.Source;

import java.util.ArrayList;
import java.util.List;

public final class TextCharsMetric implements Metric {

    private static int size(List<String> lines) {
        int sum = 0;
        for (String line : lines) {
            sum += line.length();
        }
        return sum;
    }

    private static List<Character> getChars(String line) {
        List<Character> c = new ArrayList<>(line.length());
        for (int i = 0; i < line.length(); i++) {
            c.add(line.charAt(i));
        }
        return c;
    }

    private static int charDiff(String l1, String l2) {
        List<Character> c1 = getChars(l1);
        List<Character> c2 = getChars(l2);
        return TextLinesMetric.getDelta(c1, c2);
    }

    static Measure diff(List<String> l1, List<String> l2) {
        Patch<String> diff = DiffUtils.diff(l1, l2);
        int sum = 0;
        for (Delta<String> delta : diff.getDeltas()) {
            Chunk<String> deleted = delta.getOriginal();
            Chunk<String> inserted = delta.getRevised();
            switch (delta.getType()) {
            case DELETE:
                sum += size(deleted.getLines());
                break;
            case INSERT:
                sum += size(inserted.getLines());
                break;
            case CHANGE:
                if (deleted.size() == inserted.size()) {
                    int size = deleted.size();
                    for (int i = 0; i < size; i++) {
                        sum += charDiff(deleted.getLines().get(i), inserted.getLines().get(i));
                    }
                } else {
                    sum += Math.max(size(deleted.getLines()), size(inserted.getLines()));
                }
                break;
            }
        }
        return new Measure(sum, Math.max(size(l1), size(l2)));
    }

    public Measure diff(Source s1, Source s2) {
        return diff(s1.lines, s2.lines);
    }
}
