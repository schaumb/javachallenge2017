package jsons.gamestate;

import jsons.common.ArmyExtent;
import jsons.common.PlanetExtent;
import jsons.common.PlayerExtent;
import logic.ILogic;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class GameState {
    private List<PlanetState> planetStates;

    private List<PlayerState> standings;

    private GameStatus gameStatus;

    private int timeElapsed;

    private int remainingPlayers;

    public PlanetState getArmyPlanetState(Army army) {
        return getPlanetStates().stream()
                .filter(pss -> pss.getStationedArmies().stream().anyMatch(a -> a == army) ||
                        pss.getMovingArmies().stream().anyMatch(a -> a == army))
                .findAny().orElse(null);
    }

    public ArmyExtent getArmyExtent(Army army) {
        return new ArmyExtent(this, army);
    }

    public PlayerExtent getPlayerExtent(PlayerState state) {
        return new PlayerExtent(getTimeElapsed(), this, state);
    }

    public PlanetExtent getPlanetExtent(PlanetState state) {
        return new PlanetExtent(getTimeElapsed(), this, state);
    }

    public PlanetExtent getPlanetExtent(int id) {
        PlanetState planetState = getPlanetState(id);
        return planetState == null ? null : getPlanetExtent(planetState);
    }


    private PlayerState getPlayerState(String id) {
        return getStandings().stream()
                .filter(p -> Objects.equals(p.getUserID(), id))
                .findAny().orElse(null);
    }

    public PlayerState getOurState() {
        return getPlayerState(ILogic.OUR_TEAM);
    }

    public Stream<PlanetExtent> getOurStationedArmiedExtentPlanetStates() {
        return getPlanetStates().stream()
                .filter(planetStates -> planetStates.getStationedArmies().stream().anyMatch(Army::isOurs))
                .map(this::getPlanetExtent);
    }

    public Stream<ArmyExtent> getOurMovingExtentArmy() {
        return getPlanetStates().stream()
                .flatMap(s -> s.getMovingArmies().stream())
                .filter(Army::isOurs)
                .map(this::getArmyExtent);
    }

    public Stream<ArmyExtent> getMovingExtentArmy(String owns) {
        return getPlanetStates().stream()
                .flatMap(s -> s.getMovingArmies().stream())
                .filter(a -> a.isOwns(owns))
                .map(this::getArmyExtent);
    }

    public Stream<ArmyExtent> getEnemiesMovingExtentArmy() {
        return getPlanetStates().stream()
                .flatMap(s -> s.getMovingArmies().stream())
                .filter(a -> !a.isOurs())
                .map(this::getArmyExtent);
    }

    public PlanetState getPlanetState(int id) {
        return getPlanetStates().stream()
                .filter(p -> p.getPlanetID() == id)
                .findAny().orElse(null);
    }

    public List<PlanetState> getPlanetStates() {
        return planetStates;
    }

    public List<PlayerState> getStandings() {
        return standings;
    }

    public Stream<PlayerState> getEnemies() {
        return getStandings().stream().filter(e -> !e.getAsPlayer().isUs());
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    public int getRemainingPlayers() {
        return remainingPlayers;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "planetStates=" + planetStates +
                ", standings=" + standings +
                ", gameStatus=" + gameStatus +
                ", timeElapsed=" + timeElapsed +
                ", remainingPlayers=" + remainingPlayers +
                '}';
    }

    public Stream<PlanetExtent> getStationedArmiedExtentPlanetStates(String who) {
        return getPlanetStates().stream()
                .filter(planetStates -> planetStates.getStationedArmies().stream().anyMatch(a -> Objects.equals(a.getOwner(), who)))
                .map(this::getPlanetExtent);
    }

    public int getAllArmy(String owner) {
        return getPlanetStates()
                .stream().flatMap(s -> Stream.concat(
                        s.getMovingArmies(owner),
                        Stream.of(s.getStationedArmy(owner))
                )).mapToInt(Army::getSize).sum();
    }

}
