package jsons.gamestate;

import jsons.common.ArmyExtent;
import jsons.common.PlanetExtent;
import jsons.common.PlayerExtent;
import jsons.gamedesc.GameDescription;
import logic.ILogic;

import java.util.List;
import java.util.Objects;

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
        return new ArmyExtent(GameDescription.LATEST_INSTANCE, this, army);
    }

    public PlayerExtent getPlayerExtent(PlayerState state) {
        return new PlayerExtent(GameDescription.LATEST_INSTANCE, this, state);
    }

    public PlanetExtent getPlanetExtent(PlanetState state) {
        return new PlanetExtent(GameDescription.LATEST_INSTANCE, this, state);
    }

    private PlayerState getPlayerState(String id) {
        return getStandings().stream()
                .filter(p -> Objects.equals(p.getUserID(), id))
                .findAny().orElse(null);
    }

    public PlayerState getOurState() {
        return getPlayerState(ILogic.OUR_TEAM);
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
}
