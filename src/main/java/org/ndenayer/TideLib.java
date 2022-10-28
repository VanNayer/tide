
package org.ndenayer;

import org.apache.commons.io.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TideLib
{
    private HarmonicIndexFile harmonicIndexFile;
    private Location currentLocation;
    private String harmonicIndexFilePath;

    public void setCurrentLocation(Location location) throws IOException
    {
        this.currentLocation = location;
        if(!location.isLoaded())
            loadLocation(location);
    }
    public HarmonicIndexFile getHarmonicIndexFile() { return harmonicIndexFile; }

    public TideLib()
    {
    }

    public void loadHarmonicIndex(String harmonicIndexFilePath, String harmonicIndexFileName) throws IOException
    {
        this.harmonicIndexFilePath = harmonicIndexFilePath;

        InputStream inputStream;
        inputStream = getClass().getResourceAsStream(harmonicIndexFilePath + harmonicIndexFileName);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream); //, "ISO8859_1");
        harmonicIndexFile = new HarmonicIndexFile();
        try
        {
            harmonicIndexFile.loadIndex(inputStreamReader);
        }
        finally
        {
            inputStream.close();
        }
    }

    public void loadLocation(Location location) throws IOException {
        HarmonicFile harmonicFile = location.getHarmonicFile();
        if(!harmonicFile.isLoaded())
        {
            InputStream inputStream = getClass().getResourceAsStream(harmonicIndexFilePath + harmonicFile.getFileName());

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream); //, "ISO8859_1");
            try
            {
                harmonicFile.loadConstituents(inputStreamReader);
            }
            finally
            {
                inputStream.close();
            }
        }
        if(harmonicFile.isLoaded())
        {
            InputStream inputStream = getClass().getResourceAsStream(harmonicIndexFilePath + harmonicFile.getFileName());

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream); //, "ISO8859_1");
            try
            {
                harmonicFile.loadLocation(inputStreamReader, location);
            }
            finally
            {
                inputStream.close();
            }
        }
    }
}
