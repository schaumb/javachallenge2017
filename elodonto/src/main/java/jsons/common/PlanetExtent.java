package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import java.util.Map;
import java.util.stream.Collectors;

public class PlanetExtent {
    private final long time;
    private final PlanetState planetState;
    private final GameState gameState;
    private final CurrentState state;

    public PlanetExtent(long time, GameState gameState, PlanetState planetState) {
        this.planetState = planetState;
        this.gameState = gameState;
        CurrentState state;

        if (planetState.getStationedArmies().size() > 1) {
            state = CurrentState.BATTLE;
        } else if ((planetState.getStationedArmies().size() == 0 || planetState.isOwns(planetState.getStationedArmies().get(0).getOwner())) &&
                planetState.hasOwner() &&
                planetState.getOwnershipRatio() == 1.0) {
            state = CurrentState.UNIT_CREATE;
        } else if (planetState.getStationedArmies().size() == 1) {
            state = CurrentState.CAPTURE;
        } else {
            state = planetState.hasOwner() ? CurrentState.EMPTY_WITH_NOT_WHOLE_OWNER : CurrentState.EMPTY;
        }

        this.state = state;
        this.time = time;
    }

    public PlanetState getPlanetState() {
        return planetState;
    }

    public CurrentState getState() {
        return state;
    }

    public long getEndWithoutInterruption() {
        switch (state) {
            case BATTLE:
                return Helper.timeToKillSomeone(planetState.getStationedArmies().stream().map(Army::getSize).collect(Collectors.toList()));
            case CAPTURE:
                return Helper.timeToCapture(planetState.getAsPlanet().getRadius(),
                        planetState.getStationedArmies().get(0).getSize(), planetState.isOwns(planetState.getStationedArmies().get(0).getOwner()),
                        planetState.getOwnershipRatio());
        }
        return GameDescription.LATEST_INSTANCE.getGameLength();
    }

    public long getEndTickWithoutInterruption() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getEndWithoutInterruption());
    }

    public long getInterruptionTime() {
        long interrupt = GameDescription.LATEST_INSTANCE.getGameLength();
        for (Army army : planetState.getMovingArmies()) {
            long armyToTime = gameState.getArmyExtent(army).getToTime();
            if (armyToTime < interrupt)
                interrupt = armyToTime;
        }
        return interrupt;
    }

    public long getInterruptionTick() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getInterruptionTime());
    }

    public long getStationedArmiesSizeWithoutInterruptAtTime(String who, long time) {
        switch (getState()) {
            case BATTLE:
                return getBattleStateAt(time).get(who).intValue();
            case CAPTURE:
                return getPlanetState().getStationedArmies().get(0).isOwns(who) ?
                        getPlanetState().getStationedArmies().get(0).getSize() : 0;
            case UNIT_CREATE:
                return getPlanetState().getStationedArmies().get(0).isOwns(who) ?
                        getArmiesCountAtTimeWithoutInterrupt(time) : 0;
            default:
                return 0;
        }
    }

    public Map<String, Double> getBattleStateAt(long time) {
        return Helper.killAtTime(planetState.getStationedArmies().stream().collect(Collectors.toMap(
                Army::getOwner,
                Army::getRealSize
        )), time);
    }

    public long getTimeWhenCreatedArmiesWithoutInterrupt(int armySize) {
        return Helper.timeToCreateArmy(getPlanetState().getAsPlanet().getRadius(), armySize);
    }

    public long getTickWhenCreatedArmiesWithoutInterrupt(int armySize) {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getTimeWhenCreatedArmiesWithoutInterrupt(armySize));
    }

    public int getArmiesCountAtTimeWithoutInterrupt(long atTime) {
        return (int) (getPlanetState().getStationedArmies().get(0).getRealSize() +
                Helper.creatingArmyWhileTime(getPlanetState().getAsPlanet().getRadius(), atTime - time));
    }

    public enum CurrentState {
        EMPTY,
        EMPTY_WITH_NOT_WHOLE_OWNER,
        BATTLE,
        CAPTURE,
        UNIT_CREATE
    }
}
