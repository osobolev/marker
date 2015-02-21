package ru.mirea.diff.clu;

import java.util.*;

public final class KMedoids implements ClusterAlgorithm {

    private final int k;
    private final int maxIterations;

    public KMedoids(int k, int maxIterations) {
        this.k = k;
        this.maxIterations = maxIterations;
    }

    public List<List<Integer>> cluster(double[][] matrix) {
        Random rnd = new Random();
        int[] medoids = new int[k];
        Set<Integer> used = new HashSet<>();
        for (int icluster = 0; icluster < k; icluster++) {
            int idata;
            while (true) {
                idata = rnd.nextInt(matrix.length);
                if (!used.contains(idata))
                    break;
            }
            medoids[icluster] = idata;
            used.add(idata);
        }

        int count = 0;
        while (true) {
            count++;
            int[] assignment = assign(matrix, medoids);
            boolean changed = recalculateMedoids(matrix, assignment, medoids);
            if (!changed || count >= maxIterations) {
                List<List<Integer>> output = new ArrayList<>();
                for (int icluster = 0; icluster < k; icluster++) {
                    output.add(new ArrayList<>());
                }
                for (int idata = 0; idata < matrix.length; idata++) {
                    int icluster = assignment[idata];
                    output.get(icluster).add(idata);
                }
                return output;
            }
        }
    }

    private int[] assign(double[][] matrix, int[] medoids) {
        int[] out = new int[matrix.length];
        for (int idata = 0; idata < matrix.length; idata++) {
            double bestDistance = matrix[idata][medoids[0]];
            int bestCluster = 0;
            for (int icluster = 1; icluster < k; icluster++) {
                double distance = matrix[idata][medoids[icluster]];
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestCluster = icluster;
                }
            }
            out[idata] = bestCluster;
        }
        return out;
    }

    private boolean recalculateMedoids(double[][] matrix, int[] assignment, int[] medoids) {
        boolean changed = false;
        for (int icluster = 0; icluster < k; icluster++) {
            int medoid = medoids[icluster];
            for (int idata = 0; idata < matrix.length; idata++) {
                if (idata != medoid && assignment[idata] == icluster) {
                    double costDelta = 0;
                    for (int j = 0; j < matrix.length; j++) {
                        if (assignment[j] == icluster) {
                            double oldDistance = matrix[medoid][j];
                            double newDistance = matrix[idata][j];
                            costDelta += newDistance - oldDistance;
                        }
                    }
                    if (costDelta < 0) {
                        medoids[icluster] = idata;
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
}
