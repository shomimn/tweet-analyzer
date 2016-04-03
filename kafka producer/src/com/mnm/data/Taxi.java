package com.mnm.data;

public class Taxi
{
    public double pickupLatitude;
    public double pickupLongitude;
    public double dropoffLatitude;
    public double dropoffLongitude;

    public Taxi(double lat1, double lng1, double lat2, double lng2)
    {
        pickupLatitude = lat1;
        pickupLongitude = lng1;
        dropoffLatitude = lat2;
        dropoffLongitude = lng2;
    }
}
