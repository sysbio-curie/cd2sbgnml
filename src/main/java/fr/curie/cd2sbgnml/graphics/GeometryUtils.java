package fr.curie.cd2sbgnml.graphics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Util class containing only static methods, dealing with geometry objects.
 * Methods can be grouped into different themes:
 *  - creating and applying transforms to change system of coordinates
 *  - converting anchor points to coordinates depending of shapes, and vice versa
 *  - basic geometry things like angle unit conversion, slope, intersection of lines...
 *  - auxiliary units placement geometry
 *  - and more...
 */
public class GeometryUtils {

    private static final Logger logger = LoggerFactory.getLogger(GeometryUtils.class);

    /**
     * Given 2 points in global map coordinate, defining an origin and a unit vector,
     * returns a list of transforms that can be applied to convert local coordinates (in the space
     * defined by those 2 points) to global coordinates (same system the 2 points are provided with).
     *
     * The y is considered to be perpendicular to x, pointing to the right of x (with a global system having a
     * y axis pointing down).
     *
     * @return list of AffineTransform that, if applied in order on a local coordinates, yield the global coordinates.
     */
    public static List<AffineTransform> getTransformsToGlobalCoords(Point2D origin, Point2D px) {//, Point2D py) {

        Point2D originCopy = new Point2D.Double(origin.getX(), origin.getY());
        Point2D pxCopy = new Point2D.Double(px.getX(), px.getY());

        // set new coordinate system origin to center of start element
        AffineTransform t1_5 = new AffineTransform();
        t1_5.translate(-originCopy.getX(), -originCopy.getY());
        //t1_5.transform(origin, origin);
        t1_5.transform(pxCopy, pxCopy);

        //System.out.println("after set to origin: " + originCopy + " " + pxCopy);

        // get angle of destination element from X axis
        double angle = angle(new Point2D.Float(1,0), pxCopy);
        //System.out.println("angle: " + pxCopy + " " + angle);

        // rotate system to align destination on X axis
        AffineTransform t2 = new AffineTransform();
        t2.rotate(-angle);
        //t2.transform(origin, origin);
        t2.transform(pxCopy, pxCopy);

        //System.out.println("after align destination on X: " + pxCopy);

        // scale to have destination element at 1 on X
        AffineTransform t3 = new AffineTransform();
        t3.scale(1 / pxCopy.getX(), 1 / pxCopy.getX());
        //t3.transform(origin, origin);
        t3.transform(pxCopy, pxCopy);

        //System.out.println("after scale destination to 1 on X: " + pxCopy);

        List<AffineTransform> tList = new ArrayList<>();
        try {
            tList.add(t3.createInverse());
            tList.add(t2.createInverse());
            tList.add(t1_5.createInverse());
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }

        return tList;
    }

    /**
     * just reverse the order and invert each transform
     * @param origin
     * @param px
     * @return
     */
    public static List<AffineTransform> getTransformsToLocalCoords(Point2D origin, Point2D px) {
        List<AffineTransform> initTransforms = getTransformsToGlobalCoords(origin, px);
        return reverseTransforms(initTransforms);
    }

    public static List<AffineTransform> getTransformsToLocalCoords(Point2D origin, Point2D px, Point2D py) {
        List<AffineTransform> initTransforms = getTransformsToGlobalCoords(origin, px, py);
        return reverseTransforms(initTransforms);
    }

