package ru.mirea.diff.dist;

final class MinMonoid extends Monoid {

    private double min = Double.MAX_VALUE;

    public void append(double x) {
        if (x < min) {
            min = x;
        }
    }

    public double get() {
        return min;
    }
}
