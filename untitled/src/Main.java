import java.lang.reflect.Field;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import bolt.PlaceBolt;
import bolt.PrinterBolt;
import bolt.ResultBolt;
import bolt.TimePointBolt;
import bolt.TimeUnitBolt;
import spout.TwitterSpout;
import util.AppConfig;
import util.TimeFragmenter;

public class Main
{
    public static final int DURATION = 300000;
    public static final int TIME_UNITS = 5;

    public static void main(String[] args) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String[] keyWords = new String[] { };

        AppConfig appConfig = new AppConfig();
        appConfig.readFromFile("appconfig");

//        POIRepository repo = new POIRepository();
//        int handle = repo.query(40.73824, -74.00429);
//        if (repo.isValid(handle))
//            System.out.println(repo.get(handle));

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
