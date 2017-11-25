package com.connection.model;

import org.apache.mina.core.buffer.IoBuffer;

import com.connection.message.AbsMessage;

/**
 * @author gjw
 * @create 2017-11-21 13:53
 **/
public class DataUtil {
    /**
     * 封装消息
     * @return
     */
    public static IoBuffer getDatabuffer(String data)
    {
        AbsMessage msgHeads = new AbsMessage(data);
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
}
