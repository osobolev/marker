package ru.mirea.diff.control;

import ru.mirea.diff.clu.AlgorithmSequence;
import ru.mirea.diff.clu.ClusterAlgorithm;
import ru.mirea.diff.dist.Linkage;
import ru.mirea.diff.dist.Monoid;
import ru.mirea.diff.metrics.Measure;
import ru.mirea.diff.metrics.Metric;
import ru.mirea.diff.proj.Project;
import ru.mirea.diff.proj.ProjectSource;
import ru.mirea.diff.proj.Source;

import java.util.*;

final class ClusteringData {

    private final double[][] matrix;

    ClusteringData(Metric metric, List<Project> projects) {
        matrix = new double[projects.size()][projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            Project pi = projects.get(i);
            for (int j = 0; j < i; j++) {
                Project pj = projects.get(j);
                double d = projectDistance(metric, pi, pj);
                matrix[i][j] = matrix[j][i] = d;
            }
        }
    }

    private static double projectDistance(Metric metric, Project p1, Project p2) {
        if (p2.sources.size() < p1.sources.size()) {
            Project tmp = p2;
            p2 = p1;
            p1 = tmp;
        }
        int totalDiff = 0;
        int totalSize = 0;
        for (Source s1 : p1.sources) {
            Source minEntry = null;
            Measure minDiff = null;
            for (Source s2 : p2.sources) {
                Measure diff = metric.diff(s1, s2);
                if (minDiff == null || diff.diff < minDiff.diff) {
                    minDiff = diff;
                    minEntry = s2;
                }
            }
            assert minEntry != null;
            totalDiff += minDiff.diff;
            totalSize += minDiff.size;
        }
        return (double) totalDiff / totalSize;
    }

    double[][] getMatrix() {
        return matrix;
    }

    static List<List<String>> getClusters(List<ProjectSource> projects, List<List<Integer>> clusters) {
        List<List<String>> result = new ArrayList<>(clusters.size());
        for (List<Integer> cluster : clusters) {
            List<String> pcluster = new ArrayList<>();
            for (Integer index : cluster) {
                pcluster.add(projects.get(index.intValue()).getName());
            }
            result.add(pcluster);
        }
        return result;
    }

    private double getIntraClusterDistance(List<Integer> cluster, Linkage linkage) {
        Monoid monoid = linkage.getMonoid();
        for (int i = 0; i < cluster.size(); i++) {
            int inode = cluster.get(i).intValue();
            for (int j = 0; j < i; j++) {
                int jnode = cluster.get(j).intValue();
                double d = matrix[inode][jnode];
                monoid.append(d);
            }
        }
        return monoid.get();
    }

    List<List<Integer>> getBestClusters(AlgorithmSequence sequence, double maxDistance, Linkage linkage) {
        List<ClusterAlgorithm> algorithms = sequence.algorithms(matrix);
        List<List<Integer>> best = null;
        for (ClusterAlgorithm algorithm : algorithms) {
            List<List<Integer>> clusters = algorithm.cluster(matrix);
            if (best == null) {
                best = clusters;
            } else {
                boolean ok = true;
                for (List<Integer> cluster : clusters) {
                    if (cluster.size() > 1) {
                        double d = getIntraClusterDistance(cluster, linkage);
                        if (d > maxDistance) {
                            ok = false;
                            break;
                        }
                    }
                }
                if (!ok)
                    break;
                best = clusters;
            }
        }
        return best;
    }

    private CopyTree same(List<ProjectSource> sources, int p, Double distanceFromSource) {
        List<Integer> result = new ArrayList<>();
        double[] row = matrix[p];
        for (int i = 0; i < row.length; i++) {
            if (row[i] < 1e-2) {
                result.add(i);
            }
        }
        Collections.sort(result, (i1, i2) -> {
            ProjectSource s1 = sources.get(i1.intValue());
            ProjectSource s2 = sources.get(i2.intValue());
            return s1.getCreated().compareTo(s2.getCreated());
        });
        return new CopyTree(result, distanceFromSource);
    }

    private static void fillChildren(Map<CopyTree, List<CopyTree>> byParent, CopyTree parent) {
        List<CopyTree> children = byParent.get(parent);
        if (children != null) {
            parent.copies.addAll(children);
            for (CopyTree child : children) {
                fillChildren(byParent, child);
            }
        }
    }

    CopyTree buildTree(int start, List<ProjectSource> sources, List<Integer> cluster) {
        Set<Integer> busy = new HashSet<>();
        CopyTree startNode = same(sources, start, null);
        busy.addAll(startNode.sources);
        Map<CopyTree, List<CopyTree>> byParent = new HashMap<>();
        List<CopyTree> allNodes = new ArrayList<>();
        allNodes.add(startNode);
        for (Integer ii : cluster) {
            int i = ii.intValue();
            Date icreated = sources.get(i).getCreated();
            if (busy.contains(ii))
                continue;
            double mind = Double.MAX_VALUE;
            CopyTree nearestNode = null;
            for (CopyTree treeNode : allNodes) {
                for (Integer jj : treeNode.sources) {
                    int j = jj.intValue();
                    Date jcreated = sources.get(j).getCreated();
                    if (!jcreated.before(icreated))
                        continue;
                    double d = matrix[i][j];
                    if (d < mind) {
                        mind = d;
                        nearestNode = treeNode;
                    }
                    break;
                }
            }
            CopyTree newNode = same(sources, i, mind);
            busy.addAll(newNode.sources);
            List<CopyTree> children = byParent.get(nearestNode);
            if (children == null) {
                children = new ArrayList<>();
                byParent.put(nearestNode, children);
            }
            children.add(newNode);
            allNodes.add(newNode);
        }
        fillChildren(byParent, startNode);
        return startNode;
    }
}
