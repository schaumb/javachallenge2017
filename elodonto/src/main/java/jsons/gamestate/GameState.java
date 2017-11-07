package jsons.gamestate;

import java.util.List;

public class GameState {
    private List<PlanetState> planetStates;

    private List<PlayerState> standings;

    private GameStatus gameStatus;

    private int timeElapsed;

    private int remainingPlayers;

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
