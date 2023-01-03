package ru.mirea.diff.metrics;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import ru.mirea.diff.proj.Source;

import java.util.List;

public final class TextLinesMetric implements Metric {

    static <T> int getDelta(List<T> list1, List<T> list2) {
        Patch<T> diff = DiffUtils.diff(list1, list2);
        int sum = 0;
        for (AbstractDelta<T> delta : diff.getDeltas()) {
            sum += Math.max(delta.getSource().size(), delta.getTarget().size());
        }
        return sum;
    }

    public Measure diff(Source s1, Source s2) {
        List<String> l1 = s1.lines;
        List<String> l2 = s2.lines;
        int sum = getDelta(l1, l2);
        return new Measure(sum, Math.max(l1.size(), l2.size()));
    }
}
