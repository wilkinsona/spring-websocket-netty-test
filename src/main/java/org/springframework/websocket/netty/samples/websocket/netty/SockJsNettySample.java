
package org.springframework.websocket.netty.samples.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SockJsNettySample {

	public void run(int port) throws Exception {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				RootConfig.class);

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("codec-http", new HttpServerCodec());
					pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
					pipeline.addLast("main", context.getBean(SockJsHttpMessageHandler.class));
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
		new SockJsNettySample().run(8080);
	}
}
