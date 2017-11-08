package jsons.gamestate;

import jsons.common.IOwned;
import jsons.common.Positioned;

import java.io.Serializable;
import java.util.Objects;

public class Army extends Positioned<Double> implements IOwned, Serializable {
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

        Army army = (Army) o;

        return size == army.size && Objects.equals(owner, army.owner);
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return "Army{" +
                "owner='" + owner + '\'' +
                ", size=" + size +
                ", x=" + getX() +
                ", y=" + getY() +
                '}';
    }
}
