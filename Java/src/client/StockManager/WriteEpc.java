package client.StockManager;

import client.Reachable;
import com.impinj.octane.*;

import cryptography.SGTIN96;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static client.Reachable.*;

public class WriteEpc implements TagReportListener, TagOpCompleteListener {

    static short EPC_OP_ID = 123;
    static short PC_BITS_OP_ID = 321;
    static int opSpecID = 1;
    static int outstanding = 0;
    private static ImpinjReader reader;

    private static int index;

    private static final String[] companiesList = {"Santal", "Tetley", "Serra da Estrela", "Branca", "Bonduelle", "Pingo Dodce", "3DPrinter"};
    private static final String[] productsList = {"Sumo Laranja", "Cha negro", "Agua 0.5L", "Grao de Bico", "Lata Milho", "Atum", "Peca plastico"};
    private static final String[] barcodeList = {"50605561000121", "50605562000121", "50605463020121", "50601564900121", "50602765070121", "50605566650121", "50605561122161"};
    private static final double[] priceList = {0.25, 1.20, 0.80, 0.95, 1.15, 12.07, 2.17, 20.89, 1.29, 1.12};
    private static final int company_prefix_length = 7;
    private static long[] SerialnumberList = {10000001,10000003,10000001,10000001,10000001,10000001,10000001,10000001};
    private static final int fileter_value = 5;

    private static Map<String,String> tagsEncoded;
    String epc = null;
    String newEpc = null;
    String gtin14 = null;
    int units = 0;

    private static Reachable reachable;
    private static String hostname;
    private static Settings settings;

    private static String subnet = "192.168.222";

    public static void main(String[] args) throws Exception
    {
        Scanner sc = new Scanner(System.in);

        int count = 0;

        index = 1;

        WriteEpc epcWriter = new WriteEpc();

        reachable = new Reachable();

        hostname = hostnameReacheble(subnet);

        if (hostname == null) {
            throw new Exception("Must specify the hostname property");
        }

        reader = new ImpinjReader();
        // Connect
        System.out.println("Connecting to " + hostname);
        reader.connect(hostname);

        // Get the default settings
        settings = reader.queryDefaultSettings();

        ReportConfig report = settings.getReport();
        report.setIncludePcBits(true);
        report.setIncludeFastId(true);

        // just use a single antenna here
        //settings.getAntennas().disableAll();
        settings.getAntennas().enableAll();
        settings.getAntennas().disableById(new short[]{4});

        //settings.getAntennas().getAntenna((short) 1).setEnabled(true);
        settings.getAntennas().getAntenna((short) 1).setTxPowerinDbm(20);
        settings.getAntennas().getAntenna((short) 2).setTxPowerinDbm(20);
        settings.getAntennas().getAntenna((short) 3).setTxPowerinDbm(20);

        // set session one so we see the tag only once every few seconds
        settings.getReport().setIncludeAntennaPortNumber(true);
        settings.setReaderMode(ReaderMode.AutoSetDenseReader);
        settings.setSearchMode(SearchMode.SingleTarget);
        settings.setSession(1);
        // turn these on so we have them always
        settings.getReport().setIncludePcBits(true);

        // Set periodic mode so we reset the tag and it shows up with its
        // new EPC
        settings.getAutoStart().setMode(AutoStartMode.Periodic);
        settings.getAutoStart().setPeriodInMs(2000);
        settings.getAutoStop().setMode(AutoStopMode.Duration);
        settings.getAutoStop().setDurationInMs(1000);

        tagsEncoded = new HashMap<String,String>();

        // Print products list to encode
        productsToEncode();
        System.out.println("\nSelect a valid option");

        /*index = sc.nextInt();
        while(index < 0  || index >9)
        {
            productsToEncode();
            System.out.println("\nSelect a valid option");
            index = sc.nextInt();
        }
        */

        // Before writing process
        long serial = getSerialNumber(productsList[index]);
        SerialnumberList[index] = serial;

        System.out.println("Encoding tags of : " + productsList[index] + " from " + companiesList[index]);
        epcWriter.run();
        count++;

        //After writing process, in the end!
        updateSerialNumber(productsList[index], SerialnumberList[index]);
    }

