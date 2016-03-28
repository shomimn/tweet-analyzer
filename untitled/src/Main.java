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
import util.TimeFragmenter;

public class Main
{
    public static final int DURATION = 1800000;
    public static final int TIME_UNITS = 5;

    public static void main(String[] args) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String consumerKey = "OUPp01w3n99RMLbuplpMpz3jq";
        String consumerSecret = "In1mZFGvnhuoOh3iLEZgPUx7V0OCqBf2Bqf9EkHdbBwhTrfbhg";
        String accessToken = "703712003592994816-uyTjVunf0eNowoBtBhZfxSpOSEyQI7d";
        String accessTokenSecret = "iRgGshI44MsSmc8tqzmApqzBz0fPBCm8HL1rUVK1s7nn4";
        String[] keyWords = new String[] { };

        TimeFragmenter fragmenter = new TimeFragmenter(DURATION, TIME_UNITS);

        builder.setSpout(TwitterSpout.ID, new TwitterSpout(consumerKey, consumerSecret,
                accessToken, accessTokenSecret, keyWords));

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

        cluster.shutdown();
    }
}
