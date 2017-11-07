package jsons.gamestate;

import java.util.List;

public class PlanetState {
    private int planetID;
    private String owner;
    private double ownershipRatio;

    private List<Armies> movingArmies;
    private List<Armies> stationedArmies;

    public int getPlanetID() {
        return planetID;
    }

    public String getOwner() {
        return owner;
    }

    public double getOwnershipRatio() {
        return ownershipRatio;
    }

    public List<Armies> getMovingArmies() {
        return movingArmies;
    }

    public List<Armies> getStationedArmies() {
        return stationedArmies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanetState that = (PlanetState) o;

        return planetID == that.planetID;
    }

    @Override
    public int hashCode() {
        return planetID;
    }

    @Override
    public String toString() {
        return "PlanetState{" +
                "planetID=" + planetID +
                ", owner='" + owner + '\'' +
                ", ownershipRatio=" + ownershipRatio +
                ", movingArmies=" + movingArmies +
                ", stationedArmies=" + stationedArmies +
                '}';
    }
}
