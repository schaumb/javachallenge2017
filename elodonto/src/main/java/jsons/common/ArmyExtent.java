package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import java.util.Comparator;

public class ArmyExtent {
    private final Army army;
    private final Planet toPlanet;
    private final long toTime;
    private Planet fromPlanet;
    private Long fromTime;

    public ArmyExtent(GameState gameState, PlanetState state, Army army) {
        this.army = army;
        this.toPlanet = state.getAsPlanet();
        this.toTime = gameState.getTimeElapsed() + (long) Helper.timeToMoveWithoutCeil(army, toPlanet);
    }

    public Army getArmy() {
        return army;
    }

    public long getFromTime() {
        if (fromTime == null && army.isInMove()) {
            fromTime = toTime - (long) Helper.timeToMoveWithoutCeil(getFromPlanet(), toPlanet);
        }
        return fromTime;
    }

    public long getFromTick() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getFromTime());
    }

    public long getToTick() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getToTime());
    }

    public Planet getFromPlanet() {
        if (fromPlanet == null && army.isInMove()) {
            Comparator<Planet> comparator = Comparator.comparingDouble(p -> Positioned.collinears(p, army, toPlanet));
            for (Planet planet : GameDescription.LATEST_INSTANCE.getPlanets()) {
                if (planet.getPlanetID() != toPlanet.getPlanetID()) {
                    if (fromPlanet == null || comparator.compare(planet, fromPlanet) < 0) {
                        fromPlanet = planet;
                    }
                }
            }
        }
        return fromPlanet;
    }

    public Planet getToPlanet() {
        return toPlanet;
    }

    public long getToTime() {
        return toTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArmyExtent that = (ArmyExtent) o;

        if (toTime != that.toTime) return false;
        if (army != null ? !army.equals(that.army) : that.army != null) return false;
        return toPlanet != null ? toPlanet.equals(that.toPlanet) : that.toPlanet == null;
    }

    @Override
    public int hashCode() {
        int result = army != null ? army.hashCode() : 0;
        result = 31 * result + (int) (toTime ^ (toTime >>> 32));
        result = 31 * result + (toPlanet != null ? toPlanet.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ArmyExtent{" +
                "army=" + army +
                ", toPlanet=" + toPlanet +
                ", toTime=" + toTime +
                ", fromPlanet=" + fromPlanet +
                ", fromTime=" + fromTime +
                '}';
    }
}
