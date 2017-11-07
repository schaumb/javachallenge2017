package jsons.gamedesc;

public class Planet {
    private int planetID;
    private int x;
    private int y;
    private int radius;

    public int getPlanetID() {
        return planetID;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Planet planet = (Planet) o;

        if (planetID != planet.planetID) return false;
        if (x != planet.x) return false;
        if (y != planet.y) return false;
        return radius == planet.radius;
    }

    @Override
    public int hashCode() {
        int result = planetID;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + radius;
        return result;
    }

    @Override
    public String toString() {
        return "Planet{" +
                "planetID=" + planetID +
                ", x=" + x +
                ", y=" + y +
                ", radius=" + radius +
                '}';
    }
}
