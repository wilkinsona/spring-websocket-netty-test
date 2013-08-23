package org.springframework.websocket.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.sockjs.SockJsService;
import org.springframework.websocket.netty.NettyServerHttpRequest;
import org.springframework.websocket.netty.NettyServerHttpResponse;

/**
 * An {@link AbstractHttpMessageHandler} that handles SockJS requests. For a
 * request to be processed by this handler its uri must begin with a
 * configurable prefix. Requests that do not match are passed on to the next
 * handler in the pipeline.
 *
 * @author Andy Wilkinson
 *
 */
public class SockJsHttpMessageHandler extends AbstractHttpMessageHandler {

	private final SockJsService sockJsService;

	private final WebSocketHandler webSocketHandler;

	private final String uriPrefix;

	public SockJsHttpMessageHandler(SockJsService sockJsService, WebSocketHandler webSocketHandler, String uriPrefix) {
		this.sockJsService = sockJsService;
		this.webSocketHandler = webSocketHandler;
		this.uriPrefix = uriPrefix;
	}

	@Override
	protected void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
		if (request.getUri().startsWith(this.uriPrefix)) {
			NettyServerHttpRequest serverHttpRequest = new NettyServerHttpRequest(context, request);
			NettyServerHttpResponse serverHttpResponse = new NettyServerHttpResponse(context);

			this.sockJsService.handleRequest(serverHttpRequest, serverHttpResponse, this.webSocketHandler);
		} else {
			context.fireChannelRead(request);
		}
	}

}
