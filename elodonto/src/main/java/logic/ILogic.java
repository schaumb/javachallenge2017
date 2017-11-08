package logic;

import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;

import java.util.function.Consumer;
import java.util.logging.Logger;

public interface ILogic extends AutoCloseable {
    Logger LOG = Logger.getLogger(ILogic.class.getName());
    String OUR_TEAM = "overload";

    static ILogic createLogic() {
        return new GuiLogic().collapse(new CopyLogic());
    }

    void setMessageConsumer(Consumer<Move> consumer);

    void setGameDescription(GameDescription gameDescription);

    void setGameState(GameState gameState);

    default ILogic collapse(ILogic other) {
        ILogic orig = this;
        return new ILogic() {
            @Override
            public void setMessageConsumer(Consumer<Move> consumer) {
                orig.setMessageConsumer(consumer);
                other.setMessageConsumer(consumer);
            }

            @Override
            public void setGameDescription(GameDescription gameDescription) {
                orig.setGameDescription(gameDescription);
                other.setGameDescription(gameDescription);
            }

            @Override
            public void setGameState(GameState gameState) {
                orig.setGameState(gameState);
                other.setGameState(gameState);
            }

            @Override
            public void close() {
                orig.close();
                other.close();
            }
        };
    }

    @Override
    default void close() {

    }
}
