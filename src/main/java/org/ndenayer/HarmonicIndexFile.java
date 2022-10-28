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
import java.util.Hashtable;
import java.util.Vector;

/**
 * Parses the harmonic index file (harmonic.idx)
 * and maintains an access to all the harmonic files
 * and all the locations (sub and main locations)
 * @author cosnier
 */
public class HarmonicIndexFile
{
	Hashtable regionById = new Hashtable();
	Hashtable countryById = new Hashtable();
	Hashtable stateById = new Hashtable();
	Vector fileList = new Vector();
	Vector locationList = new Vector();

	public int getHarmonicFileNum() { return fileList.size(); }
	public HarmonicFile getHarmonicFile(int index) { return (HarmonicFile)fileList.elementAt(index); }
	
	public int getLocationNum() { return locationList.size(); }
	public Location getLocation(int index) { return (Location)locationList.elementAt(index); }
	public Location getLocationByName(String locationName)
	{
		int locationNum = locationList.size();
		for(int i = 0; i < locationNum; i++)
		{
			Location location = (Location)locationList.elementAt(i);
			if(location.getLocationName().compareTo(locationName) == 0)
				return location;
		}
		return null;
	}
	
//	public void sortLocations()
//	{
//		locationList.
//	}
	
	/**
	 * Parses the harmonic index file (harmonic.idx)
	 */
	public void loadIndex(Reader fileReader) throws IOException
	{
		BufferedReaderEx br = new BufferedReaderEx(fileReader);

		String s;
		while ((s = br.readLine()) != null)
		{
			if(s.startsWith("XREF"))
			{
				while ((s = br.readLine()) != null)
				{
					if(s.startsWith("REGION"))
					{
						int pos1 = s.indexOf(' ');
						if(pos1 != -1)
						{
							int pos2 = s.indexOf(' ', pos1 + 1);
							if(pos2 != -1)
							{
								String id = s.substring(pos1 + 1, pos2);
								String value = s.substring(pos2 + 1);
								regionById.put(id, value);
							}
						}
					}
					else if(s.startsWith("COUNTRY"))
					{
						int pos1 = s.indexOf(' ');
						if(pos1 != -1)
						{
							int pos2 = s.indexOf(' ', pos1 + 1);
							if(pos2 != -1)
							{
								String id = s.substring(pos1 + 1, pos2);
								String value = s.substring(pos2 + 1);
								countryById.put(id, value);
							}
						}
					}
					else if(s.startsWith("STATE"))
					{
						int pos1 = s.indexOf(' ');
						if(pos1 != -1)
						{
							int pos2 = s.indexOf(' ', pos1 + 1);
							if(pos2 != -1)
							{
								String id = s.substring(pos1 + 1, pos2);
								String value = s.substring(pos2 + 1);
								stateById.put(id, value);
							}
						}
					}
					else if(s.startsWith("*END*"))
						break;
				}
			}
			else if(s.startsWith("Harmonic") || s.startsWith("Binary"))
			{
				while(s != null)
				{
					HarmonicFile currentHarmonicFile = null;
					if(s.startsWith("Harmonic"))
					{
						int pos0 = s.indexOf(' ');
						if(pos0 != -1)
						{
							int pos1 = s.indexOf(" file start", pos0);
							if(pos1 != -1)
							{
								String harmonicFileName = s.substring(pos0 + 1, pos1).trim();
								if(harmonicFileName.length() > 0)
								{
									//String type = s.substring(0, pos0);
									currentHarmonicFile = new HarmonicFile();
									currentHarmonicFile.setType(HarmonicFile.TYPE_ASCII);
									currentHarmonicFile.setFileName(harmonicFileName);
									fileList.addElement(currentHarmonicFile);
								}
							}
						}
					}
					else if(s.startsWith("Binary"))
					{
						currentHarmonicFile = null;
					}
					if(currentHarmonicFile == null)
						continue;

					int currentHarmonicFileIndex = fileList.size() - 1;

					while ((s = br.readLine()) != null)
					{
						if(s.length() == 0) continue;
						
						char firstChar = s.charAt(0);
						if(firstChar == 'T' || firstChar == 'C' || firstChar == 'U' ||
								 firstChar == 't' || firstChar == 'c' || firstChar == 'u')
						{
							boolean isSubordinateStation = (firstChar == 't' || firstChar == 'c' || firstChar == 'u');
							Location location;
							if(isSubordinateStation)
								location = new SubLocation();
							else
								location = new Location();

							location.setHarmonicFile(currentHarmonicFile);
							
							//T|C Reg:Co:St Lon Lat TimeZone Name
							int pos1 = s.indexOf(':');
							if(pos1 != -1)
							{
								String region = s.substring(1, pos1);
								location.setRegionName(region);
								
								int pos2 = s.indexOf(':', pos1 + 1);
								if(pos2 != -1)
								{
									String country = s.substring(pos1 + 1, pos2);
									location.setCountryName(country);
									
									int pos3 = s.indexOf(':', pos2 + 1);
									if(pos3 != -1)
									{
										String state = s.substring(pos2 + 1, pos3);
										location.setStateName(state);

										pos3 = s.indexOf(' ', pos3 + 1);
										if(pos3 != -1)
										{
											int pos4 = s.indexOf(' ', pos3 + 1);
											if(pos4 != -1)
											{
												String longitudeString = s.substring(pos3 + 1, pos4);
												float longitude = Float.parseFloat(longitudeString);
												location.setLongitude(longitude);
												
												int pos5 = s.indexOf(' ', pos4 + 1);
												if(pos5 != -1)
												{
													String latitudeString = s.substring(pos4 + 1, pos5);
													float latitude = Float.parseFloat(latitudeString);
													location.setLattidude(latitude);
												
													int pos6 = s.indexOf(' ', pos5 + 1);
													if(pos6 != -1)
													{
														String timezone = s.substring(pos5 + 1, pos6);
														
														int pos7 = timezone.indexOf(':');
														String hour = timezone.substring(0, pos7);
														int h = Integer.valueOf(hour).intValue();

														//pos8 = timezone.indexOf(' ');
														String minute = timezone.substring(pos7 + 1);
														int m = Integer.valueOf(minute).intValue();
														m = (h < 0 ? -m : m);
														//if (h < 0) m = -m;

														int meridian = h * 3600 + m * 60;
														location.setMeridian(meridian);
														
														String locationName = s.substring(pos6 + 1).trim();
														location.setLocationName(locationName);														
													}
												}
											}
										}
									}
								}
							}

							if(isSubordinateStation)
							{
								SubLocation subLocation = (SubLocation)location;

								int highTideTimeOffset = 0, lowTideTimeOffset = 0;
								float highTideLevelMultiply = 1.0f, lowTideLevelMultiply = 1.0f,
										highTideLevelOffset = 0.0f, lowTideLevelOffset = 0.0f;
								s = br.readLine();
								//&Hmin Hmpy Hoff Lmin Lmpy Loff StaID RefFileNum RefName
								if(s.length() > 0 && s.charAt(0) == '&')
								{
									int posA, posB;
									posA = s.indexOf(' ', 1);
									if(posA != -1)
									{
										highTideTimeOffset = Integer.parseInt(s.substring(1, posA));
									}
									posB = s.indexOf(' ', posA + 1);
									if(posA != -1 && posB != -1)
									{
										highTideLevelMultiply = Float.parseFloat(s.substring(posA + 1, posB));
									}
									posA = s.indexOf(' ', posB + 1);
									if(posA != -1 && posB != -1)
									{
										highTideLevelOffset = Float.parseFloat(s.substring(posB + 1, posA));
									}
									
									posB = posA;
									posA = s.indexOf(' ', posB + 1);
									if(posA != -1 && posB != -1)
									{
										lowTideTimeOffset = Integer.parseInt(s.substring(posB + 1, posA));
									}
									posB = s.indexOf(' ', posA + 1);
									if(posA != -1 && posB != -1)
									{
										lowTideLevelMultiply = Float.parseFloat(s.substring(posA + 1, posB));
									}
									posA = s.indexOf(' ', posB + 1);
									if(posA != -1 && posB != -1)
									{
										lowTideLevelOffset = Float.parseFloat(s.substring(posB + 1, posA));
									}
									
									posB = s.indexOf(' ', posA + 1);
									if(posA != -1 && posB != -1)
									{
										String stationId = s.substring(posA + 1, posB);
										subLocation.setSta_num(stationId);
									}
									posA = s.indexOf(' ', posB + 1);
									if(posA != -1 && posB != -1)
									{
										int referenceFileNumber = Integer.parseInt(s.substring(posB + 1, posA));
										if(referenceFileNumber > 0 && referenceFileNumber <= getHarmonicFileNum())
											location.setHarmonicFile(getHarmonicFile(referenceFileNumber - 1));
									}
									String baseLocationName = s.substring(posA + 1);
									subLocation.setBaseLocationName(baseLocationName);
									
									int httimeoff, lttimeoff;
									float hlevelmult = 1.0f, llevelmult = 1.0f,
											htleveloff = 0.0f, ltleveloff = 0.0f;

									if (Math.abs(highTideTimeOffset) < 1111)
										httimeoff = highTideTimeOffset * 60;
									else if (Math.abs(lowTideTimeOffset) < 1111)
										httimeoff = lowTideTimeOffset * 60;
									else 
										httimeoff = 0;

									if (Math.abs(lowTideTimeOffset) < 1111)
										lttimeoff = lowTideTimeOffset * 60;
									else
										lttimeoff = httimeoff;

									if (highTideLevelMultiply > 0.1f && highTideLevelMultiply < 10.0f)
										hlevelmult = highTideLevelMultiply;
									else if ((lowTideLevelMultiply > 0.1f) && (lowTideLevelMultiply < 10.0f))
										hlevelmult = lowTideLevelMultiply;
									else
										hlevelmult = 1.0f;

									if (lowTideLevelMultiply > 0.1f && lowTideLevelMultiply < 10.0f)
										llevelmult = lowTideLevelMultiply;
									else
										llevelmult = hlevelmult;

									if (Math.abs(highTideLevelOffset) < 100.0f)
										htleveloff = highTideLevelOffset;
									else if (Math.abs(lowTideLevelOffset) < 100.0f)
										htleveloff = lowTideLevelOffset;
									else
										htleveloff = 0f;

									if (Math.abs(lowTideLevelOffset) < 100.0f)
										ltleveloff = lowTideLevelOffset;
									else
										ltleveloff = htleveloff;

									subLocation.setHttimeoff(httimeoff);
									subLocation.setHlevelmult(hlevelmult);
									subLocation.setHtleveloff(htleveloff);
									subLocation.setLttimeoff(lttimeoff);
									subLocation.setLlevelmult(llevelmult);
									subLocation.setLtleveloff(ltleveloff);
									
								}
							}
							
							locationList.addElement(location);
						}
						else if(s.startsWith("Harmonic") || s.startsWith("Binary"))
							break;
					}
				}
			}
		}
	}
}
