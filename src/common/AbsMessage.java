package common;

import java.nio.charset.Charset;

public class AbsMessage {
	/**
     * Э���ʽ����ͷ:2 byte,����:4 byte,Json������:n byte,	��β:2 byte;
     */
	//��Ϣͷ2���ֽ�
	private byte[] startFlage = new byte[2];
	//����ĳ���4���ֽ�
	private int bodyLength = 0;
	//��Ϣ������
	byte[] bodyData = null;
	//��Ϣ��β2���ֽ�
	private byte[] endFlage = new byte[2];

	public AbsMessage(String data) {
		//��ͷ
		this.startFlage[0] = (byte) 0xaa;
		this.startFlage[1] = (byte) 0xaa;
		//��Ϣ������
		this.bodyData = data.getBytes(Charset.forName("utf-8"));
		//��Ϣ�峤��
		this.bodyLength = this.bodyData.length;
		//��β
		this.endFlage[0] = (byte) 0x0a;//'\r'
		this.endFlage[1] = (byte) 0x0d;//'\n'
	}

	public byte[] getStartFlage() {
		return startFlage;
	}

	public int getBodyLength() {
		return bodyLength;
	}

	public byte[] getBodyData() {
		return bodyData;
	}

	public byte[] getEndFlage() {
		return endFlage;
	}
}
