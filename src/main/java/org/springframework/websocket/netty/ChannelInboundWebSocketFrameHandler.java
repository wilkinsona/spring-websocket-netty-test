
package org.springframework.websocket.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.core.GenericTypeResolver;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * A Netty {@link ChannelInboundMessageHandler} that manages incoming
 * {@link WebSocketFrame}s, delegating to a {@link WebSocketHandler}.
 *
 * @author Phillip Webb
 */
public class ChannelInboundWebSocketFrameHandler extends
		SimpleChannelInboundHandler<WebSocketFrame> {

	private Map<Class<?>, FrameHandler<?>> frameHandlers;

	private WebSocketServerHandshaker handshaker;

	private WebSocketSession session;

	private WebSocketHandler handler;

	private DataFrameHandler<?, ?> lastDataFrameHandler;

	/**
	 * Create a new {@link ChannelInboundWebSocketFrameHandler} instance.
	 * @param connection the established connection
	 */
	public ChannelInboundWebSocketFrameHandler(WebSocketServerHandshaker handshaker,
			WebSocketSession session, WebSocketHandler handler) {
		this.handshaker = handshaker;
		this.session = session;
		this.handler = handler;
		addFrameHandlers();
	}


	private void addFrameHandlers() {
		this.frameHandlers = new LinkedHashMap<Class<?>, ChannelInboundWebSocketFrameHandler.FrameHandler<?>>();
		addFrameHandler(new CloseFrameHandler());
		addFrameHandler(new PingFrameHandler());
		addFrameHandler(new TextFrameHandler());
		addFrameHandler(new BinaryFrameHandler());
	}

	private void addFrameHandler(FrameHandler<?> handler) {
		Class<?> type = GenericTypeResolver.resolveTypeArgument(handler.getClass(),
				FrameHandler.class);
		this.frameHandlers.put(type, handler);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame)
			throws Exception {

		try {
			// Use the last data handler to deal with continuations
			if (frame instanceof ContinuationWebSocketFrame) {
				this.lastDataFrameHandler.handleContinuation((ContinuationWebSocketFrame) frame);
				return;
			}

			// Find and delegate to a handler
			for (Map.Entry<Class<?>, FrameHandler<?>> entry : this.frameHandlers.entrySet()) {
				if (entry.getKey().isInstance(frame)) {
					FrameHandler handler = entry.getValue();
					if (handler instanceof DataFrameHandler && !frame.isFinalFragment()) {
						// Store data handler for continuations
						this.lastDataFrameHandler = (DataFrameHandler<?, ?>) handler;
					}
					handler.handle(ctx, frame);
					return;
				}
			}
		}
		catch(Exception ex) {
			this.handler.handleTransportError(this.session, ex);
		}
	}


	/**
	 * Strategy used to handling incoming frames
	 * @param <T> the type of frame to handler
	 */
	private abstract class FrameHandler<T extends WebSocketFrame> {

		/**
		 * Handle an incoming frame.
		 * @param ctx the channel handler context
		 * @param frame the incoming frame
		 */
		public abstract void handle(ChannelHandlerContext ctx, T frame);
	}


	/**
	 * Strategy used to handle incoming {@link CloseWebSocketFrame}s.
	 */
	private class CloseFrameHandler extends FrameHandler<CloseWebSocketFrame> {

		@Override
		public void handle(ChannelHandlerContext ctx, final CloseWebSocketFrame frame) {
			handshaker.close(ctx.channel(), frame.copy()).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					int statusCode = frame.statusCode();
					CloseStatus closeStatus = statusCode == -1 ? CloseStatus.NO_STATUS_CODE : new CloseStatus(statusCode, frame.reasonText());
					handler.afterConnectionClosed(session, closeStatus);
				}
			});
		}
	}


	/**
	 * Strategy used to handle incoming {@link PingWebSocketFrame}s.
	 */
	private class PingFrameHandler extends FrameHandler<PingWebSocketFrame> {

		@Override
		public void handle(ChannelHandlerContext ctx, PingWebSocketFrame frame) {
			frame.content().retain();
			ctx.channel().write(new PongWebSocketFrame(frame.content()));
		}
	}


	/**
	 * Base strategy used to handle incoming data (text or binary) frames.
	 * @see TextFrameHandler
	 * @see BinaryFrameHandler
	 */
	private abstract class DataFrameHandler<T extends WebSocketFrame, P> extends
			FrameHandler<T> {

		private List<P> payloads;

		@Override
		public void handle(ChannelHandlerContext ctx, T frame) {
			if (frame.isFinalFragment()) {
				try {
					handler.handleMessage(session, createMessage(Collections.singletonList(extractPayload(frame))));
				} catch (Exception e) {
					// TODO Error handling
					e.printStackTrace();
				}
			}
			else {
				this.payloads = new LinkedList<P>();
				this.payloads.add(extractPayload(frame));
			}
		}

		public void handleContinuation(ContinuationWebSocketFrame continuationFrame) {
			this.payloads.add(extractPayload(continuationFrame));
			if (continuationFrame.isFinalFragment()) {
				try {
					handler.handleMessage(session, createMessage(this.payloads));
				} catch (Exception e) {
					// TODO Error handling
					e.printStackTrace();
				}
			}
		}

		/**
		 * Extract a payload from the frame that can be stored when collating data.
		 */
		protected abstract P extractPayload(WebSocketFrame frame);

		/**
		 * Create a {@link WebSocketMessage}, collating all payloads as necessary.
		 */
		protected abstract WebSocketMessage<P> createMessage(List<P> payloads);
	}


	/**
	 * Strategy used to handle incoming {@link TextWebSocketFrame}s.
	 */
	private class TextFrameHandler extends DataFrameHandler<TextWebSocketFrame, String> {

		@Override
		protected String extractPayload(WebSocketFrame frame) {
			return frame.content().toString(CharsetUtil.UTF_8);
		}

		@Override
		protected WebSocketMessage<String> createMessage(List<String> payloads) {
			if (payloads.size() == 1) {
				return new TextMessage(payloads.get(0));
			}
			StringBuilder payload = new StringBuilder();
			for (String payloadPart : payloads) {
				payload.append(payloadPart);
			}
			return new TextMessage(payload);
		}

	}


	/**
	 * Strategy used to handle incoming {@link BinaryWebSocketFrame}s.
	 */
	private class BinaryFrameHandler extends
			DataFrameHandler<BinaryWebSocketFrame, ByteBuffer> {

		@Override
		protected ByteBuffer extractPayload(WebSocketFrame frame) {
			byte[] bytes = new byte[frame.content().readableBytes()];
			frame.content().readBytes(bytes);
			return ByteBuffer.wrap(bytes);
		}

		@Override
		protected WebSocketMessage<ByteBuffer> createMessage(List<ByteBuffer> payloads) {
			if(payloads.size() == 1) {
				return new BinaryMessage(payloads.get(0));
			}
			int totalSize = 0;
			for (ByteBuffer payloadPart : payloads) {
				totalSize += payloadPart.rewind().remaining();
			}
			ByteBuffer payload = ByteBuffer.allocate(totalSize);
			for (ByteBuffer payloadPart : payloads) {
				payload.put(payloadPart);
			}
			return new BinaryMessage(payload);
		}
	}
}
