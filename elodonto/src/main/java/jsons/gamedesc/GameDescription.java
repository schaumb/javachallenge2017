package jsons.gamedesc;

import logic.ILogic;

import java.util.List;
import java.util.Objects;

public class GameDescription {
    public static GameDescription LATEST_INSTANCE;
    public static long GAME_STARTED_MS = -1;
    private int gameLength;
    private int mapSizeX;
    private int mapSizeY;

    private int commandSchedule;
    private int internalSchedule;
    private int broadcastSchedule;

    private int minMovableArmySize;
    private double movementSpeed;
    private double battleSpeed;
    private double captureSpeed;
    private double unitCreateSpeed;
    private double planetExponent;
    private double battleExponent;

    private List<Planet> planets;

    private List<Player> players;

    public GameDescription() {
        LATEST_INSTANCE = this;
    }

    public Player getPlayer(String id) {
        return getPlayers().stream()
                .filter(p -> Objects.equals(p.getUserID(), id))
                .findAny().orElse(null);
    }

    public Player getOurPlayer() {
        return getPlayer(ILogic.OUR_TEAM);
    }

    public Planet getPlanet(int id) {
        return getPlanets().stream()
                .filter(p -> p.getPlanetID() == id)
                .findAny().orElse(null);
    }

    public long getCurrentTime() {
        return Math.max(0, Math.min(getGameLength(), System.currentTimeMillis() - GameDescription.GAME_STARTED_MS));
    }

    public long getRemainingTime() {
        return getGameLength() - getCurrentTime();
    }

    public int getGameLength() {
        return gameLength;
    }

    public int getMapSizeX() {
        return mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
    }

    public int getCommandSchedule() {
        return commandSchedule;
    }

    public int getInternalSchedule() {
        return internalSchedule;
    }

    public int getBroadcastSchedule() {
        return broadcastSchedule;
    }

    public int getMinMovableArmySize() {
        return minMovableArmySize;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public double getBattleSpeed() {
        return battleSpeed;
    }

    public double getCaptureSpeed() {
        return captureSpeed;
    }

    public double getUnitCreateSpeed() {
        return unitCreateSpeed;
    }

    public double getPlanetExponent() {
        return planetExponent;
    }

    public double getBattleExponent() {
        return battleExponent;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameDescription that = (GameDescription) o;

        if (gameLength != that.gameLength) return false;
        if (mapSizeX != that.mapSizeX) return false;
        if (mapSizeY != that.mapSizeY) return false;
        if (commandSchedule != that.commandSchedule) return false;
        if (internalSchedule != that.internalSchedule) return false;
        if (broadcastSchedule != that.broadcastSchedule) return false;
        if (minMovableArmySize != that.minMovableArmySize) return false;
        if (Double.compare(that.movementSpeed, movementSpeed) != 0) return false;
        if (Double.compare(that.battleSpeed, battleSpeed) != 0) return false;
        if (Double.compare(that.captureSpeed, captureSpeed) != 0) return false;
        if (Double.compare(that.unitCreateSpeed, unitCreateSpeed) != 0) return false;
        if (Double.compare(that.planetExponent, planetExponent) != 0) return false;
        if (Double.compare(that.battleExponent, battleExponent) != 0) return false;
        if (planets != null ? !planets.equals(that.planets) : that.planets != null) return false;
        return players != null ? players.equals(that.players) : that.players == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = gameLength;
        result = 31 * result + mapSizeX;
        result = 31 * result + mapSizeY;
        result = 31 * result + commandSchedule;
        result = 31 * result + internalSchedule;
        result = 31 * result + broadcastSchedule;
        result = 31 * result + minMovableArmySize;
        temp = Double.doubleToLongBits(movementSpeed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(battleSpeed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(captureSpeed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(unitCreateSpeed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(planetExponent);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(battleExponent);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (planets != null ? planets.hashCode() : 0);
        result = 31 * result + (players != null ? players.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GameDescription{" +
                "gameLength=" + gameLength +
                ", mapSizeX=" + mapSizeX +
                ", mapSizeY=" + mapSizeY +
                ", commandSchedule=" + commandSchedule +
                ", internalSchedule=" + internalSchedule +
                ", broadcastSchedule=" + broadcastSchedule +
                ", minMovableArmySize=" + minMovableArmySize +
                ", movementSpeed=" + movementSpeed +
                ", battleSpeed=" + battleSpeed +
                ", captureSpeed=" + captureSpeed +
                ", unitCreateSpeed=" + unitCreateSpeed +
                ", planetExponent=" + planetExponent +
                ", battleExponent=" + battleExponent +
                ", planets=" + planets +
                ", players=" + players +
                '}';
    }

    public long getTickFromTime(long time) {
        return time / getBroadcastSchedule();
    }
}
