package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;

import java.io.Serializable;
import java.util.Comparator;

public class ArmyExtent implements Serializable {
    private final Army army;
    private final long fromTime;
    private final int fromPlanet;
    private final int toPlanet;

    public ArmyExtent(GameDescription gameDescription, GameState gameState, Army army) {
        Planet toPlanet = gameState.getArmyPlanetState(army).getAsPlanet();
        int timeElapsed = gameState.getTimeElapsed();
        Planet planet = army.isInMove() ? gameDescription.getPlanets()
                .stream()
                .filter(p -> p != toPlanet)
                .min(Comparator.comparingDouble(p -> Positioned.collinears(p, army, toPlanet)))
                .orElse(null) : null;

        this.army = army;
        this.toPlanet = toPlanet.getPlanetID();
        this.fromPlanet = planet == null ? this.toPlanet : planet.getPlanetID();
        this.fromTime = planet == null ? timeElapsed : Math.round(timeElapsed - 1000 * army.distance(planet) / gameDescription.getMovementSpeed());
    }

    public Army getArmy() {
        return army;
    }

    public long getFromTime() {
        return fromTime;
    }

    public int getFromPlanet() {
        return fromPlanet;
    }

    public int getToPlanet() {
        return toPlanet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArmyExtent that = (ArmyExtent) o;

        return fromTime == that.fromTime && fromPlanet == that.fromPlanet && toPlanet == that.toPlanet && (army != null ? army.equals(that.army) : that.army == null);
    }

    @Override
    public int hashCode() {
        int result = army != null ? army.hashCode() : 0;
        result = 31 * result + (int) (fromTime ^ (fromTime >>> 32));
        result = 31 * result + fromPlanet;
        result = 31 * result + toPlanet;
        return result;
    }

    @Override
    public String toString() {
        return "\nArmyExtent{" +
                "army=" + army +
                ", fromTime=" + fromTime +
                ", fromPlanet=" + fromPlanet +
                ", toPlanet=" + toPlanet +
                '}';
    }
}
