package com.mnm.data;


public class Vehicle {
    public long id;
    public long timestamp;
    public double latitude;
    public double longitude;

    public Vehicle(long i, long time, double lat, double lon)
    {
        id = i;
        timestamp = time;
        latitude = lat;
        longitude = lon;
    }


}