    public static List<AffineTransform> reverseTransforms(List<AffineTransform> transforms) {
        List<AffineTransform> finalTransforms = new ArrayList<>(transforms.size());
        for(AffineTransform t: transforms) {
            try {
                finalTransforms.add(t.createInverse());
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
        }
        Collections.reverse(finalTransforms);
        return finalTransforms;
    }

    /**
     * Given 3 points in global map coordinate, defining an origin and 2 unit vectors,
     * returns a list of transforms that can be applied to convert local coordinates (in the space
     * defined by those 3 points) to global coordinates (same system the 3 points are provided with).

     * @return list of AffineTransform that, if applied in order on a local coordinates, yield the global coordinates.
     */
    public static List<AffineTransform> getTransformsToGlobalCoords(Point2D origin, Point2D px, Point2D py) {

        // copy points to avoid altering them
        Point2D originCopy = new Point2D.Double(origin.getX(), origin.getY());
        Point2D pxCopy = new Point2D.Double(px.getX(), px.getY());
        Point2D pyCopy = new Point2D.Double(py.getX(), py.getY());

        // set new coordinate system origin to center of start element
        AffineTransform t1 = new AffineTransform();
        t1.translate(-originCopy.getX(), -originCopy.getY());
        t1.transform(pxCopy, pxCopy);
        t1.transform(pyCopy, pyCopy);

        //System.out.println("after set to origin: " + pxCopy+" "+pyCopy);

        // get angle of destination element from X axis
        // see https://stackoverflow.com/a/2150475
        double angle = angle(new Point2D.Float(1,0), pxCopy);
        //System.out.println("angle to X axis: " + pxCopy + " " + angle);

        // rotate system to align destination on X axis
        AffineTransform t2 = new AffineTransform();
        t2.rotate(-angle);
        t2.transform(pxCopy, pxCopy);
        t2.transform(pyCopy, pyCopy);

        //System.out.println("after align destination on X:  "+ pxCopy+" "+pyCopy);

        double shearFactor = pyCopy.getX() / pyCopy.getY();

        // shear to put the Y axis perpendicular
        AffineTransform t3 = new AffineTransform();
        t3.shear(-shearFactor, 0);
        t3.transform(pxCopy, pxCopy);
        t3.transform(pyCopy, pyCopy);

        //System.out.println("after shear X axis to align Y: " + pxCopy+" "+pyCopy);

        // scale to have both axis at 1
        AffineTransform t4 = new AffineTransform();
        t4.scale(1 / pxCopy.getX(), 1 / pyCopy.getY());
        t4.transform(pxCopy, pxCopy);
        t4.transform(pyCopy, pyCopy);

        //System.out.println("after scale destination to 1 on X: " + pxCopy+" "+pyCopy);


        List<AffineTransform> tList = new ArrayList<>();
        try {
            tList.add(t4.createInverse());
            tList.add(t3.createInverse());
            tList.add(t2.createInverse());
            tList.add(t1.createInverse());
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }

        return tList;
    }

    /**
     * return the signed angle in radian between 2 vectors at origin
     * @param v1
     * @param v2
     * @return signed angle in radian
     */
    public static double angle(Point2D v1, Point2D v2) {
        return Math.atan2(
                v1.getX() * v2.getY()
                        - v1.getY() * v2.getX(),
                v1.getX() * v2.getX()
                        + v1.getY() * v2.getY() );
        //return Math.atan2(v2.getY(), v2.getX()) - Math.atan2(v1.getY(), v1.getX());
    }

    /**
     * return angle from origin
     * @param p a vector at origin
     * @return signed angle in radian
     */
    public static double angle(Point2D p) {
        return angle(p, new Point2D.Float(1,0));
    }

    /**
     *
     * @return angle in degree starting from positive X axis (= East)
     */
    public static float perimeterAnchorPointToAngle(AnchorPoint anchorPoint) {
        switch(anchorPoint){
            case N: return 90; //0;
            case NNE: return 67.5f; //22.5f;
            case NE: return 45;
            case ENE: return 22.5f; //67.5f;
            case E: return 0; //90;
            case ESE: return -22.5f; //337.5f; //112.5f;
            case SE: return -45; //315; //135;
            case SSE: return -67.5f; //292.5f; //157.5f;
            case S: return -90; //270; //180;
            case SSW: return -112.5f; //247.5f; //202.5f;
            case SW: return -135; //225;
            case WSW: return -157.5f; //202.5f; //247.5f;
            case W: return 180; //270;
            case WNW: return 157.5f; //292.5f;
            case NW: return 135; //315;
            case NNW: return 112.5f; //337.5f;
            case CENTER: throw new RuntimeException("Cannot infer angle from link starting at center");
        }
        throw new RuntimeException("Unexpected error, should not be able to reach this point.");
    }

    public static Point2D.Float getRelativeRectangleAnchorPosition(AnchorPoint anchorPoint, float width, float height){
        Point.Float pl = new Point.Float();
        switch(anchorPoint) {
            case E:
                pl.x = 0.5f * width;
                pl.y = 0;
                break;
            case ENE:
                pl.x = 0.5f * width;
                pl.y = 0.25f * height;
                break;
            case NE:
                pl.x = 0.5f * width;
                pl.y = 0.5f * height;
                break;
            case ESE:
                pl.x = 0.5f * width;
                pl.y = -0.25f * height;
                break;
            case SE:
                pl.x = 0.5f * width;
                pl.y = -0.5f * height;
                break;
            case W:
                pl.x = -0.5f * width;
                pl.y = 0;
                break;
            case WNW:
                pl.x = -0.5f * width;
                pl.y = 0.25f * height;
                break;
            case NW:
                pl.x = -0.5f * width;
                pl.y = 0.5f * height;
                break;
            case WSW:
                pl.x = -0.5f * width;
                pl.y = -0.25f * height;
                break;
            case SW:
                pl.x = -0.5f * width;
                pl.y = -0.5f * height;
                break;
            case N:
                pl.x = 0;
                pl.y = 0.5f * height;
                break;
            case NNW:
                pl.x = -0.25f * width;
                pl.y = 0.5f * height;
                break;
            case NNE:
                pl.x = 0.25f * width;
                pl.y = 0.5f * height;
                break;
            case S:
                pl.x = 0;
                pl.y = -0.5f * height;
                break;
            case SSW:
                pl.x = -0.25f * width;
                pl.y = -0.5f * height;
                break;
            case SSE:
                pl.x = 0.25f * width;
                pl.y = -0.5f * height;
                break;
        }
        // all that is given in a coordinate system where Y points up.
        // but it always points down for us.
        return new Point2D.Float((float)pl.getX(), (float)-pl.getY());
    }

    /**
     * angle of the right and left "arrows" of the phenotype are fixed (45°)
     * the diagonals on the left and right are the diagonals of 4 squares of side h/2
     * @param width
     * @param height
     * @return
     */
    public static Point2D.Float getRelativePhenotypeAnchorPosition(AnchorPoint anchorPoint, float width, float height){

        float halfW = 0.5f * width;
        float halfH = 0.5f * height;
        float quartH = 0.25f * height;

        Point.Float pl = new Point.Float();
        switch(anchorPoint) {
            case E:
                pl.x = halfW;
                pl.y = 0;
                break;
            case ENE:
                pl.x = halfW - quartH;
                pl.y = quartH;
                break;
            case NE:
                pl.x = halfW - halfH;
                pl.y = halfH;
                break;
            case ESE:
                pl.x = halfW - quartH;
                pl.y = -quartH;
                break;
            case SE:
                pl.x = halfW - halfH;
                pl.y = -halfH;
                break;
            case W:
                pl.x = -halfW;
                pl.y = 0;
                break;
            case WNW:
                pl.x = quartH - halfW;
                pl.y = quartH;
                break;
            case NW:
                pl.x = halfH - halfW;
                pl.y = halfH;
                break;
            case WSW:
                pl.x = quartH - halfW;
                pl.y = -quartH;
                break;
            case SW:
                pl.x = halfH - halfW;
                pl.y = -halfH;
                break;
            case N:
                pl.x = 0;
                pl.y = halfH;
                break;
            case NNW:
                pl.x = 0.5f * (halfH - halfW);
                pl.y = halfH;
                break;
            case NNE:
                pl.x = 0.5f * (halfW - halfH);
                pl.y = halfH;
                break;
            case S:
                pl.x = 0;
                pl.y = -halfH;
                break;
            case SSW:
                pl.x = 0.5f * (halfH - halfW);
                pl.y = -halfH;
                break;
            case SSE:
                pl.x = 0.5f * (halfW - halfH);
                pl.y = -halfH;
                break;
        }
        // all that is given in a coordinate system where Y points up.
        // but it always points down for us.
        return new Point2D.Float((float)pl.getX(), (float)-pl.getY());
    }

    /**
     * right and left side also have 45° slope
     * @param anchorPoint
     * @param width
     * @param height
     * @return
     */
    public static Point2D.Float getRelativeRightParallelogramAnchorPosition(AnchorPoint anchorPoint, float width, float height){

        float halfW = 0.5f * width;
        float halfH = 0.5f * height;
        float quartH = 0.25f * height;

        Point.Float pl = new Point.Float();
        switch(anchorPoint) {
            case E:
                pl.x = halfW - halfH;
                pl.y = 0;
                break;
            case ENE:
                pl.x = halfW - quartH;
                pl.y = quartH;
                break;
            case NE:
                pl.x = halfW;
                pl.y = halfH;
                break;
            case ESE:
                pl.x = halfW - height + quartH;
                pl.y = -quartH;
                break;
            case SE:
                pl.x = halfW - height;
                pl.y = -halfH;
                break;
            case W:
                pl.x = halfH - halfW;
                pl.y = 0;
                break;
            case WNW:
                pl.x = height - halfW - quartH;
                pl.y = quartH;
                break;
            case NW:
                pl.x = height - halfW;
                pl.y = halfH;
                break;
            case WSW:
                pl.x = quartH - halfW;
                pl.y = -quartH;
                break;
            case SW:
                pl.x = -halfW;
                pl.y = -halfH;
                break;
            case N:
                pl.x = -halfW + height + 0.5f * (width - height);
                pl.y = halfH;
                break;
            case NNW:
                pl.x = -halfW + height + 0.25f * (width - height);
                pl.y = halfH;
                break;
            case NNE:
                pl.x = halfW - 0.25f * (width - height);
                pl.y = halfH;
                break;
            case S:
                pl.x = halfW - height - 0.5f * (width - height);
                pl.y = -halfH;
                break;
            case SSW:
                pl.x = -halfW + 0.25f * (width - height);
                pl.y = -halfH;
                break;
            case SSE:
                pl.x = halfW - height - 0.25f * (width - height);
                pl.y = -halfH;
                break;
        }
        // all that is given in a coordinate system where Y points up.
        // but it always points down for us.
        return new Point2D.Float((float)pl.getX(), (float)-pl.getY());
    }

    public static Point2D.Float getRelativeLeftParallelogramAnchorPosition(AnchorPoint anchorPoint, float width, float height){

        float halfW = 0.5f * width;
        float halfH = 0.5f * height;
        float quartH = 0.25f * height;

        Point.Float pl = new Point.Float();
        switch(anchorPoint) {
            case E:
                pl.x = halfW - halfH;
                pl.y = 0;
                break;
            case ENE:
                pl.x = halfW - height + quartH;
                pl.y = quartH;
                break;
            case NE:
                pl.x = halfW - height;
                pl.y = halfH;
                break;
            case ESE:
                pl.x = halfW - quartH;
                pl.y = -quartH;
                break;
            case SE:
                pl.x = halfW;
                pl.y = -halfH;
                break;
            case W:
                pl.x = halfH - halfW;
                pl.y = 0;
                break;
            case WNW:
                pl.x = -halfW + quartH;
                pl.y = quartH;
                break;
            case NW:
                pl.x = -halfW;
                pl.y = halfH;
                break;
            case WSW:
                pl.x = height - halfW - quartH;
                pl.y = -quartH;
                break;
            case SW:
                pl.x = -halfW + height;
                pl.y = -halfH;
                break;
            case N:
                pl.x = -halfW + 0.5f * (width - height);
                pl.y = halfH;
                break;
            case NNW:
                pl.x = 0.25f * (-width - height);
                pl.y = halfH;
                break;
            case NNE:
                pl.x = -halfW + 0.75f * (width - height);
                pl.y = halfH;
                break;
            case S:
                pl.x = halfW - 0.5f * (width - height);
                pl.y = -halfH;
                break;
            case SSW:
                pl.x = halfW - 0.75f * (width - height);
                pl.y = -halfH;
                break;
            case SSE:
                pl.x = 0.25f * (width + height);
                pl.y = -halfH;
                break;
        }
        // all that is given in a coordinate system where Y points up.
        // but it always points down for us.
        return new Point2D.Float((float)pl.getX(), (float)-pl.getY());
    }

    public static Point2D.Float getRelativeReceptorAnchorPosition(AnchorPoint anchorPoint, float width, float height){

        float halfW = 0.5f * width;
        float quartW = 0.25f * width;
        float halfH = 0.5f * height;
        float fifthH = 0.2f * height;
        float tenthH = 0.1f * height;

        Point.Float pl = new Point.Float();
        switch(anchorPoint) {
            case E:
                pl.x = halfW;
                pl.y = tenthH;
                break;
            case ENE:
                pl.x = halfW;
                pl.y = halfH - fifthH;
                break;
            case NE:
                pl.x = halfW;
                pl.y = halfH;
                break;
            case ESE:
                pl.x = halfW;
                pl.y = -tenthH;
                break;
            case SE:
                pl.x = halfW;
                pl.y = -halfH + fifthH;
                break;
            case W:
                pl.x = -halfW;
                pl.y = tenthH;
                break;
            case WNW:
                pl.x = -halfW;
                pl.y = halfH - fifthH;
                break;
            case NW:
                pl.x = -halfW;
                pl.y = halfH;
                break;
            case WSW:
                pl.x = -halfW;
                pl.y = -tenthH;
                break;
            case SW:
                pl.x = -halfW;
                pl.y = -halfH + fifthH;
                break;
            case N:
                pl.x = 0;
                pl.y = halfH - fifthH;
                break;
            case NNW:
                pl.x = -quartW;
                pl.y = halfH - tenthH;
                break;
            case NNE:
                pl.x = quartW;
                pl.y = halfH - tenthH;
                break;
            case S:
                pl.x = 0;
                pl.y = -halfH;
                break;
            case SSW:
                pl.x = -quartW;
                pl.y = -halfH + tenthH;
                break;
            case SSE:
                pl.x = quartW;
                pl.y = -halfH + tenthH;
                break;
        }
        // all that is given in a coordinate system where Y points up.
        // but it always points down for us.
        return new Point2D.Float((float)pl.getX(), (float)-pl.getY());
    }

    /**
     * Assuming the ellipse is aligned to axis and center of ellipse is 0,0
     * @param bboxWidth
     * @param bboxHeight
     * @param deg
     * @return
     */
    public static Point2D.Float ellipsePerimeterPointFromAngle(float bboxWidth, float bboxHeight, float deg) {
        double theta = deg * Math.PI / 180;
        return new Point2D.Float(
                (float) ((bboxWidth / 2) * Math.cos(theta)),
                (float) (-(bboxHeight / 2) * Math.sin(theta)));
    }

    /**
     * return between 0 and 4 points
     * @param line
     * @param rect
     * @return
     */
    public static List<Point2D.Float> getLineRectangleIntersection(Line2D.Float line, Rectangle2D.Float rect) {
        Point2D.Float p1 = new Point2D.Float(
                (float) (rect.getX() - rect.getWidth() / 2),
                (float) (rect.getY() - rect.getHeight() / 2));
        Point2D.Float p2 = new Point2D.Float(
                (float) (rect.getX() + rect.getWidth() / 2),
                (float) (rect.getY() - rect.getHeight() / 2));
        Point2D.Float p3 = new Point2D.Float(
                (float) (rect.getX() + rect.getWidth() / 2),
                (float) (rect.getY() + rect.getHeight() / 2));
        Point2D.Float p4 = new Point2D.Float(
                (float) (rect.getX() - rect.getWidth() / 2),
                (float) (rect.getY() + rect.getHeight() / 2));
        Line2D.Float l1 = new Line2D.Float(p1, p2);
        Line2D.Float l2 = new Line2D.Float(p2, p3);
        Line2D.Float l3 = new Line2D.Float(p3, p4);
        Line2D.Float l4 = new Line2D.Float(p4, p1);
        logger.trace(rect+" -- "+line);
        Point2D.Float i1 = getLineLineIntersection(line, l1);
        Point2D.Float i2 = getLineLineIntersection(line, l2);
        Point2D.Float i3 = getLineLineIntersection(line, l3);
        Point2D.Float i4 = getLineLineIntersection(line, l4);

        List<Point2D.Float> result = new ArrayList<>();
        // if parallele lines, we get Nan and infinity
        if(i1 != null && isDefinedAndFinite(i1.getX()) && isDefinedAndFinite(i1.getY())) {
            result.add(i1);
        }
        if(i2 != null && isDefinedAndFinite(i2.getX()) && isDefinedAndFinite(i2.getY())) {
            result.add(i2);
        }
        if(i3 != null && isDefinedAndFinite(i3.getX()) && isDefinedAndFinite(i3.getY())) {
            result.add(i3);
        }
        if(i4 != null && isDefinedAndFinite(i4.getX()) && isDefinedAndFinite(i4.getY())) {
            result.add(i4);
        }

        return result;
    }

    public static boolean isDefinedAndFinite(double n) {
        return Double.isFinite(n) && !Double.isNaN(n);
    }

    /**
     * https://stackoverflow.com/a/5185725
     * return intersection even if out of segment. It extends segments.
     * @param line1
     * @param line2
     * @return
     */
    public static Point2D.Float getLineLineIntersection(Line2D.Float line1, Line2D.Float line2) {

        final double x1,y1, x2,y2, x3,y3, x4,y4;
        x1 = line1.x1; y1 = line1.y1; x2 = line1.x2; y2 = line1.y2;
        x3 = line2.x1; y3 = line2.y1; x4 = line2.x2; y4 = line2.y2;
        final double x = (
                (x2 - x1)*(x3*y4 - x4*y3) - (x4 - x3)*(x1*y2 - x2*y1)
        ) /
                (
                        (x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4)
                );
        final double y = (
                (y3 - y4)*(x1*y2 - x2*y1) - (y1 - y2)*(x3*y4 - x4*y3)
        ) /
                (
                        (x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4)
                );


        if(line1.intersectsLine(line2)) {
            return new Point2D.Float((float)x, (float)y);
        }

        return null;
    }

    /**
     * return the point from the list that is the closest to Point p
     * @param pointList
     * @param ref
     * @return
     */
    public static Point2D.Float getClosest(List<Point2D.Float> pointList, Point2D.Float ref) {
        if(pointList.size() == 0) {
            throw new IllegalArgumentException("Point list should not be empty.");
        }

        // init with first element
        double minDist = pointList.get(0).distance(ref);
        Point2D.Float closest = pointList.get(0);

        for(int i=1; i < pointList.size(); i++) {
            if(pointList.get(i).distance(ref) < minDist) {
                closest = pointList.get(i);
            }
        }

        return closest;
    }

    /**
     * Apply a list of affine transforms to a list of points.
     * Used to change the coordinate system of a set of points.
     * @param points a list of 2D coordinates
     * @param transforms a list of transforms to be applied
     * @return a new list of points
     */
    public static List<Point2D.Float> convertPoints(List<Point2D.Float> points, List<AffineTransform> transforms) {

        List<Point2D.Float> convertedPoints = new ArrayList<>();
        for (Point2D editP : points) {
            Point2D p = new Point2D.Double(editP.getX(), editP.getY());

            for(AffineTransform t: transforms) {
                t.transform(p, p);
            }

            convertedPoints.add(new Point2D.Float((float) p.getX(), (float) p.getY()));

        }
        return convertedPoints;
    }

    public static Point2D.Float getMiddle(Point2D.Float p1, Point2D.Float p2) {
        return new Point2D.Float(
                p1.x + ((p2.x - p1.x) / 2),
                p1.y + ((p2.y - p1.y) / 2));
    }

    /**
     * Given a polyline defined by a list of points, and a segment index (starting at 0 for the first segment of the
     * polyline), returns the middle of the specified segment.
     * Polyline has to be valid, point list has to have at least 2 points.
     * @param points
     * @param segment
     * @return
     */
    public static Point2D.Float getMiddleOfPolylineSegment(List<Point2D.Float> points, int segment) {
        if(points.size() < 2) {
            throw new IllegalArgumentException("Polyline needs to have at least 2 points, "+ points.size()+" points provided.");
        }
        if(segment < 0 || segment > points.size() - 1) {
            throw new IllegalArgumentException("segment has to be between 0 and polyline segment count, "+segment+" was provided.");
        }

        Point2D.Float p1 = points.get(segment);
        Point2D.Float p2 = points.get(segment + 1);
        logger.trace("middle of "+p1+" "+p2+" -> "+getMiddle(p1, p2));
        return getMiddle(p1, p2);
    }

    public static SimpleEntry<List<Point2D.Float>, List<Point2D.Float>> splitPolylineAtSegment(List<Point2D.Float> points, int segment) {
        if(points.size() < 2) {
            throw new IllegalArgumentException("Polyline needs to have at least 2 points, "+ points.size()+" points provided.");
        }
        if(segment < 0 || segment > points.size() - 1) {
            throw new IllegalArgumentException("segment has to be between 0 and polyline segment count, "+segment+" was provided.");
        }

        List<Point2D.Float> subLinkPoints1 = new ArrayList<>();
        List<Point2D.Float> subLinkPoints2 = new ArrayList<>();
        List<Point2D.Float> currentSubLink = subLinkPoints1;

        for(int i=0; i < points.size() - 1; i++) {
            Point2D.Float currentStartPoint = points.get(i);
            Point2D.Float currenEndPoint = points.get(i + 1);

            currentSubLink.add(currentStartPoint);

            if(i == segment) { // split this segment in 2
                Point2D.Float middle = getMiddle(currentStartPoint, currenEndPoint);
                currentSubLink.add(middle);
                currentSubLink = subLinkPoints2;
                currentSubLink.add(middle);
            }

            if(i == points.size() - 2) {
                currentSubLink.add(currenEndPoint);
            }

        }

        return new SimpleEntry<>(subLinkPoints1, subLinkPoints2);
    }

    public static Point2D.Float normalizePoint(Point2D.Float p1,
                                               Point2D.Float p2,
                                               Glyph glyph,
                                               AnchorPoint anchorPoint) {
        // normalize if needed (shape wants it, or links points at center
        if(glyph.getCdShape() == CdShape.LEFT_PARALLELOGRAM
                || glyph.getCdShape() == CdShape.RIGHT_PARALLELOGRAM
                || glyph.getCdShape() == CdShape.RECEPTOR
                || anchorPoint == AnchorPoint.CENTER) {
            // normalize start
            Rectangle2D.Float rect = new Rectangle2D.Float(
                    (float) glyph.getCenter().getX(),
                    (float) glyph.getCenter().getY(),
                    glyph.getWidth(),
                    glyph.getHeight());
            Line2D.Float segment = new Line2D.Float(p1, p2);
            logger.trace("Intersect segement: "+segment.getP1()+" "+segment.getP2()+" with rectangle "+rect);
            List<Point2D.Float> intersections2 = GeometryUtils.getLineRectangleIntersection(segment, rect);
            if(intersections2.isEmpty()) {
                return p1;
            }
            else {
                Point2D.Float normalizedStart = GeometryUtils.getClosest(intersections2, p2);
                return normalizedStart;
            }
        }
        // else just take the point already defined
        else {
            return p1;
        }
    }

    /**
     * Assuming the point list is definitively set in absolute coordinates,
     * will translate the start and end points of the link, if they point to centers,
     * to the correct perimeter point of the shape.
     */
    public static List<Point2D.Float> getNormalizedEndPoints(List<Point2D.Float> points,
                                                             Glyph startGlyph,
                                                             Glyph endGlyph,
                                                             AnchorPoint startAnchor,
                                                             AnchorPoint endAnchor) {
        logger.trace("NORMALIZE points: " + points);
        Point2D.Float cdSpaceStart = points.get(0);
        Point2D.Float cdSpaceEnd = points.get(points.size() - 1);

        List<Point2D.Float> result = new ArrayList<>();

        Point2D.Float normalized1 = normalizePoint(cdSpaceStart, points.get(1), startGlyph, startAnchor);
        result.add(normalized1);

        for(int i=1; i < points.size() - 1; i++) {
            result.add(points.get(i));
        }

        Point2D.Float normalized2 = normalizePoint(
                cdSpaceEnd, points.get(points.size() - 2), endGlyph, endAnchor);
        result.add(normalized2);

        logger.trace("NORMALIZE RESULT: " + result);

        return result;
    }

    /**
     * see https://stackoverflow.com/a/9343170
     * return a point on the line defined by p1 and p2 that is ratio% away from p1
     */
    public static Point2D.Float interpolationByRatio(Point2D.Float p1, Point2D.Float p2 , float ratio) {
        // corner case when the 2 points are the same
        if(p1.distance(p2) == 0) {
            logger.warn("Interpolation by ratio for a 0-length segment (2 same points given): "+p1+" "+p2);
            return new Point2D.Float((float)p1.getX(), (float)p1.getY());
        }
        float len = (float) p1.distance(p2);
        //double ratio = d/len;
        float x = ratio*p2.x + (1.0f - ratio)*p1.x;
        float y = ratio*p2.y + (1.0f - ratio)*p1.y;
        return new Point2D.Float(x, y);
    }

    /**
     * see https://stackoverflow.com/a/9343170
     * return a point on the line defined by p1 and p2 that as away from p1 by d units
     */
    public static Point2D.Float interpolationByDistance(Point2D.Float p1, Point2D.Float p2 , float d) {
        // corner case when the 2 points are the same
        if(p1.distance(p2) == 0) {
            logger.warn("Interpolation by distance for a 0-length segment (2 same points given): "+p1+" "+p2);
            return new Point2D.Float((float)p1.getX(), (float)p1.getY());
        }

        float len = (float) p1.distance(p2);
        float ratio = d/len;
        float x = ratio*p2.x + (1.0f - ratio)*p1.x;
        float y = ratio*p2.y + (1.0f - ratio)*p1.y;
        return new Point2D.Float(x, y);
    }

    /**
     * In some case, a CD compartment only has coordinates, no width or height. We need to create an appropriate
     * bounding box for it, depending of its class.
     * @param cdClass
     * @param x
     * @param y
     * @param thickness
     * @param mapW
     * @param mapH
     * @return
     */
    public static Rectangle2D.Float getCompartmentBbox(String cdClass, float x, float y,
                                                 float thickness, float mapW, float mapH){
        float resX, resY, resW, resH;
        switch(cdClass) {
            case "SQUARE_CLOSEUP_NORTHWEST":
                resX = x;
                resY = y;
                resW = mapW - x;
                resH = mapH - y;
                break;
            case "SQUARE_CLOSEUP_NORTHEAST":
                resX = 0;
                resY = y;
                resW = x;
                resH = mapH - y;
                break;
            case "SQUARE_CLOSEUP_SOUTHWEST":
                resX = x;
                resY = 0;
                resW = mapW - x;
                resH = y;
                break;
            case "SQUARE_CLOSEUP_SOUTHEAST":
                resX = 0;
                resY = 0;
                resW = x;
                resH = y;
                break;
            case "SQUARE_CLOSEUP_NORTH":
                resX = 0;
                resY = y;
                resW = mapW;
                resH = thickness;
                break;
            case "SQUARE_CLOSEUP_EAST":
                resX = x - thickness;
                resY = 0;
                resW = thickness;
                resH = mapH;
                break;
            case "SQUARE_CLOSEUP_WEST":
                resX = x;
                resY = 0;
                resW = thickness;
                resH = mapH;
                break;
            case "SQUARE_CLOSEUP_SOUTH":
                resX = 0;
                resY = y - thickness;
                resW = mapW;
                resH = thickness;
                break;
            default:
                throw new IllegalArgumentException("Compartment bbox can be inferred only for the " +
                        "special CLOSEUP classes. Invalid class provided: "+cdClass);
        }

        return new Rectangle2D.Float(resX, resY, resW, resH);
    }

    /**
     * Return an estimation of a pixel width taken by a string s
     *
     * usdfs dfsdf sdfsd f -> fill a width of 80 in CellDesigner -> 5 / letter
     * with a minimum margin of 5.
     *
     * @param s
     * @return
     */
    public static float getLengthForString(String s) {
        if(s.trim().isEmpty()) {
            return 0;
        }
        return s.length() * 5 + 5;
    }

    /**
     *
     * @param parentBbox
     * @param s
     * @param angle
     * @return
     */
    public static Rectangle2D.Float getAuxUnitBboxFromAngle(Rectangle2D.Float parentBbox, String s, float angle) {

        Point2D.Float unitCenter = getPositionFromAngle(parentBbox, angle);
        return getAuxUnitBboxFromPoint(parentBbox, s, unitCenter);
    }

    public static Rectangle2D.Float getAuxUnitBboxFromRelativeTopRatio(Rectangle2D.Float parentBbox, String s, float ratio) {
        Point2D.Float unitCenter = getTopPositionFromRatio(parentBbox, ratio);
        return getAuxUnitBboxFromPoint(parentBbox, s, unitCenter);

    }

    public static Rectangle2D.Float getAuxUnitBboxFromPoint(Rectangle2D.Float parentBbox, String s, Point2D unitCenter) {
        float unitWidth = getLengthForString(s);
        float unitHeight = 10;
        // limit size of infobox to parent glyph width
        unitWidth = unitWidth > parentBbox.width ? parentBbox.width : unitWidth;
        // limit minimum size, in case of empty string
        unitWidth = unitWidth < unitHeight ? unitHeight : unitWidth;
        Rectangle2D.Float res = new Rectangle2D.Float(
                (float) (unitCenter.getX() + parentBbox.getX() + parentBbox.getWidth() / 2 - unitWidth / 2),
                (float) (unitCenter.getY() + parentBbox.getY() + parentBbox.getHeight() / 2 - unitHeight / 2),
                unitWidth,
                unitHeight);

        return res;
    }

    /**
     * Relative position of center of auxiliary unit from center of its parent glyph.
     * @return
     */
    public static Point2D.Float getRelativePositionOfAuxUnit(Rectangle2D parent, Rectangle2D auxUnit) {
        Point2D.Float parentMiddle = new Point2D.Float((float)parent.getCenterX(), (float)parent.getCenterY());
        Point2D.Float unitMiddle = new Point2D.Float(
                (float) (auxUnit.getCenterX() - parentMiddle.getX()),
                (float) (auxUnit.getCenterY() - parentMiddle.getY()));
        return unitMiddle;
    }

    /**
     *
     * @param parent
     * @param auxUnit
     * @return position of aux unit, in percentage of the parent glyph width from the left
     */
    public static double getTopRatioOfAuxUnit(Rectangle2D parent, Rectangle2D auxUnit) {
        Point2D.Float unitRelativeCenter = getRelativePositionOfAuxUnit(parent, auxUnit);
        return unitRelativeCenter.getX()  / parent.getWidth() + 0.5;
    }

    /**
     *
     * @param angle in radian, signed or unsigned
     * @return signed angle in radian between -PI and PI
     */
    public static double normalizeAngle(double angle) {
        double twoPI = Math.PI*2;
        double theta = angle;

        while (theta <= -Math.PI) {
            theta += twoPI;
        }

        while (theta > Math.PI) {
            theta -= twoPI;
        }
        return theta;
    }


    /**
     * taken from: https://stackoverflow.com/a/31886696
     *
     * Angle is signed in degree, ranges from -180 to 180 (can go further but is corrected at the beginning
     * of the function), 0 at east of x axis,
     * Coordinate system has Y axis pointing down.
     * Result is given as relative coordinates starting on top left corner of the rectangle.
     *
     * !! The result does not correspond to how celldesigner places things around a shape. !!
     *
     * @param rect
     * @param deg
     * @return
     */
    public static Point2D.Float rectanglePerimeterPointFromAngle(Rectangle2D.Float rect, double deg) {
        double twoPI = Math.PI*2;
        double theta = deg * Math.PI / 180;

        while (theta < -Math.PI) {
            theta += twoPI;
        }

        while (theta > Math.PI) {
            theta -= twoPI;
        }

        double rectAtan = Math.atan2(rect.height, rect.width);
        double tanTheta = Math.tan(theta);
        int region;

        if ((theta > -rectAtan) && (theta <= rectAtan)) {
            region = 1;
        } else if ((theta > rectAtan) && (theta <= (Math.PI - rectAtan))) {
            region = 2;
        } else if ((theta > (Math.PI - rectAtan)) || (theta <= -(Math.PI - rectAtan))) {
            region = 3;
        } else {
            region = 4;
        }

        Point2D.Float edgePoint = new Point2D.Float(rect.width/2, rect.height/2);
        int xFactor = 1;
        int yFactor = 1;

        switch (region) {
            case 1: yFactor = -1; break;
            case 2: yFactor = -1; break;
            case 3: xFactor = -1; break;
            case 4: xFactor = -1; break;
        }

        if ((region == 1) || (region == 3)) {
            edgePoint.x += xFactor * (rect.width / 2.);                                     // "Z0"
            edgePoint.y += yFactor * (rect.width / 2.) * tanTheta;
        } else {
            edgePoint.x += xFactor * (rect.height / (2. * tanTheta));                        // "Z1"
            edgePoint.y += yFactor * (rect.height / 2.);
        }

        return edgePoint;
    }

    /**
     *
     * result returned in relative coordinates from rectangle center
     * @param rect
     * @param deg signed angle [-180, 180]
     * @return
     */
    public static Point2D.Float getPositionFromAngle(Rectangle2D.Float rect, float deg) {

        // first get the position of the point on a square
        float fictionalSquareSize = 10;
        Point2D.Float localCoordFromTopLeft = rectanglePerimeterPointFromAngle(
                new Rectangle2D.Float(0, 0, fictionalSquareSize, fictionalSquareSize), deg);

        // convert result (relative to top left corner) to relative to center
        Point2D.Float localCoordFromCenter = new Point2D.Float(
                (float) (localCoordFromTopLeft.getX() - fictionalSquareSize/2 ),
                (float) (localCoordFromTopLeft.getY() - fictionalSquareSize/2 ));

        // get ratios of position / length
        double xRatio = localCoordFromCenter.getX() / fictionalSquareSize;
        double yRatio = localCoordFromCenter.getY() / fictionalSquareSize;

        // convert to relative coordinates from input rectangle's center
        double resultX = rect.width * xRatio;
        double resultY = rect.height * yRatio;


        return new Point2D.Float((float) resultX,(float) resultY);
    }

    /**
     *
     * @param parent
     * @param auxUnit
     * @return unsigned angle in radian
     */
    public static double getAngleOfAuxUnit(Rectangle2D parent, Rectangle2D auxUnit) {
        Point2D.Float unitRelativeCenter = getRelativePositionOfAuxUnit(parent, auxUnit);

        double xratio = unitRelativeCenter.getX() / parent.getWidth();
        double yratio = unitRelativeCenter.getY() / parent.getHeight();

        float fictionalSquareSize = 10;
        Point2D.Float unitOnSquare = new Point2D.Float(
                (float) (xratio * fictionalSquareSize),
                (float) (yratio * fictionalSquareSize));

        double signedAngle = angle(unitOnSquare);
        if(signedAngle < 0) {
            signedAngle += Math.PI * 2;
        }
        return signedAngle;
    }

    /**
     * For genes modification residues, CD uses a % of the glyph width as location. They are always placed on top.
     * @param rect
     * @param ratio between 0 and 1, from the left side
     * @return point in relative coordinate from rect center
     */
    public static Point2D.Float getTopPositionFromRatio(Rectangle2D.Float rect, float ratio) {
        // convert to relative coordinates from input rectangle's center
        float resultX = rect.width * ratio - rect.width / 2;
        float resultY = -rect.height / 2;
        return new Point2D.Float(resultX, resultY);
    }

    public static float unsignedRadianToSignedDegree(float radian) {
        float unsignedDegree = (float) (radian / Math.PI * 180);
        if(unsignedDegree > 180) {
            unsignedDegree -= 360;
        }
        return unsignedDegree;
    }

    public static float unsignedRadianToUnsignedDegree(float radian) {
        return (float) (radian / Math.PI * 180);
    }

    /**
     * @param p1
     * @param p2
     * @return the slope of the line going through p1 and p2
     */
    public static float lineSlope(Point2D p1, Point2D p2) {
        return (float) ((p1.getY() - p2.getY()) / (p1.getX() - p2.getX()));
    }

    /**
     * Get relative coordinate of an anchor point from center of the shape
     * @param anchorPoint
     * @return
     */
    public static Point2D.Float getRelativeAnchorCoordinate(CdShape shape, float width, float height, AnchorPoint anchorPoint) {

        Point2D.Float relativeAnchorPoint;
        if(anchorPoint != AnchorPoint.CENTER) {
            float angle = GeometryUtils.perimeterAnchorPointToAngle(anchorPoint);
            // ellipse shapes
            switch(shape) {
                case ELLIPSE:
                case CIRCLE:
                    relativeAnchorPoint = GeometryUtils.ellipsePerimeterPointFromAngle(
                            width, height, angle);
                    break;
                case PHENOTYPE:
                    relativeAnchorPoint = GeometryUtils.getRelativePhenotypeAnchorPosition(
                            anchorPoint, width, height);
                    break;
                case LEFT_PARALLELOGRAM:
                    relativeAnchorPoint = GeometryUtils.getRelativeLeftParallelogramAnchorPosition(
                            anchorPoint, width, height);
                    break;
                case RIGHT_PARALLELOGRAM:
                    relativeAnchorPoint = GeometryUtils.getRelativeRightParallelogramAnchorPosition(
                            anchorPoint, width, height);
                    break;
                case RECEPTOR:
                    relativeAnchorPoint = GeometryUtils.getRelativeReceptorAnchorPosition(
                            anchorPoint, width, height);
                    break;
                case TRUNCATED:
                default: // RECTANGLE as default
                    relativeAnchorPoint = GeometryUtils.getRelativeRectangleAnchorPosition(
                            anchorPoint, width, height);
            }
        }
        else {
            relativeAnchorPoint = new Point2D.Float(0,0);
        }
        return relativeAnchorPoint;
    }

    public static Point2D.Float getAbsoluteAnchorPoint(CdShape shape, Rectangle2D.Float bbox, AnchorPoint anchorPoint) {
        Point2D.Float relativePoint = getRelativeAnchorCoordinate(
                shape,
                (float) bbox.getWidth(),
                (float) bbox.getHeight(),
                anchorPoint);
        return new Point2D.Float(
                (float) (relativePoint.getX() + bbox.getX() + bbox.getWidth() / 2),
                (float) (relativePoint.getY() + bbox.getY() + bbox.getHeight() / 2)
        );
    }

    /**
     * Approximate a point on/near a shape, to a CellDesigner anchor point.
     * @param p
     * @param bbox
     * @param shape
     * @return
     */
    public static AnchorPoint getNearestAnchorPoint(Point2D.Float p, Rectangle2D.Float bbox, CdShape shape) {
        Point2D.Float currentRelativeAnchor;
        AnchorPoint result = AnchorPoint.CENTER;
        Point2D.Float relativeP  = new Point2D.Float(
                (float) (p.getX() - bbox.getX() - bbox.getWidth() / 2),
                (float) (p.getY() - bbox.getY() - bbox.getHeight() / 2)
        );
        logger.trace("Nearest anchro point: "+p+" "+relativeP+" "+bbox+" "+shape);
        double minDist = Double.MAX_VALUE;
        for(AnchorPoint a:AnchorPoint.values()){
            currentRelativeAnchor = getRelativeAnchorCoordinate(shape,
                    (float) bbox.getWidth(), (float) bbox.getHeight(), a);
            double dist = relativeP.distance(currentRelativeAnchor);
            if(dist < minDist) {
                minDist = dist;
                result = a;
            }
        }
        return result;
    }
}
