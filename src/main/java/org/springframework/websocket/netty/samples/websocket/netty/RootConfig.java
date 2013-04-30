
package org.springframework.websocket.netty.samples.websocket.netty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.websocket.netty.samples.websocket.netty.echo.DefaultEchoService;

@Configuration
@ComponentScan
public class RootConfig {

	@Bean
	public DefaultEchoService echoService() {
		return new DefaultEchoService("Hello %s");
	}

}
