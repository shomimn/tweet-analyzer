package com.mnm;

import com.mnm.data.Taxi;
import com.mnm.serialization.TaxiSerializer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;

import java.util.Properties;

public class Main
{
    public static final String TAXI_TOPIC = "taxi-topic";

    public static void main(String[] args)
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

        Producer<String, Taxi> producer = new KafkaProducer<>(props);
        for(int i = 0; i < 100; ++i)
        {
            producer.send(new ProducerRecord<String, Taxi>(TAXI_TOPIC, Integer.toString(i), new Taxi(i, i, i, i)));
            Utils.sleep(1000);
        }

        producer.close();
    }
}
