package com.connection.server;


import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSON;
import com.connection.session.SessionManager;


/**
 * 消息事件处理
 */
public class HandlerEvent {
	private static Logger logger = Logger.getLogger(HandlerEvent.class);
    private static HandlerEvent handlerEvent;
    public static HandlerEvent getInstance() {
        if (handlerEvent == null) {
            handlerEvent = new HandlerEvent();
        }
        return handlerEvent;
    }
    public void handle(IoSession ioSession,IoBuffer buf) {
    	byte StartFlage1 = buf.get();
		byte StartFlage2 = buf.get();
        int bodyLength=buf.getInt();
        byte[] bytes = new byte[bodyLength];
        buf.get(bytes);
        String json = null;
		try {
			json = new String(bytes,"utf-8");
			logger.info("服务器收到："+json);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, String> daa = (Map<String, String>)JSON.parse(json);
		String mac = daa.get("mac");
		ioSession.setAttribute("mac", mac);
		SessionManager.getManager().add(ioSession, mac);
    }
}