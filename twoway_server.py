import socket,sys
from impacket import ImpactDecoder, ImpactPacket
 
udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
udp.bind(("",11111))

sessions = {}


while True:
    data, addr = udp.recvfrom(1024) # buffer size is 1024 bytes
    print "received message:", data
    print "from:", addr[0]
    got = filter(None, data.split(":")) #Split and remove empty strings

    if got[0] == "i":  #If this is the init message, save real IP
        sessions[ got[1] ] = [addr[0]]

    elif got[0]=="s":
        assert got[1] in sessions #We've seen the IP before
        sessions[ got[1] ].append(addr[0])

    elif got[0]=="f":
        #We need to have an initial IP to do anything
        assert got[1] in sessions #We've seen the IP before
        sessions[ got[1] ].append(addr[0])

        break

print sessions

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind(("", 11112))
server.listen(5)
print "Listening for tcp"

while True:
    con,addr = server.accept()
    conid=con.recv(1024)
    if len(sessions[conid])<3: 
        con.send(str(False))
        break
    elif sessions[conid][1] == sessions[conid][0]: 
        con.send(str(False))
        break
    else:
        con.send( str(True) )
        break
server.close()
