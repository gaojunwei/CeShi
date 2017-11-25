package com.connection.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.alibaba.fastjson.JSON;
import com.connection.codec.InfoDecoder;
import com.connection.codec.InfoEncoder;
import com.connection.codec.MyCodecFactory;
import com.connection.model.DataUtil;
import com.connection.session.SessionManager;

public class ConnectionServer {
	private static Logger logger = Logger.getLogger(ConnectionServer.class);
	
    private static class ConnectionHandler extends IoHandlerAdapter {
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
            String clientIp = remoteAddress.getAddress().getHostAddress();
            
            logger.info("session created with IP: " + clientIp);
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
            String mac = session.getAttribute("mac")==null?"":session.getAttribute("mac").toString();
            logger.info("session closed："+mac);
            SessionManager.getManager().remove(mac);
        }

        @Override
        public void messageReceived(IoSession ioSession, Object message) {
        	IoBuffer buf = (IoBuffer)message;
        	HandlerEvent.getInstance().handle(ioSession, buf);
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) {
            logger.info("session in idle");
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) {
            logger.info("exception");
            session.closeOnFlush();
            String mac = session.getAttribute("mac")==null?"":session.getAttribute("mac").toString();
            SessionManager.getManager().remove(mac);
        }
    }

    private IoAcceptor ioAcceptor;

    public void sendMessage() {
    	Map<String, IoSession> sessionMap = SessionManager.getManager().getSessionMap();
    	logger.info("连接池中的连接数："+sessionMap.size());
    	for (String mackey:sessionMap.keySet()) {
    		Map<String,Object> map = new HashMap<String, Object>();
	        map.put("order", "report_mac");
	        map.put("mac","-你们收到了吗？"+mackey);
	        String data = JSON.toJSONString(map);
        	IoBuffer ioBuffer = DataUtil.getDatabuffer(data);
        	Map<String,Object> rMap = SessionManager.getManager().pushMsg(ioBuffer, mackey);
        	logger.info(rMap.toString());
		}
    }
    
    private void bind() {
        ioAcceptor = new NioSocketAcceptor();
        ioAcceptor.getFilterChain().addLast("logger", new LoggingFilter());
        
        //自定义加解码器工厂
        MyCodecFactory myCodecFactory = new MyCodecFactory(
                new InfoDecoder(Charset.forName("utf-8")),
                new InfoEncoder(Charset.forName("utf-8")));
        
        ioAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(myCodecFactory));
        ioAcceptor.setHandler(new ConnectionHandler());
        ioAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, IdleTime);
        try {
            ioAcceptor.bind(new InetSocketAddress(port));
            logger.info("Socket start,listening:"+port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true)
        {
        	sendMessage();
        	try {
				Thread.sleep(1300*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

    public void unBind() {
        if (ioAcceptor == null) return;

        ioAcceptor.dispose();
        SessionManager.getManager().removeAll();
    }
    private final int port;
    private final int IdleTime;
    
    public ConnectionServer(int port,int IdleTime) {
		this.port = port;
		this.IdleTime = IdleTime;
	}

	public static void main(String[] param) {
        ConnectionServer server = new ConnectionServer(8088,10);
        server.bind();
    }
}