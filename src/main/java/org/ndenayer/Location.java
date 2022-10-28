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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Dgis
 */
public class Location
{
	protected HarmonicFile harmonicFile;
	protected String regionName;
	protected String countryName;
	protected String stateName;
	
	protected String locationName = ""; // location
	protected float lattidude;
	protected float longitude;
	protected int meridian;
	protected float datum; // datum

	// Harmonic constants
	protected float[] locationAmplitude; // locationAmplitude
	protected float[] locationEpoch; // locationEpoch
	protected float[] amplitudes;
	protected Constituents constituents = null;
	
	protected float amplitude = 0.0f,  highTideLevelOffset = 0.0f,  lowTideLevelOffset = 0.0f,  markLevel,  absmax = 0.0f,  absmin = 0.0f,  fakeDatum = 0.0f,  fakeAmplitude = 0.0f,  Ihtleveloff = 0.0f,  Iltleveloff = 0.0f;
	protected float lowTideLevelFactor = 1.0f,  highTideLevelFactor = 1.0f,  iLowTideLevelFactor = 1.0f,  iHighTideLevelFactor = 1.0f;
	protected long nextHighTide = 0,  previousHighTide = 0,  nextHighTideAdjust = 0,  previousHighTideAdjust = 0,  fakeTime = 0,  epoch = 0,  markTimeAdjust = 0;
	protected boolean haveBogus = false,  convertBogus = false;
	protected int mark = 0,  highTideTimeOffset = 0,  lowTideTimeOffset = 0;
	protected int year = 2008;
	protected boolean haveOffsets = false;

	public Location()
	{
	}

	public Constituents getConstituents() { return constituents; }
	public void setConstituents(Constituents constituents)
	{
		this.constituents = constituents;
		amplitudes = new float[constituents.getNumConstituent()];
	}

	public HarmonicFile getHarmonicFile() { return harmonicFile; }
	public void setHarmonicFile(HarmonicFile harmonicFile) { this.harmonicFile = harmonicFile; }
	public String getRegionName() { return regionName; }
	public void setRegionName(String region) { this.regionName = region; }
	public String getCountryName() { return countryName; }
	public void setCountryName(String country) { this.countryName = country; }
	public String getStateName() { return stateName; }
	public void setStateName(String state) { this.stateName = state; }
	public String getLocationName() { return locationName; }
	public void setLocationName(String location) { this.locationName = location; }
	public int getMeridian() { return meridian; }
	public void setMeridian(int meridian) { this.meridian = meridian; }
	public float getDatum() { return datum; }
	public void setDatum(float datum) { this.datum = datum; }	// datum
	public float getLattidude() { return lattidude; }
	public void setLattidude(float lattidude) { this.lattidude = lattidude; }
	public float getLongitude() { return longitude; }
	public void setLongitude(float longitude) { this.longitude = longitude; }
	public float[] getLocationAmplitude() { return locationAmplitude; }
	public void setLocationAmplitude(float[] locationAmplitude) { this.locationAmplitude = locationAmplitude; }
	public float[] getLocactionEpoch() { return locationEpoch; }
	public void setLocactionEpoch(float[] locationEpoch) { this.locationEpoch = locationEpoch; }

	public boolean isLoaded() { return locationEpoch != null && locationEpoch.length > 0; }

	// Calculate time_t of the epoch.
	private void set_epoch(int year, int num_epochs, int first_year)
	{
		if (year < first_year || year >= first_year + num_epochs)
		{
			throw new Error("Tidelib:  Don't have equilibrium arguments for " + year);
//		wsprintf ((LPTSTR)stderrce, L"Tidelib:  Don't have equilibrium arguments for %d\n", year);
//		barf (MISSINGYEAR, _T(""));
		}

//	  struct tm ht;
//	  ht.tm_year = year - 1900;
//	  ht.tm_sec = ht.tm_min = ht.tm_hour = ht.tm_mon = 0;
//	  ht.tm_mday = 1;
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		//calendar.set(year, 0, 1, 0, 0, 0);

		//epoch = tm2gmt (calendar.getTime().getTime() / 1000);
		epoch = calendar.getTime().getTime() / 1000;
	// must be 1230768000
	//epoch = 1199145600; //1230768000;
	}

	/* BOGUS amplitude stuff - Added mgh
	 * For knots^2 current stations, returns square root of (value * amplitude),
	 * For normal stations, returns value * amplitude */
	private float bogusAmplitude(float mpy)
	{
		if (!haveBogus || !convertBogus)   // Added mgh
		{
			return (mpy * amplitude);
		}
		else
		{
			if (mpy >= 0.0f)
			{
				return (float)Math.sqrt(mpy * amplitude);
			}
			else
			{
				return (float)-Math.sqrt(-mpy * amplitude);
			}
		}
	}

