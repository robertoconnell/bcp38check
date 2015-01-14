var UDPPORT = 11111;
var TCPPORT = 11112;
var HOST = '0.0.0.0'; //Should always be 0.0.0.0

var dgram = require('dgram');
var net = require('net');

var udpserver = dgram.createSocket('udp4');
var tcpserver = net.createServer();

var sessions = new Object(); //Associative array, format:  
//sessions[session_id]=[packet 1 ip, packet 2 ip, packet 3 ip]

udpserver.on('listening', function () {
    var address = udpserver.address();
    console.log('UDP Server listening on ' + address.address + ":" + address.port);
});

udpserver.on('message', function (message, remote) {
    var got = message.toString().split(":").slice(1); //Parts of the message are split by colons
    //Slice removes front colon
    //Got[0]=packet type Got[1]=session_id
    console.log("[*] UDP message: " + remote.address + ':' + remote.port +' - ' + got);
    if(got[0]=="i") { //Inital, unspoofed, packet
        sessions[ got[1] ] = new Array(remote.address);
    }
    if(got[0]=="s") { //Second, spoofed, packet
        if( !(sessions.hasOwnProperty(got[1]) && Array.isArray(sessions[got[1]])) ) {
            console.log("Session does not exist");
        }
        else{
            sessions[ got[1] ].push(remote.address);
        }
    }
    if(got[0]=="f") { //Final, unspoofed, conclusions packet
        if( !(sessions.hasOwnProperty(got[1]) && Array.isArray(sessions[got[1]])) ) {
            console.log("Session does not exist");
        }
        else{
            sessions[ got[1] ].push(remote.address);
        }
    }

});

tcpserver.on('connection', function(sock) {

    console.log(sessions);
    var session_id = "";
    console.log('[*] TCP Connection: ' + sock.remoteAddress +':'+ sock.remotePort);
    sock.on('data', function(data) {

        data = data.toString();
        if(data.toString().slice(-1)!=":") {//The char ":" indicates the end of the session id
            session_id+=data;
        }
        else {

            session_id += data.slice(0,-1); //Remove the colon from the end of the session id
            console.log('[*] Tcp DATA ' + sock.remoteAddress + ': ' + data + " " + session_id);

            if( !(sessions.hasOwnProperty(session_id) && Array.isArray(sessions[session_id])) ) {
                console.log("[!] session_id not in sessions");   
                sock.end();
                return;
            }
            console.log(sessions[session_id]);
            if(sessions[session_id].length<3) {
                console.log(sock.remoteAddress + " " + "[*] Less than 3 data points, no spoofing");
                sock.write('False');
                sock.end();
            }
            else if(sessions[session_id][1] == sessions[session_id][0]) {
                console.log(sock.remoteAddress + " " + "[*] Spoofing did not occur");
                sock.write('False');
                sock.end();
            }
            else{
                console.log(sock.remoteAddress + " " + "[*] Spoofing seems to have occured");
                sock.write('True');
                sock.end();
            }

        }
    });
});

udpserver.bind(UDPPORT, HOST);
tcpserver.listen(TCPPORT,HOST);
