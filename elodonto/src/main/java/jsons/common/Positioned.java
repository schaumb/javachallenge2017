package jsons.common;

public class Positioned<T extends Number> {
    private T x;
    private T y;

    public Positioned() {
    }

    public Positioned(T x, T y) {
        this.x = x;
        this.y = y;
    }

    static <X extends Number, Y extends Number, Z extends Number> double collinears(
            Positioned<X> p1, Positioned<Y> p2, Positioned<Z> p3) {
        return Math.abs(
                (p1.getY().doubleValue() - p2.getY().doubleValue()) *
                        (p1.getX().doubleValue() - p3.getX().doubleValue()) -
                        (p1.getY().doubleValue() - p3.getY().doubleValue()) *
                                (p1.getX().doubleValue() - p2.getX().doubleValue()));
    }

    public Positioned<Double> goesTo(Positioned<T> oth, double percent) {
        return new Positioned<>(this.getX().doubleValue() + (oth.getX().doubleValue() - this.getX().doubleValue()) * percent,
                this.getY().doubleValue() + (oth.getY().doubleValue() - this.getY().doubleValue()) * percent);
    }

    public T getX() {
        return x;
    }

    public Positioned<T> setX(T x) {
        this.x = x;
        return this;
    }

    public T getY() {
        return y;
    }

    public Positioned<T> setY(T y) {
        this.y = y;
        return this;
    }

    public <U extends Number> double distance(Positioned<U> other) {
        return Math.sqrt(Math.pow(getX().doubleValue() - other.getX().doubleValue(), 2) + Math.pow(getY().doubleValue() - other.getY().doubleValue(), 2));
    }
}