	/* Figure out max amplitude over all the years in the node factors table. */
	/* This function by Geoffrey T. Dairiki */
	private void figure_amplitude()
	{
		if (amplitude == 0.0f)
		{
			for (int i = 0; i < constituents.getNumEpochs(); i++)
			{
				float year_amp = 0.0f;

				for (int a = 0; a < constituents.getNumConstituent(); a++)
				{
					year_amp += locationAmplitude[a] * constituents.getConstituents()[a].nodes[i];
				}
				if (year_amp > amplitude)
				{
					amplitude = year_amp;
				}
			}

			//assert(amplitude > 0.0);
			//if (amplitude <= 0.0)
			//barf(CEASSERT, _T("figure_amplitude: amp < 0\n"));
			//throw new Error("figure_amplitude: amp < 0");

			/* Figure other, related global values (DWF) */
			absmax = datum + bogusAmplitude(1.0f);
			absmin = datum - bogusAmplitude(1.0f);

			absmax = highTideLevelOffset + highTideLevelFactor * absmax;
			absmin = lowTideLevelOffset + lowTideLevelFactor * absmin;
			if (absmax <= absmin)
			//barf (MAXBELOWMIN, _T("\n"));
			{
				//throw new Error("absmax <= absmin");
			}

			fakeDatum = (absmax + absmin) / 2.0f;
			fakeAmplitude = (absmax - absmin) / 2.0f;
//		  if (fabs (fakeDatum) >= 100.0 || fakeAmplitude >= 100.0)
//			_tcscpy ((TCHAR *)stderrce, _T("Tidelib warning:  tidal range out of normal bounds\n"));
		}
	}

	// Figure out normalized multipliers for constituents for a particular
	// year.  Save amplitude for drawing unit lines.
	private void figure_multipliers()
	{
		figure_amplitude();
		if (year < constituents.getFirstYear() || year >= constituents.getFirstYear() + constituents.getNumEpochs())
		{
			throw new Error("Tidelib:  Don't have node factors for " + year);
		}
		for (int a = 0; a < constituents.getNumConstituent(); a++)
		{
			amplitudes[a] = locationAmplitude[a] * constituents.getConstituents()[a].nodes[year - constituents.getFirstYear()] / amplitude;
		}  // bogusAmplitude?
//	  if (hincmagic)
//		  pick_hinc ();
	}

	// Re-initialize for a different year
	private void happy_new_year(int new_year)
	{
		year = new_year;
		figure_multipliers();
		set_epoch(year, constituents.getNumEpochs(), constituents.getFirstYear());
	}
	
	/* TIDE_TIME_PREC
	 *   Precision (in seconds) to which we will find roots
	 */
	private final int TIDE_TIME_PREC = 15;

	/* TIDE_TIME_BLEND
	 *   Half the number of seconds over which to blend the tides from
	 *   one epoch to the next.
	 */
	private final float TIDE_BLEND_TIME = 3600;

	/* TIDE_TIME_STEP
	 *   We are guaranteed to find all high and low tides as long as their
	 * spacing is greater than this value (in seconds).
	 */
	private final int TIDE_TIME_STEP = TIDE_TIME_PREC;
	private final int TIDE_MAX_DERIV = 2;      /* Maximum derivative supported by
	 * time2dt_tide() and family. */

	private final long TIDE_BAD_TIME = -1;

	/*
	 * We will need a function for tidal height as a function of time
	 * which is continuous (and has continuous first and second derivatives)
	 * for all times.
	 *
	 * Since the epochs & multipliers for the tidal constituents change
	 * with the year, the regular time2tide(t) function has small
	 * discontinuities at new years.  These discontinuities really
	 * fry the fast root-finders.
	 *
	 * We will eliminate the new-years discontinuities by smoothly
	 * interpolating (or "blending") between the tides calculated with one
	 * year's coefficients, and the tides calculated with the next year's
	 * coefficients.
	 *
	 * i.e. for times near a new years, we will "blend" a tide
	 * as follows:
	 *
	 * tide(t) = tide(year-1, t)
	 *                  + w((t - t0) / Tblend) * (tide(year,t) - tide(year-1,t))
	 *
	 * Here:  t0 is the time of the nearest new-year.
	 *        tide(year-1, t) is the tide calculated using the coefficients
	 *           for the year just preceding t0.
	 *        tide(year, t) is the tide calculated using the coefficients
	 *           for the year which starts at t0.
	 *        Tblend is the "blending" time scale.  This is set by
	 *           the macro TIDE_BLEND_TIME, currently one hour.
	 *        w(x) is the "blending function", whice varies smoothly
	 *           from 0, for x < -1 to 1 for x > 1.
	 *
	 * Derivatives of the blended tide can be evaluated in terms of derivatives
	 * of w(x), tide(year-1, t), and tide(year, t).  The blended tide is
	 * guaranteed to have as many continuous derivatives as w(x).  */

