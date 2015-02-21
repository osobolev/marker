package ru.mirea.diff.clu;

import java.util.ArrayList;
import java.util.List;

public final class NaiveSequence implements AlgorithmSequence {

    private final double dfrom;
    private final double dto;

    public NaiveSequence(double dfrom, double dto) {
        this.dfrom = dfrom;
        this.dto = dto;
    }

    public List<ClusterAlgorithm> algorithms(double[][] matrix) {
        int n = matrix.length;
        double d = (dto - dfrom) / n;
        double x = dfrom;
        List<ClusterAlgorithm> algorithms = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            algorithms.add(new NaiveClustering(x));
            x += d;
        }
        return algorithms;
    }
}
