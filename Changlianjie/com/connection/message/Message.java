package com.connection.message;

/**
 * @author ZERO
 * @Description 消息常量
 */
public class Message {

    /**
     * 账号key，设置到session属性中
     */
    public static final String SESSION_KEY = "mac";

    /**
     * 服务端心跳请求命令
     */
    public static final String CMD_HEARTBEAT_REQUEST = "hb_request";
    /**
     * 客户端心跳响应命令
     */
    public static final String CMD_HEARTBEAT_RESPONSE = "hb_response";

}