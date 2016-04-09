package com.mnm;

import com.mnm.producer.TaxiProducer;
import com.mnm.producer.VehicleProducer;

public class Main
{
    public static void main(String[] args)
    {
        VehicleProducer vehicleProducer = new VehicleProducer();
        vehicleProducer.run();
        TaxiProducer taxiProducer = new TaxiProducer();
        taxiProducer.run();
    }
}
