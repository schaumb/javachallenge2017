package jsons.common;

import jsons.gamedesc.GameDescription;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Helper {
    public static <T extends Number, U extends Number> double timeToMoveWithoutCeil(Positioned<T> from, Positioned<U> to) {
        return from.distance(to) / GameDescription.LATEST_INSTANCE.getMovementSpeed() * 1000;
    }

    public static <T extends Number, U extends Number> long timeToMove(Positioned<T> from, Positioned<U> to) {
        return (long) Math.ceil(timeToMoveWithoutCeil(from, to));
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

    public static long timeToKillSomeone(List<Number> collect) {
        long time = 0;
        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        while (collect.stream().filter(n -> n.intValue() > 0).count() > 1) {
            time += gameDescription.getInternalSchedule();

            double sum = collect.stream().mapToDouble(Number::doubleValue).sum();
            List<Number> finalCollect = collect;

            collect = IntStream.range(0, collect.size()).mapToObj(i ->
                    finalCollect.get(i).doubleValue() - gameDescription.getBattleSpeed() *
                            Math.pow(sum - finalCollect.get(i).doubleValue(), gameDescription.getBattleExponent())
                            / sum / 1000 * gameDescription.getInternalSchedule())
                    .collect(Collectors.toList());
        }
        return time;
    }

    public static Map<String, Double> killAtTime(Map<String, Double> collect, long time) {

        GameDescription gameDescription = GameDescription.LATEST_INSTANCE;
        long currentTime = 0;
        while (collect.values().stream().filter(n -> n.intValue() > 0).count() > 1 && currentTime < time) {
            currentTime += gameDescription.getInternalSchedule();
            double sum = collect.values().stream().mapToDouble(Number::doubleValue).sum();
            Map<String, Double> finalCollect = collect;

            collect = finalCollect.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    e -> e.getValue() - gameDescription.getBattleSpeed() *
                            Math.pow(sum - e.getValue(), gameDescription.getBattleExponent())
                            / sum / 1000 * gameDescription.getInternalSchedule()));
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
}
