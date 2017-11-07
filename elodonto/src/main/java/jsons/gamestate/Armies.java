package jsons.gamestate;

public class Armies {
    private String owner;
    private int size;
    private double x;
    private double y;

    public String getOwner() {
        return owner;
    }

    public int getSize() {
        return size;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Armies armies = (Armies) o;

        if (size != armies.size) return false;
        if (Double.compare(armies.x, x) != 0) return false;
        if (Double.compare(armies.y, y) != 0) return false;
        return owner != null ? owner.equals(armies.owner) : armies.owner == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + size;
        temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Armies{" +
                "owner='" + owner + '\'' +
                ", size=" + size +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
