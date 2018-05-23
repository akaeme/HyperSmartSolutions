package client.ClientManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import client.FilteredTagReportListenerImplementation;
import client.Reachable;
import com.impinj.octane.AntennaConfig;
import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderMode;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.Settings;
import org.json.simple.JSONObject;

public class ReadTags 
{		
	private static String subnet = "192.168.222";
	
	private static int connectedAntennas = 3;
	
	private static String[] antennasName = {"Bottom", "Left", "Right", "Unused"};
	
	private static boolean[] antennasMaxRxSensitivities = {false,false,false,false};
	
	private static double[] antennasRxSensivityDbm = {-70,-70,-70,-70};
	
	private static boolean[] antennasMaxTxPower = {false,false,false,false};
	
	private static double[] antennasTxPowerDbm = {25,25,28,30};

    static ArrayList<String> epcsRead;

    static Client client = new Client();

    public static void main(String[] args)
    {
        try 
        {
            Reachable reachable = new Reachable();

        	String hostname = reachable.hostnameReacheble(subnet);

            if (hostname == null)
            {
                throw new Exception("Must specify the hostname property");
            }

            ImpinjReader reader = new ImpinjReader();

            System.out.println("Connecting");
            reader.connect(hostname);

            epcsRead = new ArrayList<String>();
            FilteredTagReportListenerImplementation fileteredRepor;
            Scanner s = new Scanner(System.in);
            Scanner sc = new Scanner(System.in);

            Settings settings = reader.queryDefaultSettings();

            ReportConfig report = settings.getReport();

            //Uncomment the information needed
            //setReport(ReportConfig report)

            //report.setIncludeChannel(true);
            report.setIncludeCrc(true);

            //report.setIncludeDopplerFrequency(true);
            report.setIncludeFastId(true);
            //report.setIncludeFirstSeenTime(true);
            //report.setIncludeGpsCoordinates(true);
            //report.setIncludeLastSeenTime(true);
            //report.setIncludePcBits(true);
            report.setIncludePeakRssi(true);
            //report.setIncludePhaseAngle(true);
            //report.setIncludeSeenCount(true);
            report.setIncludeAntennaPortNumber(true);
            report.setMode(ReportMode.Individual);

            // The reader can be set into various modes in which reader
            // dynamics are optimized for specific regions and environments.
            // The following mode, AutoSetDenseReader, monitors RF noise and interference and then automatically
            // and continuously optimizes the reader's configuration
            settings.setReaderMode(ReaderMode.AutoSetDenseReader);

            // set some special settings for antenna 1
            AntennaConfigGroup antennas = settings.getAntennas();
            //antennas.disableAll();
            antennas.enableAll();
            // Disable port 4 (not use)
            antennas.disableById(new short[]{4});
            //OR
            // Disable port 4
            //antennasConfs.get(3).setEnabled(false);

            //Antennas configurations
            System.out.println("HyperSmart antennas informations\n");
            ArrayList<AntennaConfig> antennasConfs = antennas.getAntennaConfigs();

            configureAntennas(antennasConfs, antennas);

            for(int i = 0; i < antennasConfs.size(); i++)
            {
                if(antennasConfs.get(i).isEnabled())
                {
                    System.out.println("Position: " + antennasConfs.get(i).getPortName());
                    System.out.println("Port number: " + antennasConfs.get(i).getPortNumber());

                    System.out.println("Is Max Rx Sensitivity: " + antennasConfs.get(i).getIsMaxRxSensitivity());
                    System.out.println("Rx sensivity in Dbm: " + antennasConfs.get(i).getRxSensitivityinDbm());

                    System.out.println("Is Max Tx Power: " + antennasConfs.get(i).getIsMaxTxPower());
                    System.out.println("TX power in Dbm: " + antennasConfs.get(i).getTxPowerinDbm() + "\n");
                }
            }

            boolean endOp = false;
            while(!endOp)
            {
                System.out.println("Choose from these choices");
                System.out.println("-------------------------");
                System.out.println("1 - New client");
                System.out.println("2 - Exit");

                int op = sc.nextInt();

                while(op != 1 && op != 2)
                {
                    System.out.println("Invalid option");

                    System.out.println("Choose from these choices");
                    System.out.println("-------------------------");
                    System.out.println("1 - New client");
                    System.out.println("2 - Exit");
                    op = sc.nextInt();
                }
                if(op == 1)
                    endOp = false;
                else
                {
                    endOp = true;
                    break;
                }

                //reader.setTagReportListener(new TagReportListenerImplementation());

                // connect a listener
                fileteredRepor =  new FilteredTagReportListenerImplementation();
                reader.setTagReportListener(fileteredRepor);


                System.out.println("Applying Settings");
                reader.applySettings(settings);


                System.out.println("Starting");
                reader.start();

                System.out.println("Press Enter to exit.");

                s.nextLine();

                reader.stop();

                // Send tags json array objects to database
                JSONObject[] tags = fileteredRepor.getTagsInfo(epcsRead);

                //
                if(tags == null || tags.length == 0)
                    System.out.println("No products found");
                else
                {
                    for(JSONObject obj: tags)
                    {
                        if(obj != null)
                        {
                            //System.out.println(obj.toJSONString());
                            if (obj.containsKey("epc")) {
                                epcsRead.add(obj.get("epc").toString());
                            }
                        }
                    }
                    System.out.println("Sending list do database");
                    // Uncomment to test
                    client.clientManagerSender(tags);
                }
            }
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
    
    public static void configureAntennas(ArrayList<AntennaConfig> antennasConfs, AntennaConfigGroup antennas) throws OctaneSdkException
    {
    	
    	for(int i = 0; i < connectedAntennas; i++)
    	{
    		antennasConfs.get(i).setPortName(antennasName[i]);
    		
    		antennas.getAntenna((short) i+1).setIsMaxRxSensitivity(antennasMaxRxSensitivities[i]);            
            antennas.getAntenna((short) i+1).setIsMaxTxPower(antennasMaxTxPower[i]);
            
            antennas.getAntenna((short) i+1).setTxPowerinDbm(antennasTxPowerDbm[i]);
            antennas.getAntenna((short) i+1).setRxSensitivityinDbm(antennasRxSensivityDbm[i]);
    	}	
    }
}
