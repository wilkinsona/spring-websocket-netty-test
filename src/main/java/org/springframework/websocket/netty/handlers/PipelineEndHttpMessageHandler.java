package org.springframework.websocket.netty.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * An {@link AbstractHttpMessageHandler} for use a the end of a pipeline. It handles
 * returning an internal server error (500) response if an exception has been thrown
 * earlier in the pipeline, or a not found (404) response if requets handling has
 * reached the end of the pipeline.
 *
 * @author Andy Wilkinson
 */
@Sharable
public class PipelineEndHttpMessageHandler extends AbstractHttpMessageHandler {

	@Override
	protected void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) {
		sendHttpResponse(context, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		sendHttpResponse(context, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
	}
}
