import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;
//import javax.jnlp.*;
import javax.imageio.*;
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
public class sendertest {

    public static void launchPacket(byte[] src, byte[] dst, int sport, int dport, String words) throws UnknownHostException, SocketException, IOException  {
        //Constructs and launches a packet using raw sockets

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
        
        /* byte [] DEST={104,-125,108,8};   //checker */
        /* byte [] DEST={-58,38,18,83};  //Falcon */
        byte [] DEST={-64,-88,1,9};  //Darkrook

        InetAddress local = InetAddress.getLocalHost();
        byte [] SOURCE=local.getAddress();
        /* byte [] SPOOF={-14,20,18,91}; //241.20.18.91 */
        byte [] SPOOF={8,8,8,8}; //241.20.18.91
                                      //241.0.0.0/8 is SPECIAL-IPV4-FUTURE-USE

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
        Socket clientSocket = new Socket("192.168.1.9", 11112);  // Darkrook
        /* Socket clientSocket = new Socket("104.131.108.8", 11112);  // Checker */
        /* Socket clientSocket = new Socket("198.38.18.83", 11112);   // Falcon */
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
        got = inFromServer.readLine();
        System.out.println("Got from server:" + got);
        clientSocket.close();
        if(got.equals("True")){return true;}
        else{return false;}
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException  {
        try
        {
            sendertest obj = new sendertest ();
            obj.run (args);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
    }
    public void run(String[] args) throws UnknownHostException, SocketException, IOException  {
        final JFrame frame = new JFrame("Network IP Spoofing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultLookAndFeelDecorated(true);
        frame.setSize(1000,1000);
        final JLabel label = new JLabel();
        final JPanel panel = new JPanel( new BorderLayout() );
        final Container content = frame.getContentPane();
        content.add(panel);
        panel.setBorder( new EmptyBorder(10,10,10,10));
        label.setPreferredSize(new Dimension(100, 100));
        label.setFont( new Font(label.getFont().getName(), Font.PLAIN, 20));
        panel.add(label, BorderLayout.NORTH);
        String message = "<html>Ready to run test";

        label.setText(message);

        final JButton button = new JButton("Click to test");


        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try{
                    System.out.println("Button clicked");
                    boolean canSpoof = doTest();
                    if(canSpoof){
                        label.setPreferredSize(new Dimension(300, 100));
                        label.setText("<html>Your network allows IP spoofing. <br> This can probably be fixed in your router configuration. <br> For more information visit http://checker.duckdns.org/failure.html");
                        ImageIcon image = new ImageIcon(ImageIO.read(sendertest.class.getResource("/bad.png")));
                        JLabel picture = new JLabel(image);
                        panel.add(picture, BorderLayout.CENTER);
                        frame.pack();
                        frame.show();
                    }
                    else{
                        label.setPreferredSize(new Dimension(300, 100));
                        label.setText("<html>Your network is properly configured and does not allow IP spoofing.");
                        ImageIcon image = new ImageIcon(ImageIO.read(sendertest.class.getResource("/good.png")));
                        JLabel picture = new JLabel(image);
                        panel.add(picture, BorderLayout.CENTER);
                        button.setVisible(false);
                        frame.pack();
                        frame.show();
                    }
                }
                catch (Exception e){ System.err.println("caught:" + e); }
            };
        };

        button.addActionListener(listener);
        panel.add(button, BorderLayout.SOUTH);
        frame.setSize(500,500);
        panel.setSize(300,300);
        frame.pack();
        frame.show();


    }
}
