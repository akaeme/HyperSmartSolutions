package cryptography;

import java.util.HashMap;

public class SGTIN96_tests {

	public static void main(String[] args) 
	{
		SGTIN96 sgtin = new SGTIN96();
		String itemReference = "09909";
		String companyPerfix = "1234567";
		String check = "2";
		String indicator = "7";
		
		String gtin14 = indicator + companyPerfix + itemReference + check;
		
		System.out.println(indicator + " " + companyPerfix + " " + itemReference + " " + check);
		
		Integer company_prefix_length = 7;
		Long serial_number =  (long) 12232231;				   
		Integer filter_value = 5;
		
		String epc = sgtin.encode(gtin14,company_prefix_length, serial_number, filter_value);

		System.out.println("Encoding EPC:" + epc);
		
		System.out.println("\n----------------DECODE----------\n");
		
		HashMap<String, String> result = sgtin.decode(epc);
		
		System.out.println("indicator : "+ result.get("indicator").toString());
		System.out.println("filter_value : " + result.get("filter_value").toString());
	  	System.out.println("item_reference : " + result.get("item_reference").toString());
	  	System.out.println("serial_number : "+ result.get("serial_number").toString());
	  	System.out.println("gtin14 : "+ result.get("gtin14").toString());

	  	
		System.out.println("\n ---- DECODE VALIDATION ---- \n");

	  	System.out.println("indicator : " + result.get("indicator").toString().equals(indicator.toString()));
	  	System.out.println("filter_value : " + result.get("filter_value").toString().equals(filter_value.toString()));
	  	System.out.println("item_reference : " + result.get("item_reference").toString().equals(itemReference.toString()));
	  	System.out.println("serial_number : "+ result.get("serial_number").toString().equals(serial_number.toString()));
	  	System.out.println("gtin14 : "+ result.get("gtin14").toString().equals(gtin14.toString()));
	  	
		System.out.println("\n ---- END VALIDATION ---- \n");
		
		/*
		String gtin14_3 = "00012345678905";
		//gtin14_3 = gtin14_3.replaceAll("\\s+","");
		Integer company_prefix_length_3 = 7;
		Long serial_number_3 =  (long) 10479832;				   
		Integer filter_value_3 = 5;
		
		String epc3 = sgtin.encode(gtin14_3,company_prefix_length_3, serial_number_3, filter_value_3);
		System.out.println("Encoding:" + epc3);
		HashMap<String, String> result3 = sgtin.decode(epc3);
		System.out.println("Decoding epc 3: " + result3.get("gtin14") + "\n");
		//00110000 101 111 0000000000110000001110010001000010010011001000000000000000100111111110100011011000
		 */
	}
}
