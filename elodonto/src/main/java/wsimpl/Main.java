package wsimpl;

import jsons.Move;
import logic.BestOfEveryTimeLogic;
import logic.GuiLogic;
import logic.ILogic;
import logic.LearningAlgorithm;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.function.BiConsumer;

public class Main {
    public final static Object o = new Object();
    public static ILogic logic;
    public static BiConsumer<Move, String> sender;
    public static Runnable closer = () -> {
        logic.close();
        synchronized (o) {
            o.notifyAll();
        }
    };
    public static Runnable endTick = () -> {
    };

    public static void main(String[] args) throws IOException, DeploymentException, InterruptedException {
        while (!o.equals(args)) {
            logic = new BestOfEveryTimeLogic().collapse(new GuiLogic());
            ClientEndpoint.createEndpoint();
            // new ServerImitator();

            synchronized (o) {
                o.wait();
            }
        }
    }
}
