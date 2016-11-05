package com.websocket;

import com.db.ChatSqlite;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint("/websocket")
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();

//    private static CopyOnWriteArraySet<UserMessage> userMessageSet = new CopyOnWriteArraySet<UserMessage>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //存储用户信息
    private UserMessage userMessage = new UserMessage();

    //是否为新注册
    private Boolean isNew;

    private ChatSqlite chatSqlite = new ChatSqlite();

    //是客户还是客服
    String clientOrServerClient ="客户";


    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        webSocketSet.add(this);     //加入set中
//        userMessage.setSessionId(session.getId());
//        userMessage = new UserMessage();
//        userMessageSet.add(userMessage);


        addOnlineCount();           //在线数加1
//        System.out.println("session.getId() : "+session.getId().toString());
        System.out.println("有一个新连接，当前在线人数为：" + getOnlineCount());
        sendUserInfo();
    }



    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一个连接关闭，当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        //打印message内容
        System.out.println("message: "+message);

        //解析来自客户端的数据
        parse(message);

        //如果不是初次注册信息，则视为发送的信息
        if (!isNew){

            //将聊天记录加入到 sql server
            addToHistory();

            //定向发送消息
            for(MyWebSocket item: webSocketSet){
                try {
                    if (userMessage.getToName().equals(item.userMessage.getFromName())){
                        item.sendMessage(userMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }

        } else{
            if (!userMessage.getClient()){
                addNewUser();
            } else{
                //定向发送消息到客服
                for(MyWebSocket item: webSocketSet){
                        if (userMessage.getToName().equals(item.userMessage.getFromName())) {
                            item.session.getBasicRemote().sendText("userInfo|客户|" + userMessage.getFromName().toString());
                            System.out.println("testestetsestestsestestsest");
                        }
                        continue;
                }
            }
        }

    }

    /**
     * 将聊天记录加入到 sql server
     */
    private void addToHistory() {
        if (!userMessage.getClient()){
            clientOrServerClient = "客服";
        }
        System.out.println("来自"+clientOrServerClient+"\""+userMessage.getFromName()+"\"的消息:" );
        System.out.println("发件人:"+userMessage.getFromName());
        System.out.println("收件人:"+userMessage.getToName());
        System.out.println("信息:"+userMessage.getMsg());
        Date date = new Date();
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mTimeStr = dataFormat.format(date);


        StringBuffer sqlstr = new StringBuffer();
        sqlstr.append("insert into chatOnline values(");
        sqlstr.append("'"+mTimeStr+"',");
        sqlstr.append("'"+userMessage.getFromName()+"',");
        sqlstr.append("'"+userMessage.getToName()+"',");
        sqlstr.append("'"+userMessage.getMsg()+"')");

        System.out.println(sqlstr.toString());
        chatSqlite.init();
//            String createTableSQL = "create table if not exists chatonline(mTime text,fromName text,toName text,msg text)";
        String createTableSQL = "IF NOT EXISTS (select * from dbo.sysobjects where xtype='U' and Name = 'chatOnline')\n" +
                "BEGIN\n" +
                " create table chatOnline(mTime char(20),fromName char(20),toName char(20),msg text); \n" +
                "END";
        chatSqlite.createTable(createTableSQL);
        chatSqlite.insertToSQl(sqlstr.toString());
        String querySQL = "select * from chatOnline";
        chatSqlite.querySQL(querySQL);
    }


    /**
     * 将新加入的用户加入到sql server
     */
    private void addNewUser() {
        if (!userMessage.getClient()){
            clientOrServerClient = "客服"+this.session.getId().toString();
            addNewServerUser(clientOrServerClient);
        } else{
//            addNewClientUser(clientOrServerClient+this.session.getId().toString());
        }

    }


    /**
     * 新的普通用户
     * @param clientOrServerClient 判断是客服还是用户
     */
    private void addNewClientUser(String clientOrServerClient) {
        StringBuffer sqlstr = new StringBuffer();
        sqlstr.append("if not exists(select * from clientUsers where name = '");
        sqlstr.append(userMessage.getFromName()+"')\n");
        sqlstr.append("insert into clientUsers values(");
        sqlstr.append("'"+userMessage.getFromName()+"',");
        sqlstr.append("'"+clientOrServerClient+"')");

        System.out.println(sqlstr.toString());
        chatSqlite.init();
//            String createTableSQL = "create table if not exists chatonline(mTime text,fromName text,toName text,msg text)";
        String createTableSQL = "IF NOT EXISTS (select * from dbo.sysobjects where xtype='U' and Name = 'clientUsers')\n" +
                "BEGIN\n" +
                " create table clientUsers(name char(20),info text); \n" +
                "END";
        chatSqlite.createTable(createTableSQL);
        chatSqlite.insertToSQl(sqlstr.toString());
        String querySQL = "select * from clientUsers";
        chatSqlite.querySQL(querySQL);
    }

    /**
     * 新的客服
     * @param clientOrServerClient
     */
    private void addNewServerUser(String clientOrServerClient) {
        StringBuffer sqlstr = new StringBuffer();
        sqlstr.append("if not exists(select name from serverUsers where name = '");
        sqlstr.append(userMessage.getFromName()+"')\n");
        sqlstr.append("insert into serverUsers values(");
        sqlstr.append("'"+userMessage.getFromName()+"',");
        sqlstr.append("'"+clientOrServerClient+"')");

        System.out.println(sqlstr.toString());
        chatSqlite.init();
//            String createTableSQL = "create table if not exists chatonline(mTime text,fromName text,toName text,msg text)";
        String createTableSQL = "IF NOT EXISTS (select * from dbo.sysobjects where xtype='U' and Name = 'serverUsers')\n" +
                "BEGIN\n" +
                " create table serverUsers(name char(20),info text); \n" +
                "END";
        chatSqlite.createTable(createTableSQL);
        chatSqlite.insertToSQl(sqlstr.toString());
        String querySQL = "select * from serverUsers";
        chatSqlite.querySQL(querySQL);
    }

    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误！");
        error.printStackTrace();
    }


    /**
     * session.getBasicRemote().sendText发送消息到目标用户
     * @param userManage
     * @throws IOException
     */
    public void sendMessage(UserMessage userManage) throws IOException{

        this.session.getBasicRemote().sendText("msgInfo|"+userManage.getMsg());
        //this.session.getAsyncRemote().sendText(message);
    }

    private void sendUserInfo() {
        try {
            String userInfo = getUserInfo();
            this.session.getBasicRemote().sendText("userInfo|客服"+userInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUserInfo() {
        chatSqlite.init();
        System.out.println("1111121212221");
        String createTableSQL = "IF NOT EXISTS (select * from dbo.sysobjects where xtype='U' and Name = 'serverUsers')\n" +
                "BEGIN\n" +
                " create table serverUsers(name char(20),info text); \n" +
                "END";
        chatSqlite.createTable(createTableSQL);
        System.out.println("000000002221");

        String querySQL = "select * from serverUsers";
        String result = chatSqlite.querySQLForUser(querySQL);
        System.out.println(result);

        return result;


    }

    /**
     * 对message进行解析
     * @param message 客户端发送来的信息
     */
    public void parse(String message){
        int stopFlags = 0;
        int[] stopStates = {0,0};
        for (int i = 0; i< message.length()&&stopFlags<2;i++){
            if ("|".equals(message.substring(i,i+1))){
                stopStates[stopFlags] = i;
                stopFlags++;
            }
        }

        //分别解析两次的信息
        if (message.substring(0,stopStates[0]).equals("true")){
            isNew = true;
            userMessage.setClient(message.substring(stopStates[0]+1,stopStates[1]).equals("true"));
            if (userMessage.getClient()){
                userMessage.setFromName("客户"+this.session.getId().toString());
                userMessage.setToName(message.substring(stopStates[1]+1,message.length()));
                System.out.println("发件人:"+userMessage.getFromName());
                System.out.println("收件人:"+userMessage.getToName());
                System.out.println("信息:"+userMessage.getMsg());
            }else{
                userMessage.setFromName(message.substring(stopStates[1]+1,message.length()));
            }
        } else{
            isNew = false;
            userMessage.setToName(message.substring(stopStates[0]+1,stopStates[1]));
            userMessage.setMsg(message.substring(stopStates[1]+1,message.length()));
        }
    }


    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }




}