package logic;

import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;

import java.util.function.Consumer;

public class BestOfEveryTimeLogic implements ILogic {
    private Consumer<Move> toServer;

    @Override
    public void setMessageConsumer(Consumer<Move> consumer) {
        toServer = consumer;
    }

    @Override
    public void setGameState(GameState gameState) {
        System.err.println("TODO do awsome logic");
    }
}
