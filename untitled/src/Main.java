import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.Scheme;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import bolt.PlaceBolt;
import bolt.PrinterBolt;
import bolt.ResultBolt;
import bolt.TimePointBolt;
import bolt.TimeUnitBolt;
import scheme.TaxiScheme;
import spout.TwitterSpout;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import util.AppConfig;
import util.TimeFragmenter;

class TestBolt implements IRichBolt
{
    private OutputCollector collector;
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
//        System.out.println(tuple.getString(0));
        System.out.println(tuple.toString());
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

public class Main
{
    public static final int DURATION = 300000;
    public static final int TIME_UNITS = 5;
    public static final String TAXI_TOPIC = "taxi-topic";

    public static void main(String[] args) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String[] keyWords = new String[] { };

        AppConfig appConfig = new AppConfig();
        appConfig.readFromFile("appconfig");

//        BrokerHosts hosts = new ZkHosts("localhost:2181");
//        SpoutConfig spoutConfig = new SpoutConfig(hosts, TAXI_TOPIC, "/" + TAXI_TOPIC, UUID.randomUUID().toString());
//        spoutConfig.scheme = new SchemeAsMultiScheme(new TaxiScheme());
//        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
//
//        builder.setSpout("kafkaSpout", kafkaSpout);
//
//        builder.setBolt("testBolt", new TestBolt())
//                .shuffleGrouping("kafkaSpout");

        TimeFragmenter fragmenter = new TimeFragmenter(DURATION, TIME_UNITS);

        builder.setSpout(TwitterSpout.ID, new TwitterSpout(appConfig.consumerKey, appConfig.consumerSecret,
                appConfig.accessToken, appConfig.accessTokenSecret, keyWords));

        builder.setBolt(PrinterBolt.ID, new PrinterBolt())
                .shuffleGrouping(TwitterSpout.ID);

        builder.setBolt(TimeUnitBolt.ID, new TimeUnitBolt(fragmenter))
                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.TIME_UNIT_STREAM);

        builder.setBolt(PlaceBolt.ID, new PlaceBolt())
                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.PLACE_STREAM);

        builder.setBolt(TimePointBolt.ID, new TimePointBolt(fragmenter))
                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.TIME_POINT_STREAM);

        builder.setBolt(ResultBolt.ID, new ResultBolt(DURATION, TIME_UNITS), 1)
                .shuffleGrouping(TimeUnitBolt.ID, TimeUnitBolt.STREAM)
                .shuffleGrouping(PlaceBolt.ID, PlaceBolt.STREAM)
                .shuffleGrouping(TimePointBolt.ID, TimePointBolt.STREAM);

        Config config = new Config();
//        config.setDebug(true);

        LocalCluster cluster = new LocalCluster();

        cluster.submitTopology("test", config, builder.createTopology());

        Utils.sleep(DURATION);

//        cluster.shutdown();
    }
}
