package wsimpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;
import logic.ILogic;

import javax.websocket.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientEndpoint extends Endpoint implements MessageHandler.Whole<String>, Consumer<Move> {
    private final static Logger LOG = Logger.getLogger(ClientEndpoint.class.getName());
    public static Consumer<Move> sender;
    private Session session;
    private boolean firstMessage = true;
    private Gson gson = new GsonBuilder().create();

    private ILogic logic = ILogic.createLogic();

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler(this);
        this.session = session;
        sender = this;
        logic.setMessageConsumer(this);
    }

    @Override
    public void onMessage(String message) {
        if (message.isEmpty()) {
            System.err.println("Got an empty message");
            return;
        }

        LOG.fine("Got message: " + message);
        if (firstMessage) {
            GameDescription gameDescription = gson.fromJson(message, GameDescription.class);
            LOG.fine("Consumed message as description: " + gameDescription);
            GameDescription.GAME_STARTED_MS = System.currentTimeMillis();
            logic.setGameDescription(gameDescription);
            firstMessage = false;
        } else {
            GameState gameState = gson.fromJson(message, GameState.class);
            LOG.fine("Consumed message as state: " + gameState);
            logic.setGameState(gameState);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);

        logic.close();
        LOG.warning("Closed: " + closeReason.getReasonPhrase());

        end();
    }

    @Override
    public void onError(Session session, Throwable thr) {
        super.onError(session, thr);

        logic.close();
        LOG.log(Level.WARNING, "onError", thr);

        end();
    }

    private void end() {
        synchronized (Main.o) {
            Main.o.notifyAll();
        }
    }

    @Override
    public void accept(Move move) {
        String s = gson.toJson(move);
        LOG.fine("Send move message: " + move + " as Json: " + s);
        session.getAsyncRemote().sendText(s);
    }
}
