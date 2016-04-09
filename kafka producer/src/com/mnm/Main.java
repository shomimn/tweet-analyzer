package com.mnm;

import com.mnm.producer.TaxiProducer;
import com.mnm.producer.VehicleProducer;

public class Main
{


    public static void main(String[] args)
    {
//       TaxiProducer taxiProducer = new TaxiProducer();
        VehicleProducer vehicleProducer = new VehicleProducer();
//       taxiProducer.run();
        vehicleProducer.run();
    }
}
