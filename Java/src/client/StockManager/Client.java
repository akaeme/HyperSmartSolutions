package client.StockManager;

import client.JsonUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;

import java.io.UnsupportedEncodingException;

public class Client
{
    ZMQ.Context context;
    ZMQ.Socket requester;
    JsonUtils encodeRequests;
    private final String BinderIp = "tcp://192.168.43.203:5559";


    public Client()
    {

        context = ZMQ.context(1);

        //  Socket to talk to server
        System.out.println("Connecting to HyperSmart Solutions server...");

        requester = context.socket(ZMQ.REQ);
        requester.connect(BinderIp);
    }

    /*
    public static void main(String[] args) throws ParseException, UnsupportedEncodingException
    {
        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to server
        System.out.println("Connecting to HyperSmart Solutions server...");

        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://localhost:5559");

        JsonUtils encodeRequests = new JsonUtils("StockManager");
        String[] epcs = {"40B47A11FEAB984000BAA627","40B47A11FEAB984000BAA628","40B47A11FEAB984000BAA629"};
        encodeRequests.addEncodeRequest(123456789L, epcs, 2.2, "Leite Agros 1L", 50, 12232231L);
        String message = encodeRequests.toString();
        System.out.println("Sending request " + message);
        requester.send(message, 0);

        byte[] reply = requester.recv(0);
        System.out.println(new String(reply));
        JSONParser parser = new JSONParser();
        JSONObject response = (JSONObject) parser.parse(new String(reply));

        String type = (String) response.get("type");
        if(type.equals("Acknowledge")) {
            System.out.println("Transaction done! See you later Alligator!");
        } else {
            System.out.println("Transaction Error.....\n Try again later!");
        }
        System.exit(0);
    }
    */

    public void stockManagerSender(JSONObject stockObject) throws ParseException, UnsupportedEncodingException
    {
        encodeRequests = new JsonUtils("StockManager");

        String[] epcs = (String[]) stockObject.get("EPCS");

        encodeRequests.addEncodeRequest((Long) stockObject.get("Barcode"), epcs, (Double) stockObject.get("Price"), (String) stockObject.get("Name"), (Integer) stockObject.get("uint"), (Long) stockObject.get("SerialNumber"));

        //String[] epcs = {"40B47A11FEAB984000BAA627","40B47A11FEAB984000BAA628","40B47A11FEAB984000BAA629"};
        //encodeRequests.addEncodeRequest(123456789L, epcs, 2.2, "Leite Agros 1L", 50, 12232231L);
        String message = encodeRequests.toString();
        System.out.println("Sending request " + message);
        requester.send(message, 0);

        byte[] reply = requester.recv(0);
        System.out.println(new String(reply));
        JSONParser parser = new JSONParser();
        JSONObject response = (JSONObject) parser.parse(new String(reply));

        String type = (String) response.get("type");
        if(type.equals("Acknowledge")) {
            System.out.println("Transaction done! See you later Alligator!");
        } else {
            System.out.println("Transaction Error.....\n Try again later!");
        }
        System.exit(0);
    }
}
