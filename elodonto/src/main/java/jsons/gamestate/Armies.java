package jsons.gamestate;

import jsons.common.IOwned;
import jsons.common.Positioned;

public class Armies extends Positioned<Double> implements IOwned {
    private String owner;
    private int size;

    public boolean isInMove() {
        return getX() != null && getY() != null;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Armies armies = (Armies) o;

        if (size != armies.size) return false;
        if (Double.compare(armies.getX(), getX()) != 0) return false;
        if (Double.compare(armies.getY(), getY()) != 0) return false;
        return owner != null ? owner.equals(armies.owner) : armies.owner == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + size;
        temp = Double.doubleToLongBits(getX());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Armies{" +
                "owner='" + owner + '\'' +
                ", size=" + size +
                ", x=" + getX() +
                ", y=" + getY() +
                '}';
    }
}
