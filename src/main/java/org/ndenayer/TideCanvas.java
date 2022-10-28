package org.ndenayer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class TideCanvas   {

    private Location location = null;
    private final Location lastLocation = null;
    private final Date currentDate;

    public TideCanvas(Location location, Date currentDate)
    {
        this.location = location;
        this.currentDate = currentDate;
    }

    public static String formatGMTDateLong(Date date, boolean showYear)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);
        return formatCalendarLong(calendar, showYear);
    }

    public static String formatCalendarLong(Calendar calendar, boolean showYear)
    {
        return formatCalendarToDate(calendar, showYear) + " " + formatCalendarToTime(calendar);
    }

    public static String formatCalendarToDate(Calendar calendar, boolean showYear)
    {
        return formatInteger(calendar.get(Calendar.DATE), 2) + "/" + formatInteger(calendar.get(Calendar.MONTH) + 1, 2) + (showYear ? "/" + calendar.get(Calendar.YEAR) : "");
    }

    public static String formatCalendarToTime(Calendar calendar)
    {
        return  formatInteger(calendar.get(Calendar.HOUR_OF_DAY), 2) +
                ":" + formatInteger(calendar.get(Calendar.MINUTE), 2) ;
    }

    public static String formatFloat(float value, int decimalPlaces)
    {
        String doubleString = "" + value;
        return doubleString.substring(0, (doubleString.indexOf(".") + decimalPlaces + 1));
    }

    public static String formatTideType(int tideType)
    {
        if(tideType == Location.NextTide.LOW_TIDE)
            return "TEXT_TIDE_LOW";
        else if(tideType == Location.NextTide.HIGH_TIDE)
            return "TEXT_TIDE_HIGH";
        return "";
    }

    public static String formatInteger(int value, int decimalPlaces)
    {
        String result  = "" + value;
        if(result.length() < decimalPlaces)
        {
            int missingZero = decimalPlaces - result.length();
            for (int i = 0; i < missingZero; i++)
                result = "0" + result;
        }
        return result;
    }

    /**
     * paint
     */
    public void paint() {
        int unit = HarmonicFile.UNIT_METER;
        String unitString = unit == HarmonicFile.UNIT_METER ?  "TEXT_UNIT_METER" : "TEXT_UNIT_FEET";
        System.out.println("unitString");

        int dayLightSaving = 0;
        long dayLightSavingOffsetTime = dayLightSaving * 3600000L;
        TimeZone displayTimeZone = TimeZone.getDefault(); //getTimeZone("GMT+1");
        Calendar currentGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long rawOffsetTime = displayTimeZone.getRawOffset();
        Date currentGMTDate = new Date(currentDate.getTime() - rawOffsetTime - dayLightSavingOffsetTime);
        long nowGMTTime = currentGMTDate.getTime() / 1000;
        float currentHeight = location.time2asecondary(nowGMTTime);
        currentHeight = HarmonicFile.converterUnit(currentHeight, HarmonicFile.UNIT_METER, unit);
        //float currentNormalizedHeight = location.time2secondary(nowGMTTime);
        System.out.println("TEXT_NOW");

        System.out.println(formatGMTDateLong(new Date(currentGMTDate.getTime() + rawOffsetTime + dayLightSavingOffsetTime), true) +
                " " + formatFloat(currentHeight, 1) + " " + unitString);
        System.out.println("Juste apres");



        class TideSummary
        {
            String hour;
            String shortLocalDate;
            String longLocalDate;
            long gmtTime;
            int tideType;
            float tideHeight;
            //float tideNormalizedHeight;
            public String toString() {
                return "hour: " + hour +"shortLocalDate: " + shortLocalDate +"longLocalDate: " + longLocalDate +"gmtTime: " + gmtTime + "tideType: " + tideType + "tideType: " + tideType +"tideHeight: "+tideHeight;
            }
        }

        int numTideEvent = 6;
        TideSummary[] tideEvents = new TideSummary[numTideEvent];
        long timeOffset = 0;
        long startGMTTime = nowGMTTime - 7 * 3600 + timeOffset; // Display start: now - 7 hours
        Location.NextTide nextTide = location.new NextTide(startGMTTime);
        for (int i = 0; i < numTideEvent; i++)
        {
            location.nextTide(nextTide);
            Date tideGMTDate = nextTide.getTideGMTDate();

            Date tideLocalDate = new Date(tideGMTDate.getTime() + rawOffsetTime + dayLightSavingOffsetTime);
            currentGMTCalendar.setTime(tideLocalDate);
            tideEvents[i] = new TideSummary();
            tideEvents[i].hour = formatCalendarToTime(currentGMTCalendar);
            tideEvents[i].shortLocalDate = formatCalendarToDate(currentGMTCalendar, false);
            tideEvents[i].longLocalDate = formatCalendarLong(currentGMTCalendar, true);
            tideEvents[i].gmtTime = tideGMTDate.getTime() / 1000;
            tideEvents[i].tideHeight = nextTide.getTideHeight();
            tideEvents[i].tideHeight = HarmonicFile.converterUnit(tideEvents[i].tideHeight, HarmonicFile.UNIT_METER, unit);
            tideEvents[i].tideType = nextTide.getTideType();
            System.out.println(tideEvents[i].longLocalDate + "  " + formatTideType(tideEvents[i].tideType) + " " + formatFloat(tideEvents[i].tideHeight, 1) + " " + unitString);
        }

        // Display the tide graph
        long endGMTTime = nextTide.getTideGMTTime() + 3 * 3600;

        int numStep = 60;

        long timeStep = (endGMTTime - startGMTTime) / numStep;

        long graphGMTTime = startGMTTime;
        float currentTideHeight = 0.0f, lastTideHeight = location.time2secondary (graphGMTTime);
        for (int i = 1; i < numStep; i++)
        {
            graphGMTTime += timeStep;
            currentTideHeight = location.time2secondary (graphGMTTime);
            System.out.println(i + ": "+  formatGMTDateLong(new Date(graphGMTTime*1000),true) + ": " +	 currentTideHeight*10 + "metres?");
            lastTideHeight = currentTideHeight;
        }


        // Display the tide events on the graph
        for (int i = 0; i < numTideEvent; i++)
        {
            System.out.println(	 "event"+tideEvents[i].toString());
        }
    }
}