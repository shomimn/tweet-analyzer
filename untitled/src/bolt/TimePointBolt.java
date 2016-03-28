package bolt;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import twitter4j.Status;
import util.TimeFragmenter;

public class TimePointBolt extends BaseRichBolt
{
    public static final String ID = "timePointBolt";
    public static final String STREAM = "timePointStream";

    private OutputCollector collector;
    private TimeFragmenter fragmenter;

    public TimePointBolt(TimeFragmenter timeFragmenter)
    {
        fragmenter = timeFragmenter;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        Status status = (Status) tuple.getValue(0);

//        System.out.println(status.getCreatedAt().toString() + " " + fragmenter.getFragments(status.getCreatedAt()).toString());

        collector.emit(STREAM, fragmenter.getFragments(status.getCreatedAt()));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declareStream(STREAM, new Fields("day", "hour", "minute"));
    }
}
