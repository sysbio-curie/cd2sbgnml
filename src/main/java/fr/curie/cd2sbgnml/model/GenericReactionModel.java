package fr.curie.cd2sbgnml.model;

import fr.curie.cd2sbgnml.graphics.GeometryUtils;
import fr.curie.cd2sbgnml.LinkWrapper;
import fr.curie.cd2sbgnml.xmlcdwrappers.ModelWrapper;
import fr.curie.cd2sbgnml.xmlcdwrappers.ReactionWrapper;
import org.sbml.x2001.ns.celldesigner.CelldesignerModificationDocument;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;


public class GenericReactionModel {

    private ReactionWrapper reactionW;
    //private Process process;
    private List<ReactionNodeModel> reactionNodeModels;
    private List<ReactantModel> reactantModels;
    private List<LinkModel> linkModels;

    private List<LinkWrapper> baseLinks;

    public GenericReactionModel(ReactionWrapper reactionW) {
        this.reactionW = reactionW;
        this.reactionNodeModels = new ArrayList<>();
        this.reactantModels = new ArrayList<>();
        this.linkModels = new ArrayList<>();


        /*
        // process modifiers
        for(ReactantWrapper reactantW: this.getModifiers()) {
            // simple case, no logic gate
            System.out.println("modifier: "+reactantW.getAliasW().getId());
            int modifIndex = reactantW.getLink().getIndex();
            List<Point2D.Float> editPoints = this.getEditPointsForModifier(modifIndex);
            CelldesignerModificationDocument.CelldesignerModification modif = reaction.getAnnotation().
                    getCelldesignerListOfModification().getCelldesignerModificationArray(modifIndex);
            Point2D.Float processAnchorPoint = process.getAbsoluteAnchorCoords(ReactantWrapper.getProcessAnchorIndex(modif));

            System.out.println("edit points: "+editPoints);

            List<AffineTransform> transformList =
                    GeometryUtils.getTransformsToGlobalCoords(reactantW.getLinkStartingPoint(), processAnchorPoint);
            List<Point2D.Float> absoluteEditPoints = new ArrayList<>();
            absoluteEditPoints.add(reactantW.getLinkStartingPoint());
            absoluteEditPoints.addAll(convertPoints(editPoints, transformList));
            absoluteEditPoints.add(processAnchorPoint);

            String linkCdClass = reaction.getAnnotation().getCelldesignerListOfModification().
                    getCelldesignerModificationArray(modifIndex).getType();

            LinkWrapper link = new LinkWrapper(reactantW, process, absoluteEditPoints,
                    modifIndex, linkCdClass);
            link.setSbgnSpacePointList(
                    link.getNormalizedEndPoints(
                            reactantW.getAnchorPoint(), GeometryUtils.AnchorPoint.CENTER
                    ));

            reactantW.setLink(link);


        }
        */

    }

    /*
     //TODO remove, probably unused anymore
    public Point2D getProcessCoords (ModelWrapper modelW) {

        // TODO also take complexSpeciesAlias into account <- should be ok

        //System.out.println("reactant "+reactantBbox.getX()+" "+reactantBbox.getY()+" "+reactantBbox.getW()+" "+reactantBbox.getH());
        //System.out.println("product "+productBbox.getX()+" "+productBbox.getY()+" "+productBbox.getW()+" "+productBbox.getH());

        Point2D processCenter = null;
        if(!this.isBranchType()) {
            // TODO take anchor point into account
            // for now, only from center
            Point2D reactantStart = this.baseReactants.get(0).getLinkStartingPoint();
            Point2D productEnd = this.baseProducts.get(0).getLinkStartingPoint();

            LinkWrapper baseLink = this.baseLinks.get(0);

            if(baseLink.getPointList().size() == 0) {
                //System.out.println(reactantStart+" "+productEnd);

                processCenter = new Point2D.Double(
                        reactantStart.getX() + (productEnd.getX() - reactantStart.getX()) / 2,
                        reactantStart.getY() + (productEnd.getY() - reactantStart.getY()) / 2
                );
            }
            else {
                Point2D segmentStart;
                Point2D segmentEnd;
                int segmentCount = baseLink.getPointList().size() + 1;
                if(this.processSegmentIndex == 0) { // on first segment
                    segmentStart = reactantStart;
                    segmentEnd = baseLink.getPointList().get(0);
                }
                else if(this.processSegmentIndex == segmentCount - 1) { // on last segment
                    segmentStart = baseLink.getPointList().get(segmentCount - 2);
                    segmentEnd = productEnd;
                }
                else {
                    segmentStart = baseLink.getPointList().get(this.processSegmentIndex - 1);
                    segmentEnd = baseLink.getPointList().get(this.processSegmentIndex);
                }

                processCenter = new Point2D.Double(
                        segmentStart.getX() + (segmentEnd.getX() - segmentStart.getX()) / 2,
                        segmentStart.getY() + (segmentEnd.getY() - segmentStart.getY()) / 2
                );

            }
        }
        else {
            processCenter = new Point2D.Float(0,0);
        }

        return processCenter;

    }*/

