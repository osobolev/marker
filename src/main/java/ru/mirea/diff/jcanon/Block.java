package ru.mirea.diff.jcanon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Block implements Comparable<Block> {

    static final Block EMPTY = new Block();

    private final List<String> lines;

    private Block() {
        this(Collections.emptyList());
    }

    Block(String line) {
        this(Collections.singletonList(line));
    }

    private Block(List<String> lines) {
        this.lines = lines;
    }

    Block prepend(String str) {
        if (lines.isEmpty()) {
            return new Block(str);
        } else {
            List<String> nl = new ArrayList<>(lines);
            nl.set(0, str + nl.get(0));
            return new Block(nl);
        }
    }

    Block appendOp(String op) {
        return append(" " + op + " ");
    }

    Block append(String str) {
        if (lines.isEmpty()) {
            return new Block(str);
        } else {
            List<String> nl = new ArrayList<>(lines);
            int last = nl.size() - 1;
            nl.set(last, nl.get(last) + str);
            return new Block(nl);
        }
    }

    Block append(Block that) {
        if (lines.isEmpty()) {
            return that;
        } else if (that.lines.isEmpty()) {
            return this;
        } else {
            List<String> nl = new ArrayList<>(lines.size() + that.lines.size() - 1);
            nl.addAll(lines.subList(0, lines.size() - 1));
            nl.add(lines.get(lines.size() - 1) + that.lines.get(0));
            nl.addAll(that.lines.subList(1, that.lines.size()));
            return new Block(nl);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String line : lines) {
            buf.append(line).append('\n');
        }
        return buf.toString();
    }

    Block wrap(Block before, Block after) {
        List<String> nl = new ArrayList<>(before.lines.size() + lines.size() + after.lines.size());
        nl.addAll(before.lines);
        nl.addAll(lines);
        nl.addAll(after.lines);
        return new Block(nl);
    }

    Block appendln(Block that) {
        if (lines.isEmpty()) {
            return that;
        } else if (that.lines.isEmpty()) {
            return this;
        } else {
            List<String> nl = new ArrayList<>(lines.size() + that.lines.size());
            nl.addAll(lines);
            nl.addAll(that.lines);
            return new Block(nl);
        }
    }

    Block indent(String tab) {
        if (tab.isEmpty())
            return this;
        List<String> nl = new ArrayList<>(lines.size());
        for (String line : lines) {
            nl.add(tab + line);
        }
        return new Block(nl);
    }

    public int compareTo(Block that) {
        int i = 0;
        while (true) {
            if (i < lines.size() && i < that.lines.size()) {
                String l1 = lines.get(i);
                String l2 = that.lines.get(i);
                int cmp = l1.compareTo(l2);
                if (cmp != 0)
                    return cmp;
            } else if (i < lines.size()) {
                return 1;
            } else if (i < that.lines.size()) {
                return -1;
            } else {
                return 0;
            }
            i++;
        }
    }

    public List<String> getLines() {
        return lines;
    }
}
