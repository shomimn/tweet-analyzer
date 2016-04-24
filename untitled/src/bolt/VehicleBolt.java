package bolt;


import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.commons.lang.ObjectUtils;
import org.apache.storm.shade.org.joda.time.DateTime;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

public class VehicleBolt extends BaseRichBolt
{
    public static final String ID = "vehicleBolt";
    public static final String STREAM = "vehicleStream";

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        Long id = tuple.getLong(1);
        Long timestamp = tuple.getLong(2);
        Double lat = tuple.getDouble(3);
        Double lon = tuple.getDouble(4);
        DateTime date = DateTime.now();

        collector.emit(STREAM, new Values(id, timestamp, lat, lon, date));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
    {
        outputFieldsDeclarer.declareStream(STREAM, new Fields("id","timestamp","latitude","longitude","date"));
    }
}
