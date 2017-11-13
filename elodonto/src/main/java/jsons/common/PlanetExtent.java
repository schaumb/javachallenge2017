package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

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

    public long getInterruptionTime() {
        long interrupt = GameDescription.LATEST_INSTANCE.getGameLength();
        for (Army army : planetState.getMovingArmies()) {
            long armyToTime = gameState.getArmyExtent(planetState, army).getToTime();
            if (armyToTime < interrupt)
                interrupt = armyToTime;
        }
        return interrupt;
    }

    public long getInterruptionTick() {
        return GameDescription.LATEST_INSTANCE.getTickFromTime(getInterruptionTime());
    }

    public enum CurrentState {
        EMPTY,
        EMPTY_WITH_NOT_WHOLE_OWNER,
        BATTLE,
        CAPTURE,
        UNIT_CREATE
    }
}
