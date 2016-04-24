package com.mnm;

import com.mnm.producer.BaseProducer;
import com.mnm.producer.TaxiProducer;
import com.mnm.producer.VehicleProducer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main
{
    public static final String VEHICLE_KEY = "vehicle";
    public static final String TAXI_KEY = "taxi";

    interface Converter
    {
        Object convert(String value);
    }

    static class LongConverter implements Converter
    {
        public Long convert(String value)
        {
            return Long.parseLong(value);
        }
    }

    static class BoolConverter implements Converter
    {
        public Boolean convert(String value)
        {
            return Boolean.parseBoolean(value);
        }
    }

    abstract class Command
    {
        public static final String QUERY = "(" + VEHICLE_KEY + "|" + TAXI_KEY + ")";
        public static final String EXIT = "exit";
        public static final String DELAY = "delay";
        public static final String MAX = "max";
        public static final String REPEAT = "repeat";
        public static final String QUERY_ATTRIBUTE = QUERY + " " + "(" + DELAY + "|" + MAX + "|" + REPEAT + ")";
        public static final String ASSIGN_ATTRIBUTE = QUERY_ATTRIBUTE + "( )*=( )*([0-9]+" + "|" + "true|false)";
    }

    public static void main(String[] args)
    {
        HashMap<String, BaseProducer<?>> producers = new HashMap<>();
        HashMap<String, Converter> converterMap = new HashMap<>();

        LongConverter longConverter = new LongConverter();
        BoolConverter boolConverter = new BoolConverter();

        converterMap.put(Command.DELAY, longConverter);
        converterMap.put(Command.MAX, longConverter);
        converterMap.put(Command.REPEAT, boolConverter);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            VehicleProducer vehicleProducer = new VehicleProducer(1000);
            vehicleProducer.run();
            TaxiProducer taxiProducer = new TaxiProducer(500);
            taxiProducer.run();

            producers.put(VEHICLE_KEY, vehicleProducer);
            producers.put(TAXI_KEY, taxiProducer);

            printHelp();
            String cmd;
            while (!(cmd = reader.readLine().trim()).equals(Command.EXIT))
            {
                if (cmd.matches(Command.QUERY))
                {
                    System.out.println(producers.get(cmd).getInfo());
                }
                else if (cmd.matches(Command.QUERY_ATTRIBUTE))
                {
                    String[] tokens = cmd.split(" ");
                    BaseProducer<?> producer = producers.get(tokens[0]);

                    Field field = BaseProducer.class.getDeclaredField(tokens[1]);
                    field.setAccessible(true);
                    System.out.println(cmd + ": " + field.get(producer));
                }
                else if (cmd.matches(Command.ASSIGN_ATTRIBUTE))
                {
                    String[] tokens = cmd.split(" +");
                    BaseProducer<?> producer = producers.get(tokens[0]);

                    Field field = BaseProducer.class.getDeclaredField(tokens[1]);
                    field.setAccessible(true);
                    field.set(producer, converterMap.get(tokens[1]).convert(tokens[3]));
                    System.out.println(tokens[0] + " " + tokens[1] + ": " + tokens[3]);
                }
            }
            vehicleProducer.close();
            taxiProducer.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void printHelp()
    {
        System.out.println("Commands:");
        System.out.println("vehicle [delay | max | repeat] [= value] - query or assign value");
        System.out.println("taxi [delay | max | repeat] [= value] - query or assign value");
        System.out.println("exit");
        System.out.println("---------------------------------------------------------------");
    }
}
