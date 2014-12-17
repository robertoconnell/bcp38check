import java.awt.*;
import javax.swing.*;
import javax.jnlp.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Random;
import java.io.*;
import java.net.*;
import java.lang.StringBuffer;
import org.savarese.vserv.tcpip.*;
import com.savarese.rocksaw.net.*;
import static com.savarese.rocksaw.net.RawSocket.PF_INET;
/**
 *
 * @author hp
 */
public class sendertest {

    /**
     * @param args the command line arguments
     */
    public static void launchPacket(byte[] src, byte[] dst, int sport, int dport, String words) throws UnknownHostException, SocketException, IOException  {

        int DestAsInt = OctetConverter.octetsToInt(dst); 
        int SourceAsInt = OctetConverter.octetsToInt(src);
        InetAddress dstAddress = InetAddress.getByAddress(dst);

        byte[] content = words.getBytes();				

        UDPPacket pack = new UDPPacket(content.length + 28);
        pack.setIPVersion(4);
        pack.setIPHeaderLength(5);
        pack.setProtocol(IPPacket.PROTOCOL_UDP);
        pack.setTTL(255);

        pack.setDestinationAsWord(DestAsInt); 
        pack.setSourceAsWord(SourceAsInt);
        pack.setDestinationPort(dport);
        pack.setSourcePort(sport);

        pack.setUDPDataByteLength(content.length);
        pack.setUDPPacketLength(content.length + 8);

        byte[] buffer = new byte[pack.size()];
        pack.getData(buffer);
        System.arraycopy(content,0, buffer, 28, content.length);
        pack.setData(buffer);
        pack.computeUDPChecksum(true);
        pack.computeIPChecksum(true);
        pack.getData(buffer);

        RawSocket rs = new RawSocket();				
        rs.open(RawSocket.PF_INET, RawSocket.getProtocolByName("udp"));			
        rs.setIPHeaderInclude(true);
	System.out.println("Socket created " + rs.isOpen() + " Sending " + buffer);
	byte [] local_source = {127,127,127,127};
	rs.getSourceAddressForDestination(dstAddress,local_source);
        System.out.println(rs.write(dstAddress,buffer) + " " + local_source);
        rs.close();
    }
    public static boolean doTest() throws UnknownHostException, SocketException, IOException  {
        System.out.println("Starting test");
        /* byte [] DEST={-58,38,23,53}; //198.38.23.53, hostname moon */
        byte [] DEST={-58,38,18,95}; //198.38.23.53, hostname moon
        byte [] SOURCE={-58,38,18,97}; //198.38.18.97
        byte [] SPOOF={19,20,18,91}; //198.38.18.97
        /* int DestAsInt = OctetConverter.octetsToInt(DEST);  */
        /* int SourceAsInt = OctetConverter.octetsToInt(SOURCE); */
        Random rand = new Random();
        int RANDID = rand.nextInt(2100000000)+1;
        launchPacket(SOURCE,
                DEST,
                11112,
                11111,
                ":i:" + RANDID + ":"
                );
        launchPacket(SPOOF,
                DEST,
                11112,
                11111,
                ":s:" + RANDID + ":"
                );
        launchPacket(SOURCE,
                DEST,
                11112,
                11111,
                ":f:" + RANDID + ":"
                );
        String got;
        Socket clientSocket = new Socket("198.38.18.95", 11112);
        DataOutputStream outToServer = new DataOutputStream(
                clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
        outToServer.writeBytes(String.valueOf(RANDID) + ":");
        try{
            Thread.sleep(2000);
        }
        catch (Exception e){}
        outToServer.writeBytes("Testing");
        System.out.println("About to readline from server");
        /* try{Thread.sleep(2000);} catch(Exception e){System.out.println(e);} */
        got = inFromServer.readLine();
        System.out.println("got from server:" + got);
        clientSocket.close();
        if(got.equals("True")){return true;}
        else{return false;}
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException  {
        final JFrame frame = new JFrame("BCP38 Checker thing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JLabel label = new JLabel();
        Container content = frame.getContentPane();
        content.add(label, BorderLayout.CENTER);
        String message = "Jnln Hello Word";

        label.setText(message);

        JButton button = new JButton("I sure hope this works");

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try{
                    System.out.println("Button clicked");
                    boolean canSpoof = doTest();
                    if(canSpoof){
                        System.out.println("Spoofing is possible!");
                        label.setText("Spoofing possible");
                        frame.pack();
                        frame.show();
                    }
                    else{
                        System.out.println("No spoofing is possible!");
                        label.setText("No Spoofing!");
                        frame.pack();
                        frame.show();
                    }
                }
                catch (Exception e){ System.err.println("caught:" + e); }
            };
        };

        button.addActionListener(listener);
        content.add(button, BorderLayout.SOUTH);
        frame.pack();
        frame.show();


    }
}
