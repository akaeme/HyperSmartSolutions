package client;

import org.json.simple.*;
import java.util.Arrays;

public class JsonUtils {
    JSONArray requests;
    JSONArray encodeRequests;
    String source;
    String type;

    public JsonUtils(String source){
        if(source.equals("Reader")){
            this.source = source;
            this.requests  = new JSONArray();
            this.type = "billRequest";
        }else if(source.equals("StockManager")){
            this.encodeRequests = new JSONArray();
            this.source = source;
            this.type = "encodeRequest";
        }else return;
    }

    public void addRequest(long gtin14, String epc, long serialNumber){
        JSONObject obj = new JSONObject();
        obj.put("gtin14", gtin14);
        obj.put("epc", epc);
        obj.put("serialNumber", serialNumber);
        this.requests.add(obj);
    }

    public void addEncodeRequest(long gtin14, String[] epc, Double price, String name, int unit, long serialNumber){
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        array.addAll(Arrays.asList(epc));
        obj.put("gtin14", gtin14);
        obj.put("epc", array.toString());
        obj.put("price", price);
        obj.put("name", name);
        obj.put("unit", unit);
        obj.put("serialNumber", serialNumber);
        this.encodeRequests.add(obj);
    }

    public String toString() {
        switch (this.type) {
            case "encodeRequest":
                JSONObject encodeReqs = new JSONObject();
                JSONObject encodePayload = new JSONObject();
                encodeReqs.put("src", this.source);
                encodeReqs.put("type", this.type);
                encodePayload.put("encodeRequests", this.encodeRequests);
                encodeReqs.put("payload", encodePayload.toString());
                return encodeReqs.toString();
            case "billRequest":
                JSONObject reqs = new JSONObject();
                JSONObject billPayload = new JSONObject();
                reqs.put("src", this.source);
                reqs.put("type", this.type);
                billPayload.put("billRequests", this.requests);
                reqs.put("payload", billPayload.toString());
                return reqs.toString();
            default:
                JSONObject emptyReqs = new JSONObject();
                return emptyReqs.toString();
        }

    }
}
