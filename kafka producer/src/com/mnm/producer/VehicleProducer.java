package com.mnm.producer;


import com.mnm.data.Vehicle;
import com.mnm.serialization.VehicleSerialization;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.util.Random;

import java.io.FileReader;
import java.util.Properties;

public class VehicleProducer extends BaseProducer<Vehicle>
{
    public static final String VEHICLE_TOPIC = "vehicle-topic";
    public static final String FOLDER_NAME = "/home/milan/tweet-analyzer/kafka producer/data/vehicles";

    String[] listOfFiles;
//    public Producer<String, Vehicle> producer;

    public VehicleProducer(long sleepTime, String path)
    {
        super(sleepTime, path);

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

        File folder = new File(folderPath);
        listOfFiles = folder.list();
    }
    public void run()
    {
        thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                String line;
                int ind = 0;

                while(repeat)
                {

                    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(getRandomFile())))
                    {

                        while ((line = bufferedReader.readLine()) != null)
                        {
                            String[] values = line.split(" ");
                            Vehicle vehicle = new Vehicle(Long.parseLong(values[0]), Long.parseLong(values[1]), Double.parseDouble(values[3]), Double.parseDouble(values[4]));
                            producer.send(new ProducerRecord<>(VEHICLE_TOPIC, Integer.toString(ind++), vehicle));
                            Utils.sleep(delay);
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
                }

        });
        thread.start();

    }

    @Override
    public String getRandomFile() {
        Random r = new Random();
        return folderPath + "/" + listOfFiles[r.nextInt(listOfFiles.length)];
    }

}
