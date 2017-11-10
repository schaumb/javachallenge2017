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
    private final CurrentState state;
    private final long endWithoutInterruption;
    private final long interruptionTime;

    public PlanetExtent(long time, GameState gameState, PlanetState planetState) {
        this.planetState = planetState;

        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        CurrentState state;
        long endWithoutInterruption = gameDescription.getGameLength();
        long interruptionTime = planetState.getMovingArmies().stream()
                .map(gameState::getArmyExtent)
                .mapToLong(ArmyExtent::getToTime)
                .min().orElse(gameDescription.getGameLength());

        if (planetState.getStationedArmies().size() == 0) {
            state = planetState.hasOwner() ? CurrentState.EMPTY_WITH_OWNER : CurrentState.EMPTY;
        } else if (planetState.getStationedArmies().size() > 1) {
            state = CurrentState.BATTLE;

            endWithoutInterruption = Helper.timeToKillSomeone(planetState.getStationedArmies().stream().map(Army::getSize).collect(Collectors.toList()));

        } else if (planetState.getStationedArmies().size() == 1 &&
                planetState.isOwns(planetState.getStationedArmies().get(0).getOwner()) &&
                planetState.getOwnershipRatio() == 1.0) {
            state = CurrentState.UNIT_CREATE;
        } else {
            state = CurrentState.CAPTURE;

            endWithoutInterruption = Helper.timeToCapture(planetState.getAsPlanet().getRadius(),
                    planetState.getStationedArmies().get(0).getSize(), planetState.isOwns(planetState.getStationedArmies().get(0).getOwner()),
                    planetState.getOwnershipRatio());
        }

        this.state = state;
        this.endWithoutInterruption = endWithoutInterruption;
        this.interruptionTime = interruptionTime;
        this.time = time;
    }

    public PlanetState getPlanetState() {
        return planetState;
    }

    public CurrentState getState() {
        return state;
    }

    public long getEndWithoutInterruption() {
        return endWithoutInterruption;
    }

    public long getEndTickWithoutInterruption() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(endWithoutInterruption);
    }

    public long getInterruptionTime() {
        return interruptionTime;
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

    public Map<String, Number> getBattleStateAt(long time) {
        return Helper.killAtTime(planetState.getStationedArmies().stream().collect(Collectors.toMap(
                Army::getOwner,
                Army::getSize
        )), time);
    }

    public long getTimeWhenCreatedArmiesWithoutInterrupt(int armySize) {
        return Helper.timeToCreateArmy(getPlanetState().getAsPlanet().getRadius(), armySize);
    }

    public long getTickWhenCreatedArmiesWithoutInterrupt(int armySize) {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getTimeWhenCreatedArmiesWithoutInterrupt(armySize));
    }

    public int getArmiesCountAtTimeWithoutInterrupt(long atTime) {
        return getPlanetState().getStationedArmies().get(0).getSize() +
                Helper.creatingArmyWhileTime(getPlanetState().getAsPlanet().getRadius(), atTime - time);
    }

    public enum CurrentState {
        EMPTY,
        EMPTY_WITH_OWNER,
        BATTLE,
        CAPTURE,
        UNIT_CREATE
    }
}
