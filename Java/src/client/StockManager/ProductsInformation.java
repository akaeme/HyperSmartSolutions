package client.StockManager;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductsInformation
{
    JSONObject productInfo;
    JSONObject companyInfo;
    private Map<String, JSONObject> companySpecs = new HashMap<String, JSONObject>();
    private Map<String, JSONObject> productSpecs = new HashMap<String, JSONObject>();;
    String companyName;
    String productName;

    public ProductsInformation()
        {
            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "Penacova";
            productName = "Água mineral 1.5L";
            productInfo.put("Product","Liquidos");
            productInfo.put("Price",  0.25);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 100000010);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix",  "605566");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);

            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);


            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "HyperAveia";
            productName = "Cereais Aveia";
            productInfo.put("Product","Ceral");
            productInfo.put("Price",1.20);
            productInfo.put("Name", productName);
            productInfo.put("Company", companyName);
            productInfo.put("SerialNumber",(long) 20000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000121");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);


            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "Millanesa";
            productName = "Esparguete";
            productInfo.put("Product","Massa");
            productInfo.put("Price",0.80);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 30000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);


            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "Cigala";
            productName = "Arroz Carolino";
            productInfo.put("Product","Cereal");
            productInfo.put("Price",0.95);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 40000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);



            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "Bom Petisco";
            productName = "Atum azeite";
            productInfo.put("Product","Enlatado");
            productInfo.put("Price",1.15);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 50000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);



            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "Feira";
            productName = "Tshirt M";
            productInfo.put("Product","Vestuário");
            productInfo.put("Price",12.07);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 60000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);



            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "EcoBook";
            productName = "Caderno A5";
            productInfo.put("Product","Escolar");
            productInfo.put("Price",2.17);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 70000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);



            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "Casio";
            productName = "Calculadora FX200";
            productInfo.put("Product","Technology");
            productInfo.put("Price",20.89);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 80000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);



            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "Mimosa";
            productName = "Leite UHT Meio gordo";
            productInfo.put("Product","Laticinios");
            productInfo.put("Price",1.29);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 90000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);


            productInfo = new JSONObject();
            companyInfo = new JSONObject();
            companyName = "SuperBock";
            productName = "Cerveija lata";
            productInfo.put("Product","Alcool");
            productInfo.put("Price",1.12);
            productInfo.put("Name",productName);
            productInfo.put("Company",companyName);
            productInfo.put("SerialNumber",(long) 11000001);
            productInfo.put("ItemReference",1);
            productInfo.put("BARCODE","50605566000126");

            companyInfo.put("CompanyPrefixLength",7);
            companyInfo.put("CompanyPrefix","");
            companyInfo.put("FilterValue",5);
            companyInfo.put("Indicator",5);
            companyInfo.put("Check",1);
            productSpecs.put(productName,productInfo);
            companySpecs.put(companyName, companyInfo);
        }

        public void updateBarcode(String product, String gtin14)
        {
            if(productSpecs.containsKey(product))
            {
                JSONObject tmp = productSpecs.get(product);
                tmp.put("Barcode", gtin14);
                productSpecs.put(product, tmp);
            }
        }
        public synchronized void updateSerial(String product)
        {

            //System.out.println("Updating serial number");
            JSONObject tmp;
            //System.out.println("Product contained in list? " + productSpecs.containsKey(product));

            if(productSpecs.containsKey(product))
            {
                tmp = productSpecs.get(product);
                //System.out.println("Object: " + tmp);
                long serial = (long) tmp.get("SerialNumber");
                //System.out.println("Current Serial: " + serial);
                serial = (long) (((int) serial) + 1);
                //System.out.println("NEW Serial: " + serial);
                tmp.put("SerialNumber", serial);
                productSpecs.put(product, tmp);
            }
            else
            {
                throw new IllegalArgumentException("This product does not exist");
            }
        }

        public JSONObject getEncodingInfo(String company, String product)
        {
            if(!productSpecs.containsKey(product))
                throw new IllegalArgumentException("This product does not exist");

            if(!companySpecs.containsKey(company))
                throw new IllegalArgumentException("This company does not exist");

            JSONObject tmp = new JSONObject();
            //System.out.println("Product: " + product + " company: " + company);
            JSONObject companyTmp = companySpecs.get(company);
            JSONObject productTmp = productSpecs.get(product);

            tmp.put("Indicator", companyTmp.get("Indicator"));
            tmp.put("CompanyPrefix", (long) Integer.parseInt((String) companyTmp.get("CompanyPrefix")));
            tmp.put("Filter_value", companyTmp.get("FilterValue"));
            tmp.put("company_prefix_length", companyTmp.get("CompanyPrefixLength"));
            tmp.put("Check", companyTmp.get("Check"));
            tmp.put("ItemReference", productTmp.get("ItemReference"));
            tmp.put("Serial_number", productTmp.get("SerialNumber"));
            tmp.put("gtin14", productTmp.get("BARCODE"));

            return tmp;
        }

    public JSONObject getDataToDB(String product, String company)
    {

        System.out.println("Finding product: " + product);

        if(product == null || product.equals(""))
            throw new IllegalArgumentException("Product name can not be null");


        if(!productSpecs.containsKey(product))
            throw new IllegalArgumentException("This product does not exist");

        if(!companySpecs.containsKey(company))
            throw new IllegalArgumentException("This company does not exist");

        JSONObject data = new JSONObject();
        JSONObject tmp = new JSONObject();
        JSONObject tmpCmp = new JSONObject();
        tmp = productSpecs.get(product);
        tmpCmp = companySpecs.get(company);

        data.put("Price", (Double) tmp.get("Price"));
        data.put("SerialNumber", tmp.get("SerialNumber"));
        data.put("Name", (String) (tmp.get("Name") + " " + company));
        data.put("Barcode",Long.parseLong(tmp.get("Barcode").toString()));

        return data;
    }

    public void printProducts()
        {
            System.out.println("Registered Products");
            for( Map.Entry<String, JSONObject> products : productSpecs.entrySet())
            {
                System.out.println("Product:          " + products.getValue().get("Name") +
                                   "\nType:             " + products.getValue().get("Product")+
                                   "\nPrice:            " + products.getValue().get("Price") + " €" +
                                   "\nCompany:          " + products.getValue().get("Company") + "\n");
            }
        }
}
