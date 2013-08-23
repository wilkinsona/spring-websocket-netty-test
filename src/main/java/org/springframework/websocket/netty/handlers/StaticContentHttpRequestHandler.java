package org.springframework.websocket.netty.handlers;

import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

/**
 * An {@link AbstractHttpMessageHandler} for serving static content. If no static
 * content from a configuration location. If no statich content matching the
 * request URI is available, the request is passed on to the next handler in the
 * pipeline.
 *
 * @author Andy Wilkinson
 *
 */
public class StaticContentHttpRequestHandler extends AbstractHttpMessageHandler {

	private final String location;

	/**
	 * Creates a new {@code StaticContentHttpRequestHandler} that will serve
	 * static content from the given {@code location}.
	 *
	 * @param location The location from which static content is served
	 */
	public StaticContentHttpRequestHandler(String location) {
		this.location = location;
	}

	@Override
	protected void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
		ClassPathResource page = new ClassPathResource(location + request.getUri());

		if (page.exists()) {
			ByteBuf content = Unpooled.wrappedBuffer(FileCopyUtils.copyToByteArray(page.getInputStream()));
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

			response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
			setContentLength(response, content.readableBytes());

			sendHttpResponse(context, request, response);
		} else {
			context.fireChannelRead(request);
		}
	}
}
