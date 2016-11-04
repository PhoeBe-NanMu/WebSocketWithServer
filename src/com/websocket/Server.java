package com.websocket;

import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    public Server() throws Exception{
        ServerSocket ss=new ServerSocket(8080);
        Socket socket=ss.accept();
        InputStream in=socket.getInputStream();
        OutputStream out=socket.getOutputStream();
        byte[] buff=new byte[1024];
        int count=-1;
        String req="";
        count=in.read(buff);
        req=new String(buff, 0, count);
        System.out.println("握手请求："+req);
        String secKey=getSecWebSocketKey(req);
        System.out.println("secKey="+secKey);
        String response="HTTP/1.1 101 Switching Protocols\r\nUpgrade: "
        +"websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: "
        +getSecWebSocketAccept(secKey)+"\r\n\r\n";
        System.out.println("Sec-Accept="+getSecWebSocketAccept(secKey));
        out.write(response.getBytes());
        count=in.read(buff);
        System.out.println("接收的字节数："+count);
        for(int i=0;i<count-6;i++){
            buff[i+6]=(byte)(buff[i%4+2]^buff[i+6]);
        }
        System.out.println("接收的内容："+new String(buff, 6, count-6, "UTF-8"));
        byte[] pushHead=new byte[2];
        pushHead[0]=buff[0];
        String pushMsg="收到！收到！欢迎加入WebSocket世界！";
        pushHead[1]=(byte)pushMsg.getBytes("UTF-8").length;
        out.write(pushHead);
        out.write(pushMsg.getBytes("UTF-8"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200000);
                    try {
                        socket.close();
                        ss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    private String getSecWebSocketKey(String req){
        Pattern p=Pattern.compile("^(Sec-WebSocket-Key:).+",Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        Matcher m=p.matcher(req);
        if(m.find()){
            String foundstring=m.group();
            return foundstring.split(":")[1].trim();
        }
        else{
            return null;
        }
    }
    private String getSecWebSocketAccept(String key) throws Exception{
        String guid="258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        key+=guid;
        MessageDigest md= MessageDigest.getInstance("SHA-1");
        md.update(key.getBytes("ISO-8859-1"),0,key.length());
        byte[] shaHash=md.digest();
        BASE64Encoder encoder=new BASE64Encoder();
        return encoder.encode(shaHash);
    }
    public static void main(String[] args) throws Exception{
        new Server();
    }
}