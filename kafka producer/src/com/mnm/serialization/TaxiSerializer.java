package com.mnm.serialization;

import com.mnm.data.Taxi;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class TaxiSerializer implements Serializer<Taxi>, Deserializer<Taxi>
{
    @Override
    public void configure(Map<String, ?> map, boolean b)
    {

    }

    @Override
    public byte[] serialize(String s, Taxi taxi)
    {
        String data = String.valueOf(taxi.pickupLatitude) + "," +
                String.valueOf(taxi.pickupLongitude) + "," +
                String.valueOf(taxi.dropoffLatitude) + "," +
                String.valueOf(taxi.dropoffLongitude);

        return data.getBytes();
    }

    @Override
    public Taxi deserialize(String s, byte[] bytes)
    {
        String[] data = new String(bytes).split(",");

        return new Taxi(Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]));
    }

    @Override
    public void close()
    {

    }
}