    /**
     * CellDesigner uses a special coordinate system to define the edit points of a link.
     * The x axis goes along a direct line passing through the center of both involved elements.
     * Origin of the x axis is the center of the start element, 1 is the center of the end element.
     * The y axis is orthogonal, its origin is the same as x, and the 1 coordinate is at the same distance as x.
     * Y axis is oriented on the right of x, following the global coordinate system of the map.
     *
     * It means that points going beyond the center of elements can have coordinates > 1 or < 0.
     *
     */
    /*public Point2f localToAbsoluteCoord(Point2f cStart, Point2f cEnd, Point2f p) {
        return new Point2f();
    }*/


    /**
     * Given a 3-point coordinate system, gets the absolute coordinates of a relative point
     * @param origin
     * @param pX
     * @param pY
     * @param editPoint
     * @return
     */
    public static Point2D.Float getAbsolutePoint(Point2D.Float origin, Point2D.Float pX, Point2D.Float pY, Point2D.Float editPoint) {

        // transform association glyph point
        Point2D.Float absolutePoint = new Point2D.Float((float) editPoint.getX(), (float) editPoint.getY());
        for(AffineTransform t: GeometryUtils.getTransformsToGlobalCoords(origin, pX, pY)) {
            t.transform(absolutePoint, absolutePoint);
        }

        return absolutePoint;
    }


    /**
     * already comprises start and end
     * @param origin
     * @param pX
     * @param branch
     * @return
     */
    public List<Point2D.Float> getBranchPoints(Point2D.Float origin, Point2D.Float pX, int branch) {

        List<Point2D.Float> absoluteEditPoints = new ArrayList<>();
        absoluteEditPoints.add(origin);
        System.out.println("local system: "+origin+" "+pX);
        System.out.println("points for BRANCH "+branch+" "+ ReactionWrapper.getEditPointsForBranch(this.getReactionW().getReaction(), branch));

        for (Point2D editP : ReactionWrapper.getEditPointsForBranch(this.getReactionW().getReaction(), branch)) {
            Point2D p = new Point2D.Double(editP.getX(), editP.getY());

            for(AffineTransform t: GeometryUtils.getTransformsToGlobalCoords(origin, pX)) {
                t.transform(p, p);
            }

            System.out.println("BRANCH "+branch+" result: " + editP + " -> " + p.toString());
            absoluteEditPoints.add(new Point2D.Float((float) p.getX(), (float) p.getY()));

        }
        absoluteEditPoints.add(pX);
        System.out.println("BRANCH "+branch+" stack: "+absoluteEditPoints);

        return absoluteEditPoints;
    }

    public Process getProcess() {
        for(ReactionNodeModel node: this.getReactionNodeModels()) {
            if(node instanceof Process) {
                return (Process) node;
            }
        }
        throw new RuntimeException("Reaction has no process "+this.reactionW.getId());
    }

    public AssocDissoc getAssocDissoc() {
        for(ReactionNodeModel node: this.getReactionNodeModels()) {
            if(node instanceof AssocDissoc) {
                return (AssocDissoc) node;
            }
        }
        throw new RuntimeException("Reaction has no association or dissociation glyph "+this.reactionW.getId());
    }

    public boolean hasProcess() {
        return this.getReactionW().hasProcess();
    }

    public ReactionWrapper getReactionW() {
        return reactionW;
    }

    public List<ReactionNodeModel> getReactionNodeModels() {
        return reactionNodeModels;
    }

    public List<ReactantModel> getReactantModels() {
        return reactantModels;
    }

    public List<LinkModel> getLinkModels() {
        return linkModels;
    }
}