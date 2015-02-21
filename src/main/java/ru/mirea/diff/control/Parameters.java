package ru.mirea.diff.control;

import com.sun.tools.javac.parser.ParserFactory;
import ru.mirea.diff.clu.AlgorithmSequence;
import ru.mirea.diff.clu.DBSCANSequence;
import ru.mirea.diff.clu.HierarchicalSequence;
import ru.mirea.diff.dist.Linkage;
import ru.mirea.diff.jcanon.Canonicalizer;
import ru.mirea.diff.metrics.Metric;
import ru.mirea.diff.metrics.TextCharsMetric;
import ru.mirea.diff.metrics.TextLinesMetric;
import ru.mirea.diff.proj.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Parameters {

    public final SourceFactory sourceFactory;
    public final Metric metric;
    public final AlgorithmSequence sequence;
    public final double maxDistance;
    public final Linkage intraClusterLinkage;

    public Parameters(SourceFactory sourceFactory, Metric metric, AlgorithmSequence sequence, double maxDistance, Linkage intraClusterLinkage) {
        this.sourceFactory = sourceFactory;
        this.metric = metric;
        this.sequence = sequence;
        this.maxDistance = maxDistance;
        this.intraClusterLinkage = intraClusterLinkage;
    }

    public static Parameters standardClustering(SourceFactory sourceFactory) {
        Metric metric = new TextCharsMetric();
        AlgorithmSequence sequence = new HierarchicalSequence(Linkage.AVG);
        double maxDistance = 0.5;
        Linkage intraClusterLinkage = Linkage.MAX;

        return new Parameters(
            sourceFactory, metric, sequence, maxDistance, intraClusterLinkage
        );
    }

    public static Parameters textCompare() {
        SourceFactory sourceFactory = new TextSourceFactory();
        return standardClustering(sourceFactory);
    }

    public static Parameters javaCompare(Canonicalizer canonicalizer) {
        ParserFactory parserFactory = Canonicalizer.createFactory();
        SourceFactory sourceFactory = new JavaSourceFactory(parserFactory, canonicalizer);
        return standardClustering(sourceFactory);
    }

    public static Parameters javaCompare0() {
        Canonicalizer canonicalizer = new Canonicalizer(false, true, Canonicalizer.Reorder.WEAK, "");
        return javaCompare(canonicalizer);
    }

    public static Parameters javaCompare1() {
        Canonicalizer canonicalizer = new Canonicalizer(true, true, Canonicalizer.Reorder.WEAK, "");
        return javaCompare(canonicalizer);
    }

    public static Parameters javaCompare2() {
        Canonicalizer canonicalizer = new Canonicalizer(true, true, Canonicalizer.Reorder.STRONG, "");
        return javaCompare(canonicalizer);
    }

    public static Parameters random(ParserFactory parserFactory, Random rnd) {
        SourceFactory sourceFactory;
        if (rnd.nextBoolean()) {
            sourceFactory = new TextSourceFactory();
        } else {
            Canonicalizer.Reorder[] values = Canonicalizer.Reorder.values();
            Canonicalizer.Reorder reorder = values[rnd.nextInt(values.length)];
            String tab = rnd.nextBoolean() ? "" : "    ";
            Canonicalizer canonicalizer = new Canonicalizer(rnd.nextBoolean(), rnd.nextBoolean(), reorder, tab);
            sourceFactory = new JavaSourceFactory(parserFactory, canonicalizer);
        }
        Metric metric = rnd.nextBoolean() ? new TextCharsMetric() : new TextLinesMetric();
        AlgorithmSequence sequence;
        if (rnd.nextBoolean()) {
            Linkage[] values = Linkage.values();
            Linkage linkage = values[rnd.nextInt(values.length)];
            sequence = new HierarchicalSequence(linkage);
        } else {
            sequence = new DBSCANSequence(0.2, 0.8, 1);
        }
        // todo: do not randomize maxDistance???
        // todo: depends on metric, sourceFactory (and canonicalizer)???
        double maxDistance = 0.4 + rnd.nextDouble() * 0.2;
        Linkage intraClusterLinkage = rnd.nextBoolean() ? Linkage.MAX : Linkage.AVG;
        return new Parameters(sourceFactory, metric, sequence, maxDistance, intraClusterLinkage);
    }

    private ClusteringData getClusteringData(List<ProjectSource> sources) throws IOException {
        List<Project> projects = new ArrayList<>(sources.size());
        for (ProjectSource source : sources) {
            Project project = Project.fromSource(source, sourceFactory);
            projects.add(project);
        }
        return new ClusteringData(metric, projects);
    }

    private List<List<Integer>> getNormalizedBestClusters(ClusteringData clustering) {
        List<List<Integer>> bestClusters = clustering.getBestClusters(sequence, maxDistance, intraClusterLinkage);
        for (List<Integer> cluster : bestClusters) {
            Collections.sort(cluster);
        }
        Collections.sort(bestClusters, (o1, o2) -> {
            int len1 = o1.size();
            int len2 = o2.size();
            int lim = Math.min(len1, len2);
            for (int i = 0; i < lim; i++) {
                Integer i1 = o1.get(i);
                Integer i2 = o2.get(i);
                int cmp = i1.compareTo(i2);
                if (cmp != 0)
                    return cmp;
            }
            return len1 - len2;
        });
        return bestClusters;
    }

    List<List<Integer>> getBestClusters(List<ProjectSource> sources) throws IOException {
        ClusteringData clustering = getClusteringData(sources);
        return getNormalizedBestClusters(clustering);
    }

    List<CopyTree> getCopyTree(List<ProjectSource> sources, List<List<Integer>> bestClusters) throws IOException {
        ClusteringData clustering = getClusteringData(sources);
        List<CopyTree> trees = new ArrayList<>(bestClusters.size());
        for (List<Integer> cluster : bestClusters) {
            ProjectSource original = null;
            int start = 0;
            for (Integer node : cluster) {
                int i = node.intValue();
                ProjectSource source = sources.get(i);
                if (original == null || source.getCreated().before(original.getCreated())) {
                    original = source;
                    start = i;
                }
            }
            CopyTree tree = clustering.buildTree(start, sources, cluster);
            trees.add(tree);
        }
        return trees;
    }
}
