
package org.springframework.websocket.netty.samples.websocket.netty;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.socket.server.DefaultHandshakeHandler;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.websocket.netty.NettyRequestUpgradeStrategy;
import org.springframework.websocket.netty.NettyServerHttpRequest;
import org.springframework.websocket.netty.NettyServerHttpResponse;
import org.springframework.websocket.netty.samples.websocket.netty.echo.EchoService;
import org.springframework.websocket.netty.samples.websocket.netty.echo.EchoWebSocketHandler;

@Component
@Scope("prototype")
public class HttpMessageHandler extends
		SimpleChannelInboundHandler<FullHttpRequest> {

	private EchoService echoService;

	@Autowired
	public HttpMessageHandler(EchoService echoService) {
		this.echoService = echoService;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req)
			throws Exception {

		// Ripped from the netty websocket sample

		// Handle a bad request.
		if (!req.getDecoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		// Allow only GET methods.
		if (req.getMethod() != GET) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		ClassPathResource page = new ClassPathResource("/pages" + req.getUri());
		if(page.exists()) {
			ByteBuf content = Unpooled.wrappedBuffer(FileCopyUtils.copyToByteArray(page.getInputStream()));
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

			res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
			setContentLength(res, content.readableBytes());

			sendHttpResponse(ctx, req, res);
			return;
		}

		if("/echoWebSocket".equals(req.getUri())) {
			upgradeToWebSocket(ctx, req);
			return;
		}

		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
		sendHttpResponse(ctx, req, res);
	}

	/**
	 * @param ctx
	 * @param req
	 * @throws IOException
	 */
	private void upgradeToWebSocket(ChannelHandlerContext ctx, FullHttpRequest req)
			throws IOException {

		// Spring specifics

		// FIXME handler should be injected
		EchoWebSocketHandler webSocketHandler = new EchoWebSocketHandler(this.echoService);

		// Adapt Netty to Spring
		NettyServerHttpRequest serverHttpRequest = new NettyServerHttpRequest(ctx, req);
		NettyServerHttpResponse serverHttpResponse = new NettyServerHttpResponse(ctx);

		// Handshake
		HandshakeHandler handshakeHandler = new DefaultHandshakeHandler(new NettyRequestUpgradeStrategy());
		handshakeHandler.doHandshake(serverHttpRequest, serverHttpResponse,
				webSocketHandler, Collections.<String, Object>emptyMap());
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
			FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			res.content().writeBytes(
					Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
			setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
