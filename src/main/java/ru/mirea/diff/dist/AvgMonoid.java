package ru.mirea.diff.dist;

final class AvgMonoid extends Monoid {

    private double sum = 0;
    private int count = 0;

    public void append(double x) {
        sum += x;
        count++;
    }

    public double get() {
        return sum / count;
    }
}