    void programEpc(String currentEpc, short currentPC, String newEpc)
            throws Exception {
        if ((currentEpc.length() % 4 != 0) || (newEpc.length() % 4 != 0)) {
            throw new Exception("EPCs must be a multiple of 16- bits: "
                    + currentEpc + "  " + newEpc);
        }

        if (outstanding > 0)
        {
            return;
        }
        
    	//System.out.println("Programming Tag ");
        //System.out.println("   EPC " + currentEpc + " to " + newEpc);

        TagOpSequence seq = new TagOpSequence();
        seq.setOps(new ArrayList<TagOp>());
        seq.setExecutionCount((short) 1); // delete after one time
        seq.setState(SequenceState.Active);
        seq.setId(opSpecID++);

        seq.setTargetTag(new TargetTag());
        seq.getTargetTag().setBitPointer(BitPointers.Epc);
        seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
        seq.getTargetTag().setData(currentEpc);

        TagWriteOp epcWrite = new TagWriteOp();
        epcWrite.Id = EPC_OP_ID;
        epcWrite.setMemoryBank(MemoryBank.Epc);
        epcWrite.setWordPointer(WordPointers.Epc);
        epcWrite.setData(TagData.fromHexString(newEpc));

        // add to the list
        seq.getOps().add(epcWrite);

        // have to program the PC bits if these are not the same
        if (currentEpc.length() != newEpc.length()) {
            // keep other PC bits the same.
            String currentPCString = PcBits.toHexString(currentPC);

            short newPC = PcBits.AdjustPcBits(currentPC,
                    (short) (newEpc.length() / 4));
            String newPCString = PcBits.toHexString(newPC);

            //System.out.println("   PC bits to establish new length: "+ newPCString + " " + currentPCString);

            TagWriteOp pcWrite = new TagWriteOp();
            pcWrite.Id = PC_BITS_OP_ID;
            pcWrite.setMemoryBank(MemoryBank.Epc);
            pcWrite.setWordPointer(WordPointers.PcBits);

            pcWrite.setData(TagData.fromHexString(newPCString));
            seq.getOps().add(pcWrite);
        }

        outstanding++;
        reader.addOpSequence(seq);
    }

