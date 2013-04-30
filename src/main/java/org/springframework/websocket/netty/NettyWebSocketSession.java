package org.springframework.websocket.netty;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.net.URI;

import org.springframework.websocket.CloseStatus;
import org.springframework.websocket.WebSocketMessage;
import org.springframework.websocket.WebSocketSession;


public class NettyWebSocketSession implements WebSocketSession {

	private NettyServerHttpResponse response;

	public NettyWebSocketSession(NettyServerHttpResponse response) {
		this.response = response;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(WebSocketMessage<?> message) throws IOException {
		Object payload = message.getPayload();
		if (payload instanceof String) {
			TextWebSocketFrame frame = new TextWebSocketFrame((String) payload);
			response.getChannel().write(frame);
		}
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close(CloseStatus status) throws IOException {
		// TODO Auto-generated method stub

	}

}
