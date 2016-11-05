<!DOCTYPE html>
<html charset="utf-8">
<head>
    <title>Java后端WebSocket的Tomcat实现</title>
</head>
    <script type="text/javascript" src="./css/jquery-1.11.1.min.js"></script>
    <script type="text/javascript" src="./css/bootstrap.js"></script>
    <link rel="stylesheet" type="text/css" href="./css/bootstrap.css"/>

<body>

    <div id="navbar" class="navbar-fixed-top navbar-inverse" style="display: fixed" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">Welcome To Chatting Room</a>
            </div>
            <div>
                <ul class="nav navbar-nav">
                    <li><a href="#wrap">聊天室</a></li>
                    <li><a href="#history">历史记录</a></li>
                    <li><a href="#information">客户信息</a></li>
                    <li class="dropdown">
                        <a id="selectServer" href="#" class="dropdown-toggle" data-toggle="dropdown">点击这里选择客服
                            <b class="caret"></b>
                        </a>

                        <ul id="serveruserlist" class="dropdown-menu">
                            <li><a href="#">张三</a></li>
                            <li><a href="#">小六</a></li>
                            <li><a href="#">里斯</a></li>
                        </ul>
                    </li>
                    <li><a id="exit" href="#" style="color: #c7254e;font-weight: bold">退出</a></li>

                </ul>
            </div>
        </div>
    </div>

    <div id="wrap" class="container" style="padding-top:52px; width:1000px;margin:0,auto; background-color: #204d74">

        <div class="col-sm-4" style="margin-bottom:15px;float:left;background-color: #204d74;height:100%">
            <div class="col-sm-11"style="display:block">
                <li class="list-group-item" style="background:black;color:white;">客户列表</li>
                <ul id="clientuserlist" class="list-group" style="display:block">
                    <%--<li class="list-group-item">张三</li>--%>
                    <%--<li class="list-group-item">里斯</li>--%>
                    <%--<li class="list-group-item" >王五</li>--%>
                    <%--<li class="list-group-item">小六</li>--%>
                </ul>
            </div>
        </div>

        <div class="col-sm-8" style="float: left;height:100%;background-color: #eeeeee">

            <div id="message" class="col-sm-12" style="overflow:auto;position:relative;border:solid 1px lightblue;height:70%"></div>

            <div style="padding: 10px 10px 10px;height: 30%" class="col-sm-12">
                <form class="bs-example bs-example-form" role="form" style="height:100%">
                    <div class="input-group col-sm-offset-0 col-sm-12" style="height:100%">
                        <div style="height: 15%">
                            <span class="input-group-addon" style="width: 100%;background-color:#eeeeee ">表情</span>
                        </div>
                        <div style="height: 60%;padding-top: 5px">
                            <textarea id="text" style="width:100%;height:100%;background-color: #eeeeee" ></textarea>
                            <%--<input id="text" type="text" class="form-control" style="width:100%;height:100%;background-color: #eeeeee" placeholder="请输入信息"/>--%>
                        </div>
                        <div style="height: 15%;padding-top: 10px">
                            <button onclick="send()" class="btn btn-primary" style="float: right;margin: 3px" type="button"; data-toggle="button">发送消息</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

    </div>



    <div id="infomation" style="height:40px">
        <div style="float: left">
            <img src="web/WebResources/images.jpg">
        </div>
        <div style="float: left"></div>
    </div>


    <div id="history" class="container" style="padding-top:10%; background-color: lightgreen">
        <div class="col-md-12">
            <ul class="list-group col-md-3" style="display:block;float:left;">
                <li class="list-group-item" style="background:black;color:white;">历史记录</li>
                <li class="list-group-item">张三</li>
                <li class="list-group-item">里斯</li>
                <li class="list-group-item">王五</li>
                <li class="list-group-item">小六</li>
            </ul>
        </div>
    </div>

    <div id="information" class="container" style="padding-top:10%; background-color: yellow">
        <div class="col-md-12">
            <ul class="list-group col-md-3" style="display:block;float:left;">
                <li class="list-group-item" style="background:black;color:white;">查看信息</li>
                <li class="list-group-item">张三</li>
                <li class="list-group-item">里斯</li>
                <li class="list-group-item">王五</li>
                <li class="list-group-item">小六</li>
            </ul>
        </div>
    </div>

    <div id="foot" class="footer">退出</div>
</body>

