package com.mnm.producer;


import com.mnm.data.Vehicle;
import com.mnm.serialization.VehicleSerialization;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.RunnableFuture;

public class VehicleProducer extends BaseProducer<Vehicle>
{
    public static final String VEHICLE_TOPIC = "vehicle-topic";
    public static final String FILE_PATH = "vehicles.txt";

//    public Producer<String, Vehicle> producer;

    public VehicleProducer(long sleepTime)
    {
        super(sleepTime);

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", VehicleSerialization.class.getName());

        producer = new KafkaProducer<>(props);
    }
    public void run()
    {
        thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                String line;
                int ind = 0;

                try(BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_PATH)))
                {
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        String[] values = line.split(" ");
                        Vehicle vehicle = new Vehicle(Long.parseLong(values[0]), Long.parseLong(values[1]), Double.parseDouble(values[3]), Double.parseDouble(values[4]));
                        producer.send(new ProducerRecord<>(VEHICLE_TOPIC, Integer.toString(ind++), vehicle));
                        Utils.sleep(sleepTime);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    producer.close();
                    System.out.println("vehicle producer closed");
                }

            }
        });
        thread.start();

    }

}
