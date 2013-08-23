package org.springframework.websocket.netty.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * An {@link AbstractHttpMessageHandler} that checks for the HTTP request
 * having been successfully decoded. If decoding was not successful, a
 * bad request (400) response is returned, otherwise the request is passed
 * on to the next handler.
 *
 * @author Andy Wilkinson
 *
 */
@Sharable
public class BadRequestHttpMessageHandler extends AbstractHttpMessageHandler {

	@Override
	protected void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) {
		if (!request.getDecoderResult().isSuccess()) {
			sendHttpResponse(context, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
		} else {
			context.fireChannelRead(request);
		}
	}

}
