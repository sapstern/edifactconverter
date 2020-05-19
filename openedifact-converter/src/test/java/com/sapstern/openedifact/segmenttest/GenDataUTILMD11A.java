package com.sapstern.openedifact.segmenttest;
import java.util.HashMap;
import java.util.Map;

public class GenDataUTILMD11A {

	private String leadinUTILMD11A = "UNA:+.? 'UNB+UNOC:3+9903234000005:500+9900327000009:500+170928:0403+NJ83SR429'UNH+ENJ83SR429+UTILMD:D:11A:UN:5.1f'BGM+E01+4J83TF7HR'";
	private String leadoutUTILMD11A = "UNT+61+ENJ83SR429'UNZ+1+NJ83SR429'";
	
	private Map<String, String[]> map = null;
	
	public GenDataUTILMD11A ()
	{
		map = new HashMap<String, String[]>();
		gen();
	}
	
	private void gen ()
	{
		// AGR 1
		{
			String xml =     "<S_AGR>" +
	          					"<C_C543>" +
	          						"<D_7431>11</D_7431>" +
	          						"<D_7433>E01</D_7433>" +
	          					"</C_C543>" +
	          				"</S_AGR>";
			
			String[] x = {"AGR+11:E01'", xml};
			
			map.put ("AGR01", x );
		}
		// CAV 1
		{
			String xml =     "<S_CAV>" +
								"<D_7059>Z02</D_7059>" +
	          					"<C_C889>" +
	          						"<D_7011>XYZ</D_7011>" +
	          						"<D_3055>293</D_3055>" +
	          					"</C_C889>" +
	          				  "</S_CAV>";
			String[] x = {"CAV+XYZ::293'", xml};
			
			map.put ("CAV01", x );
		}
		// CCI 1
		{
			String xml =     "<S_CCI>" +
								"<D_7059>Z02</D_7059>" +
	          					"<C_C240>" +
	          						"<D_7037>E01</D_7037>" +
	          					"</C_C240>" +
	          				  "</S_CCI>";
			String[] x = {"CCI+Z02++E01'", xml};
			
			map.put ("CCI01", x );
		}
		// IMD 1
		{
			String xml =     "<S_IMD>" +
	          					"<C_C272>" +
	          						"<D_7081>Z14</D_7081>" +
	          					"</C_C272>" +
	          					"<C_C273>" +
	          						"<D_7009>Z07</D_7009>" +
	          					"</C_C273>" +
	          				"</S_IMD>";
			
			String[] x = {"IMD++Z14+Z07'", xml};
			
			map.put ("IMD01", x );
		}
		
		// FTX 1
		{
			String xml =	"<S_FTX>" +
								"<D_4451>ACB</D_4451>" +
								"<C_C108>" +
									"<D_4440>Das ist ein Test.</D_4440>" +
								"</C_C108>" +
							"</S_FTX>";
			
			String[] x = {"FTX+ACB+++Das ist ein Test.'", xml};
			
			map.put ("FTX01", x );
		}
		
		// FTX 2
		{
			String xml =	"<S_FTX>" +
								"<D_4451>ABO</D_4451>" +
								"<C_C107>" +
									"<D_4441>Z03</D_4441>" +
								"</C_C107>" +
								"<C_C108>" +
									"<D_4440>Abweichung aufgrund von Zuordnungsfehler</D_4440>" +
								"</C_C108>" +
							"</S_FTX>";
			
			String[] x = {"FTX+ABO++Z03+Abweichung aufgrund von Zuordnungsfehler'", xml};
			
			map.put ("FTX02", x );
		}
		
		// LOC 1
		{
			String xml =	"<S_LOC>" +
								"<D_3227>Z02</D_3227>" +
								"<C_C517>" +
									"<D_3225>1234</D_3225>" +
									"<D_1131>ZT1</D_1131>" +
									"<D_3055>293</D_3055>" +
								"</C_C517>" +
							"</S_LOC>";
			
			String[] x = {"LOC+Z02+1234:ZT1:293'", xml};
			
			map.put ("LOC01", x );
		}
		// LOC 2
		{
			String xml =	"<S_LOC>" +
								"<D_3227>237</D_3227>" +
								"<C_C517>" +
									"<D_3225>GASPOOLH99990018</D_3225>" +
								"</C_C517>" +
								"<D_5479>5</D_5479>" +
							"</S_LOC>";
			
			String[] x = {"LOC+237+GASPOOLH99990018+++5'", xml};
			
			map.put ("LOC02", x );
		}
		// RFF 1
		{
			String xml =	"<S_RFF>" +
								"<C_C506>" +
									"<D_1153>Z13</D_1153>" +
									"<D_1154>110015</D_1154>" +
								"</C_C506>" +
							"</S_RFF>";
			
			String[] x = {"RFF+Z13:110015'", xml};
			
			map.put ("RFF01", x );
		}
		// SEQ 1
		{
			String xml =	"<S_SEQ>" +
								"<D_1229>Z01</D_1229>" +
							"</S_SEQ>";
			
			String[] x = {"SEQ+Z01'", xml};
			
			map.put ("SEQ01", x );
		}	
	}
	
	public String getEdi (String[] element)
	{
		String edifact = map.get(element[0]+element[1])[0];
		return leadinUTILMD11A + edifact + leadoutUTILMD11A;
	}
	public String getXml (String[] element)
	{
		return map.get(element[0]+element[1])[1];
	}
}
