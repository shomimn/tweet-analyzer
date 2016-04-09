package util;

/**
 * Created by nimbus on 4/7/16.
 */
public class Taxi
{
    private double pickup_latitude;
    private double pickup_longitude;
    private double dropoff_latitude;
    private double dropoff_longitude;

    public Taxi(double pu_la, double pu_lo, double do_la, double do_lo)
    {
        pickup_latitude = pu_la;
        pickup_longitude = pu_lo;
        dropoff_latitude = do_la;
        dropoff_longitude = do_lo;
    }
    public void setPickupLa(double pu_la)
    {
        pickup_latitude = pu_la;
    }
    public void setPickupLo(double pu_lo)
    {
        pickup_longitude = pu_lo;
    }
    public void setDropoffLa(double do_la)
    {
        dropoff_latitude = do_la;
    }
    public void setDropoffLo(double do_la)
    {
        dropoff_longitude = do_la;
    }

    public double getPickup_latitude() { return pickup_latitude; }
    public double getPickup_longitude() { return pickup_longitude; }
    public double getDropoff_latitude() { return dropoff_latitude; }
    public double getDropoff_longitude() { return dropoff_longitude; }
}