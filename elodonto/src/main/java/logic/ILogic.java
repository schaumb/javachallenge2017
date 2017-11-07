package logic;

import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;

import java.util.function.Consumer;
import java.util.logging.Logger;

public interface ILogic {
    Logger LOG = Logger.getLogger(ILogic.class.getName());

    static ILogic createLogic() {
        return new BestOfEveryTimeLogic();
    }

    void setMessageConsumer(Consumer<Move> consumer);

    void setGameDescription(GameDescription gameDescription);

    void setGameState(GameState gameState);

    default ILogic collapse(ILogic other) {
        return new ILogic() {
            @Override
            public void setMessageConsumer(Consumer<Move> consumer) {
                this.setMessageConsumer(consumer);
                other.setMessageConsumer(consumer);
            }

            @Override
            public void setGameDescription(GameDescription gameDescription) {
                this.setGameDescription(gameDescription);
                other.setGameDescription(gameDescription);
            }

            @Override
            public void setGameState(GameState gameState) {
                this.setGameState(gameState);
                other.setGameState(gameState);
            }
        };
    }
}
