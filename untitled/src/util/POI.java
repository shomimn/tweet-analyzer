package util;

import java.io.Serializable;

public class POI implements Serializable
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

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (!(obj instanceof POI))
            return false;

        POI other = (POI) obj;

        return name.equals(other.getName()) &&
                latitude == other.getLatitude() &&
                longitude == other.getLongitude();
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() * (int) (latitude * longitude);
    }

    @Override
    public String toString()
    {
        return "( " + name + " " + latitude + " " + longitude + " )";
    }
}
