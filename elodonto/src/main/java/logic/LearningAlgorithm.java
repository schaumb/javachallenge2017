package logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import jsons.Move;
import jsons.common.ArmyExtent;
import jsons.common.PlayerExtent;
import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;
import jsons.gamestate.PlayerState;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LearningAlgorithm implements ILogic {

    public static final LearningAlgorithm THE_LEARNING_ALGORITHM;
    private static final File learningStateFile = new File("learning.txt");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        THE_LEARNING_ALGORITHM = new LearningAlgorithm();
        Runtime.getRuntime().addShutdownHook(new Thread(THE_LEARNING_ALGORITHM::saveState));
    }

    private GameState currentGameState;
    private GameState prevGameState;
    private States states;
    private HashMap<String, ArrayList<StateIndices>> steps = new HashMap<>();
    private HashMap<String, HashMap<Integer, // tick
            PlayerIndex>> players = new HashMap<>();
    private Boolean up;
    private Random random = new Random();


    private LearningAlgorithm() {
        loadState();
    }

    @Override
    public void setGameDescription(GameDescription gameDescription) {
        steps.clear();
        players.clear();
        up = null;
        tick(0);
    }

    @Override
    public void setGameState(GameState gameState) {
        currentGameState = gameState;
        if (gameState.getTimeElapsed() != 0) {
            tick(gameState.getTimeElapsed() / GameDescription.LATEST_INSTANCE.getBroadcastSchedule());
        }
    }

    @Override
    public void close() {
        currentGameState = null;
        prevGameState = null;
    }

    private void loadState() {
        if (learningStateFile.isFile()) {
            try (FileInputStream file = new FileInputStream(learningStateFile)) {
                states = gson.fromJson(new InputStreamReader(file), States.class);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        if (states == null)
            states = new States();
    }

    private void saveState() {
        try (FileOutputStream file = new FileOutputStream(learningStateFile)) {
            file.write(gson.toJson(states).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tick(int tick) {
        GameDescription game = GameDescription.LATEST_INSTANCE;
        if (currentGameState != null) {
            if (up == null) {
                up = currentGameState.getOurStationedArmiedExtentPlanetStates().anyMatch(e -> e.getPlanetState().getPlanetID() == 101);
            }
            if (prevGameState != null) {
                // try to collect moves:
                int prevTick = tick - 1;
                game.getPlayers().forEach(player -> {
                    String userID = player.getUserID();
                    prevGameState.getStationedArmiesExtentPlanetStates(userID).forEach(planetExtent -> {
                        PlanetState planetState = planetExtent.getPlanetState();
                        int planetID = planetState.getPlanetID();
                        int size = planetState.getStationedArmy(userID).getSize();

                        if (size < game.getMinMovableArmySize())
                            return;

                        HashMap<Integer, Integer> collect = currentGameState.getMovingExtentArmy(userID)
                                .filter(a -> a.getFromTick() == prevTick)
                                .collect(Collectors.toMap(
                                        (ArmyExtent a) -> a.getToPlanet().getPlanetID(),
                                        a -> a.getArmy().getSize(),
                                        (i, j) -> i + j,
                                        HashMap::new
                                ));

                        int sent = collect.values().stream().mapToInt(i -> i).sum();

                        if (size > sent) {
                            collect.put(planetID, size - sent);
                        }
                        StateIndices stateIndices = new StateIndices(prevTick, planetID, size, up == player.isUs(), collect);
                        steps.computeIfAbsent(userID, i -> new ArrayList<>())
                                .add(stateIndices);


                        stateIndices.setWeight(1.0);
                    });
                });

                double average = currentGameState.getStandings()
                        .stream()
                        .mapToDouble(PlayerState::getStrength).average().orElse(0);

                currentGameState.getStandings()
                        .forEach(e -> {
                            double mult = e.getStrength() / average;

                            // TODO
                        });
            }

            // make my steps
            currentGameState.getOurStationedArmiedExtentPlanetStates()
                    .forEach(planetExtent -> {
                        PlanetState planetState = planetExtent.getPlanetState();
                        int planetID = planetState.getPlanetID();
                        Army ourStationedArmy = planetState.getOurStationedArmy();
                        int size = ourStationedArmy.getSize();

                        if (size < game.getMinMovableArmySize())
                            return;

                        long startTime = System.currentTimeMillis();
                        System.err.println("Creates possibilities");
                        Stream<StateIndices> possibles = createPossibles(tick, size, planetID, size,
                                game.getPlanetIDs().toArray(), 0, 2);
                        System.err.println("End created possibles took ms: " + (System.currentTimeMillis() - startTime));

                        startTime = System.currentTimeMillis();
                        System.err.println("Sorting...");
                        possibles = possibles.sorted(getSorter());
                        System.err.println("Sorting took ms: " + (System.currentTimeMillis() - startTime));

                        System.err.println("Get first...");
                        startTime = System.currentTimeMillis();
                        StateIndices state = possibles.findFirst().get();
                        System.err.println("Get first took ms: " + (System.currentTimeMillis() - startTime));

                        state.setWeight(1.0);

                        state.moves().forEach(m -> m.send(OUR_TEAM));
                    });

            prevGameState = currentGameState;
        } else {
            Planet planetFromUp = game.getPlanet(101);
            game.getPlanets().stream().sorted(Comparator.comparingDouble(planetFromUp::distance))
                    .skip(1).findFirst().ifPresent(planetTo ->
                    new Move().setArmySize(Integer.MAX_VALUE)
                            .setMoveFrom(planetFromUp.getPlanetID())
                            .setMoveTo(planetTo.getPlanetID()).send(OUR_TEAM));

            Planet planetFromDown = game.getPlanet(102);
            game.getPlanets().stream().sorted(Comparator.comparingDouble(planetFromDown::distance))
                    .skip(1).findFirst().ifPresent(planetTo ->
                    new Move().setArmySize(Integer.MAX_VALUE)
                            .setMoveFrom(planetFromDown.getPlanetID())
                            .setMoveTo(planetTo.getPlanetID()).send(OUR_TEAM));
        }
    }

    private Comparator<StateIndices> getSorter() {
        GameDescription game = GameDescription.LATEST_INSTANCE;
        List<ToDoubleFunction<StateIndices>> buntik = new ArrayList<>(Arrays.asList(
                // StateIndices::getWeight,
                i -> 1.0,
                i -> {
                    if (i.getCalculatedWeight() != -1.0)
                        return i.getCalculatedWeight();

                    GameState copy = currentGameState.copy().setMove(i.moves(), OUR_TEAM).setAfterTime(game.getGameLength() - currentGameState.getTimeElapsed());

                    return i.setCalculatedWeight(copy.getOurState().getStrength()).getCalculatedWeight();
                }));

        ToDoubleFunction<StateIndices> reduce = buntik.stream().reduce(i -> 1.0, (a, b) -> x -> a.applyAsDouble(x) * b.applyAsDouble(x));
        return Comparator.comparingDouble(reduce);
    }

    private Stream<StateIndices> createPossibles(int tick, int armySize, int fromPlanet, int size, int[] planetIndices, int from, int maxSplit) {
        int minMovableArmySize = GameDescription.LATEST_INSTANCE.getMinMovableArmySize();

        Stream<StateIndices> possibles = Stream.empty();
        if (size > 0) {
            StateIndices state = new StateIndices(tick, fromPlanet, armySize, up, new HashMap<>());
            state.toPlanets.put(fromPlanet, size);
            possibles = Stream.of(state);

        }

        if (maxSplit > 0) {
            possibles = Stream.concat(possibles,
                    IntStream.range(from, planetIndices.length)
                            .filter(i -> planetIndices[i] != fromPlanet)
                            .mapToObj(toPlanet ->
                                    IntStream.rangeClosed(minMovableArmySize, size)
                                            .mapToObj(count ->
                                                    createPossibles(tick, armySize, fromPlanet, size - count, planetIndices, toPlanet + 1, maxSplit - 1)
                                            )
                            ).flatMap(Function.identity())
                            .flatMap(Function.identity())).parallel();
        }

        return possibles;
    }

    static class MoveState {
        private final HashMap<Integer, // toPlanet
                Integer> concurrent;
        double weight = 1.0;

        public MoveState() {
            this(new HashMap<>());
        }

        MoveState(HashMap<Integer, Integer> concurrent) {
            this.concurrent = concurrent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MoveState moveState = (MoveState) o;

            return concurrent.equals(moveState.concurrent);
        }

        @Override
        public int hashCode() {
            return concurrent.hashCode();
        }

        public HashMap<Integer, Integer> getConcurrent() {
            return concurrent;
        }

        public double getWeight() {
            return weight;
        }

        public MoveState setWeight(double weight) {
            this.weight = weight;
            return this;
        }
    }

    private static class States extends HashMap<Integer, // tick
            HashMap<Integer, // fromPlanet
                    HashMap<Integer, // units
                            ArrayList<MoveState>>>> // to-s
    {
    }

    private static class PlayerIndex {
        private final PlayerExtent extent;
        private final boolean good;

        private PlayerIndex(PlayerExtent extent, boolean good) {
            this.extent = extent;
            this.good = good;
        }

        public PlayerExtent getExtent() {
            return extent;
        }

        public boolean getGood() {
            return good;
        }
    }

    private class StateIndices {
        private final int tick;
        private final int from;
        private final int units;
        private final HashMap<Integer, Integer> toPlanets;
        private final IntUnaryOperator intUnaryOperator;
        private String string;

        private double calculatedWeight = -1;

        private StateIndices(int tick, int from, int units, boolean swap, HashMap<Integer, Integer> toPlanets) {
            this.tick = tick;
            this.from = from;
            this.units = units;
            intUnaryOperator = swap ? i -> i % 2 == 0 ? i - 1 : i + 1 : IntUnaryOperator.identity();
            this.toPlanets = toPlanets;
        }

        public int getTick() {
            return tick;
        }

        public int getFrom() {
            return from;
        }

        public int getUnits() {
            return units;
        }

        public double getWeight() {
            ArrayList<MoveState> moveStates = states.getOrDefault(tick, new HashMap<>())
                    .getOrDefault(intUnaryOperator.applyAsInt(from), new HashMap<>())
                    .getOrDefault(units, new ArrayList<>());

            MoveState moveState = new MoveState(toPlanets
                    .entrySet().stream().collect(Collectors.toMap(
                            (Map.Entry<Integer, Integer> e) -> intUnaryOperator.applyAsInt(e.getKey()),
                            Map.Entry::getValue,
                            (a, b) -> a + b,
                            HashMap::new)));
            int index = moveStates.indexOf(moveState);
            if (index == -1) {
                return 1.0;
            } else {
                return moveStates.get(index).getWeight();
            }
        }

        public void setWeight(double weight) {
            ArrayList<MoveState> moveStates = states.computeIfAbsent(tick, t -> new HashMap<>())
                    .computeIfAbsent(intUnaryOperator.applyAsInt(from), f -> new HashMap<>())
                    .computeIfAbsent(units, f -> new ArrayList<>());

            MoveState moveState = new MoveState(toPlanets
                    .entrySet().stream().collect(Collectors.toMap(
                            (Map.Entry<Integer, Integer> e) -> intUnaryOperator.applyAsInt(e.getKey()),
                            Map.Entry::getValue,
                            (a, b) -> a + b,
                            HashMap::new)));
            int index = moveStates.indexOf(moveState);
            if (index == -1) {
                moveState.setWeight(weight);
                moveStates.add(moveState);
            } else {
                moveStates.get(index).setWeight(weight);
            }
        }

        public double getCalculatedWeight() {
            return calculatedWeight;
        }

        public StateIndices setCalculatedWeight(double calculatedWeight) {
            this.calculatedWeight = calculatedWeight;
            return this;
        }

        public Stream<Move> moves() {
            return toPlanets.entrySet().stream()
                    .map(e -> new Move().setMoveFrom(from).setMoveTo(e.getKey()).setArmySize(e.getValue()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StateIndices that = (StateIndices) o;

            return tick == that.tick && from == that.from && units == that.units && (toPlanets != null ? toPlanets.equals(that.toPlanets) : that.toPlanets == null);
        }

        @Override
        public int hashCode() {
            int result = (int) (tick ^ (tick >>> 32));
            result = 31 * result + from;
            result = 31 * result + units;
            result = 31 * result + (toPlanets != null ? toPlanets.hashCode() : 0);
            return result;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }
}

