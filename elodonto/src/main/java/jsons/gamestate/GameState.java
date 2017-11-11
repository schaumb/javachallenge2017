package jsons.gamestate;

import com.google.gson.Gson;
import jsons.Move;
import jsons.common.*;
import jsons.gamedesc.GameDescription;
import logic.ILogic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState {
    private static final Gson gson = new Gson();
    private List<PlanetState> planetStates;
    private List<PlayerState> standings;
    private GameStatus gameStatus;
    private int timeElapsed;
    private int remainingPlayers;

    public PlanetState getArmyPlanetState(Army army) {
        return getPlanetStates().stream()
                .filter(pss -> pss.getStationedArmies().stream().anyMatch(a -> a == army) ||
                        pss.getMovingArmies().stream().anyMatch(a -> a == army))
                .findAny().orElse(null);
    }

    public ArmyExtent getArmyExtent(Army army) {
        return new ArmyExtent(this, army);
    }

    public PlayerExtent getPlayerExtent(PlayerState state) {
        return new PlayerExtent(getTimeElapsed(), this, state);
    }

    public PlanetExtent getPlanetExtent(PlanetState state) {
        return new PlanetExtent(getTimeElapsed(), this, state);
    }

    public PlanetExtent getPlanetExtent(int id) {
        PlanetState planetState = getPlanetState(id);
        return planetState == null ? null : getPlanetExtent(planetState);
    }

    public Stream<PlanetExtent> getPlanetExtents() {
        return getPlanetStates().stream().map(this::getPlanetExtent);
    }

    private PlayerState getPlayerState(String id) {
        return getStandings().stream()
                .filter(p -> Objects.equals(p.getUserID(), id))
                .findAny().orElse(null);
    }

    public PlayerState getOurState() {
        return getPlayerState(ILogic.OUR_TEAM);
    }

    public Stream<PlanetExtent> getOurStationedArmiedExtentPlanetStates() {
        return getPlanetStates().stream()
                .filter(planetStates -> planetStates.getStationedArmies().stream().anyMatch(Army::isOurs))
                .map(this::getPlanetExtent);
    }

    public Stream<ArmyExtent> getOurMovingExtentArmy() {
        return getMovingExtentArmies()
                .filter(a -> a.getArmy().isOurs());
    }

    public Stream<ArmyExtent> getMovingExtentArmy(String owns) {
        return getMovingExtentArmies()
                .filter(a -> a.getArmy().isOwns(owns));
    }

    public Stream<ArmyExtent> getMovingExtentArmies() {
        return getPlanetStates().stream()
                .flatMap(s -> s.getMovingArmies().stream())
                .map(this::getArmyExtent);
    }

    public Stream<ArmyExtent> getEnemiesMovingExtentArmy() {
        return getMovingExtentArmies()
                .filter(a -> !a.getArmy().isOurs());
    }

    public PlanetState getPlanetState(int id) {
        return getPlanetStates().stream()
                .filter(p -> p.getPlanetID() == id)
                .findAny().orElse(null);
    }

    public List<PlanetState> getPlanetStates() {
        return planetStates;
    }

    public List<PlayerState> getStandings() {
        return standings;
    }

    public Stream<PlayerState> getEnemies() {
        return getStandings().stream().filter(e -> !e.getAsPlayer().isUs());
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public GameState setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        return this;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    private GameState setTimeElapsed(int timeElapsed) {
        this.timeElapsed = timeElapsed;
        return this;
    }

    public int getTickElapsed() {
        return Helper.timeToTick(timeElapsed);
    }

    public int getRemainingPlayers() {
        return remainingPlayers;
    }

    private GameState setRemainingPlayers(int remainingPlayers) {
        this.remainingPlayers = remainingPlayers;
        return this;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "planetStates=" + planetStates +
                ", standings=" + standings +
                ", gameStatus=" + gameStatus +
                ", timeElapsed=" + timeElapsed +
                ", remainingPlayers=" + remainingPlayers +
                '}';
    }

    public Stream<PlanetExtent> getStationedArmiedExtentPlanetStates(String who) {
        return getPlanetStates().stream()
                .filter(planetStates -> planetStates.getStationedArmies().stream().anyMatch(a -> Objects.equals(a.getOwner(), who)))
                .map(this::getPlanetExtent);
    }

    public int getAllArmy(String owner) {
        return getPlanetStates()
                .stream().flatMap(s -> Stream.concat(
                        s.getMovingArmies(owner),
                        s.getStationedArmy(owner) == null ? Stream.empty() : Stream.of(s.getStationedArmy(owner))
                )).mapToInt(Army::getSize).sum();
    }
    private static final HashMap<Integer, HashMap<String, Stream<Move>>> delayedMoves = new HashMap<>();

    public GameState setDelayedMove(Stream<Move> moves, String owner, int plusTick) {
        delayedMoves.computeIfAbsent(getTickElapsed() + plusTick, i -> new HashMap<>())
                .compute(owner, (o, prev) -> {
                    if(prev == null)
                        return moves;

                    return Stream.concat(prev, moves);
                });
        return this;
    }

    public GameState setMove(Stream<Move> moves, String owner) {
        GameDescription game = GameDescription.LATEST_INSTANCE;
        moves.forEach(move -> {
            if (move.getMoveFrom() == move.getMoveTo())
                return;

            PlanetState planetStateFrom = getPlanetState(move.getMoveFrom());
            PlanetState planetStateTo = getPlanetState(move.getMoveTo());
            Army ourStationedArmy = planetStateFrom.getStationedArmy(owner);
            if (ourStationedArmy == null)
                return;

            int sentProbably = Math.min(ourStationedArmy.getSize(), move.getArmySize());

            ourStationedArmy.setSize(ourStationedArmy.getRealSize() - sentProbably);

            double time = Helper.timeToMoveWithoutCeil(planetStateFrom.getAsPlanet(), planetStateTo.getAsPlanet());
            Positioned<Double> doublePositioned = planetStateFrom.getAsPlanet().goesTo(planetStateTo.getAsPlanet(),
                    -(game.getBroadcastSchedule() - game.getInternalSchedule()) / time);

            Army army = new Army();
            army.setOwner(ourStationedArmy.getOwner())
                    .setSize(sentProbably)
                    .setX(doublePositioned.getX())
                    .setY(doublePositioned.getY());

            planetStateTo.getMovingArmies().add(army);
        });

        return this;
    }

    public GameState copy() {
        return gson.fromJson(gson.toJson(this), GameState.class);
    }

    public GameState setAfterTime(long afterTime) {
        int fromTick = getTickElapsed();

        int deltaTick = Helper.timeToTick(afterTime);

        if (Helper.tickToTime(deltaTick) < afterTime) {
            ++deltaTick;
        }

        int toTick = fromTick + deltaTick;

        GameState cp = copy();
        for (int currentTick = fromTick; currentTick < toTick; ++currentTick) {
            cp.setToNextState();
        }

        return cp;
    }

    public GameState setToNextState() {
        GameDescription game = GameDescription.LATEST_INSTANCE;
        int deltatime = (int) Helper.tickToTime(1);
        setTimeElapsed(getTimeElapsed() + deltatime);

        delayedMoves.getOrDefault(getTickElapsed(), new HashMap<>())
                .forEach((k ,v) -> {
                    setMove(v, k);
                });

        // mozgó seregek
        List<ArmyExtent> armyExtentList = getMovingExtentArmies().collect(Collectors.toList());
        armyExtentList.forEach(a -> {
            Army army = a.getArmy();
            if (a.getToTick() == getTickElapsed() + 1) {
                // megérkeztetés
                PlanetState planetState = getPlanetState(a.getToPlanet());
                planetState.getMovingArmies().remove(army);

                Army stationedArmy = planetState.getStationedArmy(army.getOwner());
                if (stationedArmy == null) {
                    stationedArmy = new Army().setOwner(army.getOwner());
                    planetState.getStationedArmies().add(stationedArmy);
                }
                stationedArmy.setSize(stationedArmy.getRealSize() + army.getRealSize());
            } else {
                Positioned<Double> positionAfterTime = Helper.getPositionAfterTime(army, game.getPlanet(a.getToPlanet()), deltatime);
                army.setX(positionAfterTime.getX())
                        .setY(positionAfterTime.getY());
            }
        });

        getPlanetExtents().forEach(p -> {
            PlanetState planetState = p.getPlanetState();
            int radius = planetState.getAsPlanet().getRadius();
            List<Army> stationedArmies = planetState.getStationedArmies();
            switch (p.getState()) {
                case BATTLE:
                    Map<String, Double> stringNumberMap =
                            Helper.killAtTime(stationedArmies.stream().collect(Collectors.toMap(Army::getOwner, Army::getRealSize)), deltatime);

                    stationedArmies.removeIf(a -> stringNumberMap.get(a.getOwner()).intValue() == 0);
                    stationedArmies.forEach(a -> a.setSize(stringNumberMap.get(a.getOwner()).doubleValue()));
                    break;
                case CAPTURE:
                    Army army = stationedArmies.get(0);
                    String owner = army.getOwner();

                    double amount = Helper.capturingWhileTime(radius, army.getSize(), deltatime);
                    double ownershipRatio = planetState.getOwnershipRatio();
                    if (planetState.isOwns(owner)) {
                        planetState.setOwnershipRatio(Math.min(1, ownershipRatio + amount));
                    } else if (amount > ownershipRatio) {
                        planetState.setOwner(owner);
                        planetState.setOwnershipRatio(amount - ownershipRatio);
                    } else {
                        planetState.setOwnershipRatio(ownershipRatio - amount);
                    }

                    break;
                case UNIT_CREATE:
                    if (stationedArmies.size() == 0) {
                        stationedArmies.add(new Army().setOwner(planetState.getOwner()));
                    }
                    Army armyC = stationedArmies.get(0);

                    armyC.setSize(armyC.getRealSize() + Helper.creatingArmyWhileTime(radius, deltatime));
                    break;
            }
        });

        // standings
        getStandings().forEach(o -> o.setStrength(getAllArmy(o.getUserID()) +
                (int) getPlanetStates().stream().mapToDouble(p ->
                        Helper.planetWeight(p.getAsPlanet().getRadius(),
                                p.isOwns(o.getUserID()),
                                p.getOwnershipRatio())).sum()));

        setRemainingPlayers((int) getStandings().stream().filter(p -> p.getStrength() > 0).count());

        if (getRemainingPlayers() == 1 || getTimeElapsed() >= game.getGameLength()) {
            setGameStatus(GameStatus.ENDED);
        }

        return this;
    }
}
