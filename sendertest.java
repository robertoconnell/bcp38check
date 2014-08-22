import java.io.IOException;
import java.net.*;
import java.lang.StringBuffer;
import org.savarese.vserv.tcpip.*;
/**
 *
 * @author hp
 */
public class sendertest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, SocketException, IOException  {
    
        //byte[] buffer = {10,23,12,31,43,32,24};
		byte[] buffer = {};
        //byte [] IP={-64,-88,1,106};
		byte [] IP={-64,-88,1,11};
		byte [] SOURCE={-64,-88,1,6}
		int IPasInt = OctetConverter.octetsToInt(IP);
		int SourceasInt = OctetConverter.octetsToInt(SOURCE);
		
		byte [] spare = {};
		
        InetAddress address = InetAddress.getByAddress(IP);
		System.out.println(address.getHostAddress());
		
        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, address, 11203
                );
		
		IPPacket toedit = new IPPacket(1024);

		
		toedit.setDestinationAsWord(IPasInt);
		toedit.setSourceAsWord(SourceasInt);
		StringBuffer out = new StringBuffer();
		toedit.getDestination(out);
		
		System.out.println("Test packet destination ip:");
		System.out.println(out);
		System.out.println(toedit.getDestinationAsWord());

        DatagramSocket datagramSocket = new DatagramSocket();

		
		byte[] raw = {};
		toedit.getData(raw);
		DatagramPacket tosend = new DatagramPacket(raw,raw.length);
		tosend.setData(raw);
		tosend.setPort(11111);
		System.out.println(tosend.getAddress());
		
		datagramSocket.send(tosend);
		
		
        System.out.println(InetAddress.getLocalHost().getHostAddress());

		System.out.println(IP);

    }
}