	/* time2dt_tide(time_t t, int n)
	 *
	 *   Calculate nth time derivative the normalized tide.
	 *
	 * Notes: This function does not check for changes in year.
	 *  This is important to our algorithm, since for times near
	 *  new years, we interpolate between the tides calculated
	 *  using one years coefficients, and the next years coefficients.
	 *
	 *  Except for this detail, time2dt_tide(t,0) should return a value
	 *  identical to time2tide(t).
	 */
	private float _time2dt_tide(long t, int deriv)
	{
		float dt_tide = 0.0f;
		float tempd = (float)Math.PI / 2.0f * deriv;
		for (int a = 0; a < constituents.getNumConstituent(); a++)
		{
			Constituent constituent = constituents.getConstituents()[a];
			float term = amplitudes[a] *
					(float)Math.cos(tempd +
					constituent.speeds * ((t - epoch) + meridian) +
					constituent.epochs[year - constituents.getFirstYear()] - locationEpoch[a]);
			for (int b = deriv; b > 0; b--)
			{
				term *= constituent.speeds;
			}
			dt_tide += term;
		}
		return dt_tide;
	}

	/* blend_weight (float x, int deriv)
	 *
	 * Returns the value nth derivative of the "blending function" w(x):
	 *
	 *   w(x) =  0,     for x <= -1
	 *
	 *   w(x) =  1/2 + (15/16) x - (5/8) x^3 + (3/16) x^5,
	 *                  for  -1 < x < 1
	 *
	 *   w(x) =  1,     for x >= 1
	 *
	 * This function has the following desirable properties:
	 *
	 *    w(x) is exactly either 0 or 1 for |x| > 1
	 *
	 *    w(x), as well as its first two derivatives are continuous for all x.
	 */
	private static float blend_weight(float x, int deriv)
	{
		float x2 = x * x;

		if (x2 >= 1.0)
		{
			return deriv == 0 && x > 0.0f ? 1.0f : 0.0f;
		}

		switch (deriv)
		{
			case 0:
				return ((3.0f * x2 - 10.0f) * x2 + 15.0f) * x / 16.0f + 0.5f;
			case 1:
				return ((x2 - 2.0f) * x2 + 1.0f) * (15.0f / 16.0f);
			case 2:
				return (x2 - 1.0f) * x * (15.0f / 4.0f);
		}
		//assert (false);
		return 0.0f; // mgh+ to get rid of compiler warning
	}

	/*
	 * This function does the actual "blending" of the tide
	 * and its derivatives.
	 */
	private float blend_tide(long t, int deriv, int first_year, float blend)
	{
		float[] fl = new float[TIDE_MAX_DERIV + 1];
		float[] fr = new float[TIDE_MAX_DERIV + 1];
		float[] fp = fl;
		float[] w = new float[TIDE_MAX_DERIV + 1];
		float fact = 1.0f;
		float f;
		int n;

		//assert (deriv >= 0 && deriv <= TIDE_MAX_DERIV);
//	  if ((deriv < 0) && (deriv > TIDE_MAX_DERIV))
//		  barf(CEASSERT, _T("blend_tide\n"));

		/*
		 * If we are already happy_new_year()ed into one of the two years
		 * of interest, compute that years tide values first.
		 */
		if (year == first_year + 1)
		{
			fp = fr;
		}
		else
		{
			if (year != first_year)
			{
				happy_new_year(first_year);
			}
		}
		for (n = 0; n <= deriv; n++)
		{
			fp[n] = _time2dt_tide(t, n);
		}

		/*
		 * Compute tide values for the other year of interest,
		 *  and the needed values of w(x) and its derivatives.
		 */
		if (fp == fl)
		{
			happy_new_year(first_year + 1);
			fp = fr;
		}
		else
		{
			happy_new_year(first_year);
			fp = fl;
		}
		for (n = 0; n <= deriv; n++)
		{
			fp[n] = _time2dt_tide(t, n);
			w[n] = blend_weight(blend, n);
		}

		/*
		 * Do the blending.
		 */
		f = fl[deriv];
		for (n = 0; n <= deriv; n++)
		{
			f += fact * w[n] * (fr[deriv - n] - fl[deriv - n]);
			fact *= (float) (deriv - n) / (n + 1) * (1.0 / TIDE_BLEND_TIME);
		}
		return f;
	}

	private int yearoftimet(long t)
	{
		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		time.setTime(new Date(t * 1000));
		return time.get(Calendar.YEAR);
	}
	/*static*/ long next_epoch = TIDE_BAD_TIME; /* next years newyears */
	/*static*/ long this_epoch = TIDE_BAD_TIME; /* this years newyears */
	/*static*/ int this_year = -1;

