package org.springframework.websocket.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.util.Assert;

/**
 * Netty support for upgrading an HTTP request during a WebSocket handshake.
 *
 * @author Andy Wilkinson
 */
public class NettyRequestUpgradeStrategy implements RequestUpgradeStrategy {

	@Override
	public String[] getSupportedVersions() {
		return new String[] {"7", "8", "13"};
	}

	@Override
	public void upgrade(ServerHttpRequest request, ServerHttpResponse response, String acceptedProtocol,
			final WebSocketHandler wsHandler, final Map<String, Object> attributes) throws HandshakeFailureException {
		Assert.isInstanceOf(NettyServerHttpRequest.class, request);
		Assert.isInstanceOf(NettyServerHttpResponse.class, response);

		final NettyServerHttpRequest nettyRequest = (NettyServerHttpRequest)request;
		final NettyServerHttpResponse nettyResponse = (NettyServerHttpResponse)response;

		WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(nettyRequest.getFullHttpRequest()), null, false);

		final WebSocketServerHandshaker handshaker =
				handshakerFactory.newHandshaker(nettyRequest.getFullHttpRequest());

		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(nettyResponse.getChannel());
			throw new HandshakeFailureException("Unsupported WebSocket protocol version");
		}
		else {
			ChannelFuture future = handshaker.handshake(nettyResponse.getChannel(),
					nettyRequest.getFullHttpRequest());

			future.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					ChannelPipeline pipeline = future.channel().pipeline();
					WebSocketSession session = new NettyWebSocketSession(nettyRequest.getHeaders(), attributes,
							handshaker.selectedSubprotocol(), nettyRequest.getURI(), nettyResponse.getChannel());
					pipeline.addLast(new ChannelInboundWebSocketFrameHandler(handshaker, session, wsHandler));

					wsHandler.afterConnectionEstablished(session);
				}
			});
		}
	}

	private static String getWebSocketLocation(FullHttpRequest req) {
		return "ws://" + req.headers().get(HOST) + "/websocket";
	}

}
