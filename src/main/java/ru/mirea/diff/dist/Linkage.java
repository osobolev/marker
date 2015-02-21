package ru.mirea.diff.dist;

public enum Linkage {
    MIN, MAX, AVG;

    public Monoid getMonoid() {
        switch (this) {
        case MIN: return new MinMonoid();
        case MAX: return new MaxMonoid();
        case AVG: return new AvgMonoid();
        }
        throw new IllegalArgumentException(toString());
    }
}
