import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import bolt.PrinterBolt;
import bolt.ResultBolt;
import spout.TwitterSpout;

public class Main
{
    public static final int DELAY = 60000;

    public static void main(String[] args) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String consumerKey = "OUPp01w3n99RMLbuplpMpz3jq";
        String consumerSecret = "In1mZFGvnhuoOh3iLEZgPUx7V0OCqBf2Bqf9EkHdbBwhTrfbhg";
        String accessToken = "703712003592994816-uyTjVunf0eNowoBtBhZfxSpOSEyQI7d";
        String accessTokenSecret = "iRgGshI44MsSmc8tqzmApqzBz0fPBCm8HL1rUVK1s7nn4";
        String[] keyWords = new String[] { };
        ResultBolt resultBolt = new ResultBolt(DELAY);

        builder.setSpout("twitter", new TwitterSpout(consumerKey, consumerSecret,
                accessToken, accessTokenSecret, keyWords));
        builder.setBolt("print", new PrinterBolt())
                .shuffleGrouping("twitter");
        builder.setBolt("result", resultBolt, 1)
                .shuffleGrouping("print");

        Config config = new Config();
//        config.setDebug(true);

        LocalCluster cluster = new LocalCluster();

        cluster.submitTopology("test", config, builder.createTopology());

        Utils.sleep(DELAY);

        cluster.shutdown();
    }
}
