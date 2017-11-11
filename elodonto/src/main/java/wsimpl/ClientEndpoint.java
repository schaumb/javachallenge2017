package wsimpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;

import javax.websocket.*;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientEndpoint extends Endpoint implements MessageHandler.Whole<String>, Consumer<Move> {
    private final static Logger LOG = Logger.getLogger(ClientEndpoint.class.getName());
    private Session session;
    private boolean firstMessage = true;
    private Gson gson = new GsonBuilder().create();
    private Runnable closer;

    public static Session createEndpoint() throws IOException, DeploymentException {
        WebSocketContainer webSocket = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Authorization", Collections.singletonList("Basic " + DatatypeConverter.printBase64Binary("overload:FF76MJ5XlF6YU8HQAqr".getBytes())));
            }
        };
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(configurator).build();

        return webSocket.connectToServer(ClientEndpoint.class, config, URI.create("ws://javachallenge.loxon.hu:8080/JavaChallenge2017/websocket"));
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler(this);
        this.session = session;
        Main.sender = this;
        closer = Main.closer;
        Main.closer = () -> {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
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
            Main.logic.setGameDescription(gameDescription);
            firstMessage = false;
        } else {
            GameState gameState = gson.fromJson(message, GameState.class);
            LOG.fine("Consumed message as state: " + gameState);
            Main.logic.setGameState(gameState);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);

        LOG.warning("Closed: " + closeReason.getReasonPhrase());

        closer.run();
        Main.closer.run();
    }

    @Override
    public void onError(Session session, Throwable thr) {
        super.onError(session, thr);

        LOG.log(Level.WARNING, "onError", thr);

        closer.run();
        Main.closer.run();
    }

    @Override
    public void accept(Move move) {
        String s = gson.toJson(move);
        LOG.fine("Send move message: " + move + " as Json: " + s);
        session.getAsyncRemote().sendText(s);
    }
}
