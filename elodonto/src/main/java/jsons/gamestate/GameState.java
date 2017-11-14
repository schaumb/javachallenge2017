package jsons.gamestate;

import jsons.Move;
import jsons.common.*;
import jsons.gamedesc.GameDescription;
import logic.ILogic;
import wsimpl.ClientEndpoint;
import wsimpl.Main;

import java.util.*;

public class GameState {
    private static final HashMap<Integer, HashMap<String, List<Move>>> delayedMoves = new HashMap<>();
    private List<PlanetState> planetStates;
    private List<PlayerState> standings;
    private GameStatus gameStatus;
    private int timeElapsed;
    private int remainingPlayers;

    public GameState() {
    }

    public GameState(GameState gameState) {
        this.gameStatus = gameState.gameStatus;
        this.timeElapsed = gameState.timeElapsed;
        this.remainingPlayers = gameState.remainingPlayers;
        this.planetStates = new ArrayList<>(gameState.planetStates.size());

        for (PlanetState planetState : gameState.planetStates) {
            this.planetStates.add(new PlanetState(planetState));
        }

        this.standings = new ArrayList<>(gameState.standings.size());

        for (PlayerState standing : gameState.standings) {
            this.standings.add(new PlayerState(standing));
        }
    }

    public ArmyExtent getArmyExtent(PlanetState state, Army army) {
        return new ArmyExtent(this, state, army);
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

    private PlayerState getPlayerState(String id) {
        for (PlayerState playerState : getStandings()) {
            if (Objects.equals(playerState.getUserID(), id))
                return playerState;
        }
        return null;
    }

    public PlayerState getOurState() {
        return getPlayerState(ILogic.OUR_TEAM);
    }

    public PlanetState getPlanetState(int id) {
        for (PlanetState planetState : getPlanetStates()) {
            if (planetState.getPlanetID() == id)
                return planetState;
        }
        return null;
    }

    public List<PlanetState> getPlanetStates() {
        return planetStates;
    }

    public List<PlayerState> getStandings() {
        return standings;
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

    public int getAllArmy(String owner) {
        int sum = 0;
        for (PlanetState planetState : getPlanetStates()) {
            for (Army army : planetState.getMovingArmies()) {
                if (army.isOwns(owner))
                    sum += army.getSize();
            }

            for (Army army : planetState.getStationedArmies()) {
                if (army.isOwns(owner))
                    sum += army.getSize();
            }
        }
        return sum;
    }

    public GameState setDelayedMove(List<Move> moves, String owner, int plusTick) {
        if (Main.sender instanceof ClientEndpoint) {
            System.err.println("Can not set delayed move at real running env");
            return this;
        }

        delayedMoves.computeIfAbsent(getTickElapsed() + plusTick, i -> new HashMap<>())
                .compute(owner, (o, prev) -> {
                    if (prev == null)
                        return moves;

                    ArrayList<Move> moves1 = new ArrayList<>(prev);
                    moves1.addAll(moves);
                    return moves1;
                });
        return this;
    }

    public GameState setMove(String owner, List<Move> moves) {
        GameDescription game = GameDescription.LATEST_INSTANCE;
        for (Move move : moves) {
            if (move.getMoveFrom() == move.getMoveTo())
                continue;

            PlanetState planetStateFrom = getPlanetState(move.getMoveFrom());
            PlanetState planetStateTo = getPlanetState(move.getMoveTo());
            Army ourStationedArmy = planetStateFrom.getStationedArmy(owner);
            if (ourStationedArmy == null)
                continue;

            ourStationedArmy.setSize(ourStationedArmy.getRealSize() - move.getArmySize());

            double time = Helper.timeToMoveWithoutCeil(planetStateFrom.getAsPlanet(), planetStateTo.getAsPlanet());
            Positioned<Double> doublePositioned = planetStateFrom.getAsPlanet().goesTo(planetStateTo.getAsPlanet(),
                    -(game.getBroadcastSchedule() - game.getInternalSchedule()) / time);

            Army army = new Army();
            army.setOwner(ourStationedArmy.getOwner())
                    .setSize(move.getArmySize())
                    .setX(doublePositioned.getX())
                    .setY(doublePositioned.getY());

            planetStateTo.getMovingArmies().add(army);
        }

        return this;
    }

    public GameState copy() {
        return new GameState(this);
    }

    public GameState setWhileFirstArrives() {
        long time = Helper.tickToTime((int) getInterruptionTickOrElse(GameDescription.LATEST_INSTANCE.getGameLengthInTick())) - getTimeElapsed();
        return setAfterTime(time);
    }

    public void setWhileNotMoves() {
        int fromTick = getTickElapsed();

        // no moves
        long prevTick = fromTick;
        long currentTick;
        while((currentTick = getInterruptionTickOrElse(Integer.MAX_VALUE)) != Integer.MAX_VALUE) {
            setToNextState((int) (currentTick - prevTick));
            prevTick = currentTick;
        }
    }

    public GameState setAfterTime(long afterTime) {
        int fromTick = getTickElapsed();

        int deltaTick = Helper.timeToTick(afterTime);

        if (Helper.tickToTime(deltaTick) < afterTime) {
            ++deltaTick;
        }

        int toTick = fromTick + deltaTick;

        long prevTick = fromTick;
        long currentTick;
        while ((currentTick = getInterruptionTickOrElse(toTick)) < toTick) {
            setToNextState((int) (currentTick - prevTick));
            prevTick = currentTick;
        }

        setToNextState(deltaTick);

        setStrengthAndGameStatus();
        return this;
    }

    private void setStrengthAndGameStatus() {
        int remainingPlayers = 0;
        HashMap<String, Double> strength = new HashMap<>();
        for (PlayerState playerState : getStandings()) {
            strength.put(playerState.getUserID(), (double) getAllArmy(playerState.getUserID()));
        }

        for (PlanetState planetState : getPlanetStates()) {
            if (planetState.hasOwner()) {
                strength.compute(planetState.getOwner(), (k, v) ->
                        v + Helper.planetWeight(planetState.getAsPlanet().getRadius(),
                                true, planetState.getOwnershipRatio())
                );
            }
        }

        for (PlayerState playerState : getStandings()) {
            playerState.setStrength(strength.get(playerState.getUserID()).intValue());
            if (playerState.getStrength() > 0)
                ++remainingPlayers;
        }

        setRemainingPlayers(remainingPlayers);

        if (getRemainingPlayers() == 1 || getTimeElapsed() >= GameDescription.LATEST_INSTANCE.getGameLength()) {
            setGameStatus(GameStatus.ENDED);
        }
    }

    private long getInterruptionTickOrElse(int toTick) {
        long min = toTick;
        for (PlanetState planetState : getPlanetStates()) {
            long interruptionTick = getPlanetExtent(planetState).getInterruptionTick();
            if (interruptionTick < min)
                min = interruptionTick;
        }
        return min;
    }

    private GameState setToNextState(int deltaTick) {
        int deltaTime = (int) Helper.tickToTime(deltaTick);
        setTimeElapsed(getTimeElapsed() + deltaTime);

        HashMap<String, List<Move>> delayedMoves = GameState.delayedMoves.getOrDefault(getTickElapsed(), new HashMap<>());
        delayedMoves.forEach(this::setMove);

        moveArmies(deltaTime);
        calculatePlanetStates(deltaTime);
        return this;
    }

    private void moveArmies(int deltaTime) {
        // mozgó seregek
        for (PlanetState planetState : getPlanetStates()) {
            Iterator<Army> iterator = planetState.getMovingArmies().iterator();
            while (iterator.hasNext()) {
                Army army = iterator.next();
                ArmyExtent armyExtent = getArmyExtent(planetState, army);
                int toTime = getTimeElapsed() + deltaTime;
                if (armyExtent.getToTime() <= toTime) {
                    if (armyExtent.getToTime() < toTime) {
                        System.err.println("BAD DELTA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + armyExtent.getToTime() + " " + toTime);
                    }
                    // megérkeztetés

                    Army stationedArmy = planetState.getStationedArmy(army.getOwner());
                    if (stationedArmy == null) {
                        stationedArmy = new Army().setOwner(army.getOwner());
                        planetState.getStationedArmies().add(stationedArmy);
                    }
                    stationedArmy.setSize(stationedArmy.getRealSize() + army.getRealSize());
                    iterator.remove();
                } else {
                    Positioned<Double> positionAfterTime = Helper.getPositionAfterTime(army, planetState.getAsPlanet(), deltaTime);
                    army.setX(positionAfterTime.getX())
                            .setY(positionAfterTime.getY());
                }
            }
        }
    }

    private void calculatePlanetStates(int deltaTime) {
        for (PlanetState planetState : getPlanetStates()) {
            int radius = planetState.getAsPlanet().getRadius();
            List<Army> stationedArmies = planetState.getStationedArmies();
            switch (getPlanetExtent(planetState).getState()) {
                case BATTLE:
                    Map<String, Double> stringNumberMap =
                            Helper.killAtTime(stationedArmies, deltaTime);

                    stationedArmies.removeIf(a -> stringNumberMap.get(a.getOwner()).intValue() == 0);
                    stationedArmies.forEach(a -> a.setSize(stringNumberMap.get(a.getOwner()).doubleValue()));
                    break;
                case CAPTURE:
                    Army army = stationedArmies.get(0);
                    String owner = army.getOwner();

                    double amount = Helper.capturingWhileTime(radius, army.getSize(), deltaTime);
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

                    armyC.setSize(armyC.getRealSize() + Helper.creatingArmyWhileTime(radius, deltaTime));
                    break;
            }
        }
    }
}
