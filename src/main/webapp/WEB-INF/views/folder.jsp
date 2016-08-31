<!DOCTYPE html>
<html>
<head>
    <title>Calculator App Using Spring 4 WebSocket</title>
    <script src="js/sockjs-0.3.4.js"></script>
    <script src="js/stomp.js"></script>
    <script type="text/javascript">
        var stompClient = null;
        function setConnected(connected) {
            document.getElementById('connect').disabled = connected;
            document.getElementById('disconnect').disabled = !connected;
            document.getElementById('folderDiv').style.visibility = connected ? 'visible' : 'hidden';
            document.getElementById('folderList').innerHTML = '';
        }
        function connect() {
            var socket = new SockJS('/webstream/add');
			stompClient = Stomp.over(socket);
            stompClient.connect({}, function(frame) {
                setConnected(true);
                console.log('Connected: ' + frame);

                stompClient.subscribe('/topic/show', function(calResult){
                    var p = JSON.parse(calResult.body);
                	showResult(p.fList);
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
            stompClient.send("/list/add", {}, JSON.stringify({ 'folderName': name}));
        }
        function showResult(message) {
            var response = document.getElementById('vList');
            while (response.firstChild) {
                response.removeChild(response.firstChild);
            }
            for (var prp in message) {
                var p = document.createElement('p');
                p.style.wordWrap = 'break-word';
                p.appendChild(document.createTextNode(message[prp]));
                response.appendChild(p);
            }
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
    <div id="folderDiv">
        <label>Folder name:</label><input type="text" id="name" /><br/><br/>
        <button id="fetch" onclick="sendName();">Fetch folder</button>
        <p id="folderList">
            <label>Folder contains:</label><br/>
            <p id="vList" style="word-wrap: break-word">
            </p>
        </p>
    </div>
</div>
</body>
</html>
