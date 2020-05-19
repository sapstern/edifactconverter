package com.sapstern.openedifact.segmenttest;
import java.util.HashMap;
import java.util.Map;

public class GenDataORDERS01B {

	private String leadinORDERS01B = "UNB+UNOC:3+SENDER ID:ZZZ:A1B2C3D4E5+RECEIVER ID:ZZZ:A1B2C3D4E5+20151128:1037+1++++1++1'UNH+1+ORDERS:D:01B:UN:EAN010'BGM+220+PO357893+9'";
	private String leadoutORDERS01B = "UNT+40+1'UNZ+1+1'";
	
	private Map<String, String[]> map = null;
	
	public GenDataORDERS01B ()
	{
		map = new HashMap<String, String[]>();
		gen();
	}
	
	private void gen ()
	{
		// IMD 1
		{
			String xml =     "<S_IMD>" +
								"<D_7077>F</D_7077>"+
	          					"<C_C273>" +
	          						"<D_7008>PRODUCT 1</D_7008>" +
	          					"</C_C273>" +
	          				"</S_IMD>";
			
			String[] x = {"IMD+F++:::PRODUCT 1'", xml};
			
			map.put ("IMD01", x );
		}
	}
	
	public String getEdi (String[] element)
	{
		String edifact = map.get(element[0]+element[1])[0];
		return leadinORDERS01B + edifact + leadoutORDERS01B;
	}
	public String getXml (String[] element)
	{
		return map.get(element[0]+element[1])[1];
	}
}
