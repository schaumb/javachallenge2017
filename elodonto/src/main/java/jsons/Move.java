package jsons;

import com.google.gson.Gson;
import jsons.common.Positioned;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;
import wsimpl.ClientEndpoint;

public class Move {
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

    public void send() {
        ClientEndpoint.sender.accept(this);
    }

    private final static Gson gson = new Gson();

    public GameState sendAndRefreshGameState(GameState gameState) {
        GameState cp = gson.fromJson(gson.toJson(gameState), GameState.class);
        PlanetState planetStateFrom = cp.getPlanetState(moveFrom);
        PlanetState planetStateTo = cp.getPlanetState(moveTo);
        Army ourStationedArmy = planetStateFrom.getOurStationedArmy();
        int sentProbably = Math.min(ourStationedArmy.getSize(), armySize);

        if(ourStationedArmy.getSize() == sentProbably) {
            planetStateFrom.getStationedArmies().remove(ourStationedArmy);
        } else {
            ourStationedArmy.setSize(ourStationedArmy.getSize() - sentProbably);
        }
        Positioned<Double> doublePositioned = planetStateFrom.getAsPlanet().goesTo(planetStateTo.getAsPlanet(), 0.01);

        Army army = new Army();
        army.setOwner(ourStationedArmy.getOwner())
                .setSize(sentProbably)
                .setX(doublePositioned.getX())
                .setY(doublePositioned.getY());

        planetStateTo.getMovingArmies().add(army);

        return cp;
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
