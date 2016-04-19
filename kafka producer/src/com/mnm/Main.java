package com.mnm;

import com.mnm.producer.BaseProducer;
import com.mnm.producer.TaxiProducer;
import com.mnm.producer.VehicleProducer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Main
{
    public static final String VEHICLE_KEY = "vehicleDelay";
    public static final String TAXI_KEY = "taxiDelay";

    abstract class Command
    {
        public static final String QUERY = "(" + VEHICLE_KEY + "|" + TAXI_KEY + ")";
        public static final String ASSIGN = QUERY + "(=)[0-9]+";
        public static final String EXIT = "exit";
    }

    public static void main(String[] args)
    {
        HashMap<String, BaseProducer<?>> producers = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            VehicleProducer vehicleProducer = new VehicleProducer(1000);
//            vehicleProducer.run();
            TaxiProducer taxiProducer = new TaxiProducer(500);
//            taxiProducer.run();

            producers.put(VEHICLE_KEY, vehicleProducer);
            producers.put(TAXI_KEY, taxiProducer);

            printHelp();
            String cmd;
            while (!(cmd = reader.readLine().trim()).equals(Command.EXIT))
            {
                if (cmd.matches(Command.QUERY))
                {
                    System.out.println(producers.get(cmd).getSleepTime());
                }
                else if (cmd.matches(Command.ASSIGN))
                {
                    String[] tokens = cmd.split("=");
                    long sleepTime = Long.parseLong(tokens[1]);
                    BaseProducer<?> producer = producers.get(tokens[0]);
                    producer.setSleepTime(sleepTime);
                    System.out.println(producer.getSleepTime());
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
        System.out.println("vehicleDelay[=value in ms] - query or assign value");
        System.out.println("taxiDelay[=value in ms] - query or assign value");
    }
}
