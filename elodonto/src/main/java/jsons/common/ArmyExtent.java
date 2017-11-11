package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;

import java.util.Comparator;

public class ArmyExtent {
    private final Army army;
    private final long fromTime;
    private final int fromPlanet;
    private final int toPlanet;

    public ArmyExtent(GameState gameState, Army army) {
        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
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

    public ArmyExtent(Army army, long fromTime, int fromPlanet, int toPlanet) {
        this.army = army;
        this.fromTime = fromTime;
        this.fromPlanet = fromPlanet;
        this.toPlanet = toPlanet;
    }

    public Army getArmy() {
        return army;
    }

    public long getFromTime() {
        return fromTime;
    }

    public long getFromTick() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getFromTime());
    }

    public long getToTick() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getToTime());
    }

    public int getFromPlanet() {
        return fromPlanet;
    }

    public int getToPlanet() {
        return toPlanet;
    }

    public long getToTime() {
        return getFromTime() + (long) Math.ceil(GameDescription.LATEST_INSTANCE.getPlanet(fromPlanet).distance(
                GameDescription.LATEST_INSTANCE.getPlanet(toPlanet)) / GameDescription.LATEST_INSTANCE.getMovementSpeed() * 1000);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArmyExtent that = (ArmyExtent) o;

        return fromTime == that.fromTime && fromPlanet == that.fromPlanet && toPlanet == that.toPlanet && (army == that.army ||
                (army != null && that.army != null && army.getSize() == that.army.getSize()));
    }

    @Override
    public int hashCode() {
        int result = army != null ? army.getSize() : 0;
        result = 31 * result + (int) (fromTime ^ (fromTime >>> 32));
        result = 31 * result + fromPlanet;
        result = 31 * result + toPlanet;
        return result;
    }

    @Override
    public String toString() {
        return "ArmyExtent{" +
                "army=" + army +
                ", fromTime=" + fromTime +
                ", fromPlanet=" + fromPlanet +
                ", toPlanet=" + toPlanet +
                '}';
    }
}