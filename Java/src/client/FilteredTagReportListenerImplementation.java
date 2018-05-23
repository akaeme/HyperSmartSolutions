package client;

import cryptography.SGTIN96;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class FilteredTagReportListenerImplementation implements
        TagReportListener {
    // an internal map to store unique stuff

    Map<String, Tag> map;
    int index = 0;

    public FilteredTagReportListenerImplementation() {
        map = new HashMap<String, Tag>();
    }
    
    public JSONObject[] getTagsInfo(ArrayList<String> epcsRead)
    {
        // Decode tags
        SGTIN96 sgtin96 = new SGTIN96();
        
        Map<String, String> tagsInfo = new HashMap<String,String>();
        JSONObject[] tags = new  JSONObject [map.size()];

        for(String key : map.keySet())
        {
        	String epc = map.get(key).getEpc().toString().replaceAll(" ", "");

        	if(!epcsRead.contains(epc))
        	{
                tagsInfo = sgtin96.decode(epc);

                JSONObject tagsInfoObj = new JSONObject();
                tagsInfoObj.put("gtin14", tagsInfo.get("gtin14"));
                tagsInfoObj.put("serial_number", tagsInfo.get("serial_number"));
                tagsInfoObj.put("epc", epc);
                tags[index] = tagsInfoObj;
                index++;

                System.out.println("Add product to list: " + tagsInfoObj.toJSONString());
            }
        }
        index = 0;
        map = null;
    	return tags;
    }
    
    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            String key;

            if (t.isFastIdPresent()) {
                key = t.getTid().toHexString();
            } else {
                key = t.getEpc().toHexString();
            }

            if (map.containsKey(key)) {
                continue;
            } else {
                map.put(key, t);
            }

            System.out.print("Count: " + map.size() + " EPC: " + t.getEpc().toString() + " TID: " + t.getTid());
            

            if (t.isAntennaPortNumberPresent()) {
                System.out.print(" antenna: " + t.getAntennaPortNumber());
            }

            if (t.isFirstSeenTimePresent()) {
                System.out.print(" first: " + t.getFirstSeenTime().ToString());
            }

            if (t.isLastSeenTimePresent()) {
                System.out.print(" first: " + t.getLastSeenTime().ToString());
            }

            if (t.isSeenCountPresent()) {
                System.out.print(" count: " + t.getTagSeenCount());
            }

            if (t.isRfDopplerFrequencyPresent()) {
                System.out.print(" doppler: " + t.getRfDopplerFrequency());
            }
            
            if (t.isPeakRssiInDbmPresent()) {
                System.out.print(" Rssi: " + t.getPeakRssiInDbm());
            }

            System.out.println("");
        }
    }
    
    
}