	private float time2dt_tide(long t, int deriv)
	{
		int new_year = yearoftimet(t);

		/* Make sure our values of next_epoch and epoch are up to date. */
		if (new_year != this_year)
		{
			if (new_year + 1 < constituents.getFirstYear() + constituents.getNumEpochs())
			{
				set_epoch(new_year + 1, constituents.getNumEpochs(), constituents.getFirstYear());
				next_epoch = epoch;
			}
			else
			{
				next_epoch = TIDE_BAD_TIME;
			}

			happy_new_year(this_year = new_year);
			this_epoch = epoch;
		}

		//assert (t >= this_epoch);
		//assert (next_epoch == TIDE_BAD_TIME || t < next_epoch);
//	  if(t < this_epoch)
//		  barf(CEASSERT, _T("time2dt_tide\n"));
//	  if (next_epoch == TIDE_BAD_TIME || t >= next_epoch)
//		  barf(CEASSERT, _T("time2dt_tide\n"));

		/*
		 * If we're close to either the previous or the next
		 * new years we must blend the two years tides.
		 */
		if (t - this_epoch <= TIDE_BLEND_TIME && this_year > constituents.getFirstYear())
		{
			return blend_tide(t, deriv, this_year - 1, (float) (t - this_epoch) / TIDE_BLEND_TIME);
		}
		else
		{
			if (next_epoch - t <= TIDE_BLEND_TIME && this_year + 1 < constituents.getFirstYear() + constituents.getNumEpochs())
			{
				return blend_tide(t, deriv, this_year, -(float) (next_epoch - t) / TIDE_BLEND_TIME);
			}
		}

		/*
		 * Else, we're far enough from newyears to ignore the blending.
		 */
		if (this_year != year)
		{
			happy_new_year(this_year);
		}
		return _time2dt_tide(t, deriv);
	}
	
	
	private final float[] maxdt = new float[TIDE_MAX_DERIV + 2]; /* Initilized to zeroes by ANSI */
	/* dt_tide_max (int n)
	 *   Returns the maximum that the absolute value of the nth derivative
	 * of the tide can ever attain.
	 */
	private float dt_tide_max(int deriv)
	{
		//static float maxdt[TIDE_MAX_DERIV+2]; /* Initilized to zeroes by ANSI */
		float max = 0.0f;
		int myyear, wasyear;

		/* We need to be able to calculate max tide derivatives for one
		 * derivative higher than we actually need to know the tides.
		 */
		//assert (deriv >= 0 && deriv <= TIDE_MAX_DERIV + 1);

		if (maxdt[deriv] <= 0.0)
		{
			/* Actually doing a happy_new_year on 1970 is unsafe because
			the mktime in tm2gmt will, on rare occasions, fail because the
			uncorrected time_t is before the beginning of the Unix epoch.
			I've kludged this to include 1970 without crashing mktime.
			-- DWF
			tm2gmt has since been redone, but this "workaround" doesn't
			harm anything, so I'll leave it in. -- DWF */

			wasyear = year;
			if (wasyear == 0)
			{
				wasyear = constituents.getFirstYear() + 1;
			}
			for (myyear = constituents.getFirstYear(); myyear < constituents.getFirstYear() + constituents.getNumEpochs(); myyear++)
			{
				/* happy_new_year(myyear);    Crash.  Burn. */
				year = myyear;
				figure_multipliers();

				max = 0.0f;
				for (int a = 0; a < constituents.getNumConstituent(); a++)
				{
					//max += amplitudes[a] * Math.pow(constituents.getConstituents()[a].speeds, (float) deriv);
					float power = 1.0f;
					for(int p = 0; p < deriv; p++)
						power *= constituents.getConstituents()[a].speeds;
					max += amplitudes[a] * power;
				}
				if (max > maxdt[deriv])
				{
					maxdt[deriv] = max;
				}
			}
			maxdt[deriv] *= 1.1;      /* Add a little safety margin... */
			happy_new_year(wasyear);   /* Clean up the mess */
		}
		return maxdt[deriv];
	}

	/* time2dt_atide (time_t t, int n)
	 *   Calcualte the nth derivative of the dimensional tide.
	 */
	private float time2dt_atide(long t, int deriv)
	{
		//  float tide = time2dt_tide(t,deriv) * amplitude;
		float tide = bogusAmplitude(time2dt_tide(t, deriv));
		if (deriv == 0)
		{
			tide += datum;
		}
		return tide;

//  if (deriv == 0) // v2.1
//       return(bogusAmplitude(time2dt_tide(t,deriv)) + currentLocation.datum);
//  else return(time2dt_tide(t,deriv)*amplitude);
	}

	
	
	/* Estimate the normalized mean tide level around a particular time by
	   summing only the long-term constituents. */
	/* Does not do any blending around year's end. */
	/* This is used only by time2asecondary for finding the mean tide level */
	private float time2mean(long t)
	{
		float tide = 0.0f;
		int a, new_year = yearoftimet(t);
		if (new_year != year)
		{
			happy_new_year(new_year);
		}
		Constituent[] constituents1 = constituents.getConstituents();
		for (a = 0; a < constituents.getNumConstituent(); a++)
		{
			Constituent currentConstituent = constituents1[a];
			if (currentConstituent.speeds < 6e-6)
			{
				tide += amplitudes[a] *
						Math.cos(currentConstituent.speeds * ((t - epoch) + meridian) +
						currentConstituent.epochs[year - constituents.getFirstYear()] - locationEpoch[a]);
			}
		}
		return tide;
	}

