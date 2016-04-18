package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;

public class AppConfig
{
    interface Converter
    {
        Object convert(String value);
    }

    class StringConverter implements Converter
    {
        public String convert(String value)
        {
            return value;
        }
    }

    class IntConverter implements Converter
    {
        public Integer convert(String value)
        {
            return Integer.parseInt(value);
        }
    }

    class DoubleConverter implements Converter
    {
        public Double convert(String value)
        {
            return Double.parseDouble(value);
        }
    }

    public String consumerKey;
    public String consumerSecret;
    public String accessToken;
    public String accessTokenSecret;
    public String poiPath;
    public double test1;
    public int test2;

    private HashMap<String, Converter> converterMap;

    public AppConfig()
    {
        converterMap = new HashMap<>();
        StringConverter stringConverter = new StringConverter();
        IntConverter intConverter = new IntConverter();
        DoubleConverter doubleConverter = new DoubleConverter();

        converterMap.put("consumerKey", stringConverter);
        converterMap.put("consumerSecret", stringConverter);
        converterMap.put("accessToken", stringConverter);
        converterMap.put("accessTokenSecret", stringConverter);
        converterMap.put("poiPath", stringConverter);
        converterMap.put("test1", doubleConverter);
        converterMap.put("test2", intConverter);
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
//                field.set(this, value);
                field.set(this, converterMap.get(name).convert(value));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
