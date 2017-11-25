package com.client;

public class Task implements Runnable {
	
	int a=0;
	public Task(int a) {
		this.a=a;
	}
	
	@Override
	public void run() {
		new SocketClient("192.168.1.104", 8088,"ttt"+a);
	}
	
}
