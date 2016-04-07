package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;

public class AppConfig
{
    public String consumerKey;
    public String consumerSecret;
    public String accessToken;
    public String accessTokenSecret;
    public String poiPath;

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
