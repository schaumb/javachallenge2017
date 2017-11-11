package wsimpl;

import jsons.Move;
import logic.GuiLogic;
import logic.ILogic;
import logic.LearningAlgorithm;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.function.Consumer;

public class Main {
    public final static Object o = new Object();
    public static ILogic logic = LearningAlgorithm.THE_LEARNING_ALGORITHM.collapse(new GuiLogic());
    public static Consumer<Move> sender;
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

            new ServerImitator();

            synchronized (o) {
                o.wait();
            }
        }
    }
}
