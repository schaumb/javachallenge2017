package wsimpl;

import com.google.gson.Gson;
import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;
import jsons.gamestate.GameStatus;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

public class ServerImitator {

    private final static Gson gson = new Gson();
    private final static GameDescription gameDescription =
            gson.fromJson("{\"mapSizeY\":900,\"unitCreateSpeed\":0.002,\"battleSpeed\":2.0,\"mapSizeX\":1200," +
                            "\"players\":[{\"raceID\":1,\"userName\":\"Overloaded Operators\",\"userID\":\"overload\"}," +
                            "{\"raceID\":1,\"userName\":\"Test Bot\",\"userID\":\"bot1\"}],\"commandSchedule\":200," +
                            "\"broadcastSchedule\":200,\"movementSpeed\":60.0,\"captureSpeed\":2.0,\"planetExponent\":1.5," +
                            "\"planets\":[" +
                            "{\"x\":240,\"y\":200,\"planetID\":101,\"radius\":45}," +
                            "{\"x\":960,\"y\":700,\"planetID\":102,\"radius\":45}," +
                            "{\"x\":120,\"y\":460,\"planetID\":103,\"radius\":40}," +
                            "{\"x\":1080,\"y\":440,\"planetID\":104,\"radius\":40}," +
                            "{\"x\":360,\"y\":420,\"planetID\":105,\"radius\":40}," +
                            "{\"x\":840,\"y\":480,\"planetID\":106,\"radius\":40}," +
                            "{\"x\":600,\"y\":260,\"planetID\":107,\"radius\":50}," +
                            "{\"x\":600,\"y\":640,\"planetID\":108,\"radius\":50}," +
                            "{\"x\":940,\"y\":120,\"planetID\":109,\"radius\":45}," +
                            "{\"x\":260,\"y\":780,\"planetID\":110,\"radius\":45}],\"" +
                            "internalSchedule\":40,\"battleExponent\":1.5,\"minMovableArmySize\":5,\"gameLength\":150000}",
                    GameDescription.class);

    private final static GameState firstGameState =
            gson.fromJson("{\"timeElapsed\":0," +
                            "\"planetStates\":[" +
                            "{\"owner\":\"bot1\",\"ownershipRatio\":1.0,\"stationedArmies\":[" +
                            "{\"owner\":\"bot1\",\"size\":50}]," +
                            "\"movingArmies\":[],\"planetID\":101}," +
                            "{\"owner\":\"overload\",\"ownershipRatio\":1.0,\"stationedArmies\":[" +
                            "{\"owner\":\"overload\",\"size\":50}]," +
                            "\"movingArmies\":[],\"planetID\":102}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":103}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":104}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":105}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":106}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":107}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":108}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":109}," +
                            "{\"owner\":null,\"ownershipRatio\":0.0,\"stationedArmies\":[],\"movingArmies\":[],\"planetID\":110}]," +
                            "\"remainingPlayers\":2,\"gameStatus\":\"PLAYING\"," +
                            "\"standings\":[" +
                            "{\"score\":0,\"strength\":351,\"userID\":\"overload\"}," +
                            "{\"score\":0,\"strength\":351,\"userID\":\"bot1\"}]}",
                    GameState.class);
    private final Timer timer = new Timer();
    private GameState curr = firstGameState.copy().setMove(Stream.of(new Move().setArmySize(50).setMoveFrom(101).setMoveTo(105)), "bot1")
            .setDelayedMove(Stream.of(new Move().setArmySize(50).setMoveFrom(105).setMoveTo(103)), "bot1", 20);

    public ServerImitator() {
        Main.sender = (m, s) -> curr.setMove(Stream.of(m), s);
        Main.endTick = new Runnable() {
            @Override
            public synchronized void run() {
                Main.logic.setGameState(curr.setAfterTime(gameDescription.getBroadcastSchedule()));
            }
        };

        Main.logic.setGameDescription(gameDescription);
        Main.logic.setGameState(curr);

        timer.scheduleAtFixedRate(new TimerTask() {
            int latestTime = 0;

            @Override
            public void run() {
                if (latestTime == curr.getTimeElapsed()) {
                    Main.endTick.run();
                }
                latestTime = curr.getTimeElapsed();

                if (curr.getGameStatus() == GameStatus.ENDED) {
                    timer.cancel();
                    Main.closer.run();
                }
            }
        }, gameDescription.getBroadcastSchedule(), gameDescription.getBroadcastSchedule());
    }
}
