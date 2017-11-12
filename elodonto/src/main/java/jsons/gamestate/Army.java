package jsons.gamestate;

import jsons.common.IOwned;
import jsons.common.Positioned;

public class Army extends Positioned<Double> implements IOwned {
    private String owner;
    private double size;

    public Army() {

    }

    public Army(Army movingArmy) {
        this.owner = movingArmy.owner;
        this.size = movingArmy.size;
        this.setX(movingArmy.getX());
        this.setY(movingArmy.getY());
    }

    public boolean isInMove() {
        return getX() != null && getY() != null;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public Army setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public int getSize() {
        return (int) size;
    }

    public Army setSize(double size) {
        this.size = size;
        return this;
    }

    public double getRealSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Army army = (Army) o;

        if (Double.compare(army.size, size) != 0) return false;
        return owner != null ? owner.equals(army.owner) : army.owner == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = owner != null ? owner.hashCode() : 0;
        temp = Double.doubleToLongBits(size);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
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
