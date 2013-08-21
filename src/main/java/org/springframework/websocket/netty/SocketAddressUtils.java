package org.springframework.websocket.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.springframework.util.Assert;

final class SocketAddressUtils {

	SocketAddressUtils() {

	}

	static InetSocketAddress convertToInetSocketAddress(SocketAddress socketAddress) {
		Assert.isInstanceOf(InetSocketAddress.class,  socketAddress);
		return (InetSocketAddress)socketAddress;
	}

}
