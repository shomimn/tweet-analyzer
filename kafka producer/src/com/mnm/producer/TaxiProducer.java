package com.mnm.producer;


import com.mnm.data.Taxi;
import com.mnm.serialization.TaxiSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.util.Properties;
import java.util.Random;


public class TaxiProducer extends BaseProducer<Taxi>
{
    public static final String TAXI_TOPIC = "taxi-topic";
    String[] listOfFiles;
//    public Producer<String, Taxi> producer;

    public TaxiProducer(long sleepTime, String path)
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
        props.put("value.serializer", TaxiSerializer.class.getName());

        producer = new KafkaProducer<>(props);

        File folder = new File(folderPath);
        listOfFiles = folder.list();

    }
    public void run()
    {
        thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String line;
                int ind = 0;

                while(repeat)
                {
                    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(getRandomFile())))
                    {
                        bufferedReader.readLine();
                        while ((line = bufferedReader.readLine()) != null)
                        {
                            String[] values = line.split(",");

                            if (values.length < 13)
                                continue;

                            Taxi taxi = new Taxi(Double.parseDouble(values[11]), Double.parseDouble(values[10]), Double.parseDouble(values[13]), Double.parseDouble(values[12]));

                            //u fajlu postoje redovi gde su sve tacke 0, pa sam ih izbacio
                            if (taxi.pickupLatitude != 0)
                            {
                                producer.send(new ProducerRecord<String, Taxi>(TAXI_TOPIC, Integer.toString(ind++), taxi));

                                if (max > 0 && ind > max)
                                {
                                    repeat = false;
                                    break;
                                }

                                Utils.sleep(delay);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                producer.close();
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
