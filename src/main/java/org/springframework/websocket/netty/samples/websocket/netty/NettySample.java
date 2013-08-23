
package org.springframework.websocket.netty.samples.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.sockjs.SockJsService;
import org.springframework.websocket.netty.handlers.BadRequestHttpMessageHandler;
import org.springframework.websocket.netty.handlers.PipelineEndHttpMessageHandler;
import org.springframework.websocket.netty.handlers.SockJsHttpMessageHandler;
import org.springframework.websocket.netty.handlers.StaticContentHttpRequestHandler;
import org.springframework.websocket.netty.handlers.WebSocketUpgradeHttpMessageHandler;

public class NettySample {

	public void run(int port) throws Exception {
		final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				RootConfig.class);

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		final ChannelHandler badRequestHandler = new BadRequestHttpMessageHandler();
		final ChannelHandler pipelineEndHandler = new PipelineEndHttpMessageHandler();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();

					pipeline.addLast(new HttpServerCodec());
					pipeline.addLast(new HttpObjectAggregator(65536));

					pipeline.addLast(badRequestHandler);

					pipeline.addLast(new StaticContentHttpRequestHandler("/pages"));

					WebSocketHandler webSocketHandler = context.getBean(WebSocketHandler.class);
					WebSocketUpgradeHttpMessageHandler webSocketUpgradeHandler =
							new WebSocketUpgradeHttpMessageHandler(webSocketHandler, "/echoWebSocket");
					pipeline.addLast(webSocketUpgradeHandler);

					SockJsService sockJsService = context.getBean(SockJsService.class);
					SockJsHttpMessageHandler sockJsHandler =
							new SockJsHttpMessageHandler(sockJsService, webSocketHandler, "/echoSockJS");
					pipeline.addLast(sockJsHandler);

					pipeline.addLast(pipelineEndHandler);
				}
			});
			Channel ch = b.bind(port).sync().channel();
			ch.closeFuture().sync();
		}
		finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			context.close();
		}
	}


	public static void main(String[] args) throws Exception {
		new NettySample().run(8081);
	}
}
