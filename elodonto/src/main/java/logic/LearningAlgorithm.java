package logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import jsons.Move;
import jsons.common.ArmyExtent;
import jsons.common.Helper;
import jsons.common.PlanetExtent;
import jsons.common.PlayerExtent;
import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;
import jsons.gamestate.PlayerState;

import java.io.*;
import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LearningAlgorithm implements ILogic {

    static final LearningAlgorithm THE_LEARNING_ALGORITHM;
    private static final File learningStateFile = new File("learning.txt");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        THE_LEARNING_ALGORITHM = new LearningAlgorithm();
        Runtime.getRuntime().addShutdownHook(new Thread(THE_LEARNING_ALGORITHM::saveState));
    }

    private GameState currentGameState;
    private GameState prevGameState;
    private States states;
    private HashMap<String, HashSet<StateIndices>> steps = new HashMap<>();
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
                    prevGameState.getStationedArmiedExtentPlanetStates(userID).forEach(planetExtent -> {
                        PlanetState planetState = planetExtent.getPlanetState();
                        int planetID = planetState.getPlanetID();
                        int size = planetState.getStationedArmy(userID).getSize();

                        HashMap<Integer, Integer> collect = currentGameState.getMovingExtentArmy(userID)
                                .filter(a -> a.getFromTick() == prevTick)
                                .collect(Collectors.toMap(
                                        ArmyExtent::getToPlanet,
                                        a -> a.getArmy().getSize(),
                                        (i, j) -> i + j,
                                        HashMap::new
                                ));

                        int sent = collect.values().stream().mapToInt(i -> i).sum();

                        if (size > sent) {
                            collect.put(planetID, size - sent);
                        }
                        StateIndices stateIndices = new StateIndices(prevTick, planetID, size, up == player.isUs(), collect);
                        steps.computeIfAbsent(userID, i -> new HashSet<>())
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

                        System.err.println("Create possibles: ");
                        ArrayList<StateIndices> possibles = createPossibles(tick, size, planetID, size, game.getPlanets().stream()
                                .map(Planet::getPlanetID).collect(Collectors.toList()), 0, game.getPlanets().size());
                        System.err.println("End created possibles");

                        System.err.println(possibles.size());

                        possibles.sort(getSorter(planetExtent));
                        StateIndices state = possibles.get(0);

                        state.setWeight(1.0);

                        state.moves().forEach(Move::send);

                    });

            prevGameState = currentGameState;
        } else {
            Planet planetFromUp = game.getPlanet(101);
            game.getPlanets().stream().sorted(Comparator.comparingDouble(planetFromUp::distance))
                    .skip(1).findFirst().ifPresent(planetTo ->
                    new Move().setArmySize(Integer.MAX_VALUE)
                            .setMoveFrom(planetFromUp.getPlanetID())
                            .setMoveTo(planetTo.getPlanetID()).send());

            Planet planetFromDown = game.getPlanet(102);
            game.getPlanets().stream().sorted(Comparator.comparingDouble(planetFromDown::distance))
                    .skip(1).findFirst().ifPresent(planetTo ->
                    new Move().setArmySize(Integer.MAX_VALUE)
                            .setMoveFrom(planetFromDown.getPlanetID())
                            .setMoveTo(planetTo.getPlanetID()).send());
        }
    }

    private Comparator<StateIndices> getSorter(PlanetExtent planetExtent) {
        List<ToDoubleFunction<StateIndices>> buntik = new ArrayList<>(Arrays.asList(
                StateIndices::getWeight
                , i -> 1 / i.toPlanets.entrySet().stream().mapToDouble(e -> {
                    PlanetExtent toPlanetExtent = currentGameState.getPlanetExtent(e.getKey());
                    PlanetState toPlanetState = toPlanetExtent.getPlanetState();
                    Planet toPlanet = toPlanetState.getAsPlanet();

                    long timeToMove = Helper.timeToMove(planetExtent.getPlanetState().getAsPlanet(), toPlanet);

                    int enemySize = toPlanetState.getEnemyStationedArmies()
                            .mapToInt(Army::getSize).sum();
                    // TODO hozzáadni a mozgással töltött idő alatt odamenő, illetve generálódó enemyket

                    boolean owns = toPlanetState.isOwns(OUR_TEAM);
                    double amount = toPlanetState.getOwnershipRatio();
                    // TODO genrálni az odamozgásra keletkező cuccokat

                    if(enemySize > e.getValue()) { //  nem éri meg ??
                        return Double.MIN_VALUE;
                    }

                    double got = owns && amount == 1.0 ? Double.MIN_VALUE : Helper.planetWeight(toPlanet.getRadius(), 1) // got toPlanet
                            ; // TODO kill enemy ???

                    long timeToKill = Helper.timeToKillSomeone(Arrays.asList(e.getValue(), enemySize));

                    long timeToCapture = Helper.timeToCapture(toPlanet.getRadius(), e.getValue(), owns, amount);

                    return got / (timeToMove + timeToKill + timeToCapture);
                }).sum()));

        ToDoubleFunction<StateIndices> reduce = buntik.stream().reduce(i -> 1.0, (a, b) -> x -> a.applyAsDouble(x) * b.applyAsDouble(x));
        return Comparator.comparingDouble(reduce);
    }

    private ArrayList<StateIndices> createPossibles(int tick, int all, int fromPlanet, int size, List<Integer> planetIndices, int from, int to) {
        int minMovableArmySize = GameDescription.LATEST_INSTANCE.getMinMovableArmySize();
        ArrayList<StateIndices> possibles = new ArrayList<>();
        for (int toPlanet = from; toPlanet < to; ++toPlanet) {
            if (planetIndices.get(toPlanet) != fromPlanet) {
                for (int count = minMovableArmySize; count <= size; count = Math.max(minMovableArmySize + count, size)) {
                    for (StateIndices state : createPossibles(tick, all, fromPlanet, size - count, planetIndices, toPlanet + 1, to)) {
                        state.toPlanets.put(planetIndices.get(toPlanet), count);
                        possibles.add(state);
                    }
                }
            }
        }
        StateIndices state = new StateIndices(tick, fromPlanet, all, up, new HashMap<>());
        if (size > 0) {
            state.toPlanets.put(fromPlanet, size);
        }
        possibles.add(state);

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

        public Stream<Move> moves() {
            return toPlanets.entrySet().stream()
                    .map(e -> new Move().setMoveFrom(from).setMoveTo(e.getKey()).setArmySize(e.getValue()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StateIndices that = (StateIndices) o;

            if (tick != that.tick) return false;
            if (from != that.from) return false;
            if (units != that.units) return false;
            return toPlanets != null ? toPlanets.equals(that.toPlanets) : that.toPlanets == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (tick ^ (tick >>> 32));
            result = 31 * result + from;
            result = 31 * result + units;
            result = 31 * result + (toPlanets != null ? toPlanets.hashCode() : 0);
            return result;
        }
    }
}

