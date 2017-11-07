package wsimpl;

import javax.websocket.*;

public class ClientEndpoint extends Endpoint implements MessageHandler.Whole<String> {
	private Session session;

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		session.addMessageHandler(this);
		this.session = session;
		System.err.println("OPEN");
	}

	@Override
	public void onMessage(String message) {
		System.out.println(message);
		System.err.println("MESSAGE");
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		super.onClose(session, closeReason);
		System.err.println("ONCLOSE");
	}

	@Override
	public void onError(Session session, Throwable thr) {
		super.onError(session, thr);
		System.err.println("ONERROR");
	}

	private void sendMessage(String message) {
		session.getAsyncRemote().sendText(message);
	}
}
