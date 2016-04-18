package com.mnm.producer;


import com.mnm.data.Taxi;
import com.mnm.serialization.TaxiSerializer;
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

public class TaxiProducer
{
    public static final String TAXI_TOPIC = "taxi-topic";
    public static final String FILE_PATH = "/home/milos/IdeaProjects/tweet-analyzer/untitled/trip_data_1.csv";
    public Producer<String, Taxi> producer;
    String[] values;

    public TaxiProducer()
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", TaxiSerializer.class.getName());

        producer = new KafkaProducer<>(props);
    }
    public void run()
    {

        BufferedReader bufferedReader = null;
        String line;
        int ind = 0;

        try
        {
            bufferedReader = new BufferedReader(new FileReader(FILE_PATH));
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] values = line.split(",");

                if (values.length < 13)
                    continue;

                Taxi taxi = new Taxi(Double.parseDouble(values[11]), Double.parseDouble(values[10]), Double.parseDouble(values[13]), Double.parseDouble(values[12]));

                //u fajlu postoje redovi gde su sve tacke 0, pa sam ih izbacio
                if(taxi.pickupLatitude != 0)
                {
                    producer.send(new ProducerRecord<String, Taxi>(TAXI_TOPIC, Integer.toString(ind++), taxi));
                    Utils.sleep(500);
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(bufferedReader != null)
                try
                {
                    bufferedReader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            producer.close();
            System.out.println("taxi producer closed");
        }
    }
}
