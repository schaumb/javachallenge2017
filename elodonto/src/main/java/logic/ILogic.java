package logic;

import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;

import java.util.logging.Logger;

public interface ILogic extends AutoCloseable {
    Logger LOG = Logger.getLogger(ILogic.class.getName());
    String OUR_TEAM = "overload";

    default void setGameDescription(GameDescription gameDescription) {
    }

    void setGameState(GameState gameState);

    default ILogic collapse(ILogic other) {
        ILogic orig = this;
        return new ILogic() {
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
