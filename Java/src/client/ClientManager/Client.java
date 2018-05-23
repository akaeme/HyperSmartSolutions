package client.ClientManager;

import client.JsonUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class Client {

    private final String BinderIp = "tcp://192.168.43.203:5559";
    private static final Set<String> RESPONSE_KEYS = new HashSet<>(Arrays.asList("total", "products", "count", "bill"));

    ZMQ.Context context;
    ZMQ.Socket requester;
    public Client()
    {
        context = ZMQ.context(1);
        //  Socket to talk to server
        System.out.println("Connecting to HyperSmart Solutions server...");

        requester = context.socket(ZMQ.REQ);
        //requester.connect("tcp://localhost:5559");
        requester.connect(BinderIp);
    }

    /*
    public static void main(String[] args) throws ParseException, UnsupportedEncodingException {
        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to server
        System.out.println("Connecting to HyperSmart Solutions server...");

        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://localhost:5559");

        JsonUtils requests = new JsonUtils("Reader");
        requests.addRequest(71999999000012L, "30B47A11FEAB984000BAA627", 12232231L);
        requests.addRequest(71999999000013L, "30B47A11FEAB984000BAA628", 12232232L);
        requests.addRequest(71999999000012L, "30B47A11FEAB984000BAA629", 12232233L);
        String message = requests.toString();
        System.out.println("Sending request " + message);
        requester.send(message, 0);

        byte[] reply = requester.recv(0);

        JSONParser parser = new JSONParser();
        JSONObject response = (JSONObject) parser.parse(new String(reply));
        String type = (String) response.get("type");
        if(type.equals("Acknowledge")) {
            response = (JSONObject) response.get("payload");
            Set keys = response.keySet();
            boolean flag = true;
            for (String key : RESPONSE_KEYS) {
                if (!keys.contains(key)){
                    flag = false;
                }
            }
            if (flag){
                String bill = (String) response.get("bill");
                byte[] decodedBytes = Base64.getDecoder().decode(bill);
                bill = new String(decodedBytes);
                System.out.println(bill);
            }
        }else{
            System.out.print("Ups...");
        }
        requester.close();
        context.term();
        System.exit(0);
    }
    */

    public void clientManagerSender(JSONObject[] jsonProductsObject) throws ParseException
    {
        JsonUtils requests = new JsonUtils("Reader");

        for(JSONObject jsonObj : jsonProductsObject)
        {
            requests.addRequest(Long.parseLong(jsonObj.get("gtin14").toString()), (String)jsonObj.get("epc"), Long.parseLong(jsonObj.get("serial_number").toString()));
        }

        //requests.addRequest(71999999000012L, "30B47A11FEAB984000BAA627", 12232231L);
        //requests.addRequest(71999999000013L, "30B47A11FEAB984000BAA628", 12232232L);
        //requests.addRequest(71999999000012L, "30B47A11FEAB984000BAA629", 12232233L);
        String message = requests.toString();
        System.out.println("Sending request " + message);
        requester.send(message, 0);

        byte[] reply = requester.recv(0);

        JSONParser parser = new JSONParser();
        JSONObject response = (JSONObject) parser.parse(new String(reply));
        String type = (String) response.get("type");
        if(type.equals("Acknowledge")) {
            response = (JSONObject) response.get("payload");
            Set keys = response.keySet();
            boolean flag = true;
            for (String key : RESPONSE_KEYS) {
                if (!keys.contains(key)){
                    flag = false;
                }
            }
            if (flag){
                String bill = (String) response.get("bill");
                byte[] decodedBytes = Base64.getDecoder().decode(bill);
                bill = new String(decodedBytes);
                System.out.println(bill);
            }
        }else{
            System.out.print("Ups...");
        }
        requester.close();
        context.term();
        System.exit(0);
    }
}