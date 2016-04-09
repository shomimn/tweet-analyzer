package bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.storm.shade.org.joda.time.DateTime;

import java.util.Date;
import java.util.Map;

/**
 * Created by nimbus on 4/9/16.
 */
public class TaxiBolt implements IRichBolt {

    public static final String ID = "taxiBolt";
    public static final String TAXI_BOLT_STREAM = "taxiBoltStream";

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        collector.emit(TAXI_BOLT_STREAM, new Values(tuple.getDouble(0), tuple.getDouble(1), tuple.getDouble(2), tuple.getDouble(3), tuple.getValue(4)));
    }

    @Override
    public void cleanup()
    {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
    {

    }

    @Override
    public Map<String, Object> getComponentConfiguration()
    {
        return null;
    }
}
