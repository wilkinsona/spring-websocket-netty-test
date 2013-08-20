package org.springframework.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;


public class NettyServerHttpResponse implements ServerHttpResponse {

	private ChannelHandlerContext ctx;


	public NettyServerHttpResponse(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public OutputStream getBody() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpHeaders getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatusCode(HttpStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	public Channel getChannel() {
		return this.ctx.channel();
	}

}
