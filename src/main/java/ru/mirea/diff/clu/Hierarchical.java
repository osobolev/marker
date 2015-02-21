package ru.mirea.diff.clu;

import ru.mirea.diff.dist.Linkage;
import ru.mirea.diff.dist.Monoid;

import java.util.ArrayList;
import java.util.List;

public final class Hierarchical implements ClusterAlgorithm {

    private final int k;
    private final Linkage linkage;

    public Hierarchical(int k, Linkage linkage) {
        this.k = k;
        this.linkage = linkage;
    }

    private double distance(double[][] matrix, List<Integer> c1, List<Integer> c2) {
        Monoid monoid = linkage.getMonoid();
        for (Integer pi : c1) {
            int i = pi.intValue();
            for (Integer pj : c2) {
                monoid.append(matrix[i][pj.intValue()]);
            }
        }
        return monoid.get();
    }

    public List<List<Integer>> cluster(double[][] matrix) {
        return cluster(matrix, null);
    }

    public List<List<Integer>> cluster(double[][] matrix, List<List<List<Integer>>> history) {
        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            List<Integer> cluster = new ArrayList<>();
            cluster.add(i);
            clusters.add(cluster);
        }
        while (true) {
            if (history != null) {
                List<List<Integer>> copy = new ArrayList<>(clusters.size());
                for (List<Integer> cluster : clusters) {
                    copy.add(new ArrayList<>(cluster));
                }
                history.add(copy);
            }
            if (clusters.size() <= k)
                break;
            double minDistance = Double.MAX_VALUE;
            int nearestLarge = -1;
            int nearestSmall = -1;
            for (int i = 0; i < clusters.size(); i++) {
                List<Integer> c1 = clusters.get(i);
                for (int j = 0; j < i; j++) {
                    List<Integer> c2 = clusters.get(j);
                    double d = distance(matrix, c1, c2);
                    if (d < minDistance) {
                        minDistance = d;
                        nearestLarge = i;
                        nearestSmall = j;
                    }
                }
            }
            List<Integer> removed = clusters.remove(nearestLarge);
            clusters.get(nearestSmall).addAll(removed);
        }
        return clusters;
    }
}
