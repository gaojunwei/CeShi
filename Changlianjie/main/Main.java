package main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

public class Main {
	
	 private final static Map<String, String> sessions = new ConcurrentHashMap<>();
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 10000; i++) {
			sessions.put("1", "a1");
		}
		
		
		System.out.println(sessions.size());
		System.out.println(sessions.get("1"));
		System.out.println(sessions.size());
		
		System.out.println(sessions.remove(""));
		System.out.println(sessions.size());
		
		for (String str:sessions.keySet()) {
			System.out.println("*"+str);
			System.out.println("*"+sessions.get(str));
		}
		
	}
}
