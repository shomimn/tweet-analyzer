import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.OperatorBuffer;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Point2D;
import com.esri.core.geometry.QuadTree;
import com.esri.core.geometry.SpatialReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

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
import util.POI;
import util.TimeFragmenter;

public class Main
{
    public static final int DURATION = 300000;
    public static final int TIME_UNITS = 5;

    public static void main(String[] args) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String consumerKey = "OUPp01w3n99RMLbuplpMpz3jq";
        String consumerSecret = "In1mZFGvnhuoOh3iLEZgPUx7V0OCqBf2Bqf9EkHdbBwhTrfbhg";
        String accessToken = "703712003592994816-uyTjVunf0eNowoBtBhZfxSpOSEyQI7d";
        String accessTokenSecret = "iRgGshI44MsSmc8tqzmApqzBz0fPBCm8HL1rUVK1s7nn4";
        String[] keyWords = new String[] { };

        BufferedReader reader = new BufferedReader(new FileReader("/home/milos/storm/tweet-analyzer/new-york.csv"));
        String line;
        ArrayList<POI> pois = new ArrayList<>();
        double xmax = Double.MIN_VALUE;
        double ymax = Double.MIN_VALUE;
        double xmin = Double.MAX_VALUE;
        double ymin = Double.MAX_VALUE;
        while ((line = reader.readLine()) != null)
        {
            String[] tokens = line.split("\\|");
            double lat = Double.parseDouble(tokens[2]);
            double lng = Double.parseDouble(tokens[3]);

            if (lat > xmax)
                xmax = lat;
            if (lat < xmin)
                xmin = lat;
            if (lng > ymax)
                ymax = lng;
            if (lng < ymin)
                ymin = lng;

            pois.add(new POI(lat, lng, tokens[4]));
            System.out.println(line);
        }

        QuadTree quadTree = new QuadTree(new Envelope2D(xmin, ymin, xmax, ymax), 16);
        SpatialReference ref = SpatialReference.create(4326);
        for (int i = 0; i < pois.size(); ++i)
        {
            Envelope envelope = new Envelope();
            Point p = new Point(pois.get(i).getLatitude(), pois.get(i).getLongitude());
            Geometry geom = OperatorBuffer.local().execute(p, ref, 0.00011, null);
            geom.queryEnvelope(envelope);

            quadTree.insert(i, new Envelope2D(envelope.getXMin(), envelope.getYMin(),
                    envelope.getXMax(), envelope.getYMax()));
        }

        QuadTree.QuadTreeIterator it = quadTree.getIterator(new Point(40.73824, -74.00429), 0);
        int handle = it.next();
        while (handle >= 0)
        {
            int element = quadTree.getElement(handle);
            System.out.println(pois.get(element).getName());
            handle = it.next();
        }


//        TimeFragmenter fragmenter = new TimeFragmenter(DURATION, TIME_UNITS);
//
//        builder.setSpout(TwitterSpout.ID, new TwitterSpout(consumerKey, consumerSecret,
//                accessToken, accessTokenSecret, keyWords));
//
//        builder.setBolt(PrinterBolt.ID, new PrinterBolt())
//                .shuffleGrouping(TwitterSpout.ID);
//
//        builder.setBolt(TimeUnitBolt.ID, new TimeUnitBolt(fragmenter))
//                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.TIME_UNIT_STREAM);
//
//        builder.setBolt(PlaceBolt.ID, new PlaceBolt())
//                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.PLACE_STREAM);
//
//        builder.setBolt(TimePointBolt.ID, new TimePointBolt(fragmenter))
//                .shuffleGrouping(PrinterBolt.ID, PrinterBolt.TIME_POINT_STREAM);
//
//        builder.setBolt(ResultBolt.ID, new ResultBolt(DURATION, TIME_UNITS), 1)
//                .shuffleGrouping(TimeUnitBolt.ID, TimeUnitBolt.STREAM)
//                .shuffleGrouping(PlaceBolt.ID, PlaceBolt.STREAM)
//                .shuffleGrouping(TimePointBolt.ID, TimePointBolt.STREAM);
//
//        Config config = new Config();
////        config.setDebug(true);
//
//        LocalCluster cluster = new LocalCluster();
//
//        cluster.submitTopology("test", config, builder.createTopology());
//
//        Utils.sleep(DURATION);

//        cluster.shutdown();
    }
}
