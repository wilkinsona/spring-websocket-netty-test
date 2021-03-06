package org.springframework.websocket.netty;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServerHttpAsyncRequestControl;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.Assert;


/**
 * {@link ServerHttpRequest} implementation that is based upon a {@link
 * FullHttpRequest} from Netty.
 *
 * @author Andy Wilkinson
 */
public class NettyServerHttpRequest implements ServerHttpRequest {

	private final ChannelHandlerContext ctx;

	private final FullHttpRequest req;


	public NettyServerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
		this.ctx = ctx;
		this.req = request;
	}

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.valueOf(req.getMethod().name());
	}

	@Override
	public URI getURI() {
		try {
			return new URI(req.getUri());
		}
		catch (URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		for (Map.Entry<String, String> entry : req.headers()) {
			headers.add(entry.getKey(), entry.getValue());
		}
		return headers;
	}

	@Override
	public InputStream getBody() throws IOException {
		return new ByteBufInputStream(req.content());
	}

	public FullHttpRequest getFullHttpRequest() {
		return this.req;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return this.ctx;
	}

	@Override
	public Principal getPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return SocketAddressUtils.convertToInetSocketAddress(this.ctx.channel().localAddress());
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return SocketAddressUtils.convertToInetSocketAddress(this.ctx.channel().remoteAddress());
	}

	@Override
	public ServerHttpAsyncRequestControl getAsyncRequestControl(ServerHttpResponse response) {
		Assert.isInstanceOf(NettyServerHttpResponse.class, response);
		final NettyServerHttpResponse nettyResponse = (NettyServerHttpResponse)response;

		return new ServerHttpAsyncRequestControl() {

			private volatile boolean started;

			private volatile boolean completed;

			@Override
			public void start() {
				start(-1);
			}

			@Override
			public void start(long timeout) {
				this.started = true;
				nettyResponse.setAsync();
			}

			@Override
			public boolean isStarted() {
				return this.started;
			}

			@Override
			public void complete() {
				ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
				ctx.channel().close();
			}

			@Override
			public boolean isCompleted() {
				return this.completed;
			}
		};
	}

}
