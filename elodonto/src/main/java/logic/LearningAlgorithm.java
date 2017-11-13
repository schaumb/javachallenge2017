package logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import jsons.Move;
import jsons.common.PlayerExtent;
import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

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
                up = currentGameState.getPlanetState(101).getStationedArmy(OUR_TEAM) != null;
            }
            /*
            if (prevGameState != null) {
                // try to collect moves:
                int prevTick = tick - 1;
                for (Player player : game.getPlayers()) {
                    String userID = player.getUserID();

                    for (PlanetState planetState : prevGameState.getPlanetStates()) {
                        Army army = null;
                        for (Army army1 : planetState.getStationedArmies()) {
                            if(army1.isOwns(userID)) {
                                army = army1;
                                break;
                            }
                        }
                        if(army == null)
                            continue;

                        int planetID = planetState.getPlanetID();
                        int size = army.getSize();

                        if (size < game.getMinMovableArmySize())
                            return;

                        int sent = 0;
                        HashMap<Integer, Integer> collect = new HashMap<>();
                        for (PlanetState state : currentGameState.getPlanetStates()) {
                            for (Army army1 : state.getMovingArmies()) {
                                if(army1.isOwns(userID) && currentGameState.getArmyExtent(state, army1).getFromTick() == prevTick) {
                                    sent +=  army1.getSize();
                                    collect.put(state.getPlanetID(), army1.getSize());
                                }
                            }
                        }

                        if (size > sent) {
                            collect.put(planetID, size - sent);
                        }

                        StateIndices stateIndices = new StateIndices(prevTick, planetID, size, up == player.isUs(), collect);
                        steps.computeIfAbsent(userID, i -> new ArrayList<>())
                                .add(stateIndices);

                        stateIndices.setWeight(1.0);
                    }
                }

                double average = currentGameState.getStandings()
                        .stream()
                        .mapToDouble(PlayerState::getStrength).average().orElse(0);

                currentGameState.getStandings()
                        .forEach(e -> {
                            double mult = e.getStrength() / average;

                            // TODO
                        });
            }

            prevGameState = currentGameState;
            */
            // System.err.println("MAKE STEPS");

            // make my steps
            for (PlanetState planetState : currentGameState.getPlanetStates()) {
                Army ourStationedArmy = null;
                for (Army army : planetState.getStationedArmies()) {
                    if (army.isOurs()) {
                        ourStationedArmy = army;
                        break;
                    }
                }
                if (ourStationedArmy == null)
                    continue;

                int planetID = planetState.getPlanetID();
                int size = ourStationedArmy.getSize();

                if (size < game.getMinMovableArmySize())
                    return;


                StateIndices state = null;
                long startTime = System.currentTimeMillis();

                List<StateIndices> states = createPossibles(tick, size, planetID, size,
                        game.getPlanets(), 0, 1).sorted(getSorter()).limit(10).collect(Collectors.toList());
                state = states.get(0);
                System.err.println("Get first took ms: " + (System.currentTimeMillis() - startTime));
                for (StateIndices stateIndices : states) {
                    System.err.println(stateIndices + " calcWeight: " + stateIndices.getCalculatedWeight());
                }


                /*
                startTime = System.currentTimeMillis();
                ArrayList<StateIndices> list = new ArrayList<>();
                createPossibles(list, tick, size, planetID, size, game.getPlanetIDs().toArray(), 0, 2);
                list.sort(getSorter());
                state = list.get(0);
                System.err.println("Get list generate and sort took: " + (System.currentTimeMillis() - startTime));
                */
                state.setWeight(1.0);

                state.moves().forEach(m -> m.send(OUR_TEAM));
            }

        } else {
            new Move().setArmySize(Integer.MAX_VALUE)
                    .setMoveFrom(102)
                    .setMoveTo(106).send(OUR_TEAM);
            new Move().setArmySize(Integer.MAX_VALUE)
                    .setMoveFrom(101)
                    .setMoveTo(105).send(OUR_TEAM);
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
                    GameState copy = currentGameState.copy().setMove(OUR_TEAM, i.moves());

                    double d = 0.0;
                    for (int x = 0; x < 3; ++x) {
                        d += copy.getPlayerExtent(copy.setWhileFirstArrives().getOurState()).getCurrentPossibleScore();
                    }

                    return i.setCalculatedWeight(d).getCalculatedWeight();
                }));

        ToDoubleFunction<StateIndices> reduce = buntik.stream().reduce(i -> 1.0, (a, b) -> x -> a.applyAsDouble(x) * b.applyAsDouble(x));
        return Comparator.comparingDouble(reduce).reversed()
                .thenComparing(Comparator.comparingInt(i -> i.toPlanets.size()));
    }

    private Stream<StateIndices> createPossibles(int tick, int armySize, int fromPlanet, int size, List<Planet> planetIndices, int from, int maxSplit) {
        int minMovableArmySize = GameDescription.LATEST_INSTANCE.getMinMovableArmySize();

        StateIndices state = new StateIndices(tick, fromPlanet, armySize, up, new HashMap<>());
        if (size > 0) {
            state.toPlanets.put(fromPlanet, size);
        }
        Stream<StateIndices> possibles = Stream.of(state);

        if (maxSplit > 0) {
            possibles = Stream.concat(possibles,
                    IntStream.range(from, planetIndices.size())
                            .mapToObj(i -> planetIndices.get(i).getPlanetID())
                            .filter(i -> i != fromPlanet)
                            .map(toPlanet ->
                                    IntStream.rangeClosed(Math.max(minMovableArmySize, Math.min(size, size / 4)), size)
                                            .mapToObj(count ->
                                                    createPossibles(tick, armySize, fromPlanet, size - count, planetIndices, toPlanet + 1, maxSplit - 1)
                                                            .peek(i -> i.toPlanets.put(toPlanet, count))
                                            )
                            ).flatMap(Function.identity())
                            .flatMap(Function.identity()));
        }

        return possibles;
    }

    private void createPossibles(ArrayList<StateIndices> to, int tick, int armySize, int fromPlanet, int size, int[] planetIndices, int from, int maxSplit) {
        int minMovableArmySize = GameDescription.LATEST_INSTANCE.getMinMovableArmySize();

        if (size > 0) {
            StateIndices state = new StateIndices(tick, fromPlanet, armySize, up, new HashMap<>());
            state.toPlanets.put(fromPlanet, size);
            to.add(state);
        }

        if (maxSplit > 0) {
            for (int toPlanet = from; toPlanet < planetIndices.length; ++toPlanet) {
                if (planetIndices[toPlanet] == fromPlanet)
                    continue;

                for (int count = Math.max(minMovableArmySize, size / 4); count <= size; ++count) {
                    int addFrom = to.size();
                    createPossibles(to, tick, armySize, fromPlanet, size - count, planetIndices, toPlanet + 1, maxSplit - 1);

                    for (int add = addFrom; add < to.size(); ++add) {
                        to.get(add).toPlanets.put(planetIndices[toPlanet], count);
                    }
                }
            }
        }
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

        public List<Move> moves() {
            return toPlanets.entrySet().stream()
                    .map(e -> new Move().setMoveFrom(from).setMoveTo(e.getKey()).setArmySize(e.getValue()))
                    .collect(Collectors.toList());
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

        @Override
        public String toString() {
            return "StateIndices{" +
                    "tick=" + tick +
                    ", from=" + from +
                    ", units=" + units +
                    ", toPlanets=" + toPlanets +
                    '}';
        }
    }
}

