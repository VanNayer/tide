/**
 *    Free Tide Mini is a free harmonic tide predictor
 *    Copyright (C) 2008  Regis COSNIER
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ndenayer;

import java.io.IOException;
import java.io.Reader;

public class HarmonicFile
{
	private int type;
	private String fileName = "";
	private Constituents constituents;
	private int unitTo = UNIT_METER;

	public static int TYPE_ASCII;
	public static int BINARY_TCD;

	public final static int UNIT_METER = 0;
	public final static int UNIT_FEET = 1;

	public int getType() { return type; }
	public void setType(int type) { this.type = type; }
	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName;  }
	public Constituents getConstituents() { return constituents; }

	public int getUnitTo() { return unitTo; }
	public void setUnitTo(int unit) { unitTo = unit; }

	public boolean isLoaded() { return constituents != null && constituents.getNumConstituent() > 0; }

	public void loadConstituents(Reader fileReader) throws IOException
	{
		if(fileReader == null) return;
		
		BufferedReaderEx bufferedReader = new BufferedReaderEx(fileReader);

		constituents = new Constituents();
		
		String s;
		while ((s = bufferedReader.readLine()) != null && s.startsWith("#")) {}
		
		int numConstituents = Integer.parseInt(s);
		Constituent[] constituent = new Constituent[numConstituents];
		
		while ((s = bufferedReader.readLine()) != null && s.startsWith("#")) {}
		
		for(int i = 0; i < numConstituents; i++)
		{
			constituent[i] = new Constituent();
			int pos = s.indexOf(' ');
			if(pos != -1)
			{
				constituent[i].name = s.substring(0, pos);
				String speedString = s.substring(pos + 1).trim();
				constituent[i].speeds = Float.parseFloat(speedString) * (float)Math.PI / 648000.0f; // Convert to radians per second;
			}
			s = bufferedReader.readLine();
		}

		while (s != null && s.startsWith("#"))
		{
			s = bufferedReader.readLine();
		}

		int startYear = Integer.parseInt(s);
		constituents.setFirstYear(startYear);

		while ((s = bufferedReader.readLine()) != null && s.startsWith("#")) {}

		int numEpoch = Integer.parseInt(s);

		for(int i = 0; i < numConstituents; i++)
		{
			s = bufferedReader.readLine(); // We don't care of the constituents name
			
			float [] equilibriums = new float[numEpoch];
			int equilibriumsIndex = 0;
			int posA = -1, posB;
			String stringValue;

			for(int j = 0; j < 7; j++)
			{
				s = bufferedReader.readLine().trim();

				do
				{
					posB = s.indexOf(' ', posA + 1);
					if(posB != -1)
					{
						stringValue = s.substring(posA + 1, posB);
						while(posB + 1 < s.length() && s.charAt(posB + 1) == ' ') { posB++; }
					}
					else
						stringValue = s.substring(posA + 1);
					posA = posB;
					float value = Float.parseFloat(stringValue) * (float)Math.PI / 180.0f; // degre to radian;
					equilibriums[equilibriumsIndex++] = value;
				} while(posB != -1);
			}
			constituent[i].epochs = equilibriums;
		}
		s = bufferedReader.readLine(); // We don't care of the check *END*
		
		while ((s = bufferedReader.readLine()) != null && s.startsWith("#")) {}
		
		//numEpoch = Integer.parseInt(s);

		for(int i = 0; i < numConstituents; i++)
		{
			s = bufferedReader.readLine(); // We don't care of the constituents name
			
			float [] nodes = new float[numEpoch];
			int nodesIndex = 0;
			int posA = -1, posB;
			String stringValue;

			for(int j = 0; j < 7; j++)
			{
				s = bufferedReader.readLine().trim();

				do
				{
					posB = s.indexOf(' ', posA + 1);
					if(posB != -1)
					{
						stringValue = s.substring(posA + 1, posB);
						while(posB + 1 < s.length() && s.charAt(posB + 1) == ' ') { posB++; }
					}
					else
						stringValue = s.substring(posA + 1);
					posA = posB;
					float value = Float.parseFloat(stringValue);
					nodes[nodesIndex++] = value;
				} while(posB != -1);
			}
			constituent[i].nodes = nodes;
		}
		s = bufferedReader.readLine(); // We don't care of the check *END*
		
		constituents.setConstituents(constituent);

		//bufferedReader.mark(0);
	}
	
	public void loadLocation(Reader fileReader, Location location) throws IOException
	{
		if(fileReader == null)
			throw new Error("Harmonic file is not loaded. Please call the load() method before");
		
		BufferedReaderEx bufferedReader = new BufferedReaderEx(fileReader);
		String s;
		
		//bufferedReader.reset();
		
		//"*END*"
		while ((s = bufferedReader.readLine()) != null && !s.startsWith("*END*")) {}

		while ((s = bufferedReader.readLine()) != null && !s.startsWith("*END*")) {}

		while ((s = bufferedReader.readLine()) != null && (s.startsWith("#") || s.trim().length() == 0)) {}
		
		int numConstituent = constituents.getNumConstituent();

		boolean isSubLocation = (location instanceof SubLocation);
		String locationName;
		if(isSubLocation)
			locationName = ((SubLocation)location).getBaseLocationName();
		else
			locationName = location.getLocationName();
		while(s != null && locationName.compareTo(s) != 0)
		{
			for(int i = 0; i < numConstituent + 2; i++)
				bufferedReader.skipOneLine(); //s = bufferedReader.readLine();
			
			while ((s = bufferedReader.readLine()) != null && (s.startsWith("#") || s.trim().length() == 0)) {}
		}
		if(s == null) // Not found
			return;

		s = bufferedReader.readLine();
		int timeZone = hhmm2seconds (s);
		location.setMeridian(timeZone);

		s = bufferedReader.readLine();
		
		int unitFrom = UNIT_METER;
		
		int pos = s.indexOf(' ');
		if(pos != -1)
		{
			String amplitudeString = s.substring(0, pos);
			float amplitude = Float.parseFloat(amplitudeString);
			String unit = s.substring(pos + 1).trim();
			unitFrom = stringToUnit(unit);
			amplitude = converterUnit(amplitude, unitFrom, unitTo);
			location.setDatum(amplitude);
		}

		location.setConstituents(constituents);

		float[] amplitudes = new float[numConstituent];
		float[] phases = new float[numConstituent];
		for(int i = 0; i < numConstituent; i++)
		{
			s = bufferedReader.readLine();
	
			pos = s.indexOf(' ');
			while(pos + 1 < s.length() && s.charAt(pos + 1) == ' ') { pos++; }
			if(pos != -1)
			{
				//String constituentName = s.substring(0, pos).trim();
				int pos1 = s.indexOf(' ', pos + 1);
				if(pos1 != -1)
				{
					String amplitudeString = s.substring(pos + 1, pos1);
					float amplitude = Float.parseFloat(amplitudeString);
					String phaseString = s.substring(pos1 + 1).trim();
					float phase = Float.parseFloat(phaseString);
					
					amplitudes[i] = converterUnit(amplitude, unitFrom, unitTo); // * 0.3048; // feet to meter;
					phases[i] = phase * (float)Math.PI / 180.0f; // degre to radian;
				}
			}
		}
		location.setLocationAmplitude(amplitudes);
		location.setLocactionEpoch(phases);
		
		// Unit conversion
		if(isSubLocation)
		{
			float highTideLevelOffset = ((SubLocation)location).getHtleveloff();
			((SubLocation)location).setHtleveloff(converterUnit(highTideLevelOffset, UNIT_FEET, unitTo)); //highTideLevelOffset * 0.3048);
			float lowTideLevelOffset = ((SubLocation)location).getLtleveloff();
			((SubLocation)location).setLtleveloff(converterUnit(lowTideLevelOffset, UNIT_FEET , unitTo)); //lowTideLevelOffset * 0.3048);
		}
	}
	
	public static int stringToUnit(String unit)
	{
		if(unit.toLowerCase().startsWith("m"))
			return UNIT_METER;
		else if(unit.toLowerCase().startsWith("f"))
			return UNIT_FEET;
		return UNIT_METER;
	}
	
	public static float converterUnit(float value, int unitFrom, int unitTo)
	{
		switch(unitFrom)
		{
			case UNIT_METER:
				if (unitTo == UNIT_FEET) {
					value /= 0.3048;
				}
				break;
			case UNIT_FEET:
				if (unitTo == UNIT_METER) {
					value *= 0.3048;
				}
				break;
		}
		return value;
	}
	
	// Convert timezone offset to seconds
	private static int hhmm2seconds (String hhmm)
	{
	  int pos0 = hhmm.indexOf(':');
	  String hour = hhmm.substring(0, pos0);
	  int h = Integer.valueOf(hour).intValue();

  	  int pos1 = hhmm.indexOf(' ');
	  String minute = hhmm.substring(pos0 + 1, pos1);
	  int m = Integer.valueOf(minute).intValue();

	  String s = hhmm.substring(pos1 + 1);

	  if (h < 0 || s.charAt(0) == '-')
		m = -m;
	  return h * 3600 + m * 60;
	}
}