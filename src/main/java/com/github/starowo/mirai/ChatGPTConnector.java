package com.github.starowo.mirai;

import com.google.gson.Gson;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class ChatGPTConnector extends WebSocketClient {

    public boolean open = false;
    public GPTRequest request;
    private final Gson gson = new Gson();

    public ChatGPTConnector(URI serverUri) {
        super(serverUri);
    }

    public void request(Contact contact, MessageChain msg, String content) {
        if(request != null)
            return;
        request = new GPTRequest(contact, msg);
        send(gson.toJson(new GPTBean(content)));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("已连接到服务器");
    }

    @Override
    public void onMessage(String message) {
        if(request != null) {
            request.process(gson.fromJson(message, GPTBean.class).msg);
            request = null;
        }
        open = false;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        connect();
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public static class GPTBean {
        public String msg;

        public GPTBean(){}

        public GPTBean(String msg) {
            this.msg = msg;
        }
    }

    public static class GPTRequest {

        public Contact contact;
        public MessageChain body;

        public GPTRequest(Contact contact, MessageChain body) {
            this.contact = contact;
            this.body = body;
        }

        public void process(String response) {
            contact.sendMessage(new MessageChainBuilder().append(new QuoteReply(body)).append(response).build());
        }

    }

}
