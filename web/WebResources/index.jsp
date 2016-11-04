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

    <div id="wrap" class="container" style="padding-top:10%; background-color: lightpink">
        <div class="col-md-12" style="margin-bottom: 15px;">
            <div class="col-md-12  col-md-3"style="display:block;float:left">
                <li class="list-group-item" style="background:black;color:white;">联系人列表</li>
                <ul id="clientuserlist" class="list-group" style="display:block">
                    <li class="list-group-item">张三</li>
                    <li class="list-group-item">里斯</li>
                    <li class="list-group-item" >王五</li>
                    <li class="list-group-item">小六</li>
                </ul>
            </div>
            <div id="message" class="col-md-6" style="float:left;border:solid 1px lightblue;height:300px;"></div>
        </div>
        <div style="padding: 10px 10px 10px;" class="col-md-12">
            <form class="bs-example bs-example-form" role="form">
                <div class="input-group col-md-offset-3 col-md-9">
                    <span class="input-group-addon">我</span>
                    <input id="text" type="text" class="form-control" style="width:300px;" placeholder="请输入信息"/>
                    <button onclick="send()" class="btn btn-primary" type="button" data-toggle="button">发送消息</button>
                </div>
            </form>
        </div>
        <br/>
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
            <div class="col-md-6" style="float:left;border:solid 1px lightblue;height:300px;"></div>
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
            <div class="col-md-6" style="float:left;border:solid 1px lightblue;height:300px;"></div>
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
    //判断当前浏览器是否支持WebSocket
    if ('WebSocket' in window) {
        websocket = new WebSocket("ws://localhost:8080/websocket");
    }
    else {
        alert('当前浏览器 Not support websocket')
    }

    //连接发生错误的回调方法
    websocket.onerror = function () {
        setMessageInnerHTML("WebSocket连接发生错误");
    };

    //连接成功建立的回调方法
    websocket.onopen = function () {
        setMessageInnerHTML("WebSocket连接成功");
//        isNew = "true";
//        websocket.send(isNew + "|" + isClient + "|" + fromName);
    }

    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        setMessageInnerHTML(event.data);
    }

    //连接关闭的回调方法
    websocket.onclose = function () {
        setMessageInnerHTML("WebSocket连接关闭");
    }

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        closeWebSocket();
    }

    //将消息显示在网页上
    function setMessageInnerHTML(innerHTML) {
        document.getElementById('message').innerHTML += innerHTML + '<br/>';
    }

    //关闭WebSocket连接
    function closeWebSocket() {
        websocket.close();
    }

    //发送消息
    function send() {
        var message = document.getElementById('text').value;
        setMessageInnerHTML(message);
        isNew = "false";
        websocket.send(isNew + "|" + toName + "|" + message);

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

        $("#clientuserlist li").click(function () {
            toName = $(this).text();
            alert("客户名字："+toName);
        });

        $("#exit").click(function () {
            closeWebSocket();
        })

    });
</script>


</html>
