package bolt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import server.WebServer;
import twitter4j.Status;

public class ResultBolt extends BaseBasicBolt
{
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
    private int delay;
    private int timeUnits;
    private ArrayList<ArrayList<SpatialData>> list;

    public ResultBolt(int d, int units)
    {
        delay = d;
        timeUnits = units;
        list = new ArrayList<>();

        for (int i = 0; i < timeUnits; ++i)
            list.add(new ArrayList<>());
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

        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                System.out.println(list.size());

                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<ArrayList<SpatialData>>>(){}.getType();
                String json = gson.toJson(list, type);

                System.out.println(json);

                server.sendToAll(json);

                try
                {
                    server.stop();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, delay);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector)
    {
        int timeUnit = tuple.getInteger(0);
        double latitude = tuple.getDouble(1);
        double longitude = tuple.getDouble(2);

        insert(list.get(timeUnit), latitude, longitude);
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

    private void insert(ArrayList<SpatialData> innerList, double lat, double lng)
    {
        innerList.add(new SpatialData(lat, lng));
    }
}
