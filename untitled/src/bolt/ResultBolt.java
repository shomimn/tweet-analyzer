package bolt;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.storm.shade.org.joda.time.Days;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import server.WebServer;


public class ResultBolt extends BaseBasicBolt
{
    public static final String ID = "resultBolt";

    public static class SpatialData
    {
        public double latitude;
        public double longitude;
        public Date date;

        public SpatialData(double lat, double lng, Date d)
        {
            latitude = lat;
            longitude = lng;
            date = d;
        }

        public SpatialData(double lat, double lng)
        {
            latitude = lat;
            longitude = lng;
        }
    }

    private WebServer server;
    private int duration;
    private int timeUnits;

//    private ArrayList<ArrayList<SpatialData>> list = new ArrayList<>();
    private ArrayList<SpatialData> list = new ArrayList<>();
    private HashMap<String, Integer> placesMap = new HashMap<>();
    private HashMap<String, Integer> daysMap = new HashMap<>();
    private HashMap<String, Integer> hoursMap = new HashMap<>();
    private HashMap<String, Integer> minsMap = new HashMap<>();

//    private Type listType = new TypeToken<ArrayList<ArrayList<SpatialData>>>(){}.getType();
    private Type listType = new TypeToken<ArrayList<SpatialData>>(){}.getType();
    private Type mapType = new TypeToken<HashMap<String, Integer>>(){}.getType();

    public ResultBolt(int d, int units)
    {
        duration = d;
        timeUnits = units;

        initMaps();
    }

    private void initMaps()
    {
        daysMap.put("Monday", 0);
        daysMap.put("Tuesday", 0);
        daysMap.put("Wednesday", 0);
        daysMap.put("Thursday", 0);
        daysMap.put("Friday", 0);
        daysMap.put("Saturday", 0);
        daysMap.put("Sunday", 0);

        for (int i = 1; i < 25; ++i)
            hoursMap.put(String.valueOf(i), 0);

        for (int i = 0; i < 60; ++i)
            minsMap.put(String.valueOf(i), 0);
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context)
    {
        super.prepare(stormConf, context);

        try
        {
            server = new WebServer(8888);
            server.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        new Timer().scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                Gson gson = new Gson();

                System.out.println(list.size());
                JsonElement timeUnits = gson.toJsonTree(list, listType);
                JsonElement places = gson.toJsonTree(placesMap, mapType);
                JsonElement days = gson.toJsonTree(daysMap, mapType);
                JsonElement hours = gson.toJsonTree(hoursMap, mapType);
                JsonElement mins = gson.toJsonTree(minsMap, mapType);

                JsonObject timePoints = new JsonObject();
                timePoints.add("days", days);
                timePoints.add("hours", hours);
                timePoints.add("mins", mins);

                JsonObject root = new JsonObject();
                root.add("timeUnits", timeUnits);
                root.add("places", places);
                root.add("timePoints", timePoints);

                String json = root.toString();
                System.out.println(json);

                server.sendToAll(json);

                synchronized (this)
                {
                    list.clear();
                    placesMap.clear();
                    daysMap.clear();
                    hoursMap.clear();
                    minsMap.clear();
                }
            }
        }, 5 * 60000, 5 * 60000);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector)
    {
        String source = tuple.getSourceStreamId();

        synchronized (this)
        {
            if (source.equals(PlaceBolt.STREAM))
            {
                String place = tuple.getString(0);

                tryUpdateCount(placesMap, place);
            }
            else if (source.equals(TimeUnitBolt.STREAM))
            {
                int timeUnit = tuple.getInteger(0);
                double latitude = tuple.getDouble(1);
                double longitude = tuple.getDouble(2);

                list.add(new SpatialData(latitude, longitude));
            }
            else
            {
                String day = tuple.getString(0);
                String hour = tuple.getString(1);
                String min = tuple.getString(2);

                tryUpdateCount(daysMap, day);
                tryUpdateCount(hoursMap, hour);
                tryUpdateCount(minsMap, min);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
    {

    }

    @Override
    public void cleanup()
    {
        super.cleanup();
    }


    private void tryUpdateCount(HashMap<String, Integer> map, String key)
    {
        if (!map.containsKey(key))
            map.put(key, 0);

        updateCount(map, key);
    }

    private void updateCount(HashMap<String, Integer> map, String key)
    {
        Integer value = map.get(key);
        ++value;
        map.put(key, value);
    }

}
