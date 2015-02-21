package ru.mirea.diff.clu;

import java.util.List;

public interface AlgorithmSequence {

    /**
     * @return список от большого кол-ва кластеров к меньшему (от большей специфичности к меньшей)
     */
    List<ClusterAlgorithm> algorithms(double[][] matrix);
}
