package ru.mirea.diff.clu;

import java.util.ArrayList;
import java.util.List;

public final class DBSCANSequence implements AlgorithmSequence {

    private final double dfrom;
    private final double dto;
    private final int minPoints;

    public DBSCANSequence(double dfrom, double dto, int minPoints) {
        this.dfrom = dfrom;
        this.dto = dto;
        this.minPoints = minPoints;
    }

    public List<ClusterAlgorithm> algorithms(double[][] matrix) {
        int n = matrix.length;
        double d = (dto - dfrom) / n;
        double x = dfrom;
        List<ClusterAlgorithm> algorithms = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            algorithms.add(new DBSCAN(x, minPoints));
            x += d;
        }
        return algorithms;
    }
}
