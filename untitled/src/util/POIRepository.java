package util;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.OperatorBuffer;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.QuadTree;
import com.esri.core.geometry.SpatialReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class POIRepository
{
    private static final int SPATIAL_REF_WKID = 4326;
    private static final int QUADTREE_HEIGHT = 16;
    private static final double BUFFER_DISTANCE = 0.00011;

    private QuadTree quadTree;
    private Envelope2D quadEnvelope;
    private ArrayList<POI> pois;

    public POIRepository(ArrayList<POI> p)
    {
        pois = p;
        findEnvelope();
        createQuadTree();
    }

    private void findEnvelope()
    {
        quadEnvelope = new Envelope2D(Double.MAX_VALUE, Double.MAX_VALUE,
                Double.MIN_VALUE, Double.MIN_VALUE);

        for (POI poi : pois)
            adjustEnvelope(poi.getLatitude(), poi.getLongitude());
    }

        private void adjustEnvelope(double lat, double lng)
    {
        if (lat > quadEnvelope.xmax)
            quadEnvelope.xmax = lat;
        if (lat < quadEnvelope.xmin)
            quadEnvelope.xmin = lat;
        if (lng > quadEnvelope.ymax)
            quadEnvelope.ymax = lng;
        if (lng < quadEnvelope.ymin)
            quadEnvelope.ymin = lng;
    }

    private void createQuadTree()
    {
        quadTree = new QuadTree(quadEnvelope, QUADTREE_HEIGHT);
        SpatialReference ref = SpatialReference.create(SPATIAL_REF_WKID);
        Envelope envelope = new Envelope();

        for (int i = 0; i < pois.size(); ++i)
        {
            Envelope2D env2d = envelopeOf(i, envelope, ref);
            quadTree.insert(i, env2d);
        }
    }

    private Envelope2D envelopeOf(int i, Envelope envelope, SpatialReference ref)
    {
        Point p = new Point(pois.get(i).getLatitude(), pois.get(i).getLongitude());
        Geometry geom = OperatorBuffer.local().execute(p, ref, BUFFER_DISTANCE, null);
        geom.queryEnvelope(envelope);

        return new Envelope2D(envelope.getXMin(), envelope.getYMin(),
                envelope.getXMax(), envelope.getYMax());
    }

    public int query(double latitude, double longitude)
    {
        QuadTree.QuadTreeIterator it = quadTree.getIterator(new Point(latitude, longitude), 0);

        return it.next();
    }

    public POI get(int handle)
    {
        return pois.get(quadTree.getElement(handle));
    }

    public boolean isValid(int handle)
    {
        return handle >= 0;
    }
}
