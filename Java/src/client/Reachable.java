package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Reachable
{
    public static String hostnameReacheble(String subnet) throws UnknownHostException, IOException
    {
        String localEthernetAddress = null;
        String hostname = null;

        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                //System.out.println(i.getHostAddress());

                // Reader IP with /24 mask
                if(i.getHostAddress().contains(subnet))
                    localEthernetAddress = i.getHostAddress();
            }
        }

        int timeout=10;
        for (int i=1;i<255;i++){
            String host=subnet + "." + i;
            if (InetAddress.getByName(host).isReachable(timeout)){
                System.out.println(host + " is reachable");
                if(!host.equals(localEthernetAddress))
                    hostname = host;
            }
        }
        System.out.println("Hostname : " + hostname);
        System.out.println("Local ethernet address : " + localEthernetAddress);

        return hostname;
    }
}
