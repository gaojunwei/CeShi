package client;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

public class InfoDecoder implements MessageDecoder {
	private Charset charset;
	
	public InfoDecoder(Charset charset) {
        this.charset = charset;
    }
	
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		System.out.println("���������ѽ��յ�������"+in.remaining());
		if (in.remaining() < 8)//�����������ʱ��ʣ�೤��С��8��ʱ��ı������������׳���
        {
            return false;
        }
        if (in.remaining() > 1) {
            //�Ա��̵�reset�����ָܻ�positionλ��
            in.mark();
            ////ǰ6�ֽ��ǰ�ͷ��һ��int��һ��short��������ȡһ��int
            int len = in.getInt();//�Ȼ�ȡ�������ݳ���ֵ

            //�Ƚ���Ϣ���Ⱥ�ʵ���յ��ĳ����Ƿ���ȣ�����-2����Ϊ���ǵ���Ϣͷ�и�shortֵ��ûȡ
            if (len > in.remaining() - 2) {
                //���ֶϰ��������ûָ�positionλ�õ�����ǰ,������һ��, ���������ݣ���ƴ�ճ���������
                in.reset();
                return false;
            } else {
                //��Ϣ�����㹻
                in.reset();//���ûָ�positionλ�õ�����ǰ
                int sumLen = 6 + len;//�ܳ� = ��ͷ+����
                byte[] packArr = new byte[sumLen];
                in.get(packArr, 0, sumLen);
                IoBuffer buffer = IoBuffer.allocate(sumLen);
                buffer.put(packArr);
                buffer.flip();
                out.write(buffer);
                //�ߵ���������DefaultHandler��messageReceived����
                if (in.remaining() > 0) {//����ճ�������ø����ٵ���һ�Σ�������һ�ν���
                    return true;
                }
            }
        }
        return false;//����ɹ����ø�����н����¸���

	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		System.out.println("���������ѽ��յ�������"+in.remaining());
		if (in.remaining() < 8)//�����������ʱ��ʣ�೤��С��8��ʱ��ı������������׳���
        {
            return MessageDecoderResult.NEED_DATA;
        }
		byte StartFlage1 = in.get();//��ͷ1
		byte StartFlage2 = in.get();//��ͷ2
        int bodyLength=in.getInt();//��Ϣ�峤��
        
        System.out.println("�����"+(StartFlage1==(byte) 0xaa));
        System.out.println("�����"+(StartFlage2==(byte) 0xaa));
        System.out.println("��Ϣ�峤�ȣ�"+bodyLength);
        
        if(StartFlage1==(byte) 0xaa && StartFlage2==(byte) 0xaa && in.remaining()>=bodyLength+2)
        {
        	return MessageDecoderResult.OK;
        }
        return MessageDecoderResult.NEED_DATA;
        
        /*//�Ա��̵�reset�����ָܻ�positionλ��
        in.mark();
        //ǰ6�ֽ��ǰ�ͷ��һ��int��һ��short��������ȡһ��int
        int len = in.getInt();//�Ȼ�ȡ�������ݳ���ֵ

        //�Ƚ���Ϣ���Ⱥ�ʵ���յ��ĳ����Ƿ���ȣ�����-2����Ϊ���ǵ���Ϣͷ�и�shortֵ��ûȡ
        if (len > in.remaining() - 2) {
            //���ֶϰ��������ûָ�positionλ�õ�����ǰ,������һ��, ���������ݣ���ƴ�ճ���������
            in.reset();
            return false;
        } else {
            //��Ϣ�����㹻
            in.reset();//���ûָ�positionλ�õ�����ǰ
            int sumLen = 6 + len;//�ܳ� = ��ͷ+����
            byte[] packArr = new byte[sumLen];
            in.get(packArr, 0, sumLen);
            IoBuffer buffer = IoBuffer.allocate(sumLen);
            buffer.put(packArr);
            buffer.flip();
            out.write(buffer);
            //�ߵ���������DefaultHandler��messageReceived����
            if (in.remaining() > 0) {//����ճ�������ø����ٵ���һ�Σ�������һ�ν���
                return true;
            }
        }*/
        
        
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		//�Ա��̵�reset�����ָܻ�positionλ��
        in.mark();
        
		byte StartFlage1 = in.get();
		byte StartFlage2 = in.get();
        int bodyLength=in.getInt();
        
        System.out.println("�����"+(StartFlage1==(byte) 0xaa));
        System.out.println("�����"+(StartFlage2==(byte) 0xaa));
        System.out.println("��Ϣ�峤�ȣ�"+bodyLength);
        
		System.out.println("in.remaining()1-"+in.remaining());
		in.reset();//���ûָ�positionλ�õ�����ǰ
		System.out.println("in.remaining()2-"+in.remaining());
		
		int dlength = bodyLength+8;
		byte[] packArr = new byte[dlength];
		
		in.get(packArr, 0, dlength);

		IoBuffer buffer = IoBuffer.allocate(dlength);
        buffer.put(packArr);
        buffer.flip();
        System.out.println(buffer);
        out.write(buffer);
        
        return MessageDecoderResult.OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
		// TODO �Զ����ɵķ������
		
	}
	
}