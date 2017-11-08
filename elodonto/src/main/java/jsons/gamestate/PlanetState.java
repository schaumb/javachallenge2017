package jsons.gamestate;

import jsons.common.IOwned;
import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;

import java.util.List;

public class PlanetState implements IOwned {
    private int planetID;
    private String owner;
    private double ownershipRatio;

    private List<Army> movingArmies;
    private List<Army> stationedArmies;

    public Planet getAsPlanet() {
        return GameDescription.LATEST_INSTANCE.getPlanet(getPlanetID());
    }

    public int getPlanetID() {
        return planetID;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public double getOwnershipRatio() {
        return ownershipRatio;
    }

    public List<Army> getMovingArmies() {
        return movingArmies;
    }

    public List<Army> getStationedArmies() {
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
