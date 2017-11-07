package wsimpl;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, DeploymentException {

        WebSocketContainer webSocket = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Authorization", Collections.singletonList("Basic " + DatatypeConverter.printBase64Binary("overload:FF76MJ5XlF6YU8HQAqr".getBytes())));
            }
        };
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(configurator).build();

        webSocket.connectToServer(ClientEndpoint.class, config, URI.create("ws://javachallenge.loxon.hu:8080/JavaChallenge2017/websocket"));



    }
}
