package logic;

import jsons.Move;
import jsons.common.ArmyExtent;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import java.util.function.IntUnaryOperator;

public class BestOfEveryTimeLogic implements ILogic {

    IntUnaryOperator upper;
    boolean stop;
    @Override
    public void setGameDescription(GameDescription gameDescription) {
        new Move().setArmySize(50).setMoveFrom(101).setMoveTo(105).send(OUR_TEAM);
        new Move().setArmySize(50).setMoveFrom(102).setMoveTo(106).send(OUR_TEAM);
        stop = false;
        upper = null;
    }

    @Override
    public void setGameState(GameState gameState) {
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
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(106)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 234:
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(106)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(105)).setMoveTo(upper.applyAsInt(109)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 249:
                if(!new Move().setMoveFrom(upper.applyAsInt(101)).setMoveTo(upper.applyAsInt(109)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                break;
            case 269:
                if(!new Move().setMoveFrom(upper.applyAsInt(106)).setMoveTo(upper.applyAsInt(102)).setArmySize(10).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
                if(!new Move().setMoveFrom(upper.applyAsInt(108)).setMoveTo(upper.applyAsInt(102)).setArmySize(5).sendWithCheck(gameState, OUR_TEAM)) { System.err.println("IN TICK" + tickElapsed); }
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

    }
}
