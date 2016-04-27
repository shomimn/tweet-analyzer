import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import bolt.PlaceBolt;
import bolt.PrinterBolt;
import bolt.ResultBolt;
import bolt.TimePointBolt;
import bolt.LatLngBolt;
import scheme.TaxiScheme;
import spout.TwitterSpout;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.KeyValueSchemeAsMultiScheme;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;
import bolt.*;
import scheme.VehicleScheme;
import util.AppConfig;
import util.POI;

public class Main
{
    public static final int DURATION = 300000;
    public static final int TIME_UNITS = 5;
    public static final String TAXI_TOPIC = "taxi-topic";
    public static final String VEHICLE_TOPIC = "vehicle-topic";

    public static void fillPois(String path, ArrayList<POI> pois)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                String[] tokens = line.split("\\|");
                double lat = Double.parseDouble(tokens[2]);
                double lng = Double.parseDouble(tokens[3]);

                pois.add(new POI(lat, lng, tokens[4]));
            }

            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String[] keyWords = new String[] { };

        AppConfig appConfig = new AppConfig();
        appConfig.readFromFile("appconfig");

        ArrayList<POI> pois = new ArrayList<>();
        fillPois(appConfig.poiPath, pois);

        BrokerHosts hosts = new ZkHosts(appConfig.zkHost);
        SpoutConfig spoutConfig = new SpoutConfig(hosts, TAXI_TOPIC, "/" + TAXI_TOPIC, UUID.randomUUID().toString());
        spoutConfig.scheme = new KeyValueSchemeAsMultiScheme(new TaxiScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

        SpoutConfig spoutConfigVehicle = new SpoutConfig(hosts, VEHICLE_TOPIC, "/" + VEHICLE_TOPIC, UUID.randomUUID().toString());
        spoutConfigVehicle.scheme = new KeyValueSchemeAsMultiScheme(new VehicleScheme());
        KafkaSpout vehicleSpout = new KafkaSpout(spoutConfigVehicle);

        builder.setSpout("taxiSpout", kafkaSpout);

        builder.setBolt(TaxiBolt.ID, new TaxiBolt())
                .shuffleGrouping("taxiSpout");

        builder.setSpout("vehicleSpout", vehicleSpout);

        builder.setBolt(VehicleBolt.ID, new VehicleBolt())
                .shuffleGrouping("vehicleSpout");

        builder.setSpout(TwitterSpout.ID, new TwitterSpout(appConfig.consumerKey, appConfig.consumerSecret,
                appConfig.accessToken, appConfig.accessTokenSecret, keyWords));

        builder.setBolt(PrinterBolt.ID, new PrinterBolt())
                .shuffleGrouping(TwitterSpout.ID);

        builder.setBolt(LatLngBolt.ID, new LatLngBolt(pois))
                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.LAT_LNG_STREAM)
                .shuffleGrouping(TaxiBolt.ID, TaxiBolt.TAXI_BOLT_STREAM);

        builder.setBolt(PlaceBolt.ID, new PlaceBolt())
                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.PLACE_STREAM);

        builder.setBolt(TimePointBolt.ID, new TimePointBolt())
                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.TIME_POINT_STREAM);

        builder.setBolt(ResultBolt.ID, new ResultBolt(), 1)
                .shuffleGrouping(LatLngBolt.ID, LatLngBolt.STREAM)
                .shuffleGrouping(LatLngBolt.ID, LatLngBolt.TAXI_POI_STREAM)
                .shuffleGrouping(PlaceBolt.ID, PlaceBolt.STREAM)
                .shuffleGrouping(TimePointBolt.ID, TimePointBolt.STREAM)
                .shuffleGrouping(VehicleBolt.ID, VehicleBolt.STREAM);

        Config config = new Config();
//        config.setDebug(true);

        LocalCluster cluster = new LocalCluster();

        cluster.submitTopology("test", config, builder.createTopology());

//        Utils.sleep(DURATION);
//        StormSubmitter.submitTopology("tweet-analyzer", config, builder.createTopology());

//        cluster.shutdown();
    }
}