	long lowtime = 0, hightime = 0;
	float lowlvl, highlvl; /* Normalized tide levels for MIN, MAX */

	/* If offsets are in effect, interpolate the 'corrected' denormalized
	tide.  The normalized is derived from this, instead of the other way
	around, because the application of height offsets requires the
	denormalized tide. */
	public float time2asecondary (long t)
	{
	  /* Get rid of the normals. */
	  if (!haveOffsets)
		return time2atide (t);
	  else
	  {
		/* Intervalwidth of 14 (was originally 13) failed on this input:
		-location Dublon -hloff +0.0001 -gstart 1997:09:10:00:00 -raw 1997:09:15:00:00
		*/
		int intervalwidth = 15;
		int stretchfactor = 3;

		long T;  /* Adjusted t */
		float S, Z, HI, HS, magicnum;
		long interval = 3600 * intervalwidth;
		long difflow, diffhigh;
		boolean badlowflag = false, badhighflag = false;

		//assert (t > interval*stretchfactor);  /* Avoid underflow */

		/* Algorithm by Jean-Pierre Lapointe (scipur@collegenotre-dame.qc.ca) */
		/* as interpreted, munged, and implemented by DWF */

		/* This is the initial guess (average of time offsets) */
		T = t - (highTideTimeOffset + lowTideTimeOffset) / 2;

		/* The usage of an estimate of mean tide level here is to correct
		   for seasonal changes in tide level.  Previously I had simply used
		   the zero of the tide function as the mean, but this gave bad
		   results around summer and winter for locations with large seasonal
		   variations. */
		Z = time2mean(T);
		S = time2tide(T) - Z;

		/* Find MAX and MIN.  I use the highest high tide and the lowest
		   low tide over a 26 hour period, but I allow the interval to stretch
		   a lot if necessary to avoid creating discontinuities.  The
		   heuristic used is not perfect but will hopefully be good enough.

		   It is an assumption in the algorithm that the tide level will
		   be above the mean tide level for MAX and below it for MIN.  A
		   changeover occurs at mean tide level.  It would be nice to
		   always use the two tides that immediately bracket T and to put
		   the changeover at mid tide instead of always at mean tide
		   level, since this would eliminate much of the inaccuracy.
		   Unfortunately if you change the location of the changeover it
		   causes the tide function to become discontinuous.

		   Now that I'm using time2mean, the changeover does move, but so
		   slowly that it makes no difference.
		*/

		if (lowtime < T)
		  difflow = T - lowtime;
		else
		  difflow = lowtime - T;
		if (hightime < T)
		  diffhigh = T - hightime;
		else
		  diffhigh = hightime - T;

		/* Update MIN? */
		if (difflow > Math.abs(interval) * stretchfactor)
		  badlowflag = true;
		if (badlowflag || (difflow > Math.abs(interval) && S > 0))
		{
		  long tt;
		  float tl;
		  tt = T - interval;
		  LongReference ttReference = new LongReference();
		  ttReference.value = tt;
		  next_big_event (ttReference);
		  tt = ttReference.value;
		  lowlvl = time2tide (tt);
		  lowtime = tt;
		  while (tt < T + interval)
		  {
			  ttReference.value = tt;
			  next_big_event (ttReference);
			  tt = ttReference.value;
			  tl = time2tide(tt);
			  if (tl < lowlvl && tt < T + interval)
			  {
				  lowlvl = tl;
				  lowtime = tt;
			  }
		  }
		}
		
		/* Update MAX? */
		if (diffhigh > Math.abs(interval) * stretchfactor)
		  badhighflag = true;
		if (badhighflag || (diffhigh > Math.abs(interval) && S < 0))
		{
		  long tt;
		  float tl;
			tt = T - interval;
		  LongReference ttReference = new LongReference();
			ttReference.value = tt;
			next_big_event(ttReference);
			tt = ttReference.value;

		  highlvl = time2tide (tt);
		  hightime = tt;
		  while (tt < T + interval) {
			ttReference.value = tt;
			next_big_event(ttReference);
			tt = ttReference.value;
			tl = time2tide (tt);
			if (tl > highlvl && tt < T + interval) {
			  highlvl = tl;
			  hightime = tt;
			}
		  }
		}

		/* Now that I'm using time2mean, I should be guaranteed to get
		   an appropriate low and high. */
		//assert (lowlvl < Z);
		//assert (highlvl > Z);

		/* Improve the initial guess. */
		if (S > 0)
		  magicnum = 0.5f * S / Math.abs(highlvl - Z);
		else
		  magicnum = 0.5f * S / Math.abs(lowlvl - Z);
		T = T - (long)magicnum * (highTideTimeOffset - lowTideTimeOffset);
		HI = time2tide(T);

		/* Denormalize and apply the height offsets. */
		HI = bogusAmplitude(HI) + datum;
		{
		  float RH=1.0f, RL=1.0f, HH=0.0f, HL=0.0f;
		  RH = highTideLevelFactor;
		  HH = highTideLevelOffset;
		  RL = lowTideLevelFactor;
		  HL = lowTideLevelOffset;

		  /* I patched the usage of RH and RL to avoid big ugly
		  discontinuities when they are not equal.  -- DWF */

		  HS =  HI * ((RH+RL)/2 + (RH-RL)*magicnum)
					+ (HH+HL)/2 + (HH-HL)*magicnum;
		}

		return HS;
	  }
	}

