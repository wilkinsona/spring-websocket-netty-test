
package org.springframework.websocket.netty.samples.websocket.netty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.sockjs.SockJsService;
import org.springframework.web.socket.sockjs.transport.handler.DefaultSockJsService;
import org.springframework.websocket.netty.samples.websocket.netty.echo.DefaultEchoService;

@Configuration
@ComponentScan
public class RootConfig {

	@Bean
	public DefaultEchoService echoService() {
		return new DefaultEchoService("Hello %s");
	}

	@Bean
	public SockJsService sockJsService() {
		return new DefaultSockJsService(taskScheduler());
	}

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(4);

		return taskScheduler;
	}

}
