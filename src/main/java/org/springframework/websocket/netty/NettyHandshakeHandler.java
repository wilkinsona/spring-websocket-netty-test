
package org.springframework.websocket.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.io.IOException;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.websocket.WebSocketHandler;
import org.springframework.websocket.WebSocketSession;
import org.springframework.websocket.server.HandshakeHandler;

public class NettyHandshakeHandler implements HandshakeHandler {

	private WebSocketServerHandshakerFactory handshakerFactory;

	public NettyHandshakeHandler(WebSocketServerHandshakerFactory handshakerFactory) {
		this.handshakerFactory = handshakerFactory;
	}

	@Override
	public boolean doHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler webSocketHandler) throws IOException {
		Assert.isInstanceOf(NettyServerHttpRequest.class, request);
		Assert.isInstanceOf(NettyServerHttpResponse.class, response);
		return doHandshake((NettyServerHttpRequest) request,
				(NettyServerHttpResponse) response, webSocketHandler);
	}

	private boolean doHandshake(final NettyServerHttpRequest request,
			final NettyServerHttpResponse response,
			final WebSocketHandler webSocketHandler) throws IOException {
		final WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request.getFullHttpRequest());
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(response.getChannel());
			return false;
		}

		ChannelFuture future = handshaker.handshake(response.getChannel(),
				request.getFullHttpRequest());
		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {

				// At this point HttpRequestDecoder or HttpServerCodec
				// will have been removed by the WebSocketServerHandshaker
				// and WebSocketFrameEncoder / WebSocketFrameDecoders will
				// be plugged in

				// We need to add a ChannelHandler that can delegate to
				// our provider
				ChannelPipeline pipeline = future.channel().pipeline();
				WebSocketSession session = new NettyWebSocketSession(response);
				pipeline.addLast(new ChannelInboundWebSocketFrameHandler(handshaker, session, webSocketHandler));
			}
		});

		// FIXME how should we deal with this? should we wait?
		await(future);
		return true;
	}

	private void await(ChannelFuture future) {
		try {
			future.await();
		}
		catch (InterruptedException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
