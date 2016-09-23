<!DOCTYPE html>
<html>
<head>
    <title>Spring 4 WebSocket</title>
    <script src="js/sockjs-0.3.4.js"></script>
    <script src="js/stomp.js"></script>
    <style media="screen" type="text/css">
    body {
        margin: 20px;
        line-height: 3;
        font-family: sans-serif;

    }

    div {
        position: relative;
    }

    ul {
        text-indent: .5em;
        border-left: .25em solid gray;
    }

    ul ul {
        margin-top: -1.25em;
        margin-left: 1em;

    }

    li {
        position: relative;
        bottom: -1.25em;
    }

    li:before {
        content: "";
        display: inline-block;
        width: 2em;
        height: 0;
        position: relative;
        left: -.75em;
        border-top: .25em solid gray;
    }

    ul ul:before, h3:before, li:after {
        content: '';
        display: block;
        width: 1em;
        height: 1em;
        position: absolute;
        background: salmon;
        border: .25em solid white;
        top: 1em;
        left: .4em;
    }

    h3 {
        position: absolute;
        top: -1em;
        left: 1.75em;
    }

    h3:before {
        left: -2.25em;
        top: .5em;
    }
    </style>
    <script type="text/javascript">
        var stompClient = null;
        function setConnected(connected) {
            document.getElementById('connect').disabled = connected;
            document.getElementById('disconnect').disabled = !connected;
            document.getElementById('mainDiv').style.visibility = connected ? 'visible' : 'hidden';
        }
        function connect() {
            var socket = new SockJS('/webstream/fetch');
			stompClient = Stomp.over(socket);
			// Handle any errors that occur.
            socket.onerror = function(error) {
              console.log('WebSocket Error: ' + error);
            };
            stompClient.connect({}, function(frame) {
                setConnected(true);
                console.log('Connected: ' + frame);

                stompClient.subscribe('/user/topic/show', function(calResult){
                    var p = JSON.parse(calResult.body);
                	showResult(p);
                });
            });
        }
        function disconnect() {
            stompClient.disconnect();
            setConnected(false);
            console.log("Disconnected");
        }
        function sendName() {
            var name = document.getElementById('name').value;
            stompClient.send("/list/fetch", {}, JSON.stringify({ 'folderName': 'temp'}));
            var rootDir = document.getElementById('dirList');
            clearResult(rootDir);
            var rootDir = document.getElementById('rootDir');
            clearResult(rootDir);
            rootDir.appendChild(document.createTextNode(name));
        }
        function clearResult(rootDiv){
            while (rootDiv.firstChild) {
                rootDiv.removeChild(rootDiv.firstChild);
            }
        }
        function showResult(message) {
            if ("ADD" === message.state){
                var response = document.getElementById(message.parent[message.parent.length-1]+"_dir");
                var rootDir = document.getElementById('dirList');
                if (response != null){
                    rootDir = response;
                }
                var li = document.createElement('li');
                li.style.wordWrap = 'break-word';
                li.id = message.hashId;
                li.name = message.fileName;
                li.appendChild(document.createTextNode(message.fileName));
                rootDir.appendChild(li);
                if (message.directory){
                    var ulList = document.createElement('ul');
                    ulList.id = message.fileName+"_dir";
                    rootDir.appendChild(ulList);
                }
            }
            if ("DEL" === message.state){
                var rootDir = document.getElementById('dirList');
                var fileObj = document.getElementById(message.hashId);
                var parentDir = document.getElementById(message.parent[message.parent.length-1]+"_dir");
                if (parentDir != null){
                    rootDir = parentDir;
                }
                if (fileObj != null){
                    rootDir.removeChild(fileObj);
                }
            }

            //for (var prp in message) {
            //    var p = document.createElement('p');
            //    p.style.wordWrap = 'break-word';
            //    p.appendChild(document.createTextNode(message[prp]));
            //    response.appendChild(p);
            //}
        }
    </script>
</head>
<body>
<noscript><h2>Enable Java script and reload this page to run Websocket Demo</h2></noscript>
<h1>App Using Spring 4 WebSocket</h1>
<div>
    <div>
        <button id="connect" onclick="connect();">Connect</button>
        <button id="disconnect" disabled="disabled" onclick="disconnect();">Disconnect</button><br/><br/>
    </div>
    <div id="mainDiv">
        <label>Folder name:</label><input type="text" id="name" /><br/><br/>
        <button id="fetch" onclick="sendName();">Fetch folder</button>
        <div id="dirContainer">
            <h3 id="rootDir"></h3>
            <ul id="dirList"></ul>
        </div>
    </div>
</div>
</body>
</html>