    void run()
    {
        try
        {
            // Apply the new settings
            reader.applySettings(settings);


            // set up listeners to hear stuff back from SDK
            reader.setTagReportListener(this);
            reader.setTagOpCompleteListener(this);

            //products.printProducts();

            // Start the reader
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            System.out.println("Generating db object ... \n");
            //JSONObject DBObj = products.getDataToDB(productName, companyName);
            JSONObject DBObj = new JSONObject();

            DBObj.put("Price", priceList[index]);
            DBObj.put("SerialNumber", SerialnumberList[index]);
            DBObj.put("Name", companiesList[index] + " " + productsList[index]);
            DBObj.put("Barcode", Long.parseLong(barcodeList[index]));

            Object[] tmp = tagsEncoded.entrySet().toArray();

            if (tmp == null)
                throw new IllegalArgumentException("Invalid epc list");

            String[] epcs = new String[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                epcs[i] = tmp[i].toString().split("=")[1];
                System.out.println(epcs[i].toString());
            }
            DBObj.put("EPCS", epcs);
            DBObj.put("uint", units);

            System.out.println(DBObj.toJSONString());

            Client client = new Client();
            client.stockManagerSender(DBObj);

            System.out.println("Stopping  " + hostname);
            reader.stop();

            System.out.println("Done");
            System.out.println(units + " tags encoding of product: " + productsList[index] + " company: " + companiesList[index]);

        }
        catch (OctaneSdkException ex)
        {
            System.out.println(ex.getMessage());
        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public void onTagReported(ImpinjReader reader, TagReport report)
    {
        List<Tag> tags = report.getTags();
        SGTIN96 sgtin96 = new SGTIN96();

        //companyName = "Penacova";
        //productName = "Água mineral 1.5L";

        //JSONObject productsInfo = products.getEncodingInfo(companyName, productName);

        for (Tag t : tags)
        {
        	//System.out.println("EPC:" + t.getEpc() + " TID: " + t.getTid());
        	if(t.getTid().toHexString() != null && !t.getTid().toHexString().equals(""))
        	{
                if(!tagsEncoded.containsKey(t.getTid().toHexString()))
                {
                    if (t.isPcBitsPresent())
                    {
                        short pc = t.getPcBits();
                        String currentEpc = t.getEpc().toHexString();

                        try {

                            gtin14 = barcodeList[index];
                            //gtin14 = String.format("%01d", productsInfo.get("Indicator")) + String.format("%07d", productsInfo.get("CompanyPrefix")) + String.format("%05d", productsInfo.get("ItemReference")) + String.format("%01d", productsInfo.get("Check"));
                            //epc = sgtin96.encode(gtin14, (Integer) productsInfo.get("company_prefix_length"), (long) productsInfo.get("Serial_number"), (Integer) productsInfo.get("Filter_value"));
                            epc = sgtin96.encode( gtin14, (Integer) company_prefix_length, (long) SerialnumberList[index], (Integer) fileter_value);
                            newEpc = epc.toUpperCase();
                            programEpc(currentEpc, pc, newEpc);

                        } catch (Exception e) {
                            System.out.println("Failed To program EPC: " + e.toString());
                        }
                    }
                }
        	}
        }
    }

    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("TagOpComplete: ");
        for (TagOpResult t : results.getResults()) {
            //System.out.print("  EPC: " + t.getTag().getEpc().toHexString() + " TID: "  + t.getTag().getTid());
            if (t instanceof TagWriteOpResult) {
                TagWriteOpResult tr = (TagWriteOpResult) t;

                if (tr.getOpId() == EPC_OP_ID) {
                    System.out.print("  Write to EPC Complete: ");
                } else if (tr.getOpId() == PC_BITS_OP_ID) {
                    System.out.print("  Write to PC Complete: ");
                }
                //System.out.println(" result: " + tr.getResult().toString() + " words_written: " + tr.getNumWordsWritten());
                outstanding--;

                if(tr.getNumWordsWritten() == 6)
                {
                    System.out.println("EPC WRITTING... " + newEpc + " GTIN14: " + gtin14);
                    tagsEncoded.put(t.getTag().getTid().toHexString(), newEpc);
                    SerialnumberList[index] += 1;
                    units ++;
                }
            }
        }
    }

    public static long getSerialNumber(String productName) throws IOException, ParseException
    {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("/Users/rafaelalmeida/Documents/HyperSmartSolutions/Java/src/client/StockManager/products.json"));
        JSONObject jsonObject = (JSONObject) obj;
        //System.out.println(jsonObject.toJSONString());

        JSONObject products = (JSONObject) jsonObject.get("products");
        //System.out.println("products: " + products.toJSONString());

        long serial = Long.parseLong(products.get(productName).toString());
        System.out.println("Product name: " + productName + " \nSerial number: " + serial);

        return serial;
    }

    public static void updateSerialNumber(String productName, long serialNumber) throws IOException, ParseException
    {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("/Users/rafaelalmeida/Documents/HyperSmartSolutions/Java/src/client/StockManager/products.json"));
        JSONObject jsonObject = (JSONObject) obj;
        System.out.println(jsonObject.toJSONString());

        System.out.println("Product name: " + productName + " \nNew serial: " + serialNumber);

        JSONObject products = (JSONObject) jsonObject.get("products");
        products.remove(productName);
        products.put(productName, serialNumber);
        JSONObject list = new JSONObject();
        list.put("products", products);

        try (FileWriter file = new FileWriter("/Users/rafaelalmeida/Documents/HyperSmartSolutions/Java/src/client/StockManager/products.json"))
        {
            file.write(jsonObject.toString());
            System.out.println("Successfully updated json object to file...!!");
        }
    }

    public static void productsToEncode()
    {
        System.out.println("HyperSmart Stock Manager\n");
        for(int i = 0; i < productsList.length; i++)
        {
            System.out.println("\nOption: " + i);
            System.out.println("    Company:    " + companiesList[i]);
            System.out.println("    Product:    " + productsList[i]);
            System.out.println("    Price:      " + priceList[i]);
        }
    }
}
