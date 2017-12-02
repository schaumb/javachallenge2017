package logic;

import jsons.Move;
import jsons.*;
import jsons.common.*;
import jsons.common.ArmyExtent;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import java.util.function.IntUnaryOperator;
import java.util.*;
import java.util.stream.Collectors;

public class BestOfEveryTimeLogic implements ILogic {

    IntUnaryOperator upper;
    boolean stop;
    @Override
    public void setGameDescription(GameDescription gameDescription) {
        // new Move().setArmySize(50).setMoveFrom(101).setMoveTo(105).send(OUR_TEAM);
        // new Move().setArmySize(50).setMoveFrom(102).setMoveTo(106).send(OUR_TEAM);
        stop = false;
        upper = null;
    }

    void init(GameState gameState) {

    }

    List<PlanetState> getTargetPlanets(GameState gameState, PlanetState origPlanet, int x) {
        Map<PlanetState, Double> planetsWeight = new HashMap<>();
        return gameState.getPlanetStates()
                .stream()
                .filter(p -> p != origPlanet)
                .sorted(Comparator.comparingDouble((PlanetState p) -> planetsWeight.computeIfAbsent(p, pl -> {

                    boolean b = Helper.planetMyWeightIsGood(gameState, pl);
                    System.err.println("From " + origPlanet.getPlanetID() + " to " + pl.getPlanetID() + " is good for us: " + b);
                    boolean b2 = !pl.isOurs() || pl.getOwnershipRatio() < 0.5;
                    System.err.println("Is ours: " + pl.isOurs() +  " or " + pl.getOwnershipRatio() + " < 0.5: " + b);

                    return (b && b2 ? -1 : -10000) * Helper.timeToMoveWithoutCeil(origPlanet.getAsPlanet(), pl.getAsPlanet());
                })).reversed())
                .collect(Collectors.toList());
    }

    ArrayList<PlanetState> getEmptyPlanets(GameState gameState, PlanetState origPlanet) {
        ArrayList<PlanetState> states = new ArrayList<>();
        for (PlanetState empty_ps : gameState.getPlanetStates()) {
            if (empty_ps.getStationedArmies().size() == 0) {
                states.add(empty_ps);
            }
        }
        Collections.sort(states, (lhs, rhs) -> (int)(Helper.timeToMoveWithoutCeil(
            lhs.getAsPlanet(), origPlanet.getAsPlanet()) - Helper.timeToMoveWithoutCeil(rhs.getAsPlanet(), origPlanet.getAsPlanet())));
        return states;
    }

