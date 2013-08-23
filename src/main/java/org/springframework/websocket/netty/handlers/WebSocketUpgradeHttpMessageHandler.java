package org.springframework.websocket.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.DefaultHandshakeHandler;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.websocket.netty.NettyRequestUpgradeStrategy;
import org.springframework.websocket.netty.NettyServerHttpRequest;
import org.springframework.websocket.netty.NettyServerHttpResponse;

/**
 * An {@link AbstractHttpMessageHandler} that handles WebSocket upgrade
 * requests. For a request to be processed by this handler its uri must
 * match a configurable path. Requests that do not match are passed on to
 * the next handler in the pipeline.
 *
 * @author Andy Wilkinson
 *
 */
public class WebSocketUpgradeHttpMessageHandler extends AbstractHttpMessageHandler {

	private final WebSocketHandler webSocketHandler;

	private final String path;

	/**
	 * Creates a new {@code WebSocketUpgradeHttpMessageHandler}. The given
	 * {@code webSocketHandler will be used to handle WebSocket requests
	 * following the upgrade. For a request to be eligible for upgrade its
	 * uri must match the given {@code path}.
	 *
	 * @param webSocketHandler The handler for WebSocket requests
	 * @param path The path to be matched by an upgrade request's uri
	 */
	@Autowired
	public WebSocketUpgradeHttpMessageHandler(WebSocketHandler webSocketHandler, String path) {
		this.webSocketHandler = webSocketHandler;
		this.path = path;
	}

	@Override
	protected void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
		if (request.getUri().startsWith(path)) {
			NettyServerHttpRequest serverHttpRequest = new NettyServerHttpRequest(context, request);
			NettyServerHttpResponse serverHttpResponse = new NettyServerHttpResponse(context);

			HandshakeHandler handshakeHandler = new DefaultHandshakeHandler(new NettyRequestUpgradeStrategy());
			handshakeHandler.doHandshake(serverHttpRequest, serverHttpResponse,
					webSocketHandler, Collections.<String, Object>emptyMap());
		} else {
			context.fireChannelRead(request);
		}
	}
}
