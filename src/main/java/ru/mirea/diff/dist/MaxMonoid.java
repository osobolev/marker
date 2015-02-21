package ru.mirea.diff.dist;

final class MaxMonoid extends Monoid {

    private double max = Double.MIN_VALUE;

    public void append(double x) {
        if (x > max) {
            max = x;
        }
    }

    public double get() {
        return max;
    }
}
