package jsons.gamedesc;

import jsons.common.Positioned;

public class Planet extends Positioned<Integer> {
    private int planetID;
    private int radius;

    public int getPlanetID() {
        return planetID;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Planet planet = (Planet) o;

        return planetID == planet.planetID;
    }

    @Override
    public int hashCode() {
        return planetID;
    }

    @Override
    public String toString() {
        return "Planet{" +
                "planetID=" + planetID +
                ", x=" + getX() +
                ", y=" + getY() +
                ", radius=" + radius +
                '}';
    }
}
