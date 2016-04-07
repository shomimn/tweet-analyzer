package bolt;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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
import util.OptionsHandler;


public class ResultBolt extends BaseBasicBolt implements OptionsHandler
{
    public static final String ID = "resultBolt";
    public static final long MINUTE = 60000;

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

    private ArrayList<SpatialData> list = new ArrayList<>();
    private HashMap<String, Integer> placesMap = new HashMap<>();
    private HashMap<String, Integer> daysMap = new HashMap<>();
    private HashMap<String, Integer> hoursMap = new HashMap<>();
    private HashMap<String, Integer> minsMap = new HashMap<>();

    private Type listType = new TypeToken<ArrayList<SpatialData>>(){}.getType();
    private Type mapType = new TypeToken<HashMap<String, Integer>>(){}.getType();

    private Timer updateTimer;
    private TimerTask updateTask;

    public ResultBolt()
    {
    }

    private void createTask()
    {
        updateTask = new TimerTask()
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
                root.add("tweets", timeUnits);
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
        };
    }

    private void scheduleTask(long interval)
    {
        updateTimer.scheduleAtFixedRate(updateTask, interval, interval);
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context)
    {
        super.prepare(stormConf, context);

        try
        {
            server = new WebServer(this, 8888);
            server.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        updateTimer = new Timer();
        createTask();
        scheduleTask(1 * MINUTE);
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
            else if (source.equals(LatLngBolt.STREAM))
            {
                double latitude = tuple.getDouble(0);
                double longitude = tuple.getDouble(1);

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

    @Override
    public void changeUpdateInterval(String json)
    {
        long millis = new JsonParser().parse(json).getAsJsonObject().get("interval").getAsLong();

        updateTask.cancel();
        createTask();
        updateTimer.scheduleAtFixedRate(updateTask, millis, millis);
    }

}
