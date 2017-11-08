package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;
import jsons.gamestate.PlayerState;

public class PlayerExtent {
    private final PlayerState playerState;
    private final long currentPossibleScore;
    private final CurrentState currentState;

    public PlayerExtent(GameDescription gameDescription, GameState gameState, PlayerState playerState) {
        this.playerState = playerState;

        long possibleScore;
        CurrentState state;
        if (gameState.getPlanetStates().stream().allMatch(p -> p.isOwns(playerState.getUserID()))  // elfoglalta az összes bolygót
                || gameState.getStandings().stream().mapToInt(PlayerState::getStrength).sum() == playerState.getStrength()) // kiejtette a többi játékost
        {
            state = CurrentState.WON;
            possibleScore = 2000 + gameDescription.getRemainingTime() * 1000 / gameDescription.getGameLength();
        } else if (playerState.getStrength() == 0) {
            // TODO NEM ÁLLJA MEG AZ IGAZSÁGOT -> gameDescriotion.getCurrentTime() nem a kiesés ideje, de 1v1-nél oké
            possibleScore = gameDescription.getCurrentTime() * 1000 / gameDescription.getGameLength();
            state = CurrentState.DEAD;
        } else {
            possibleScore = gameDescription.getCurrentTime() * 1000 / gameDescription.getGameLength() +
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
