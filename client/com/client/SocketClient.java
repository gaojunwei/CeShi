package com.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.alibaba.fastjson.JSON;
import com.connection.codec.InfoDecoder;
import com.connection.codec.InfoEncoder;
import com.connection.message.AbsMessage;

public class SocketClient extends IoHandlerAdapter {
	
	private static Logger logger = Logger.getLogger(SocketClient.class);
	
	public static final int CONNECT_TIMEOUT = 3000;

	private String host;
	private int port;
	private String mac;
	private SocketConnector connector;
	private IoSession session;

	
	public static void main(String[] args) {
		new SocketClient("192.168.1.101", 8088,"ttt");
	}

	public SocketClient(String host, int port,String mac) {
		try{
			this.host = host;
			this.port = port;
			connector = new NioSocketConnector();
			connector.getFilterChain().addLast("codec", 
			 		new ProtocolCodecFilter(new MyCodecFactory(
							new InfoDecoder(Charset.forName("utf-8")),
							new InfoEncoder(Charset.forName("utf-8")))
							));
			connector.setHandler(this);
			// 连接到特定的remote地址，InetSocketAddress封装IP和port,Java网络编程规范，
			// 不提供直接的ip地址和端口的connect方法
			ConnectFuture connectFuture = connector.connect(new InetSocketAddress(this.host, this.port));
			// 等待建立连接
			connectFuture.awaitUninterruptibly();
	
			// 返回代表两端连接的一个对象,有读写方法以及获取本端网络设置的相关方法
			this.session = connectFuture.getSession();
			
			if(session==null || !session.isConnected())
			{
				logger.info("链接不成功");
			}
			logger.info("链接成功");
			while(true)
			{
				Map<String,Object> maps = new HashMap<String, Object>();
		        maps.put("order", "report_mac");
		        maps.put("mac", mac);
				String datas = JSON.toJSONString(maps);
				IoBuffer buffers = getDatabuffer(datas);
				
				session.write(buffers);
				try {
					Thread.sleep(1300*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	        
			
		}finally{
			if(connector!=null)
			{
				connector.dispose();
			}
		}
	}
	
	/**
	 * 封装消息
	 * @return
	 */
	public IoBuffer getDatabuffer(String data)
	{
		AbsMessage msgHeads = new   AbsMessage(data);
        //创建一个缓冲，缓冲大小为:消息头长度(8位)+消息体长度
        IoBuffer buffer = IoBuffer.allocate(8+msgHeads.getBodyLength());
        buffer.put(msgHeads.getStartFlage());
        buffer.putInt(msgHeads.getBodyLength());
        buffer.put(msgHeads.getBodyData());
        buffer.put(msgHeads.getEndFlage());
        //把消息体put进去
        buffer.flip();
		return buffer;
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		IoBuffer buf = (IoBuffer) message;
		byte StartFlage1 = buf.get();
		byte StartFlage2 = buf.get();
        int bodyLength=buf.getInt();
    	
        byte[] bytes = new byte[bodyLength];
        
        buf.get(bytes);
        
        
        String json = new String(bytes,"utf-8");
        logger.info("服务器返回："+json);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		logger.info("sessionCreated:"+session.getId());
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		logger.info("sessionOpened:"+session.getId());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("sessionClosed:"+session.getId());
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		logger.info("空闲:"+session.getId()+"，空闲次数："+session.getIdleCount(status));
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		cause.printStackTrace();
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		logger.info("messageSent:"+session.getId());
	}
}