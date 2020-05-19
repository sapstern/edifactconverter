package com.sapstern.openedifact.sax.parse.segments;

public abstract class AbstractEdifactField
{ 
	
	AbstractEdifactField()
	{
		
	}
	/**
	 * @param str
	 * @return
	 */
	protected String removeReleaseChar(String str, String releaseChar)
	{
		// added by 01ottfro
		// is this the best spot to add this functionality?
		StringBuffer buf = new StringBuffer();
		// System.err.println("debug removeReleaseChar " + str);
		int i = 0;
		boolean prev = false;
		while (i < str.length())
		{
			String s = str.substring(i, i + 1);
			if (prev)
			{
				prev = false;
				buf.append(s);
				i++;
			}
			else
				if (s.equals(releaseChar))
				{
					i++;
					prev = true;
				}
				else
				{
					buf.append(s);
					i++;
				}
		}

		return buf.toString();

	}

}
