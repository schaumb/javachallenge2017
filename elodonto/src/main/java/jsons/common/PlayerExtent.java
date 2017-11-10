package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;
import jsons.gamestate.PlayerState;

public class PlayerExtent {
    private final PlayerState playerState;
    private final long currentPossibleScore;
    private final CurrentState currentState;

    public PlayerExtent(long time, GameState gameState, PlayerState playerState) {
        this.playerState = playerState;

        long possibleScore;
        CurrentState state;
        GameDescription game = GameDescription.LATEST_INSTANCE;
        if (gameState.getPlanetStates().stream().allMatch(p -> p.isOwns(playerState.getUserID()))  // elfoglalta az összes bolygót
                || gameState.getStandings().stream().mapToInt(PlayerState::getStrength).sum() == playerState.getStrength()) // kiejtette a többi játékost
        {
            state = CurrentState.WON;
            possibleScore = 2000 + (game.getGameLength() - time) * 1000 / game.getGameLength();
        } else if (playerState.getStrength() == 0) {
            // TODO NEM ÁLLJA MEG AZ IGAZSÁGOT -> gameDescriotion.getCurrentTime() nem a kiesés ideje, de 1v1-nél oké
            possibleScore = time * 1000 / game.getGameLength();
            state = CurrentState.DEAD;
        } else {
            possibleScore = time * 1000 / game.getGameLength() +
                    playerState.getStrength() * 1000 / gameState.getStandings().stream().mapToInt(PlayerState::getStrength).sum();
            state = CurrentState.FIGHTS;
        }

        this.currentPossibleScore = possibleScore;
        this.currentState = state;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public long getCurrentPossibleScore() {
        return currentPossibleScore;
    }

    public CurrentState getCurrentAliveState() {
        return currentState;
    }

    public enum CurrentState {
        DEAD,
        WON,
        FIGHTS
    }
}
