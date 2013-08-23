package org.springframework.websocket.netty.handlers;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

/**
 * Base class for Netty pipeline handlers interested in {@link FullHttpRequest}s.
 *
 * @author Andy Wilkinson
 *
 */
public abstract class AbstractHttpMessageHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
		if (message instanceof FullHttpRequest) {
			handleHttpRequest(context, (FullHttpRequest)message);
		} else {
			context.fireChannelRead(message);
		}
	}

	protected abstract void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) throws Exception;

	protected final void sendHttpResponse(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
		sendHttpResponse(ctx, null, response);
	}

	protected final void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request,
			FullHttpResponse response) {

		if (!HttpResponseStatus.OK.equals(response.getStatus())) {
			ByteBuf buffer = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
			response.content().writeBytes(buffer);

			setContentLength(response, response.content().readableBytes());
		}

		ChannelFuture writeFuture = ctx.channel().writeAndFlush(response);

		if (shouldCloseConnection(request, response)) {
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private boolean shouldCloseConnection(HttpRequest request, HttpResponse response) {
		return (request == null || !isKeepAlive(request) || !HttpResponseStatus.OK.equals(response.getStatus()));
	}

}
