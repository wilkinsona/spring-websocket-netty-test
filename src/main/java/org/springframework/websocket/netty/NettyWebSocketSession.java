package org.springframework.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.util.ObjectUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * A {@link WebSocketSession} implementation for use with Netty
 *
 * @author Andy Wilkinson
 */
public class NettyWebSocketSession implements WebSocketSession {

	private final HttpHeaders handshakeHeaders;

	private final Map<String, Object> handshakeAttributes;

	private final String acceptedProtocol;

	private final URI uri;

	private final Channel channel;


	/**
	 * Creates a new {@code NettyWebSocketSession}.
	 *
	 * @param handshakeHeaders The HTTP headers from the handshake request
	 * @param handshakeAttributes The attributes associated with the handshake
	 * @param acceptedProtocol The sub-protocol accepted as part of the handshake
	 * @param uri The URI used to open the WebSocket connection
	 * @param channel The Netty channel that represents the WebSocket connection
	 */
	public NettyWebSocketSession(HttpHeaders handshakeHeaders, Map<String, Object> handshakeAttributes,
			String acceptedProtocol, URI uri, Channel channel) {
		this.handshakeHeaders = handshakeHeaders;
		this.handshakeAttributes = handshakeAttributes;
		this.acceptedProtocol = acceptedProtocol;
		this.uri = uri;
		this.channel = channel;
	}

	@Override
	public String getId() {
		return ObjectUtils.getIdentityHexString(this.channel);
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public void sendMessage(WebSocketMessage<?> message) throws IOException {
		Object payload = message.getPayload();
		if (payload instanceof String) {
			TextWebSocketFrame frame = new TextWebSocketFrame((String) payload);
			channel.writeAndFlush(frame);
		}
	}

	@Override
	public void close() throws IOException {
		CloseWebSocketFrame closeFrame = new CloseWebSocketFrame();
		close(closeFrame);

	}

	@Override
	public void close(CloseStatus status) throws IOException {
		CloseWebSocketFrame closeFrame = new CloseWebSocketFrame(status.getCode(), status.getReason());
		close(closeFrame);
	}

	private void close(CloseWebSocketFrame closeFrame) {
		this.channel.writeAndFlush(closeFrame).addListener(ChannelFutureListener.CLOSE);

	}

	@Override
	public HttpHeaders getHandshakeHeaders() {
		return this.handshakeHeaders;
	}

	@Override
	public Map<String, Object> getHandshakeAttributes() {
		return this.handshakeAttributes;
	}

	@Override
	public Principal getPrincipal() {
		// TODO Security
		return null;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return SocketAddressUtils.convertToInetSocketAddress(channel.localAddress());
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return SocketAddressUtils.convertToInetSocketAddress(channel.remoteAddress());
	}

	@Override
	public String getAcceptedProtocol() {
		return this.acceptedProtocol;
	}

}
