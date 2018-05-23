package cryptography;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SGTIN96 
{
	// Table 6. The EPC SGTIN-96 bit allocation, header, and maximum decimal values. page 27.
    private final static Integer BIN = 2;
    private final static Integer HEX = 16;
    private final static String sgtin96_bin_header = "00110000";
    private final static Integer sgtin96_filter_value_bits = 3;
    private final static Integer sgtin96_partition_value_bits = 3;
    private final static Integer sgtin96_serial_number_bits = 38;
    private final static Integer sgtin96_length_bits = 96;
    private final static Integer sgtin96_length_hex = 24;
    private final static Map<Integer, int[]> sgtin96_company_prefix_len_partitions;

    static {
        // Table 7. SGTIN-96 Partitions. page 28.
        // Column order: (L), P, M, N Nd
        sgtin96_company_prefix_len_partitions = new HashMap<Integer, int[]>();
        sgtin96_company_prefix_len_partitions.put(12, new int[]{0, 40, 4, 1});
        sgtin96_company_prefix_len_partitions.put(11, new int[]{1, 37, 7, 2});
        sgtin96_company_prefix_len_partitions.put(10, new int[]{2, 34, 10, 3});
        sgtin96_company_prefix_len_partitions.put(9, new int[]{3, 30, 14, 4});
        sgtin96_company_prefix_len_partitions.put(8, new int[]{4, 27, 17, 5});
        sgtin96_company_prefix_len_partitions.put(7, new int[]{5, 24, 20, 6});
        sgtin96_company_prefix_len_partitions.put(6, new int[]{6, 20, 24, 7});
    }

    private static int[] getPartitionsByCompanyPrefixLengthInDigits(int company_prefix_length) {
        // column 3 (L)
        return sgtin96_company_prefix_len_partitions.get(company_prefix_length);
    }

    private static int[] getPartitionsByPartitionValue(int partition_value)
    {
        // column 1 (P)
        //System.out.println("\n PARTITION VALUE: " + partition_value);

        for (Map.Entry<Integer, int[]> parts : sgtin96_company_prefix_len_partitions.entrySet())
        {
            //System.out.println("Partition value " + partition_value + " -> " + parts.getKey() + " : " + parts.getValue()[0] + " " + parts.getValue()[1] + " " + parts.getValue()[2] + " " + parts.getValue()[3]);

            if (parts.getValue()[0] == partition_value)
        	{
                int[]rv = parts.getValue();
                int rv2[] = rv.clone();

                rv[0] = parts.getKey();

                //System.out.println("\n Structure to " + rv[0] + " = ");

                rv2[0] = partition_value;

                /*
                for(int b : rv2)

                    System.out.print(b + " ");
                */

                sgtin96_company_prefix_len_partitions.put(rv[0], rv2);

                /*
                System.out.println("\n Return = ");
                for(int a : rv)
                    System.out.print(a + " ");
                */

                return rv;
            }
        }
        throw new IllegalArgumentException("invalid partition value");
    }

    private static String longToBinaryWithFill(Long number, int bits) {
        // This is never called with any number larger than 40 bits, so Long is a good choice
        return zeroFill(Long.toBinaryString(number), bits);
    }

    private static String zeroFill(String s, int n) {
        int fill = n - s.length();
        String Zeroes = "";
        if (fill > 0) {
            Zeroes = new String(new char[fill]).replace("\0", "0");
        }
        return Zeroes + s;
    }

    private static String binaryToHex(String bin) {
        // bin_epc is 96 bits, so need to use BitInt for hex conversion
        return new BigInteger(bin, BIN).toString(HEX);
    }

    private static String hexToBinary(String hex) {
        return new BigInteger(hex, HEX).toString(BIN);
    }

    private static Long binaryToLong(String bin) {
        return Long.parseLong(bin, BIN);
    }

    private static Integer binaryToInt(String bin) {
        return Integer.parseInt(bin, BIN);
    }


    public static String encodeUPC(String upc, Long serial_number) {
        if (upc.length() != 12) {
            throw new IllegalArgumentException("UPC must be 12 digits long");
        }
        // UPC-12 barcodes in this use case have 6 digit company identifier
        // Table 5. SGTIN Filter Values. page 24.
        return encode(zeroFill(upc, 14), 6, serial_number, 0);
    }

    /**
     * Encodes an SGTIN-96 encoded EPC ID.
     *
     * Follows the procedures details in document "EPC Generation 1 Tag Data
     * Standards Version 1.1 Rev.1.27" which can be found at:
     * http://www.gs1.org/sites/default/files/docs/epc/tds_1_1_rev_1_27-standard-20050510.pdf
     *
     * @param  gtin14 - the 14 character GTIN
     * @param  company_prefix_length - The length L of the Company Prefix portion of the GTIN
     * @param  serial_number - A Serial Number S where 0 ≤ S < 238, or an UCC/EAN-128 Application Identifier 21
     * @param  filter_value - A Filter Value F where 0 ≤ F < 8
     * @return       a 24 character EPC
     */
    public static String encode(String gtin14, Integer company_prefix_length, Long serial_number, Integer filter_value)
    {
        // 3.4.2.1 SGTIN-96 Encoding Procedure. page 28.
        if (serial_number.toString().length() > 1 && serial_number.toString().charAt(0) == '0') {
            throw new IllegalArgumentException("serial number may not begin with 0");
        }
        
        int[] partitions = getPartitionsByCompanyPrefixLengthInDigits(company_prefix_length);
        
        if (partitions == null) {
            throw new IllegalArgumentException("company prefix length must be <=12 and >= 6");
        }
       
        if(serial_number < 10000000)
    	{
            throw new IllegalArgumentException("Serial_number with leading zeros");
    	}

        //System.out.println("L : " + company_prefix_length);
        Integer partition_value = partitions[0];
       // System.out.println("Partition value: "  + partition_value);
    	//System.out.println("partitions 0 " + partitions[0]);
    	//System.out.println("partitions 1 " + partitions[1]);
    	//System.out.println("partitions 2 " + partitions[2]);
    	//System.out.println("partitions 3 " + partitions[3]);
        
        if (Integer.parseInt(gtin14.substring(1, company_prefix_length+1)) >= Math.pow(10, company_prefix_length)) {
            throw new IllegalArgumentException("Company Prefix exceeded specified length");
        }
    
        Integer company_prefix_bits = partitions[1];
        Integer item_reference_and_indicator_bits = partitions[2];

        if (gtin14.length() != 14) {
            throw new IllegalArgumentException("GTIN must be 14 digits long");
        }


        Long company_prefix = Long.valueOf(gtin14.substring(1, (company_prefix_length + 1)));
        
        Long item_reference_and_indicator = Long.valueOf(gtin14.charAt(0) +
                gtin14.substring(company_prefix_length + 1, 13));
        
        String bin_filter_value = longToBinaryWithFill((long) filter_value, sgtin96_filter_value_bits);
        String bin_partition_value = longToBinaryWithFill((long) partition_value, sgtin96_partition_value_bits);
        String bin_company_prefix = longToBinaryWithFill(company_prefix, company_prefix_bits);
        String bin_item_reference = longToBinaryWithFill(item_reference_and_indicator, item_reference_and_indicator_bits);
        String bin_serial_number = longToBinaryWithFill(serial_number, sgtin96_serial_number_bits);

        String bin_epc = sgtin96_bin_header + bin_filter_value + bin_partition_value + bin_company_prefix + bin_item_reference + bin_serial_number;

        return zeroFill(binaryToHex(bin_epc), sgtin96_length_hex);
    }

    /**
     * Decodes an SGTIN-96 encoded EPC ID.
     *
     * Follows the procedures details in document "EPC Generation 1 Tag Data;
     * Standards Version 1.1 Rev.1.27" which can be found at;
     * http://www.gs1.org/sites/default/files/docs/epc/tds_1_1_rev_1_27-standard-20050510.pdf
     *
     * @param  sgtin96_epc - the SGTIN-96 encoded EPC ID
     * @return       HashMap<String, String> with the keys:
     * filter_value - Encoded Filter Value
     * item_reference - Item Reference
     * serial_number - Serial Number
     * gtin14 - Encoded GTIN-14
     */
    public static HashMap<String, String> decode(String sgtin96_epc) {
        // 3.4.2.2 SGTIN-96 Decoding Procedure. page 29.
        if (sgtin96_epc.length() != sgtin96_length_hex) {
            throw new IllegalArgumentException("EPC must be 24 characters long");
        }
        String binary = zeroFill(hexToBinary(sgtin96_epc), sgtin96_length_bits);
        //System.out.println(binary);

        String header = binary.substring(0, 8);
        if (!header.equals(sgtin96_bin_header)) {
            throw new IllegalArgumentException("EPC header does not correlate to SGTIN-96");
        }

        Integer filter_value = binaryToInt(binary.substring(8, 11));
        Integer partition_value = binaryToInt(binary.substring(11, 14));

        if (partition_value > 6) {
            throw new IllegalArgumentException("Partition value cannot be greater than 6");
        }
        
        int[] lookup = getPartitionsByPartitionValue(partition_value);
                
        Integer company_prefix_len_digits = lookup[0];
        Integer company_prefix_len_bits = lookup[1];
        
        /*
        for(int i = 0; i < lookup.length; i++)
        {
        	System.out.print("\n" +lookup[i] + " \n");
        }
        */

        Long long_company_prefix_value = binaryToLong(binary.substring(14, 14 + company_prefix_len_bits));
       // System.out.println("\n long_company_prefix_value: " + long_company_prefix_value);

        
        //System.out.println("\ncompany perfix value " + long_company_prefix_value);
        //System.out.println("len bits " + company_prefix_len_bits);
        //System.out.println("len digits " + company_prefix_len_digits);
        //System.out.println("Max company perfix " + Math.pow(10, company_prefix_len_digits+3));
        
        if (long_company_prefix_value >= Math.pow(10, company_prefix_len_digits)) {
            throw new IllegalArgumentException("Company Prefix exceeded specified length");
        }
        String company_prefix = zeroFill(long_company_prefix_value.toString(), company_prefix_len_digits);
        //System.out.println("company prefix: " + company_prefix);
        
        //System.out.println("company_prefix_len_digits: " + company_prefix_len_digits);
        
        Integer item_reference_and_indicator_len_digits = 13 - company_prefix_len_digits;
        Integer int_item_reference_and_indicator = binaryToInt(binary.substring(14 + company_prefix_len_bits, 58));
        
        //System.out.println("\n ->" + binary.substring(14 + company_prefix_len_bits, 58));
        //System.out.println(int_item_reference_and_indicator);
        
        if (int_item_reference_and_indicator >= Math.pow(10, (13-company_prefix_len_digits)) ) {
            throw new IllegalArgumentException("Item Reference and Indicator exceeded specified length");
        }
        String item_reference_and_indicator = zeroFill(int_item_reference_and_indicator.toString(), item_reference_and_indicator_len_digits);
        //System.out.println("\nitem: " + item_reference_and_indicator);

        String thirteen = item_reference_and_indicator.substring(0, 1) + company_prefix + item_reference_and_indicator.substring(1);
        //System.out.println("\n" + thirteen);
        
        int termA = 0;
        int termB = 0;
        for (int i = 1; i < thirteen.length(); i++){
            String c = thirteen.substring(i, i+1);
            if (i%2 == 0) {
                termB += Integer.parseInt(c);
            } else {
                termA += Integer.parseInt(c);
            }
        }
        int check_digit = Math.abs(((-3 * termA) - termB) % 10);
        //System.out.println("check digit: " + check_digit);
        String gtin14 = thirteen + String.valueOf(check_digit);
        //String gtin14 = thirteen + "0";


        Long serial_number = binaryToLong(binary.substring(58));

        HashMap<String, String> rv = new HashMap<String, String>();
        rv.put("indicator", item_reference_and_indicator.substring(0, 1));
        rv.put("filter_value", filter_value.toString());
        rv.put("item_reference", item_reference_and_indicator.substring(1));
        rv.put("serial_number", serial_number.toString());
        rv.put("check", "" + check_digit);
        rv.put("gtin14", gtin14.substring(0,13)+"1");
        //System.out.println("gtin14: " + rv.get("gtin14"));
        
        return rv;
    }
}
