package com.connection.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
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
	private IoAcceptor ioAcceptor;
	private final int port;
    private final int IdleTime;
    
    public ConnectionServer(int port,int IdleTime) {
		this.port = port;
		this.IdleTime = IdleTime;
	}

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
        
        KeepAliveMessageFactoryImpl heartBeatFactory = new KeepAliveMessageFactoryImpl();
        //IdleStatus参数为BOTH_IDLE,及表明如果当前连接的读写通道都空闲的时候在指定的时间间隔getRequestInterval后发送出发Idle事件。
        KeepAliveFilter kaf = new KeepAliveFilter(heartBeatFactory,IdleStatus.BOTH_IDLE);
        kaf.setRequestInterval(10);
        //使用了 KeepAliveFilter之后，IoHandlerAdapter中的sessionIdle方法默认是不会再被调用的！ 所以必须加入这句话 sessionIdle才会被调用
        kaf.setForwardEvent(true);
        
        ioAcceptor.getFilterChain().addLast("heart", kaf);
        ioAcceptor.getFilterChain().addLast("logger", new LoggingFilter());
        
        
        //自定义加解码器工厂
        MyCodecFactory myCodecFactory = new MyCodecFactory(
                new InfoDecoder(Charset.forName("utf-8")),
                new InfoEncoder(Charset.forName("utf-8")));
        
        ioAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(myCodecFactory));
        
        ioAcceptor.setHandler(new ConnectionHandler());
        //ioAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, IdleTime);
        try {
            ioAcceptor.bind(new InetSocketAddress(port));
            logger.info("Socket start,listening:"+port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*while(true)
        {
        	sendMessage();
        	try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }*/
    }
    
    private static class KeepAliveMessageFactoryImpl implements KeepAliveMessageFactory {

		@Override
		public boolean isRequest(IoSession session, Object message) {
			logger.info("接收到的心跳包数据："+message+(message instanceof IoBuffer));
			IoBuffer in = (IoBuffer)message;
			logger.info("数据包长度："+in.remaining());
			if (in.remaining() < 8)//是用来当拆包时候剩余长度小于8的时候的保护，不加容易出错
	        {
	            return false;
	        }
	        if (in.remaining() > 1) {
	            //以便后继的reset操作能恢复position位置
	            in.mark();
	            ////前6字节是包头，一个int和一个short，我们先取一个int
	            byte StartFlage1 = in.get();//包头1
        		byte StartFlage2 = in.get();//包头2
	            int bodyLength = in.getInt();//先获取包体数据长度值

	            //比较消息长度和实际收到的长度是否相等，这里-2是因为我们的消息头有个short值还没取
	            if (bodyLength > in.remaining() - 2) {
	                //出现断包，则重置恢复position位置到操作前,进入下一轮, 接收新数据，以拼凑成完整数据
	                in.reset();//重置恢复position位置到操作前
	                return false;
	            }
	            //消息内容足够
                in.reset();//重置恢复position位置到操作前
                //取出完整的数据包（不包含包尾）
                int sumLen = 6 + bodyLength;
                byte[] packArr = new byte[sumLen];
                in.get(packArr, 0, sumLen);
                IoBuffer buffer = IoBuffer.allocate(sumLen);
                buffer.put(packArr);
                buffer.flip();
	            //分别取出包的部分信息
                StartFlage1 = buffer.get();//包头1
                StartFlage2 = buffer.get();//包头2
                bodyLength = buffer.getInt();//先获取包体数据长度值
                
                if(StartFlage1==(byte) 0xaa && StartFlage2==(byte) 0xaa && buffer.remaining()>=bodyLength)
                {
                	byte[] bytes = new byte[bodyLength];
                	buffer.get(bytes);
                	String json;
					try {
						json = new String(bytes,"utf-8");
						Map<String,Object> map = (Map<String,Object>)JSON.parse(json);
						String ht = map.get("order")==null?"":map.get("order").toString();
						if(ht.contains("h_t"))
						{
							logger.info("心跳数据："+json);
							return true;
						}
						logger.info("非心跳数据："+json);
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage());
					}
                }
                in.reset();//重置恢复position位置到操作前
	        }
	        return false;//下一个处理器进行处理
		}

		@Override
		public boolean isResponse(IoSession session, Object message) {
			return false;
		}

		@Override
		public Object getRequest(IoSession session) {
			return null;
		}

		@Override
		public Object getResponse(IoSession session, Object request) {
			logger.info("---------------接收到心跳包并响应数据---------------");
			Map<String,Object> maps = new HashMap<String, Object>();
	        maps.put("order","h_t");
			String datas = JSON.toJSONString(maps);
			IoBuffer buffers = DataUtil.getDatabuffer(datas);
			//session.write(buffers);
			return buffers;
			
		}

	}
    
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
            logger.info("************************************session in idle");
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) {
            logger.info("exception");
            session.closeOnFlush();
            String mac = session.getAttribute("mac")==null?"":session.getAttribute("mac").toString();
            SessionManager.getManager().remove(mac);
        }
    }

	public static void main(String[] param) {
        ConnectionServer server = new ConnectionServer(8088,10);
        server.bind();
    }
}