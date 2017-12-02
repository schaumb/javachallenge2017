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
                .filter(pl -> pl != origPlanet)
                .filter(pl -> !pl.isOurs() || pl.getOwnershipRatio() < 1.0)
                .sorted(Comparator.comparingDouble((PlanetState p) -> planetsWeight.computeIfAbsent(p, pl -> {

                    boolean isGoodForMe = Helper.planetMyWeightIsGood(gameState, pl, x);
                    double mulIfUncharted = !pl.hasOwner() ? 0.8 : 1.0;

                    double mulIfOur = pl.isOurs() ? 1.0: 1.0;


                    double weight = (isGoodForMe ? 1 : 10000000) * Helper.timeToMoveWithoutCeil(origPlanet.getAsPlanet(), pl.getAsPlanet()) * mulIfUncharted * mulIfOur;
                    System.err.println(pl.getPlanetID() + " is ours, good: " + isGoodForMe + "  weight: " + mulIfOur + " && " + weight);

                    return weight;
                })))
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

    public int getBiggestOurArmy(GameState gameState) {
        int biggestOurArmy = 0;
        for (PlanetState ps : gameState.getPlanetStates()) {
            for (Army army : ps.getStationedArmies()) {
                if (army.isOurs() && army.getSize() > biggestOurArmy) {
                    biggestOurArmy = army.getSize();
                }
            }
            for (Army army : ps.getMovingArmies()) {
                if (army.isOurs() && army.getSize() > biggestOurArmy) {
                    biggestOurArmy = army.getSize();
                }
            }
        }
        return biggestOurArmy;
    }

    public int getBiggestEnemyArmy(GameState gameState) {
        int biggestOurArmy = 0;
        for (PlanetState ps : gameState.getPlanetStates()) {
            for (Army army : ps.getStationedArmies()) {
                if (!army.isOurs() && army.getSize() > biggestOurArmy) {
                    biggestOurArmy = army.getSize();
                }
            }
            for (Army army : ps.getMovingArmies()) {
                if (!army.isOurs() && army.getSize() > biggestOurArmy) {
                    biggestOurArmy = army.getSize();
                }
            }
        }
        return biggestOurArmy;
    }

    public PlanetState getBiggestPlanet(GameState gameState) {
        PlanetState biggestPlanet = null;
        int biggestR = 0;
        for (PlanetState ps : gameState.getPlanetStates()) {
            int r = ps.getAsPlanet().getRadius();
            if (r > biggestR) {
                biggestR = r;
                biggestPlanet = ps;
            }
        }

        return biggestPlanet;
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

        int ourBiggestArmy = getBiggestOurArmy(gameState);
        int enemyBiggestArmy = getBiggestEnemyArmy(gameState);
        boolean weHaveBiggestArmy = ourBiggestArmy > enemyBiggestArmy;

        for (PlanetState ps : gameState.getPlanetStates()) {
            for (Army army : ps.getStationedArmies()) {
                if (!army.isOurs()) {
                    continue;
                }
                boolean shouldWeRun = !Helper.planetMyWeightIsGood(gameState, ps, 0);
                if (shouldWeRun) {
                    System.out.println("Should we run id:" + ps.getPlanetID() + " " + shouldWeRun);
                }
                if ((ps.getOwnershipRatio() >= 1.0 && ps.getStationedArmies().size() == 1) || shouldWeRun) {
                    if((ps.getOwnershipRatio() >= 1.0 && ps.getStationedArmies().size() == 1)) {
                        boolean shouldWeStay = false;

                        int diff1 = army.getSize() - ps.biggestEnemyArmySizeWhichArrives();
                        if(diff1 >= 0 && diff1 < 10) {
                            shouldWeStay = true;
                        }
                        int diff2 = army.getSize() - ps.biggestEnemyArmySizeWhichIsHere();
                        if(diff2 >= 0 && diff2 < 10) {
                            shouldWeStay = true;
                        }

                        if(shouldWeStay)
                            break;
                    }
                    if(shouldWeRun) {
                        System.err.println("SHOUD WE RUN TRUE");
                    }
                    if (weHaveBiggestArmy && ourBiggestArmy == army.getSize()) {
                        PlanetState biggestPlanet = getBiggestPlanet(gameState);
                        if (biggestPlanet.getOwnershipRatio() < 1.0) {
                            System.err.println("GoToBiggestPlanet");
                            new Move().setMoveFrom(ps.getPlanetID()).setMoveTo(biggestPlanet.getPlanetID()).setArmySize(army.getSize()).sendWithCheck(gameState, OUR_TEAM);
                            continue;
                        }
                    }
                    List<PlanetState> planets = getTargetPlanets(gameState, ps, army.getSize());
                    if (planets.size() == 0) {
                        System.out.println("No target planets :(");
                        continue;
                    }
                    if (army.getSize() < (shouldWeRun ? 5 : 10)) {
                        continue;
                    }
                    int splitTeams = 1;
                    if (army.getSize() > 99 && planets.size() > 1) {
                        splitTeams = 2;
                    }
                    if (army.getSize() > 199 && planets.size() > 2) {
                        splitTeams = 3;
                    }
                    for (int i=0; i<splitTeams; ++i) {
                        int armyToSend = army.getSize() / splitTeams;
                        if (!shouldWeRun && armyToSend > 50 && (armyToSend - ps.biggestEnemyArmySizeWhichArrives()) >= 20) {
                            armyToSend = armyToSend - ps.biggestEnemyArmySizeWhichArrives();
                        }
                        new Move().setMoveFrom(ps.getPlanetID()).setMoveTo(planets.get(i).getPlanetID()).setArmySize(armyToSend).sendWithCheck(gameState, OUR_TEAM);
                    }
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
