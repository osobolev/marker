package ru.mirea.diff.clu;

import ru.mirea.diff.dist.Linkage;

import java.util.ArrayList;
import java.util.List;

public final class HierarchicalSequence implements AlgorithmSequence {

    private static final class FakeAlgorithm implements ClusterAlgorithm {

        private final List<List<Integer>> clusters;

        private FakeAlgorithm(List<List<Integer>> clusters) {
            this.clusters = clusters;
        }

        public List<List<Integer>> cluster(double[][] matrix) {
            return clusters;
        }
    }

    private final Linkage linkage;

    public HierarchicalSequence(Linkage linkage) {
        this.linkage = linkage;
    }

    public List<ClusterAlgorithm> algorithms(double[][] matrix) {
        Hierarchical hierarchical = new Hierarchical(1, linkage);
        ArrayList<List<List<Integer>>> history = new ArrayList<>();
        hierarchical.cluster(matrix, history);
        List<ClusterAlgorithm> algorithms = new ArrayList<>(history.size());
        for (List<List<Integer>> clusters : history) {
            algorithms.add(new FakeAlgorithm(clusters));
        }
        return algorithms;
    }
}
