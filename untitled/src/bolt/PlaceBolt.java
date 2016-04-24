package bolt;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.Status;

public class PlaceBolt extends BaseRichBolt
{
    public static final String ID = "placeBolt";
    public static final String STREAM = "placeStream";

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        Status status = (Status) tuple.getValue(0);

        collector.emit(STREAM, new Values(status.getPlace().getName()));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declareStream(STREAM, new Fields("place"));
    }
}
