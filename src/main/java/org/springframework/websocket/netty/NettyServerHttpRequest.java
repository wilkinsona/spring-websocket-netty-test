package org.springframework.websocket.netty;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.springframework.http.Cookies;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.AsyncServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

public class NettyServerHttpRequest implements AsyncServerHttpRequest {

	private ChannelHandlerContext ctx;

	private FullHttpRequest req;

	
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
	public Cookies getCookies() {
		Cookies cookies = new Cookies();
		String header = req.headers().get("Cookie");
		if(header != null) {
			// FIXME better as an adapter
			// FIXME Cookie may need more in it
			Set<Cookie> decoded = CookieDecoder.decode(header);
			for (Cookie cookie : decoded) {
				cookies.addCookie(cookie.getName(), cookie.getValue());
			}
		}
		return cookies;
	}

	@Override
	public InputStream getBody() throws IOException {
		return new ByteBufInputStream(req.data());
	}

	@Override
	public MultiValueMap<String, String> getQueryParams() {
		QueryStringDecoder decoder = new QueryStringDecoder(req.getUri());
		return CollectionUtils.toMultiValueMap(decoder.parameters());
	}

	@Override
	public void startAsync() {
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public void completeAsync() {
	}

	@Override
	public boolean isAsyncCompleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTimeout(long timeout) {
	}

	public FullHttpRequest getFullHttpRequest() {
		return this.req;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return this.ctx;
	}

}
