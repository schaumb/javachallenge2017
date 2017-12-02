package jsons.gamestate;

import jsons.common.IOwned;
import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import logic.ILogic;

import java.util.*;
import java.util.function.Function;

public class PlanetState implements IOwned {
    private int planetID;
    private String owner;
    private double ownershipRatio;

    private List<Army> movingArmies;
    private List<Army> stationedArmies;

    public PlanetState() {

    }

    public PlanetState(PlanetState planetState) {
        this.planetID = planetState.planetID;
        this.owner = planetState.owner;
        this.ownershipRatio = planetState.ownershipRatio;

        this.movingArmies = new ArrayList<>(planetState.movingArmies.size());

        for (Army movingArmy : planetState.movingArmies) {
            this.movingArmies.add(new Army(movingArmy));
        }

        this.stationedArmies = new ArrayList<>(planetState.stationedArmies.size());

        for (Army stationedArmy : planetState.stationedArmies) {
            this.stationedArmies.add(new Army(stationedArmy));
        }
    }


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

    public PlanetState setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public double getOwnershipRatio() {
        return ownershipRatio;
    }

    public PlanetState setOwnershipRatio(double ownershipRatio) {
        this.ownershipRatio = ownershipRatio;
        return this;
    }

    public List<Army> getMovingArmies() {
        return movingArmies;
    }

    public List<Army> getStationedArmies() {
        return stationedArmies;
    }

    public Army getStationedArmy(String owner) {
        for (Army army : getStationedArmies()) {
            if(army.isOwns(owner))
                return army;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanetState that = (PlanetState) o;

        return planetID == that.planetID;
    }

    public int biggestEnemyArmySizeWhichArrives() {
        Map<String, Integer> armies = new HashMap<>();

        for (Army army : getMovingArmies()) {
            armies.compute(army.getOwner(), (k, v) -> {
                if(v == null)
                    return army.getSize();
                return v + army.getSize();
            });
        }

        return armies.entrySet().stream().filter(i -> !Objects.equals(i.getKey(), ILogic.OUR_TEAM))
            .mapToInt(Map.Entry::getValue)
            .max().orElse(0);
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

    public int biggestEnemyArmySizeWhichIsHere() {
        Map<String, Integer> armies = new HashMap<>();

        for (Army army : getStationedArmies()) {
            armies.compute(army.getOwner(), (k, v) -> {
                if(v == null)
                    return army.getSize();
                return v + army.getSize();
            });
        }

        return armies.entrySet().stream().filter(i -> !Objects.equals(i.getKey(), ILogic.OUR_TEAM))
                .mapToInt(Map.Entry::getValue)
                .max().orElse(0);
    }
}