	/************** COMMENT BLOCK ************************

	This is Jean-Pierre's explanation of his original algorithm.

	1-  Store the following data
		 Z  (datum at reference station)
		 t  (time of prediction at the secondary station)
		 TH (correction for high tide time at secondary station)
		 TL (correction for low tide time at secondary station)
		 RH (correction ratio for high tide height at secondary station)
		 RL (correction ratio for low tide height at secondary station)
		 HH (height correction for high tide height at secondary station)
		 HL (height correction for low tide height at secondary station)

	2-  Run XTIDE for the reference station for the day of prediction
		 to find the height of the higher tide and store it as MAX and
		 to find the height of the lower tide and store it as MIN.

	3-  Run XTIDE for the reference station at a time T defined as
		 T =  t - (TH + TL) / 2
		Store the height calculated by XTIDE as HI (intermediate height)

	4-  Store S defined as S =HI - Z

	5-  Run XTIDE for the reference station at a time T defined as:
		 if S > 0 then
		 T = t - (TH + TL) / 2 - (TH-TL) / 2 x S / absolute value(MAX - Z)
		 else
		 T = t - (TH + TL) / 2 - (TH-TL) / 2 x S / absolute value(MIN - Z)

		Store the height calculated by XTIDE as HI (intermediate height)
		and calculate HS (height at secondary station at time t) defined as:
		if S > 0 then
		HS =  HI x RH + (HH + HL) / 2 + (HH-HL) / 2 x S/absolute value(MAX - Z)
		else
		HS =  HI x RL + (HH + HL) / 2 + (HH-HL) / 2 x S/absolute value(MIN - Z)

		You now have HS the height of the tide at the secondary station at a
		time t for this station.

	********  END COMMENT BLOCK  **********************************************/

	
	
	
	
	// Calculate the normalized tide (-1.0 .. 1.0) for a given time.
	public float time2tide(long t)
	{
		return time2dt_tide(t, 0);
	}

	// Calculate the denormalized tide.
	public float time2atide(long t)
	{
		return time2dt_atide(t, 0);
	}


	// Normalized 'corrected' tide.
	public float time2secondary(long t)
	{
		// Get rid of the normals.
	  if (!haveOffsets)
		return time2tide(t);

	  return (time2asecondary(t) - fakeDatum) / fakeAmplitude;
	}

	private interface TideFunction
	{

		float function(long t, int deriv);
	}

	private class BooleanReference
	{

		public boolean value;
	}

	private class LongReference
	{
		long value;
	}
	
	/*
	 * Here's a better root finder based upon a modified Newton-Raphson method.
	 */
	private long find_zero(long t1, long t2, TideFunction f)
	{
		long tl = t1;
		long tr = t2;
		float fl = f.function(tl, 0);
		float fr = f.function(tr, 0);
		float scale = 1.0f;
		int dt;
		long t = 0;
		float fp = 0.0f;
		float ft = 1.0f;              /* Forces first step to be bisection */
		float f_thresh = 0.0f;

		//assert (fl != 0.0 && fr != 0.0);
		//assert (tl < tr);
		if (fl > 0)
		{
			scale = -1.0f;
			fl = -fl;
			fr = -fr;
		}
		//assert (fl < 0.0 && fr > 0.0);

		while (tr - tl > TIDE_TIME_PREC)
		{
			if (Math.abs(ft) > f_thresh /* not decreasing fast enough */ || (ft > 0 ? /* newton step would go outside bracket */ (fp <= ft / (t - tl)) : (fp <= -ft / (tr - t))))
			{
				dt = 0;                       /* Force bisection */
			}
			else
			{
				/* Attempt a newton step */
				dt = (int) Math.floor(-ft / fp + 0.5);

				/* Since our goal specifically is to reduce our bracket size
				as quickly as possible (rather than getting as close to
				the zero as possible) we should ensure that we don't take
				steps which are too small.  (We'd much rather step over
				the root than take a series of steps which approach the
				root rapidly but from only one side.) */
				if (Math.abs(dt) < TIDE_TIME_PREC)
				{
					dt = ft < 0 ? TIDE_TIME_PREC : -TIDE_TIME_PREC;
				}

				if ((t += dt) >= tr || t <= tl)
				{
					dt = 0;
				}           /* Force bisection if outside bracket */
				f_thresh = Math.abs(ft) / 2.0f;
			}

			if (dt == 0)
			{
				/* Newton step failed, do bisection */
				t = tl + (tr - tl) / 2;
				f_thresh = fr > -fl ? fr : -fl;
			}

			if ((ft = scale * f.function(t, 0)) == 0)
			{
				return t;
			}
			/* Exact zero */
			else
			{
				if (ft > 0.0)
				{
					tr = t;
					fr = ft;
				}
				else
				{
					tl = t;
					fl = ft;
				}
			}

			fp = scale * f.function(t, 1);
		}

		return tr;
	}

