package ru.mirea.diff.clu;

import java.util.ArrayList;
import java.util.List;

public final class KMedoidsSequence implements AlgorithmSequence {

    private final int maxIterations;

    public KMedoidsSequence(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public List<ClusterAlgorithm> algorithms(double[][] matrix) {
        int n = matrix.length;
        List<ClusterAlgorithm> algorithms = new ArrayList<>(n);
        for (int i = n; i >= 1; i--) {
            algorithms.add(new KMedoids(i, maxIterations));
        }
        return algorithms;
    }
}
