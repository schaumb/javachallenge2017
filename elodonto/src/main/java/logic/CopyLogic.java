package logic;

import jsons.Move;
import jsons.common.ArmyExtent;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import java.io.*;
import java.util.HashSet;
import java.util.function.Consumer;

public class CopyLogic implements ILogic {
    private static final String SERIALIZED_FILE = "test.txt";
    private MoveState prev;
    private MoveState now = new MoveState();
    private Consumer<Move> consumer;
    private GameDescription gameDescription;
    private GameState currGameState;

    public CopyLogic() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SERIALIZED_FILE))) {
            prev = (MoveState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (prev == null)
            prev = new MoveState();

        System.err.println(prev);
    }

    @Override
    public void setMessageConsumer(Consumer<Move> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setGameDescription(GameDescription gameDescription) {
        this.gameDescription = gameDescription;
    }

    @Override
    public void setGameState(GameState gameState) {
        if (gameState.getTimeElapsed() == 0) {
            now.upside = !gameState.getPlanetState(101).isOurs();
            // TODO start play
        }
        this.currGameState = gameState;

        for (PlanetState planetState : gameState.getPlanetStates()) {
            for (Army army : planetState.getMovingArmies()) {
                if (!army.isOurs()) {
                    now.doing.add(currGameState.getArmyExtent(army));
                }
            }
        }
    }

    @Override
    public void close() {
        System.err.println(now);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SERIALIZED_FILE))) {
            oos.writeObject(now);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class MoveState implements Serializable {
        boolean upside = false;
        HashSet<ArmyExtent> doing = new HashSet<>();

        @Override
        public String toString() {
            return "MoveState{" +
                    "upside=" + upside +
                    ", doing=" + doing +
                    '}';
        }
    }
}