	/* next_zero(time_t t, float (*f)(), float max_fp, float max_fpp)
	 *   Find the next zero of the function f which occurs after time t.
	 *   The arguments max_fp and max_fpp give the maximum possible magnitudes
	 *   that the first and second derivative of f can achieve.
	 *
	 *   Algorithm:  Our goal here is to bracket the next zero of f ---
	 *     then we can use find_zero() to quickly refine the root.
	 *     So, we will step forward in time until the sign of f changes,
	 *     at which point we know we have bracketed a root.
	 *     The trick is to use large steps in are search, which making
	 *     sure the steps are not so large that we inadvertently
	 *     step over more than one root.
	 *
	 *     The big trick, is that since the tides (and derivatives of
	 *     the tides) are all just harmonic series', it is easy to place
	 *     absolute bounds on their values.
	 */
	private long next_zero(long t, TideFunction f, BooleanReference risingflag, float max_fp, float max_fpp)
	{
		long t_left = t;
		long t_right;
		int step, step1, step2;

		float f_left, df_left, f_right;
		float scale = 1.0f;

		/* If we start at a zero, step forward until we're past it. */
		while ((f_left = f.function(t_left, 0)) == 0.0)
		{
			t_left += TIDE_TIME_PREC;
		}

		if (!(risingflag.value = f_left < 0))
		{
			scale = -1.0f;
			f_left = -f_left;
		}

		while (true)
		{
			/* Minimum time to next zero: */
			step1 = (int) (Math.abs(f_left) / max_fp);

			/* Minimum time to next turning point: */
			df_left = scale * f.function(t_left, 1);
			step2 = (int) (Math.abs(df_left) / max_fpp);

			if (df_left < 0.0)
			{
				/* Derivative is in the wrong direction. */
				step = step1 + step2;
			}
			else
			{
				step = step1 > step2 ? step1 : step2;
			}

			if (step < TIDE_TIME_STEP)
			{
				step = TIDE_TIME_STEP;
			} /* No rediculously small steps... */

			t_right = t_left + step;
			/*
			 * If we hit upon an exact zero, step right until we're off
			 * the zero.  If the sign has changed, we are bracketing a desired
			 * root, if the sign hasn't changed, then the zero was at
			 * an inflection point (i.e. a double-zero to within TIDE_TIME_PREC)
			 * and we want to ignore it.
			 */
			while ((f_right = scale * f.function(t_right, 0)) == 0.0)
			{
				t_right += TIDE_TIME_PREC;
			}
			if (f_right > 0.0)
			{
				return find_zero(t_left, t_right, f);
			} /* Found a bracket */

			t_left = t_right;
			f_left = f_right;
		}
	}

	private class f_hiorlo implements TideFunction
	{

		public float function(long t, int deriv)
		{
			return time2dt_tide(t, deriv + 1);
		}
	}

	private long next_high_or_low_tide(long t, BooleanReference hiflag)
	{
		//int           rising;
		BooleanReference rising = new BooleanReference();
		long thilo = next_zero(t, new f_hiorlo(), rising, dt_tide_max(2), dt_tide_max(3));
		hiflag.value = !rising.value;
		return thilo;
	}

//	private class FMark implements TideFunction
//	{
//
//		public float function(long t, int deriv)
//		{
//			float fval = time2dt_atide(t, deriv);
//			if (deriv == 0)
//			{
//				fval -= markLevel;
//			} // orig
//			//      fval -= (markLevel * (amplitude/fakeAmplitude) + (datum-fakeDatum)); // +v2.1
//			//      fval -= markLevel*amplitude/fakeAmplitude + datum/amplitude - fakeDatum/fakeAmplitude;// v2.2
//			//      fval -= (markLevel-fakeDatum)/fakeAmplitude * amplitude + datum;
//			return fval;
//		}
//	}
//
//	private long find_mark_crossing(long t1, long t2, BooleanReference risingflag)
//	{
//		FMark f_mark = new FMark();
//		float f1 = f_mark.function(t1, 0);
//		float f2 = f_mark.function(t2, 0);
//
//		//assert (f1 != f2);
//
//		if (!(risingflag.value = f1 < 0.0 || f2 > 0.0))
//		{
//			f1 = -f1;
//			f2 = -f2;
//		}
//
//		if (f1 == 0.0)
//		{
//			return t1;
//		}
//		else
//		{
//			if (f2 == 0.0)
//			{
//				return t2;
//			}
//		}
//
//		return (f1 < 0.0 && f2 > 0.0) ? find_zero(t1, t2, f_mark) : TIDE_BAD_TIME;
//	}
	
