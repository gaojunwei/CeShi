package server;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import common.AbsMessage;

public class ImageServerIoHandler extends IoHandlerAdapter {
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {

		IoBuffer buf = (IoBuffer) message;
		HandlerEvent.getInstance().handle(buf);
		
		
		String data = "OK";
		AbsMessage msgHead = new AbsMessage(data);
		
        System.out.println("������������Ϣ���ȣ�"+(8+msgHead.getBodyLength()));
        IoBuffer buffer = IoBuffer.allocate(8+msgHead.getBodyLength());
        //����Ϣͷput��ȥ
        buffer.put(msgHead.getStartFlage());
        buffer.putInt(msgHead.getBodyLength());
        buffer.put(msgHead.getBodyData());
        buffer.put(msgHead.getEndFlage());
        //����Ϣ��put��ȥ
        buffer.flip();
		session.write(buffer);
		
	}
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("sessionCreated:"+session.getId());
	}
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed:"+session.getId());
	}
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		System.out.println("����:"+session.getId()+"�����д�����"+session.getIdleCount(status));
	}
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		System.out.println("messageSent:"+session.getId());
	}
	@Override
	public void inputClosed(IoSession session) throws Exception {
		System.out.println("inputClosed:"+session.getId());
		session.closeOnFlush();
	}
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println("��һ�ͻ��˴�����");
	}
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		cause.printStackTrace();
	}
}