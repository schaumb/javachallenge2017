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

    public T getX() {
        return x;
    }

    public T getY() {
        return y;
    }

    public <U extends Number> double distance(Positioned<U> other) {
        return Math.sqrt(Math.pow(getX().doubleValue() - other.getX().doubleValue(), 2) + Math.pow(getY().doubleValue() - other.getY().doubleValue(), 2));
    }
}