	long last_tm = TIDE_BAD_TIME;
	long cache_hilo;
	BooleanReference is_high = new BooleanReference();

	//	Next high tide, low tide, transition of the mark level, or some
	//	combination.
	//	Bit      Meaning
	//	0       low tide
	//	1       high tide
	//	2       falling transition
	//	3       rising transition
	private int next_big_event(LongReference tm)
	{
//	  static long last_tm   = TIDE_BAD_TIME;
//	  static long cache_hilo;
//	  static int    is_high;
		long t_hilo;
		//long t_mark, t_entry = tm.value, t_beg, t_end;
		//BooleanReference is_rising = new BooleanReference();
		int stat = 0;

		/* Find next high/low tide */
		if (tm.value == last_tm)           /* If we have a cached hi/lo tide, use it */
		{
			t_hilo = cache_hilo;
		}
		else
		{
			/* Find time of next high or low tide */
			t_hilo = next_high_or_low_tide(tm.value, is_high);
			//assert (t_hilo > tm.value);

//			if (mark != 0) //&& ((text && !graphmode) || (!text && graphmode) || ps))
//			{
//				if ((t_mark = find_mark_crossing(tm.value, t_hilo, is_rising)) != TIDE_BAD_TIME)
//				{
//					//assert (t_mark >= tm.value && t_mark <= t_hilo);
//					cache_hilo = t_hilo;  /* Save time of next hi/lo */
//					last_tm = tm.value = t_mark;
//					stat = is_rising.value ? 0x08 : 0x04;
//					/* Added mgh to adjust time of mark crossings*/
//					if (is_rising.value)
//					{
//						t_beg = t_entry + lowTideTimeOffset;
//						t_end = t_hilo + highTideTimeOffset;
//					}
//					else
//					{
//						t_beg = t_entry + highTideTimeOffset;
//						t_end = t_hilo + lowTideTimeOffset;
//					}
//					markTimeAdjust = ((t_mark - t_entry) / (t_hilo - t_entry)) *
//							(t_end - t_beg) + t_beg;
//					//          markTimeAdjust = t_mark;
//					if (t_mark < t_hilo)
//					{
//						return stat;
//					}
//				}
//			}
		}

		last_tm = TIDE_BAD_TIME;              /* tag cache as invalid */
		tm.value = t_hilo;
		return stat | (is_high.value ? 0x02 : 0x01);
	}
	
	public class NextTide
	{
		public static final int LOW_TIDE = 1;
		public static final int HIGH_TIDE = 2;

		private static final int BIT_LOW_TIDE = 1;
		private static final int BIT_HIGH_TIDE = 2;
		//private static final int BIT_FALLING_TRANSITION = 4;
		//private static final int BIT_RISING_TRANSITION = 8;
		private int eventBitField = 0;
		private final LongReference currentUnAdjustGMTTime;
		
		public NextTide(long startGMTTime)
		{
			currentUnAdjustGMTTime = new LongReference ();
			currentUnAdjustGMTTime.value = startGMTTime;
		}
		
		/**
		 * Get the tide type
		 * @return The tide type LOW_TIDE or HIGH_TIDE
		 */
		public int getTideType()
		{
			if((eventBitField & BIT_LOW_TIDE) != 0)
				return LOW_TIDE;
			else if((eventBitField & BIT_HIGH_TIDE) != 0)
				return HIGH_TIDE;
			else
				return 0;
		}
		
		/**
		 * Gets the time of the next tide.
		 * @return The time in second since the 1970 1st of january.
		 */
		public long getTideGMTTime()
		{
			if (haveOffsets)
			{
				if ((eventBitField & BIT_LOW_TIDE) != 0)
					return currentUnAdjustGMTTime.value + lowTideTimeOffset;
				else if ((eventBitField & BIT_HIGH_TIDE) != 0)
					return currentUnAdjustGMTTime.value + highTideTimeOffset;
			}
			return currentUnAdjustGMTTime.value;
		}
		
		/**
		 * Gets the date of the next tide.
		 * @return A Date object with the time in second since the 1970 1st of january.
		 */
		public Date getTideGMTDate()
		{
			return new Date(getTideGMTTime() * 1000);
		}
		
		/**
		 * Calculates the tide height for a given time.
		 * @return The tide height in the current unit.
		 */
		public float getTideHeight()
		{
			return time2asecondary(getTideGMTTime());
		}
		
		/**
		 * Calculates the normalized tide (-1.0 .. 1.0) for a given time.
		 * @return The normalized tide height.
		 */
		public float getTideNormalizedHeight()
		{
			return time2secondary(getTideGMTTime());
		}
	}
	
	/**
	 * Calcs the next tide event starting with the start time of the NextTide.
	 * @param nextTide
	 */
	public void nextTide(NextTide nextTide)
	{
		nextTide.eventBitField = next_big_event(nextTide.currentUnAdjustGMTTime);
	}
	
	public String toString()
	{
		return locationName;
	}
}
