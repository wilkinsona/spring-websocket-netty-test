package org.springframework.websocket.netty;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;


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
	public URI getUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(WebSocketMessage<?> message) throws IOException {
		Object payload = message.getPayload();
		if (payload instanceof String) {
			TextWebSocketFrame frame = new TextWebSocketFrame((String) payload);
			response.getChannel().writeAndFlush(frame);
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

	@Override
	public HttpHeaders getHandshakeHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getHandshakeAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return getInetSocketAddressFrom(response.getChannel().localAddress());
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return getInetSocketAddressFrom(response.getChannel().localAddress());
	}

	@Override
	public String getAcceptedProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	private InetSocketAddress getInetSocketAddressFrom(SocketAddress socketAddress) {
		if (socketAddress instanceof InetSocketAddress) {
			return (InetSocketAddress) socketAddress;
		}
		else {
			return null;
		}
	}

}
