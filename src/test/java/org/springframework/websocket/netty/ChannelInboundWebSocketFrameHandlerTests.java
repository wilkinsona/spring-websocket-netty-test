
package org.springframework.websocket.netty;

import org.junit.Ignore;

@Ignore
public class ChannelInboundWebSocketFrameHandlerTests {

	//FIXME

//	@Rule
//	public ExpectedException thrown = ExpectedException.none();
//
//	@Mock
//	private WebSocketConnection session;
//
//	@Mock
//	private ChannelHandlerContext ctx;
//
//	@Mock
//	private Channel channel;
//
//	@Captor
//	private ArgumentCaptor<WebSocketMessage<?>> messageCaptor;
//
//	private ChannelInboundWebSocketFrameHandler adapter;
//
//	@Before
//	public void setup() {
//		MockitoAnnotations.initMocks(this);
//		given(this.ctx.channel()).willReturn(this.channel);
//		this.adapter = new ChannelInboundWebSocketFrameHandler(this.session);
//	}
//
//	@Test
//	public void sessionMustNotBeNull() throws Exception {
//		thrown.expect(IllegalArgumentException.class);
//		thrown.expectMessage("Session must not be null");
//		new ChannelInboundWebSocketFrameHandler(null);
//	}
//
//	@Test
//	public void handlesClose() throws Exception {
//		CloseWebSocketFrame frame = new CloseWebSocketFrame();
//		this.adapter.messageReceived(this.ctx, frame);
//		verify(this.session).close(frame);
//	}
//
//	@Test
//	public void pingRespondsWithPong() throws Exception {
//		PingWebSocketFrame frame = new PingWebSocketFrame();
//		this.adapter.messageReceived(this.ctx, frame);
//		verify(this.channel).write(isA(PongWebSocketFrame.class));
//		assertEquals(2, frame.refCnt());
//	}
//
//	@Test
//	public void textFrameToPartialWebSocketHandler() throws Exception {
//		given(this.session.canHandlePartialMessages()).willReturn(true);
//		TextWebSocketFrame frame = new TextWebSocketFrame("text");
//		this.adapter.messageReceived(this.ctx, frame);
//		verify(this.session).handleMessage(messageCaptor.capture());
//		WebSocketMessage<?> message = messageCaptor.getValue();
//		assertThat(message, instanceOf(TextMessage.class));
//		assertThat(((TextMessage) message).getPayload(), equalTo("text"));
//		// FIXME isLast
//	}
//
//	@Test
//	public void binaryFrameToPartialWebSocketHandler() throws Exception {
//		given(this.session.canHandlePartialMessages()).willReturn(true);
//		byte[] bytes = {0,1,2};
//		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes));
//		this.adapter.messageReceived(this.ctx, frame);
//		verify(this.session).handleMessage(messageCaptor.capture());
//		WebSocketMessage<?> message = messageCaptor.getValue();
//		assertThat(message, instanceOf(BinaryMessage.class));
//		assertThat(((BinaryMessage) message).isLast(), equalTo(true));
//		assertThat(((BinaryMessage) message).getPayload().array(), equalTo(bytes));
//	}
//
//	@Test
//	public void textFrameFragmentsCollatedToWebSocketHandler() throws Exception {
//		TextWebSocketFrame frame1 = new TextWebSocketFrame(false, 0, "big");
//		ContinuationWebSocketFrame frame2 = new ContinuationWebSocketFrame(false, 0, "te");
//		ContinuationWebSocketFrame frame3 = new ContinuationWebSocketFrame(true, 0, "xt");
//		this.adapter.messageReceived(this.ctx, frame1);
//		this.adapter.messageReceived(this.ctx, frame2);
//		this.adapter.messageReceived(this.ctx, frame3);
//		verify(this.session).handleMessage(messageCaptor.capture());
//		WebSocketMessage<?> message = messageCaptor.getValue();
//		assertThat(message, instanceOf(TextMessage.class));
//		assertThat(((TextMessage) message).getPayload(), equalTo("bigtext"));
//		// FIXME isLast
//	}
//
//	@Test
//	public void binaryFrameFragmentsCollatedToWebSocketHandler() throws Exception {
//		BinaryWebSocketFrame frame1 = new BinaryWebSocketFrame(false, 0, Unpooled.wrappedBuffer(new byte[] {0, 1}));
//		ContinuationWebSocketFrame frame2 = new ContinuationWebSocketFrame(false, 0, Unpooled.wrappedBuffer(new byte[] {2, 3}));
//		ContinuationWebSocketFrame frame3 = new ContinuationWebSocketFrame(true, 0, Unpooled.wrappedBuffer(new byte[] {4, 5}));
//		this.adapter.messageReceived(this.ctx, frame1);
//		this.adapter.messageReceived(this.ctx, frame2);
//		this.adapter.messageReceived(this.ctx, frame3);
//		verify(this.session).handleMessage(messageCaptor.capture());
//		WebSocketMessage<?> message = messageCaptor.getValue();
//		assertThat(message, instanceOf(BinaryMessage.class));
//		assertThat(((BinaryMessage) message).getPayload().array(), equalTo(new byte[] { 0, 1, 2, 3, 4, 5 }));
//	}
//
//	@Test
//	public void cannotCollateTooMuch() throws Exception {
//		// FIXME Should not allow a remote client to consume too much memory
//	}
//
//	@Test
//	public void controlFrameCannotBeFragmented() throws Exception {
//		// FIXME
//	}
//
//	@Test
//	public void continuationNeedsPriorFrame() throws Exception {
//		// FIXME
//	}
//
//	@Test
//	public void textFrameCannotFollowFinFrame() throws Exception {
//		// FIXME
//	}
//
//	@Test
//	public void binaryFrameCannotFollowFinFrame() throws Exception {
//		// FIXME
//	}
//
	// FIXME transport errors
}
