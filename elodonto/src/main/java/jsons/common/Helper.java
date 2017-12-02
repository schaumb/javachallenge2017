package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;
import logic.ILogic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helper {
    public static <T extends Number, U extends Number> double timeToMoveWithoutCeil(Positioned<T> from, Positioned<U> to) {
        return from.distance(to) / GameDescription.LATEST_INSTANCE.getMovementSpeed() * 1000;
    }

    public static <T extends Number, U extends Number> Positioned<Double> getPositionAfterTime(Positioned<T> now, Positioned<U> to, long time) {
        return new Positioned<>(
                now.getX().doubleValue() + (to.getX().doubleValue() - now.getX().doubleValue()) * time / timeToMoveWithoutCeil(now, to),
                now.getY().doubleValue() + (to.getY().doubleValue() - now.getY().doubleValue()) * time / timeToMoveWithoutCeil(now, to)
        );

    }

    public static long timeToCapture(int radius, int armySize, boolean owns, double amount) {
        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        amount = owns ? 1 - amount : 1 + amount;

        return (long) (amount / (armySize * gameDescription.getCaptureSpeed() / Math.pow(radius, gameDescription.getPlanetExponent()) / 1000));
    }

    public static double capturingWhileTime(int radius, int armySize, long time) {
        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;

        return armySize * gameDescription.getCaptureSpeed() / Math.pow(radius, gameDescription.getPlanetExponent()) / 1000 * time;
    }

    public static long timeToCreateArmy(int radius, int armySize) {
        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        return (long) (armySize / (Math.pow(radius, gameDescription.getPlanetExponent()) * gameDescription.getUnitCreateSpeed() / 1000));
    }

    public static double creatingArmyWhileTime(int radius, long time) {
        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        return Math.pow(radius, gameDescription.getPlanetExponent()) * gameDescription.getUnitCreateSpeed() / 1000 * time;
    }

    public static long timeToKillSomeone(List<Army> armies) {
        Map<String, Double> collect = new HashMap<>();
        int hasUnit = 0;
        double sum = 0;
        for (Army army : armies) {
            collect.put(army.getOwner(), army.getRealSize());
            if (army.getSize() > 0)
                ++hasUnit;
            sum += army.getRealSize();
        }

        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        long currentTime = 0;
        while (hasUnit > 1) {
            currentTime += gameDescription.getInternalSchedule();
            double prevSum = sum;
            sum = 0;
            hasUnit = 0;
            for (Map.Entry<String, Double> e : collect.entrySet()) {
                if(e.getValue().intValue() == 0)
                    continue;

                double next = e.getValue() - gameDescription.getBattleSpeed() *
                        Math.pow(prevSum - e.getValue(), gameDescription.getBattleExponent())
                        / prevSum / 1000 * gameDescription.getInternalSchedule();

                if((int)next == 0) {
                    e.setValue(0.0);

                } else {
                    e.setValue(next);
                    sum += next;
                    ++hasUnit;
                }
            }
        }
        return currentTime;
    }

    public static Map<String, Double> killAtTime(List<Army> armies, long time) {
        Map<String, Double> collect = new HashMap<>();
        int hasUnit = 0;
        double sum = 0;
        for (Army army : armies) {
            collect.put(army.getOwner(), army.getRealSize());
            if (army.getSize() > 0)
                ++hasUnit;
            sum += army.getRealSize();
        }


        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        long currentTime = 0;
        while (hasUnit > 1 && currentTime < time) {
            currentTime += gameDescription.getInternalSchedule();
            double prevSum = sum;
            sum = 0;
            hasUnit = 0;
            for (Map.Entry<String, Double> e : collect.entrySet()) {
                double next = e.getValue() - gameDescription.getBattleSpeed() *
                        Math.pow(prevSum - e.getValue(), gameDescription.getBattleExponent())
                        / prevSum / 1000 * gameDescription.getInternalSchedule();
                e.setValue(next);
                sum += next;
                if ((int) next > 0)
                    ++hasUnit;
            }
        }
        return collect;
    }

    public static int timeToTick(long time) {
        return (int) time / GameDescription.LATEST_INSTANCE.getBroadcastSchedule();
    }

    public static long tickToTime(int tick) {
        return tick * GameDescription.LATEST_INSTANCE.getBroadcastSchedule();
    }

    public static double planetWeight(int radius, boolean owns, double amount) {
        return (owns ? amount : 0) * Math.pow(radius, GameDescription.LATEST_INSTANCE.getPlanetExponent());
    }


    public static boolean planetMyWeightIsGood(GameState gameState, PlanetState planetState) {
        Map<String, Double> factions = new HashMap<>();

        for (Army army : planetState.getStationedArmies()) {
            factions.compute(army.getOwner(), (k, v) -> {
                if(v == null)
                    return (double) army.getSize();
                return v + army.getSize();
            });
        }

        for (Army army : planetState.getMovingArmies()) {
            factions.compute(army.getOwner(), (k, v) -> {
                ArmyExtent armyExtent = gameState.getArmyExtent(planetState, army);
                double mult = (armyExtent.getToTick() + 1 - gameState.getTickElapsed()) * 0.5;

                if(v == null)
                    return army.getSize() / mult;
                return v + army.getSize() / mult;
            });
        }

        Double ourWeight = factions.get(ILogic.OUR_TEAM);

        if(ourWeight == null)
            return false;

        for (Map.Entry<String, Double> stringDoubleEntry : factions.entrySet()) {
            if(stringDoubleEntry.getValue() > ourWeight)
                return false;
        }

        System.out.println("Overriding running away due to inbound armies");
        return true;
    }
}
