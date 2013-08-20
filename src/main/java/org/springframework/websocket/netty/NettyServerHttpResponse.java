package org.springframework.websocket.netty;

import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
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

	private final FullHttpResponse httpResponse =
			new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

	private final ChannelHandlerContext ctx;

	private volatile boolean headersWritten;

	private volatile boolean flushed;

	private volatile boolean async;


	public NettyServerHttpResponse(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public OutputStream getBody() throws IOException {
		return new OutputStream() {

			@Override
			public void write(int arg0) throws IOException {
				httpResponse.content().writeByte(arg0);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				httpResponse.content().writeBytes(b, off, len);
				NettyServerHttpResponse.this.flush();
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.httpHeaders;
	}

	@Override
	public void setStatusCode(HttpStatus status) {
		logger.debug("Set status to " + status + " on " + this);
		if (status == HttpStatus.NO_CONTENT) {
			this.httpResponse.setStatus(HttpResponseStatus.NO_CONTENT);
		} else if (status == HttpStatus.NOT_FOUND) {
			this.httpResponse.setStatus(HttpResponseStatus.NOT_FOUND);
		} else {
			logger.warn(status + " has not been mapped to a Netty HttpResponseStatus");
		}
	}

	@Override
	public void flush() throws IOException {
		if (!flushed) {
			logger.debug(this.httpResponse.content().toString(Charset.forName("UTF8")));
			this.writeHeaders();
			setContentLength(this.httpResponse, this.httpResponse.content().readableBytes());
			this.ctx.channel().writeAndFlush(this.httpResponse);
			this.flushed = true;
			logger.debug("Wrote and flushed response for " + this);
		}
	}

	@Override
	public void close() {
		writeHeaders();
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
			this.headersWritten = true;
		}
	}

}
