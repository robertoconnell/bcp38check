import socket,sys,random,time
from impacket import ImpactDecoder, ImpactPacket

def make_packet(dst, data, src=None):

    ip = ImpactPacket.IP()
    ip.set_ip_dst(dst)
    if src:
        ip.set_ip_src(src)

    #Create a new ICMP packet
    udp = ImpactPacket.UDP()

    udp.set_uh_sport(11112)
    udp.set_uh_dport(11111)
    
    udp.contains(ImpactPacket.Data(data))
    ip.contains(udp)
    
    udp.set_uh_sum(0)
    udp.auto_checksum = 0
    return ip

 
SPOOF = "7.8.8.8" #sys.argv[1]
SERVER = sys.argv[1]
RANDID = str(random.randint(1000000000,9999999999))
 
#Create socket for sending packets
s = socket.socket(socket.AF_INET, socket.SOCK_RAW, socket.IPPROTO_RAW)

#Send starting packet to server
init=make_packet(SERVER, ":i:" + RANDID + ":")
s.sendto(init.get_packet(), (SERVER, 0))
print "Sent initial packet"

#Create spoof packet
fake = make_packet(SERVER, ":s:" + RANDID + ":", SPOOF)
s.sendto(fake.get_packet(), (SERVER, 0))
print "Sent spoofed packet"

end = make_packet(SERVER, ":f:" + RANDID + ":")
s.sendto(end.get_packet(), (SERVER, 0))
print "Sent conclusion packet"

# sock = socket.socket(proto = socket.IPPROTO_ICMP, type = socket.SOCK_RAW)
# sock = socket.socket(socket.AF_INET, # Internet
#                      socket.SOCK_DGRAM) # UDP
socktcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socktcp.connect((SERVER,11112))
print "Established connection, sending randid:",RANDID
socktcp.send(RANDID + ":")
result = socktcp.recv(1024)
if result == "True": print "You are capable of spoofing your IP address."
else: print "You are not capable of spoofing your IP address."
