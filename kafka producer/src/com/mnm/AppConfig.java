package com.mnm;


import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;


public class AppConfig
{
    public String vehiclesPath;
    public String taxisPath;

    public AppConfig()
    {
    }

    public void readFromFile(String path)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            String line;

            while((line = reader.readLine()) != null)
            {
                line = line.trim();
                String[] tokens = line.split("=");

                String name = tokens[0].trim();
                String value = tokens[1].trim();

                Field field = AppConfig.class.getDeclaredField(name);
                field.set(this, value);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

