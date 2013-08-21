package org.springframework.websocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;


public class NettyServerHttpResponse implements ServerHttpResponse {

	private final Log logger = LogFactory.getLog(NettyServerHttpResponse.class);

	private final HttpHeaders httpHeaders = new HttpHeaders();

	private final HttpResponse httpResponse =
			new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

	private final ChannelHandlerContext ctx;

	private volatile boolean headersWritten;

	private volatile boolean async;


	public NettyServerHttpResponse(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public OutputStream getBody() throws IOException {
		return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				writeHeaders();

				ByteBuf buffer = ctx.alloc().buffer(1).writeByte(b);
				HttpContent content = new DefaultHttpContent(buffer);
				ctx.channel().write(content);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				writeHeaders();

				ByteBuf buffer = ctx.alloc().buffer(len).writeBytes(b, off, len);
				HttpContent content = new DefaultHttpContent(buffer);
				ctx.channel().write(content);
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};
	}

	@Override
	public HttpHeaders getHeaders() {
		return (this.headersWritten ? HttpHeaders.readOnlyHttpHeaders(this.httpHeaders) : this.httpHeaders);
	}

	@Override
	public void setStatusCode(HttpStatus status) {
		logger.debug("Set status to " + status + " on " + this);
		if (status == HttpStatus.NO_CONTENT) {
			this.httpResponse.setStatus(HttpResponseStatus.NO_CONTENT);
		} else if (status == HttpStatus.NOT_FOUND) {
			this.httpResponse.setStatus(HttpResponseStatus.NOT_FOUND);
		} else {
			logger.error(status + " has not been mapped to a Netty HttpResponseStatus");
		}
	}

	@Override
	public void flush() throws IOException {
		writeHeaders();
		this.ctx.channel().flush();
	}

	@Override
	public void close() {
		writeHeaders();

		if (!this.async) {
			this.ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			logger.debug("Closed response");
		} else {
			logger.debug("Supressing close as response is async");
		}
	}

	public Channel getChannel() {
		return this.ctx.channel();
	}

	public void becomeAsync() {
		this.async = true;
	}

	public boolean isAsync() {
		return this.async;
	}

	private void writeHeaders() {
		if (!this.headersWritten) {
			logger.debug("Writing headers: " + this.httpHeaders);
			io.netty.handler.codec.http.HttpHeaders nettyHttpHeaders = this.httpResponse.headers();
			for (Entry<String, List<String>> header: this.httpHeaders.entrySet()) {
				nettyHttpHeaders.add(header.getKey(), header.getValue());
			}
			io.netty.handler.codec.http.HttpHeaders.setTransferEncodingChunked(this.httpResponse);
			this.ctx.channel().write(this.httpResponse);
			this.headersWritten = true;
		}
	}

}
