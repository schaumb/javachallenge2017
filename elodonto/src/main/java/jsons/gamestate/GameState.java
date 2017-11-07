package jsons.gamestate;

import jsons.gamedesc.Player;

import java.util.List;

public class GameState {
    private List<PlanetState> planetStates;

    private List<Player> standings;

    private GameStatus gameStatus;

    private int timeElapsed;

    private int remainingPlayers;

    public List<PlanetState> getPlanetStates() {
        return planetStates;
    }

    public List<Player> getStandings() {
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
