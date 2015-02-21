package ru.mirea.diff.control;

import ru.mirea.diff.proj.ProjectSource;

import java.util.ArrayList;
import java.util.List;

final class CopyTree {

    final List<Integer> sources;
    final Double distanceFromSource;
    final List<CopyTree> copies = new ArrayList<>();

    CopyTree(List<Integer> sources, Double distanceFromSource) {
        this.sources = sources;
        this.distanceFromSource = distanceFromSource;
    }

    private void append(StringBuilder buf, List<ProjectSource> projectSources, String tab) {
        buf.append(tab);
        for (int i = 0; i < sources.size(); i++) {
            if (i > 1) {
                buf.append(", ");
            } else if (i == 1) {
                buf.append(" -> ");
            }
            int source = sources.get(i).intValue();
            String name = projectSources.get(source).getName();
            buf.append(name);
        }
        if (distanceFromSource != null) {
            buf.append(": " + distanceFromSource);
        }
        buf.append('\n');
        String newTab = "    " + tab;
        for (CopyTree copy : copies) {
            copy.append(buf, projectSources, newTab);
        }
    }

    String toString(List<ProjectSource> projectSources) {
        StringBuilder buf = new StringBuilder();
        append(buf, projectSources, "");
        return buf.toString();
    }
}
