package ru.mirea;

import ru.mirea.diff.clu.ClusterAlgorithm;
import ru.mirea.diff.clu.Hierarchical;
import ru.mirea.diff.dist.Linkage;

import java.util.List;

final class CluAlgTest {

    public static void main(String[] args) {
        int[][] data = {
            {2, 6},
            {3, 4},
            {3, 8},
            {4, 7},
            {6, 2},
            {6, 4},
            {7, 3},
            {7, 4},
            {8, 5},
            {7, 6}
        };
        double[][] matrix = new double[10][10];
        for (int i = 0; i < 10; i++) {
            int[] p1 = data[i];
            int x1 = p1[0];
            int y1 = p1[1];
            for (int j = 0; j < 10; j++) {
                int[] p2 = data[j];
                int x2 = p2[0];
                int y2 = p2[1];
                matrix[i][j] = Math.abs(x2 - x1) + Math.abs(y2 - y1);
            }
        }
        //ClusterAlgorithm algorithm = new KMedoids(2, 1000);
        ClusterAlgorithm algorithm = new Hierarchical(2, Linkage.MAX);
        List<List<Integer>> clusters = algorithm.cluster(matrix);
        for (List<Integer> cluster : clusters) {
            for (Integer integer : cluster) {
                System.out.print((integer.intValue() + 1) + " ");
            }
            System.out.println();
        }
    }
}
