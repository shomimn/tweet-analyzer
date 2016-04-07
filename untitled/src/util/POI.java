package util;

import java.io.Serializable;

public class POI
{
    private double latitude;
    private double longitude;
    private String name;

    public POI(double lat, double lng, String n)
    {
        latitude = lat;
        longitude = lng;
        name = n;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}