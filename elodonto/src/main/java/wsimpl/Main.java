package wsimpl;

import javax.websocket.*;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Main {
    public final static Object o = new Object();

    public static void main(String[] args) throws IOException, DeploymentException, InterruptedException {
        while (!o.equals(args)) {
            WebSocketContainer webSocket = ContainerProvider.getWebSocketContainer();
            ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    headers.put("Authorization", Collections.singletonList("Basic " + DatatypeConverter.printBase64Binary("overload:FF76MJ5XlF6YU8HQAqr".getBytes())));
                }
            };
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(configurator).build();

            Session session = webSocket.connectToServer(ClientEndpoint.class, config, URI.create("ws://javachallenge.loxon.hu:8080/JavaChallenge2017/websocket"));

            synchronized (o) {
                o.wait();
            }
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}