<script type="text/javascript">
    var websocket = null;

    var isNew = "false";

    var fromName = "NULL";
    var toName = "NULL";
    var isClient = "false";
    var isGetMsg = 1;

    var messageInfo = null;

    //判断当前浏览器是否支持WebSocket
    if ('WebSocket' in window) {
        websocket = new WebSocket("ws://localhost:8080/websocket");
    }
    else {
        alert('当前浏览器 Not support websocket')
    }

    //连接发生错误的回调方法
    websocket.onerror = function () {
        isGetMsg = 1;
        setMessageInnerHTML("服务器出现故障，请稍后再试一下。");
    };

    //连接成功建立的回调方法
    websocket.onopen = function () {
        isGetMsg = 1;
        setMessageInnerHTML("您好！有什么可以帮助您的吗？");
//        isNew = "true";
//        websocket.send(isNew + "|" + isClient + "|" + fromName);
    }

    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        isGetMsg = 1;

        parseMsg(event.data);

        if (messageInfo != null){
            setMessageInnerHTML(messageInfo);
        }
    }

    //对收到消息解析
    function parseMsg(data) {
        var str = data.toString();
            var mIsStartWithMsgInfo = str.indexOf("msgInfo");
            var mIsStartWithUserInfo = str.indexOf("userInfo");
            if (mIsStartWithMsgInfo == 0){
                messageInfo = str.substring(8,str.length);
            } else if (mIsStartWithUserInfo ==0){
                var users = str.split("|");

                var userClientOrServer = users[1];
                var user = users[2];
                if (userClientOrServer == "客户"){
                    document.getElementById('clientuserlist').innerHTML +='<li class="list-group-item">'+user+'</li>';
                }
            }
    }

    //连接关闭的回调方法
    websocket.onclose = function () {
        isGetMsg = 1;
        setMessageInnerHTML("祝您生活愉快！再见。");
    }

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        closeWebSocket();
    }



    //将消息显示在网页上
    function setMessageInnerHTML(innerHTML) {

        var getMsg = '<div id="infomation" style="margin:0;padding:0;"> <div style="float: left" > <img style="margin: 3px" src="images/tupian.jpg"> </div> <div style="float: left;padding-left: 5px;padding-right: 5px"> <li  style="position: relative;margin: 2px;display: block;padding: 10px 15px;margin-bottom: -1px;background-color: #fff;border-radius: 4px;border: 1px solid #ddd;background-color: #b2e281 ">'+
                innerHTML+
                '</li> </div> <div style="clear: both;height: 20px"></div> </div>'
        var sendMsg = '<div id="infomation" style="margin:0;padding:0;"> <div style="float: right" > <img style="margin: 3px" src="images/sendtupian.jpg"> </div> <div style="float: right;padding-left: 5px;padding-right: 5px"> <li  style="position: relative;margin: 2px;display: block;padding: 10px 15px;margin-bottom: -1px;background-color: #fff;border-radius: 4px;border: 1px solid #ddd;background-color: #b2e281">'+
                innerHTML+
                '</li> </div> <div style="clear: both;height: 20px"></div> </div>'
        if (1 == isGetMsg){
            document.getElementById('message').innerHTML += getMsg;
        } else{
            document.getElementById('message').innerHTML += sendMsg;
        }
    }

    //关闭WebSocket连接
    function closeWebSocket() {
        websocket.close();
    }

    //发送消息
    function send() {
        if (fromName == "NULL"||toName == "NULL"){
            alert("您还没有选择会话对象，请返回选择")
        } else{
            var message = document.getElementById('text').value;
            isGetMsg = 0;
            setMessageInnerHTML(message);
            document.getElementById('text').value = '';
            isNew = "false";
            websocket.send(isNew + "|" + toName + "|" + message);

        }
    }
</script>
<script type="text/javascript">
    window.onload = function () {
        var h = window.innerHeight;
        var w = window.innerWidth;
        var contain = document.getElementsByClassName("container");
        for (var i = 0; i < contain.length; i++) {
            contain[i].style.height = h + "px";
            //contain[i].style.width=w+"px";
        }
    }
</script>

<script>
    $(function(){
        $("#serveruserlist li").click(function () {
            fromName = $(this).text();
            document.getElementById("selectServer").innerHTML="当前客服："+fromName;
            isNew = "true";
            websocket.send(isNew + "|" + isClient + "|" + fromName);
            alert("客服名字："+fromName);
        });

        $("#clientuserlist").delegate('li','click',function () {
            toName = $(this).text();

            alert("客户名字："+toName);
        });

        $("#exit").click(function () {
            closeWebSocket();
        })


    });
</script>


</html>
