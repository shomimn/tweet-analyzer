package com.mnm.serialization;


import com.mnm.data.Taxi;
import com.mnm.data.Vehicle;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class VehicleSerialization implements Serializer<Vehicle>, Deserializer<Vehicle>
{
        @Override
        public void configure(Map<String, ?> map, boolean b)
        {

        }

        @Override
        public byte[] serialize(String s, Vehicle vehicle)
        {
                String data = String.valueOf(vehicle.id) + "," +
                        String.valueOf(vehicle.timestamp) + "," +
                String.valueOf(vehicle.latitude) + "," +
                String.valueOf(vehicle.longitude);

                return data.getBytes();
        }

        @Override
        public Vehicle deserialize(String s, byte[] bytes)
        {
                String[] data = new String(bytes).split(",");

                return new Vehicle(Long.parseLong(data[0]),Long.parseLong(data[1]),
                        Double.parseDouble(data[2]),Double.parseDouble(data[3]));
        }

        @Override
        public void close()
        {

        }
}
