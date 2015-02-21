package ru.mirea.diff.metrics;

import ru.mirea.diff.proj.Source;

public interface Metric {

    Measure diff(Source s1, Source s2);
}
