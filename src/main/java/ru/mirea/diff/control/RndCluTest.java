package ru.mirea.diff.control;

import com.sun.tools.javac.parser.ParserFactory;
import ru.mirea.diff.jcanon.Canonicalizer;
import ru.mirea.diff.proj.ProjectSource;
import ru.mirea.diff.proj.ZipProjectSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

final class RndCluTest {

    private static <T> List<List<T>> fromIndexes(List<T> list, List<List<Integer>> clusters) {
        List<List<T>> result = new ArrayList<>(clusters.size());
        for (List<Integer> cluster : clusters) {
            List<T> sub = new ArrayList<>(cluster.size());
            result.add(sub);
            for (Integer i : cluster) {
                sub.add(list.get(i.intValue()));
            }
        }
        return result;
    }

    private static FinalResult getFinalResult(List<ProjectSource> projectSources, Parameters[] parameters) throws InterruptedException, ExecutionException {
        Stats stats = new Stats();
        int nThreads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        try {
            List<Future<?>> futures = new ArrayList<>(parameters.length);
            for (Parameters parameter : parameters) {
                Future<?> future = executor.submit(() -> {
                    List<List<Integer>> clusters = parameter.getBestClusters(projectSources);
                    stats.add(clusters);
                    return null;
                });
                futures.add(future);
            }
            for (int i = 0; i < futures.size(); i++) {
                System.out.print((i + 1) + " of " + parameters.length + "\r");
                futures.get(i).get();
            }
            System.out.println();
        } finally {
            executor.shutdown();
        }
        return stats.getResult();
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        List<ProjectSource> projectSources = Files.list(Paths.get("zips/abook"))
            .filter(path -> path.toString().endsWith(".zip"))
            .map(ZipProjectSource::new)
            .collect(Collectors.toList());

        Random rnd = new Random();
        Parameters[] parameters = new Parameters[20];
        for (int i = 0; i < parameters.length; i++) {
            ParserFactory parserFactory = Canonicalizer.createFactory();
            parameters[i] = Parameters.random(parserFactory, rnd);
        }

        FinalResult result = getFinalResult(projectSources, parameters);
        // todo: считать perfect, если разрыв 1 и 2 очень большой
        // todo: выкидывать маргинальные случаи
        for (FinalResult.CountResult bestCluster : result.bestClusters) {
            // todo: искать пересечения кластеров, отделить точно определенные от неточно определенных
            // todo: складывать все кластеры из bestClusters с весом count; если суммарный вес кластера == tries, то он верен 100%
            System.out.println(bestCluster.count + " of " + result.tries);
            System.out.println(fromIndexes(projectSources, bestCluster.clusters));
            Parameters treeParameters = Parameters.javaCompare2();
            List<CopyTree> trees = treeParameters.getCopyTree(projectSources, bestCluster.clusters);
            for (CopyTree tree : trees) {
                System.out.println(tree.toString(projectSources));
            }
            System.out.println();
        }
    }
}