    @Override
    public void setGameState(GameState gameState) {
        int tickElapsed = gameState.getTickElapsed();
        if (tickElapsed == 0) {
            init(gameState);
        }

        int splitSize = 10;
        if (tickElapsed == 0) {
            for (PlanetState ps : gameState.getPlanetStates()) {
                for (Army army : ps.getStationedArmies()) {
                    if (army.isOurs()) {
                        int armySize = army.getSize();
                        for (PlanetState empty_ps : getEmptyPlanets(gameState, ps)) {
                            if (empty_ps.getStationedArmies().size() == 0 && armySize >= splitSize) {
                                new Move().setMoveFrom(ps.getPlanetID()).setMoveTo(empty_ps.getPlanetID()).setArmySize(splitSize).sendWithCheck(gameState, OUR_TEAM);
                                armySize -= splitSize;
                            }
                        }
                    }
                }
            }
            return;
        }

        for (PlanetState ps : gameState.getPlanetStates()) {
            boolean shouldWeRun = !Helper.planetMyWeightIsGood(gameState, ps);
            for (Army army : ps.getStationedArmies()) {
                if (army.isOurs() && ((ps.getOwnershipRatio() >= 1.0 && ps.getStationedArmies().size() == 1) || shouldWeRun)) {
                    List<PlanetState> planets = getTargetPlanets(gameState, ps, army.getSize());
                    if (planets.size() == 0) {
                        System.out.println("No target planets :(");
                        continue;
                    }
                    if (army.getSize() < (shouldWeRun ? 5 : 10)) {
                        continue;
                    }
                    int armyToSend = army.getSize();
                    if (!shouldWeRun && army.getSize() > 50) {
                        armyToSend = army.getSize() - 5;
                    }
                    new Move().setMoveFrom(ps.getPlanetID()).setMoveTo(planets.get(0).getPlanetID()).setArmySize(armyToSend).sendWithCheck(gameState, OUR_TEAM);
                }
            }
        }
        /*
        GameDescription game = GameDescription.LATEST_INSTANCE;
        int tickElapsed = gameState.getTickElapsed();
        if(stop || (tickElapsed > 0 && upper == null)) {
            long lastTick = GameDescription.GAME_STARTED_MS + game.getGameLength() - (upper == null ? gameState.getTimeElapsed() : 0);
            long now = System.currentTimeMillis();
            if(now < lastTick) {
                System.err.println("Waiting for next round, ms: " + (lastTick - now));
                if(stop) {
                    try {
                        Thread.sleep(lastTick - now);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
        for (PlanetState planetState : gameState.getPlanetStates()) {
            for (Army army : planetState.getMovingArmies()) {
                if(!army.isOurs()) {
                    ArmyExtent armyExtent = gameState.getArmyExtent(planetState, army);
                    if(armyExtent.getFromTick() == tickElapsed - 1) {
                        System.err.println("Sent enemy army: " + armyExtent);
                    }
                }
            }
        }


        switch (tickElapsed) {
            case 0:
                upper = gameState.getPlanetState(101).getStationedArmies().get(0).isOurs() ? i -> i : i -> i % 2 == 0 ? i - 1 : i + 1;
                break;
            case 34:
                if(!new Move().setMoveFrom(upper.applyAsInt(105)).setMoveTo(upper.applyAsInt(108)).setArmySize(50).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 42:
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(107)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 69:
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(102)).setArmySize(10).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(104)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(106)).setArmySize(27).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 83:
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(107)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 84:
                if(!new Move().setMoveFrom(upper.applyAsInt(105)).setMoveTo(upper.applyAsInt(107)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(107)).setArmySize(8).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 114:
                if(!new Move().setMoveFrom(upper.applyAsInt(102)).setMoveTo(upper.applyAsInt(104)).setArmySize(8).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(106)).setMoveTo(upper.applyAsInt(104)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(106)).setMoveTo(upper.applyAsInt(107)).setArmySize(21).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 125:
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(107)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 133:
                if(!new Move().setMoveFrom(upper.applyAsInt(105)).setMoveTo(upper.applyAsInt(108)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 144:
                if(!new Move().setMoveFrom(upper.applyAsInt(104)).setMoveTo(upper.applyAsInt(102)).setArmySize(6).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(104)).setMoveTo(upper.applyAsInt(106)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 146:
                if(!new Move().setMoveFrom(upper.applyAsInt(107)).setMoveTo(upper.applyAsInt(106)).setArmySize(17).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(107)).setMoveTo(upper.applyAsInt(105)).setArmySize(16).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 166:
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(107)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 169:
                if(!new Move().setMoveFrom(upper.applyAsInt(102)).setMoveTo(upper.applyAsInt(110)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 171:
                if(!new Move().setMoveFrom(upper.applyAsInt(105)).setMoveTo(upper.applyAsInt(103)).setArmySize(15).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 197:
                if(!new Move().setMoveFrom(upper.applyAsInt(107)).setMoveTo(upper.applyAsInt(106)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(107)).setMoveTo(upper.applyAsInt(109)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 208:
                if(!new Move().setMoveFrom(upper.applyAsInt(105)).setMoveTo(upper.applyAsInt(103)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(109)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 234:
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(102)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(105)).setMoveTo(upper.applyAsInt(106)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 249:
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(109)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 269:
                if(!new Move().setMoveFrom(upper.applyAsInt(106)).setMoveTo(upper.applyAsInt(102)).setArmySize(10).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(102)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 289:
                if(!new Move().setMoveFrom(upper.applyAsInt(106)).setMoveTo(upper.applyAsInt(102)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            default:
                for (PlanetState planetState : gameState.getPlanetStates()) {
                    Army stationedArmy = planetState.getStationedArmy(OUR_TEAM);
                    if(stationedArmy == null || stationedArmy.getSize() < game.getMinMovableArmySize())
                        continue;

                    if(planetState.getOwnershipRatio() == 1.0 && planetState.isOurs() && planetState.getStationedArmies().size() == 1) {
                        System.err.println("Not moved at tick " + tickElapsed + " " + planetState);
                        System.err.println(gameState);
                        // stop = true;
                    }
                }

        }
    */

    }
}
