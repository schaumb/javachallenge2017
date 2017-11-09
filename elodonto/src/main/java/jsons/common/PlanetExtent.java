package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlanetExtent {
    private final PlanetState planetState;
    private final CurrentState state;
    private final long endWithoutInterruption;
    private final long interruptionTime;

    public PlanetExtent(GameDescription gameDescription, GameState gameState, PlanetState planetState) {
        this.planetState = planetState;

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

            List<Number> collect = planetState.getStationedArmies().stream().map(Army::getSize).collect(Collectors.toList());

            endWithoutInterruption = gameDescription.getCurrentTime();
            while (collect.stream().filter(n -> n.intValue() > 0).count() > 1) {
                endWithoutInterruption += gameDescription.getInternalSchedule();

                double sum = collect.stream().mapToDouble(Number::doubleValue).sum();
                List<Number> finalCollect = collect;
                collect = IntStream.range(0, collect.size()).mapToObj(i ->
                        finalCollect.get(i).doubleValue() - gameDescription.getBattleSpeed() *
                                Math.pow(IntStream.range(0, finalCollect.size()).filter(j -> j != i).mapToDouble(j -> finalCollect.get(j).doubleValue()).sum(),
                                        gameDescription.getBattleExponent())
                                / sum / 1000 * gameDescription.getInternalSchedule())
                        .collect(Collectors.toList());
            }
        } else if (planetState.getStationedArmies().size() == 1 &&
                planetState.isOwns(planetState.getStationedArmies().get(0).getOwner()) &&
                planetState.getOwnershipRatio() == 1.0) {
            state = CurrentState.UNIT_CREATE;
        } else {
            state = CurrentState.CAPTURE;

            endWithoutInterruption = gameDescription.getCurrentTime();
            double ownershipRatio = planetState.getOwnershipRatio();
            boolean owns = planetState.isOwns(planetState.getStationedArmies().get(0).getOwner());
            while (ownershipRatio < 1.0 || !owns) {
                endWithoutInterruption += gameDescription.getInternalSchedule();

                double grow = planetState.getStationedArmies().get(0).getSize()
                        * gameDescription.getCaptureSpeed()
                        / Math.pow(planetState.getAsPlanet().getRadius(), gameDescription.getPlanetExponent())
                        / 1000 * gameDescription.getInternalSchedule();
                if (owns)
                    ownershipRatio += grow;
                else if (ownershipRatio <= grow) {
                    ownershipRatio = grow - ownershipRatio;
                    owns = true;
                } else {
                    ownershipRatio -= grow;
                }
            }
        }


        this.state = state;
        this.endWithoutInterruption = endWithoutInterruption;
        this.interruptionTime = interruptionTime;
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

    public long getInterruptionTime() {
        return interruptionTime;
    }

    public long getTimeWhenCreatedArmiesWithoutInterrupt(int armySize) {
        if (getState() != CurrentState.UNIT_CREATE)
            return -1;

        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        double currentSize = getPlanetState().getStationedArmies().get(0).getSize();
        long currentTime = gameDescription.getCurrentTime();
        while (currentSize < armySize) {
            currentTime += gameDescription.getInternalSchedule();
            currentSize += Math.pow(getPlanetState().getAsPlanet().getRadius(), gameDescription.getPlanetExponent()) *
                    gameDescription.getUnitCreateSpeed() * gameDescription.getInternalSchedule() / 1000;
        }
        return currentTime;
    }

    public int getCreatedArmiesCountAtTimeWithoutInterrupt(long atTime) {
        if (getState() != CurrentState.UNIT_CREATE)
            return -1;

        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        double currentSize = getPlanetState().getStationedArmies().get(0).getSize();
        long currentTime = gameDescription.getCurrentTime();
        while (currentTime < atTime) {
            currentTime += gameDescription.getInternalSchedule();
            currentSize += Math.pow(getPlanetState().getAsPlanet().getRadius(), gameDescription.getPlanetExponent()) *
                    gameDescription.getUnitCreateSpeed() * gameDescription.getInternalSchedule() / 1000;
        }
        return (int) currentSize;
    }

    public enum CurrentState {
        EMPTY,
        EMPTY_WITH_OWNER,
        BATTLE,
        CAPTURE,
        UNIT_CREATE
    }
}
