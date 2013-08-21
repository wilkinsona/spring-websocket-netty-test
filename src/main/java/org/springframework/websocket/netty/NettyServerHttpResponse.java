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

/**
 * {@link ServerHttpResponse} implementation that is based on a Netty {@link
 * HttpResponse}.
 *
 * @author Andy Wilkinson
 */
public class NettyServerHttpResponse implements ServerHttpResponse {

	private final Log logger = LogFactory.getLog(NettyServerHttpResponse.class);

	private final HttpHeaders httpHeaders = new HttpHeaders();

	private final HttpResponse httpResponse =
			new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

	private final ChannelHandlerContext context;

	private volatile boolean headersWritten;

	private volatile boolean async;


	/**
	 * Creates a new {@code NettyServerHttpResponse} that will send date to
	 * the channel in the given {@code context}.
	 *
	 * @param context The context for the response
	 */
	public NettyServerHttpResponse(ChannelHandlerContext context) {
		this.context = context;
	}

	@Override
	public OutputStream getBody() throws IOException {
		logger.debug("Returning body outputstream for " + this);
		return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				writeHeaders();

				ByteBuf buffer = context.alloc().buffer(1).writeByte(b);
				HttpContent content = new DefaultHttpContent(buffer);
				context.channel().write(content);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				writeHeaders();

				ByteBuf buffer = context.alloc().buffer(len).writeBytes(b, off, len);
				HttpContent content = new DefaultHttpContent(buffer);
				context.channel().write(content);
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
		this.httpResponse.setStatus(HttpResponseStatus.valueOf(status.value()));
	}

	@Override
	public void flush() throws IOException {
		writeHeaders();
		this.context.channel().flush();
	}

	@Override
	public void close() {
		writeHeaders();

		if (!this.async) {
			this.context.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		}
	}

	/**
	 * Returns the channel to which this response will be written
	 *
	 * @return The response channel
	 */
	public Channel getChannel() {
		return this.context.channel();
	}

	/**
	 * Marks this response as being asynchronous
	 */
	public void setAsync() {
		this.async = true;
	}

	/**
	 * Returns {@code true} if the response is asynchronous, otherwise {@code false}.
	 */
	public boolean isAsync() {
		return this.async;
	}

	private void writeHeaders() {
		if (!this.headersWritten) {
			io.netty.handler.codec.http.HttpHeaders nettyHttpHeaders = this.httpResponse.headers();
			for (Entry<String, List<String>> header: this.httpHeaders.entrySet()) {
				nettyHttpHeaders.add(header.getKey(), header.getValue());
			}
			io.netty.handler.codec.http.HttpHeaders.setTransferEncodingChunked(this.httpResponse);
			this.context.channel().write(this.httpResponse);
			this.headersWritten = true;
		}
	}

}
