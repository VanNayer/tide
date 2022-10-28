package org.ndenayer;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

public class Main {
    public static void main(String[] args) throws IOException {
         String harmonicFile = "Harmonic.idx";
        System.out.println("Hello world!");
        TideLib tideLibrary = new TideLib();
        tideLibrary.loadHarmonicIndex("/", harmonicFile);


        HarmonicIndexFile harmonicIndexFile = tideLibrary.getHarmonicIndexFile();
        int locationNum = harmonicIndexFile.getLocationNum();
        for(int i = 0; i < locationNum; i++)
        {
            System.out.print(i);
            System.out.print(" - ");
            System.out.println(harmonicIndexFile.getLocation(i).getLocationName());
        }

        final int locationId = 19;
        tideLibrary.setCurrentLocation(tideLibrary.getHarmonicIndexFile().getLocation(locationId));
        Location location = tideLibrary.getHarmonicIndexFile().getLocation(locationId);
        GregorianCalendar calendar = new GregorianCalendar(122 + 1900, 10, 24, 12, 0, 0);

        System.out.println("Go pour: " + location +" at " + calendar.getTime());
        new TideCanvas(location, calendar.getTime()).paint();
    }
}