package bolt;

import com.esri.core.geometry.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.storm.shade.org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.*;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import org.apache.storm.shade.org.joda.time.DateTime;
import server.WebServer;
import twitter4j.Status;
import util.OptionsHandler;
import util.POI;


public class ResultBolt extends BaseBasicBolt implements OptionsHandler
{
    private static final int SPATIAL_REF_WKID = 4326;
    private static final double BUFFER_DISTANCE = 0.00055;
    private static final int TIME_OFFSET = 240;

    public static final String ID = "resultBolt";
    public static final long MINUTE = 60000;
    public static final int TWEET_THRESHOLD = 1;
    public static final int TAXI_THRESHOLD = 1;
    public int taxiCounter = 0;

    public static class SpatialData
    {
        public double latitude;
        public double longitude;
        public DateTime date;

        public SpatialData(double lat, double lng, DateTime d)
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
    private ArrayList<SpatialData> vehicleTweets = new ArrayList<>();
    private HashMap<String, Integer> placesMap = new HashMap<>();
    private HashMap<String, Integer> daysMap = new HashMap<>();
    private HashMap<String, Integer> hoursMap = new HashMap<>();
    private HashMap<String, Integer> minsMap = new HashMap<>();
    private HashMap<POI, Integer> twitterPoiMap = new HashMap<>();
    private HashMap<POI, Integer> taxiPoiMap = new HashMap<>();

    private Type listType = new TypeToken<ArrayList<SpatialData>>(){}.getType();
    private Type mapType = new TypeToken<HashMap<String, Integer>>(){}.getType();
    private Type poiType = new TypeToken<Set<POI>>(){}.getType();

    private Timer updateTimer;
    private TimerTask updateTask;

    private int tweetThreshold = 1;
    private int taxiThreshold = 1;

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

                clearBelowThreshold(twitterPoiMap, tweetThreshold);
                clearBelowThreshold(taxiPoiMap, taxiThreshold);


                Set<POI> intersection = new HashSet<>(twitterPoiMap.keySet());
                intersection.retainAll(taxiPoiMap.keySet());
                twitterPoiMap.keySet().removeAll(intersection);
                taxiPoiMap.keySet().removeAll(intersection);


                System.out.println(list.size());
                System.out.println(gson.toJsonTree(taxiPoiMap.keySet(), poiType).toString());
                JsonElement tweets = gson.toJsonTree(list, listType);
                JsonElement places = gson.toJsonTree(placesMap, mapType);
                JsonElement days = gson.toJsonTree(daysMap, mapType);
                JsonElement hours = gson.toJsonTree(hoursMap, mapType);
                JsonElement mins = gson.toJsonTree(minsMap, mapType);
                JsonElement vehicles = gson.toJsonTree(vehicleTweets, listType);
                JsonElement twitterPois = gson.toJsonTree(twitterPoiMap.keySet(), poiType);
                JsonElement taxiPois = gson.toJsonTree(taxiPoiMap.keySet(), poiType);
                JsonElement taxiTwitterPois = gson.toJsonTree(intersection);


                JsonObject timePoints = new JsonObject();
                timePoints.add("days", days);
                timePoints.add("hours", hours);
                timePoints.add("mins", mins);

                JsonObject root = new JsonObject();
                root.add("tweets", tweets);
                root.add("places", places);
                root.add("timePoints", timePoints);
                root.add("vehiclesPOIS", vehicles);
                root.add("twitterPois", twitterPois);
                root.add("taxiPois", taxiPois);
                root.add("taxiTwitterPois", taxiTwitterPois);
                root.addProperty("taxiTotal", taxiCounter);




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

                    vehicleTweets.clear();

                    twitterPoiMap.clear();
                    taxiPoiMap.clear();
                    taxiCounter = 0;
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

                DateTime date = (DateTime)tuple.getValue(2);
                POI poi = (POI) tuple.getValue(3);

                if (poi != null)
                {
                    System.out.println("POI: " + poi.getName());
                    tryUpdateCount(twitterPoiMap, poi);
                }

                list.add(new SpatialData(latitude, longitude, date));
            }
            else if(source.equals(LatLngBolt.TAXI_POI_STREAM))
            {
//                double latitude = tuple.getDouble(0);
//                double longitude = tuple.getDouble(1);
                taxiCounter++;
                POI poi = (POI) tuple.getValue(2);

//                DateTime dateTime = (DateTime) tuple.getValue(3);

                if(poi != null)
                    tryUpdateCount(taxiPoiMap, poi);
            }

            else if(source.equals(VehicleBolt.STREAM))
            {
                long id = tuple.getLong(0);
                long timestamp = tuple.getLong(1);
                double latitude = tuple.getDouble(2);
                double longitude = tuple.getDouble(3);
                DateTime date = (DateTime)tuple.getValue(4);

                SpatialReference ref = SpatialReference.create(SPATIAL_REF_WKID);
                Envelope envelope = new Envelope();
                Point p = new Point(latitude, longitude);
                Geometry geom = OperatorBuffer.local().execute(p, ref, BUFFER_DISTANCE, null);
                geom.queryEnvelope(envelope);

                DateTime refDateMin = date.minusSeconds(TIME_OFFSET);
                DateTime refDateMax = date.plusSeconds(TIME_OFFSET);

                for (SpatialData data: list)
                {
                    if(envelope.contains(new Point(data.latitude,data.longitude)) && refDateMin.isBefore(data.date) && refDateMax.isAfter(data.date))
                    {
                        vehicleTweets.add(new SpatialData(latitude,longitude));
                        System.out.println("TWEET FROM VEHICLE: LAT: " + latitude + " LON: " + longitude);
                    }
//                    if(envelope.contains(new Point(data.latitude,data.longitude)))
//                    {
//                        vehicleTweets.add(new SpatialData(latitude,longitude));
//                        System.out.println("TWEET FROM VEHICLE: LAT: " + latitude + " LON: " + longitude);
//                    }
                }
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


    private<T> void tryUpdateCount(HashMap<T, Integer> map, T key)
    {
        if (!map.containsKey(key))
            map.put(key, 0);

        updateCount(map, key);
    }

    private<T> void updateCount(HashMap<T, Integer> map, T key)
    {
        Integer value = map.get(key);
        ++value;
        map.put(key, value);
    }

    private void clearBelowThreshold(HashMap<POI, Integer> map, int threshold)
    {
        for (Iterator<Map.Entry<POI, Integer>> it = map.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<POI, Integer> entry = it.next();

            if (entry.getValue() < threshold)
                it.remove();
        }
    }

    @Override
    public void changeUpdateInterval(String json)
    {
        long millis = new JsonParser().parse(json).getAsJsonObject().get("interval").getAsLong();

        updateTask.cancel();
        createTask();
        updateTimer.scheduleAtFixedRate(updateTask, millis, millis);
    }

    @Override
    public void changeTweetThreshold(String json)
    {
        tweetThreshold = new JsonParser().parse(json).getAsJsonObject().get("threshold").getAsInt();
        System.out.println("tweet threshold: " + tweetThreshold);
    }

}
