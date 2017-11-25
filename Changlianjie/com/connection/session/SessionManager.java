package com.connection.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

public class SessionManager {
	private static Logger logger = Logger.getLogger(SessionManager.class);
    private final static Map<String, IoSession> sessions = new ConcurrentHashMap<>();
    private final static SessionManager manager = new SessionManager();

    public static SessionManager getManager() {
        return manager;
    }

    private SessionManager() {}

    public void add(IoSession ioSession,String mac) {
        if (ioSession == null || mac == null) return;
        int a = sessions.size();
        sessions.put(mac, ioSession);
        int b = sessions.size();
        if(a==b)
        {
        	logger.info("没有增加");
        }
        logger.info("增加前："+a);
        logger.info("增加后："+b);
    }

    public void remove(String mac) {
        if (mac == null || mac.length()==0) return;
        logger.info("移除前："+sessions.size());
        sessions.remove(mac);
        logger.info("移除后："+sessions.size());
    }

    public void removeAll() {
        if (sessions.size() == 0) return;
        logger.info("清空前："+sessions.size());
        sessions.clear();
        logger.info("清空后："+sessions.size());
    }
    
    public Map<String, IoSession> getSessionMap() {
    	return sessions;
    }
    
    public Map<String,Object> pushMsg(IoBuffer ioBuffer,String mac) {
    	Map<String,Object> rMap = new HashMap<String, Object>();
    	rMap.put("success", false);
    	
        for (String macKey: sessions.keySet()) {
        	if(macKey.equals(mac))
        	{
        		IoSession ioSession = sessions.get(macKey);
        		WriteFuture writeFuture = ioSession.write(ioBuffer).awaitUninterruptibly();
        		if(writeFuture.isWritten())
        		{
        			rMap.put("success", true);
        			rMap.put("reason", "信息发送成功");
        			return rMap;
        		}
    			rMap.put("reason", "信息发送失败");
    			return rMap;
        	}
        }
        rMap.put("reason", "连接不存在");
		return rMap;
    }
}