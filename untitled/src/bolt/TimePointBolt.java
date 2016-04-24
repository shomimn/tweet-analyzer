package bolt;

import java.util.Date;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.Status;

public class TimePointBolt extends BaseRichBolt
{
    public static final String ID = "timePointBolt";
    public static final String STREAM = "timePointStream";
    private static final String[] days = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

    private OutputCollector collector;

    public TimePointBolt()
    {
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
        Date date = status.getCreatedAt();

        String day = days[date.getDay()];
        String hour = String.valueOf(date.getHours());
        String minutes =  String.valueOf(date.getMinutes());

        collector.emit(STREAM, new Values(day, hour, minutes));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declareStream(STREAM, new Fields("day", "hour", "minute"));
    }
}
