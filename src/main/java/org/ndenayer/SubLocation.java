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

/**
 *
 * @author Dgis
 */
public class SubLocation extends Location
{
//	# Subordinate sta'ns: t|c Reg:Co:St Lon Lat TimeZone Name
//	# Subordinate extra:  &Hmin Hmpy Hoff Lmin Lmpy Loff StaID RefFileNum RefName
	String sta_num;
	String baseLocationName;

	public void setBaseLocation(Location baseLocation)
	{
		meridian = baseLocation.getMeridian();
		datum = baseLocation.getDatum();
		float[] baseLocationAmplitudes = baseLocation.getLocationAmplitude();
		locationAmplitude = new float[baseLocationAmplitudes.length];
        System.arraycopy(baseLocationAmplitudes, 0, locationAmplitude, 0, baseLocationAmplitudes.length);

		float[] baseLocationEpochs = baseLocation.getLocationAmplitude();
		locationEpoch = new float[baseLocationEpochs.length];
        System.arraycopy(baseLocationEpochs, 0, locationEpoch, 0, baseLocationEpochs.length);
	}
	
	public int getHttimeoff() { return highTideTimeOffset; }
	public void setHttimeoff(int httimeoff) { this.highTideTimeOffset = httimeoff; haveOffsets = true; }
	public int getLttimeoff() { return lowTideTimeOffset; }
	public void setLttimeoff(int lttimeoff) { this.lowTideTimeOffset = lttimeoff; haveOffsets = true; }
	
	public float getHlevelmult() { return highTideLevelFactor; }
	public void setHlevelmult(float hlevelmult) { this.highTideLevelFactor = hlevelmult; haveOffsets = true; }
	public float getLlevelmult() { return lowTideLevelFactor; }
	public void setLlevelmult(float llevelmult) { this.lowTideLevelFactor = llevelmult; haveOffsets = true; }
	
	public float getHtleveloff() { return highTideLevelOffset; }
	public void setHtleveloff(float htleveloff) { this.highTideLevelOffset = htleveloff; haveOffsets = true; }
	public float getLtleveloff() { return lowTideLevelOffset; }
	public void setLtleveloff(float ltleveloff) { this.lowTideLevelOffset = ltleveloff; haveOffsets = true; }

	public String getSta_num() { return sta_num; }
	public void setSta_num(String sta_num) { this.sta_num = sta_num; }
	public String getBaseLocationName() { return baseLocationName; }
	public void setBaseLocationName(String baseLocationName) { this.baseLocationName = baseLocationName; }

	public SubLocation()
	{
	}
}
