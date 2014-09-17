var UDPPORT = 11111;
var TCPPORT = 11112;
var HOST = '0.0.0.0';

var dgram = require('dgram');
var net = require('net');

var udpserver = dgram.createSocket('udp4');
var tcpserver = net.createServer();

var sessions = new Object();

udpserver.on('listening', function () {
    var address = udpserver.address();
    console.log('UDP Server listening on ' + address.address + ":" + address.port);
});

udpserver.on('message', function (message, remote) {
    var got = message.toString().split(":").slice(1);
    console.log(remote.address + ':' + remote.port +' - ' + got);
    if(got[0]=="i") {
        sessions[ got[1] ] = new Array(remote.address);
    }
    if(got[0]=="s") {
        sessions[ got[1] ].push(remote.address);
    }
    if(got[0]=="f") {
        sessions[ got[1] ].push(remote.address);
    }

});

tcpserver.on('connection', function(sock) {

    console.log(sessions);
    var conid = "";
    console.log('CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort);
    sock.on('data', function(data) {

        data = data.toString();
        if(data.toString().slice(-1)!=":"){conid+=data;}
        else{

            conid += data.slice(0,-1);
            console.log('DATA ' + sock.remoteAddress + ': ' + data + " " + conid);

            console.log(sessions[conid]);
            if(sessions[conid].length<3){
                sock.write('False')
                    console.log(sock.remoteAddress + " " + "Less than 3 data points, no spoofing");
            }
            else if(sessions[conid][1] == sessions[conid][0]){
                sock.write('False');
                console.log(sock.remoteAddress + " " + "Spoofing did not occur");
            }
            else{
                sock.write('True');
                sock.end();
                console.log(sock.remoteAddress + " " + "Spoofing seems to have occured");
            }

        }
    });
});

udpserver.bind(UDPPORT, HOST);
tcpserver.listen(TCPPORT,HOST);
