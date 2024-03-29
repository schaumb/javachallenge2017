package jsons;

import com.google.gson.Gson;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import wsimpl.Main;

public class Move {
    private final static Gson gson = new Gson();
    private int moveFrom;
    private int moveTo;
    private int armySize;

    public int getMoveFrom() {
        return moveFrom;
    }

    public Move setMoveFrom(int moveFrom) {
        this.moveFrom = moveFrom;
        return this;
    }

    public int getMoveTo() {
        return moveTo;
    }

    public Move setMoveTo(int moveTo) {
        this.moveTo = moveTo;
        return this;
    }

    public int getArmySize() {
        return armySize;
    }

    public Move setArmySize(int armySize) {
        this.armySize = armySize;
        return this;
    }

    public void send(String who) {
        Main.sender.accept(this, who);
    }

    public boolean sendWithCheck(GameState state, String who) {
        boolean success = false;
        Army stationedArmy = state.getPlanetState(moveFrom).getStationedArmy(who);
        if(stationedArmy == null) {
            System.err.println("NO STATIONED ARMY AT CHECKED MOVE");
        } else if(stationedArmy.getSize() < armySize) {
            System.err.println("NOT ENOUGH STATIONED ARMY AT CHECKED MOVE " + armySize + " but was: " + stationedArmy.getSize());
        } else {
            success = true;
        }
        Main.sender.accept(this, who);
        return success;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (moveFrom != move.moveFrom) return false;
        if (moveTo != move.moveTo) return false;
        return armySize == move.armySize;
    }

    @Override
    public int hashCode() {
        int result = moveFrom;
        result = 31 * result + moveTo;
        result = 31 * result + armySize;
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "moveFrom=" + moveFrom +
                ", moveTo=" + moveTo +
                ", armySize=" + armySize +
                '}';
    }
}
