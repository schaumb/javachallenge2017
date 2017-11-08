package logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import jsons.Move;
import jsons.common.ArmyExtent;
import jsons.common.IOwned;
import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import java.io.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class CopyLogic implements ILogic, Runnable {
    private static final String SERIALIZED_FILE = "test.txt";
    private static final String SERIALIZED_BEST = "best.txt";
    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();
    private MoveState prev;
    private MoveState now = new MoveState();
    private MoveState best = new MoveState();
    private Consumer<Move> consumer;
    private GameDescription gameDescription;
    private GameState currGameState;
    private long startMS;
    private boolean notFromStart = true;


    CopyLogic() {
        try (FileInputStream fis = new FileInputStream(SERIALIZED_FILE)) {
            prev = gson.fromJson(new InputStreamReader(fis), MoveState.class);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream(SERIALIZED_BEST)) {
            best = gson.fromJson(new InputStreamReader(fis), MoveState.class);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }

        if (prev == null)
            prev = new MoveState();
        if (best == null)
            best = new MoveState();

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
        if (this.currGameState == null || gameState.getTimeElapsed() == 0) {
            now.upside = !gameState.getPlanetState(101).isOurs();
            notFromStart = gameState.getTimeElapsed() != 0;
            startMS = System.currentTimeMillis();
            new Thread(this).start();
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
        if(!notFromStart) {
            System.err.println(now);
            now.score = currGameState.getOurState().getScore();
            if (best.score < now.score) {
                try (FileOutputStream fos = new FileOutputStream(SERIALIZED_BEST)) {
                    fos.write(gson.toJson(now, MoveState.class).getBytes());
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try (FileOutputStream fos = new FileOutputStream(SERIALIZED_FILE)) {
                fos.write(gson.toJson(now, MoveState.class).getBytes());
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        boolean needSwitch = now.upside == prev.upside;
        Function<Integer, Integer> transformer = needSwitch ?
                i -> i % 2 == 0 ? i - 1 : i + 1
                : Function.identity();
        prev.doing
                .stream()
                .sorted(Comparator.comparingLong(ArmyExtent::getFromTime))
                .forEach(d -> {
                    try {
                        long timeToSleep = d.getFromTime() - (System.currentTimeMillis() - startMS);

                        if (timeToSleep > 0)
                            Thread.sleep(d.getFromTime() - (System.currentTimeMillis() - startMS));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.err.println("Sleeped to " + d.getFromTime());

                    Integer fromPlanetID = transformer.apply(d.getFromPlanet());
                    PlanetState fromPlanetState = currGameState.getPlanetState(fromPlanetID);
                    if (fromPlanetState.getStationedArmies().size() == 1 &&
                        fromPlanetState.getMovingArmies().stream().allMatch(IOwned::isOurs)
                    && fromPlanetState.getOwnershipRatio() < 1.0) {
                        Army army = fromPlanetState.getStationedArmies().get(0);

                        if(army.getSize() == d.getArmy().getSize()) {
                            System.err.println("Hacked to wait");
                            Planet asPlanet = fromPlanetState.getAsPlanet();

                            double seconds = army.getSize() * gameDescription.getCaptureSpeed() / Math.pow(asPlanet.getRadius(), gameDescription.getPlanetExponent());

                            double timeNeed = (1.0 - fromPlanetState.getOwnershipRatio()) * seconds / 1000;
                            long floor = (long) timeNeed;

                            try {
                                if(floor > 0)
                                    Thread.sleep(floor);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    consumer.accept(new Move().setArmySize(d.getArmy().getSize()).setMoveFrom(
                            fromPlanetID
                    ).setMoveTo(
                            transformer.apply(d.getToPlanet())
                    ));
                });
    }

    static class MoveState {
        boolean upside = false;
        int score = 0;
        HashSet<ArmyExtent> doing = new HashSet<>();

        @Override
        public String toString() {
            return "MoveState{" +
                    "upside=" + upside +
                    ", doing=" + doing +
                    ", score=" + score +
                    '}';
        }
    }
}
