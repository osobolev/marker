package ru.mirea.diff.clu;

import java.util.List;

public interface ClusterAlgorithm {

    List<List<Integer>> cluster(double[][] matrix);
}
