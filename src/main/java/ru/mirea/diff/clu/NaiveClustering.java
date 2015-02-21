package ru.mirea.diff.clu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NaiveClustering implements ClusterAlgorithm {

    private final double d;

    public NaiveClustering(double d) {
        this.d = d;
    }

    public List<List<Integer>> cluster(double[][] matrix) {
        List<Integer> allItems = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            allItems.add(i);
        }
        List<List<Integer>> clusters = new ArrayList<>();
        while (!allItems.isEmpty()) {
            Iterator<Integer> it = allItems.iterator();
            Integer first = it.next();
            int i = first.intValue();
            List<Integer> cluster = new ArrayList<>();
            clusters.add(cluster);
            cluster.add(first);
            it.remove();
            while (it.hasNext()) {
                Integer pj = it.next();
                int j = pj.intValue();
                if (matrix[i][j] < d) {
                    cluster.add(pj);
                    it.remove();
                }
            }
        }
        return clusters;
    }
}
