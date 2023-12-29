import java.io.*;
import java.net.*;

public class udp_send
{

  String host = "127.0.0.1";
  int port = 8889; //view mode 2  / 24k I/Q
  DatagramSocket dsock = null;
  DatagramPacket dpack = null;

  InetAddress address = null;

  public void send( byte[] data, int len )
  {
    try {
      if( address == null ) address = InetAddress.getByName( host );
      if( dsock == null ) dsock = new DatagramSocket();

      dpack = new DatagramPacket( data, len, address, port );
      dsock.send( dpack );
    } catch( Exception e ) {
      //e.printStackTrace();
    }
  }

}
