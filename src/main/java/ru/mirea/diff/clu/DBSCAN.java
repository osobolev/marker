package ru.mirea.diff.clu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DBSCAN implements ClusterAlgorithm {

    private final double eps;
    private final int minPoints;

    public DBSCAN(double eps, int minPoints) {
        this.eps = eps;
        this.minPoints = minPoints;
    }

    public List<List<Integer>> cluster(double[][] matrix) {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> clustered = new HashSet<>();
        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            if (visited.contains(i))
                continue;
            visited.add(i);
            List<Integer> neighbours = getNeighbours(i, matrix);
            if (neighbours.size() >= minPoints) {
                List<Integer> cluster = expandCluster(visited, clustered, i, neighbours, matrix);
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    private List<Integer> expandCluster(Set<Integer> visited, Set<Integer> clustered,
                                        int p, List<Integer> neighbours, double[][] matrix) {
        List<Integer> cluster = new ArrayList<>();
        cluster.add(p);
        clustered.add(p);
        for (int i = 0; i < neighbours.size(); i++) {
            Integer neighbour = neighbours.get(i);
            if (!visited.contains(neighbour)) {
                visited.add(neighbour);
                List<Integer> nneighbours = getNeighbours(neighbour.intValue(), matrix);
                if (nneighbours.size() >= minPoints) {
                    for (Integer nneighbour : nneighbours) {
                        if (!neighbours.contains(nneighbour)) {
                            neighbours.add(nneighbour);
                        }
                    }
                }
            }
            if (!clustered.contains(neighbour)) {
                cluster.add(neighbour);
                clustered.add(neighbour);
            }
        }
        return cluster;
    }

    private List<Integer> getNeighbours(int p, double[][] matrix) {
        List<Integer> neighbours = new ArrayList<>();
        double[] row = matrix[p];
        for (int i = 0; i < row.length; i++) {
            if (row[i] < eps) {
                neighbours.add(i);
            }
        }
        return neighbours;
    }
}
