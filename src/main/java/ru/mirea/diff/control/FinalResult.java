package ru.mirea.diff.control;

import java.util.List;

final class FinalResult {

    static final class CountResult {

        final List<List<Integer>> clusters;
        final int count;

        CountResult(List<List<Integer>> clusters, int count) {
            this.clusters = clusters;
            this.count = count;
        }
    }

    final int tries;
    final List<CountResult> bestClusters;

    FinalResult(int tries, List<CountResult> bestClusters) {
        this.tries = tries;
        this.bestClusters = bestClusters;
    }
}
