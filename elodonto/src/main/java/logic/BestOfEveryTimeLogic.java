package logic;

import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;

import java.util.function.Consumer;

public class BestOfEveryTimeLogic implements ILogic {
    private Consumer<Move> toServer;
    private GameDescription gameDescription;

    @Override
    public void setMessageConsumer(Consumer<Move> consumer) {
        toServer = consumer;
    }

    @Override
    public void setGameDescription(GameDescription gameDescription) {
        this.gameDescription = gameDescription;
    }

    @Override
    public void setGameState(GameState gameState) {
        System.err.println("TODO do awsome logic");
    }
}
