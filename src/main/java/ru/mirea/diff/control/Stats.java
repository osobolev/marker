package ru.mirea.diff.control;

import java.util.*;

final class Stats {

    private final Map<List<List<Integer>>, Integer> stats = new HashMap<>();
    private int totalCount = 0;

    synchronized void add(List<List<Integer>> clusters) {
        Integer count = stats.get(clusters);
        if (count == null) {
            count = 1;
        } else {
            count = count.intValue() + 1;
        }
        stats.put(clusters, count);
        totalCount++;
    }

    synchronized FinalResult getResult() {
        List<Map.Entry<List<List<Integer>>, Integer>> entries = new ArrayList<>(stats.entrySet());
        Collections.sort(entries, (o1, o2) -> -o1.getValue().compareTo(o2.getValue()));
        List<FinalResult.CountResult> bestClusters = new ArrayList<>();

        for (Map.Entry<List<List<Integer>>, Integer> entry : entries) {
            bestClusters.add(new FinalResult.CountResult(
                entry.getKey(), entry.getValue().intValue()
            ));
        }
        return new FinalResult(totalCount, bestClusters);
    }
}
