import javafx.geometry.Point2D;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by domino on 5. 5. 2015.
 */
public class PolygonConvexMaker {

    private static final int leftOrientation = +1, rightOrientation = -1, lineOrientation = 0;

    public static ArrayList<ArrayList<Point>> breakToConvexSubPolygons(ArrayList<Point> polygonPoints) {

        if(!isPolygon(polygonPoints)){
            JOptionPane.showMessageDialog(null, "Predane body netvori polygon", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }


        ArrayList<ArrayList<Point>> waitingPolygons = new ArrayList<>();
        ArrayList<ArrayList<Point>> convexPolygons = new ArrayList<>();

        waitingPolygons.add(polygonPoints);

        ArrayList<Integer> tempIndexMarkers;
        boolean tempClockWise;

        while (!waitingPolygons.isEmpty()){

            final ArrayList<Point> tempPolygon = waitingPolygons.remove(0);

            if(!isConvex(tempPolygon)){

                final int tempStartMarker, tempStopMarker;

                // 1. get all markers
                tempClockWise    = isClockWiseDirection(tempPolygon);
                tempIndexMarkers = findMarkers(tempPolygon, tempClockWise); //no markers in convex polygon

                // 2. if   markers list is not empty => choice first => marker1
                tempStartMarker  = tempIndexMarkers.remove(0); //can't be exception

                // 3. delete all markers which are not visible from the marker1
                tempIndexMarkers = deleteAllPointsWhichAreNotVisibleFromStartPoint(tempPolygon, tempStartMarker, tempIndexMarkers, tempClockWise);

                // 4. delete markers on the same edge with start marker
                int neighborhoodIndex = tempIndexMarkers.indexOf(new Integer(getIndex(tempStartMarker + 1, tempPolygon.size())));
                if(neighborhoodIndex != -1){tempIndexMarkers.remove(neighborhoodIndex);}

                neighborhoodIndex = tempIndexMarkers.indexOf(new Integer(getIndex(tempStartMarker - 1, tempPolygon.size())));
                if(neighborhoodIndex != -1){tempIndexMarkers.remove(neighborhoodIndex);}

                // 5. if   markers list is not empty => choice the most nearest => marker2
                //    else choice last visible point until 180° which is in visible area
                if(!tempIndexMarkers.isEmpty()){

                    tempIndexMarkers.sort((o1, o2) ->
                        Double.compare(
                                pointDistance(tempPolygon.get(o1), tempPolygon.get(tempStartMarker)),
                                pointDistance(tempPolygon.get(o2), tempPolygon.get(tempStartMarker))
                        )
                    );

                    tempStopMarker = tempIndexMarkers.get(0);

                } else {
//                    tempStopMarker = findStopMarker(tempPolygon, tempStartMarker, tempIndexMarkers, tempClockWise);
//                    tempStopMarker = findIndexMark2(tempPolygon, tempStartMarker, tempClockWise);


                    //vrat viditelne body
                    for (int i = 0; i < tempPolygon.size(); i++) {tempIndexMarkers.add(new Integer(i));}
                    tempIndexMarkers.remove(new Integer(tempStartMarker));
                    tempIndexMarkers = deleteAllPointsWhichAreNotVisibleFromStartPoint(tempPolygon, tempStartMarker, tempIndexMarkers, tempClockWise);

                    //odstran se spolecnou hranou
                    neighborhoodIndex = tempIndexMarkers.indexOf(new Integer(getIndex(tempStartMarker + 1, tempPolygon.size())));
                    if(neighborhoodIndex != -1){tempIndexMarkers.remove(neighborhoodIndex);}

                    neighborhoodIndex = tempIndexMarkers.indexOf(new Integer(getIndex(tempStartMarker - 1, tempPolygon.size())));
                    if(neighborhoodIndex != -1){tempIndexMarkers.remove(neighborhoodIndex);}

                    //serad je podle vzdalenosti
                    tempIndexMarkers.sort((o1, o2) ->
                                    Double.compare(
                                            pointDistance(tempPolygon.get(o1), tempPolygon.get(tempStartMarker)),
                                            pointDistance(tempPolygon.get(o2), tempPolygon.get(tempStartMarker))
                                    )
                    );

                    //vyber ten nejkratsi
                    tempStopMarker = tempIndexMarkers.get(0);
                }

                // 6. cut polygon by marker1 and marker2
                ArrayList<ArrayList<Point>> tt = cutPolygonToTwoSubsets(tempPolygon, tempStartMarker, tempStopMarker);

                if(tt.get(0).size() < 3 || tt.get(1).size() < 3){
                    System.out.println("mensi 2");
                    break;
                }

                waitingPolygons.addAll(tt);
            } else {
                convexPolygons.add(tempPolygon);
            }
        }


        return convexPolygons;

//        return null;
    }



    private static boolean isPolygon(ArrayList<Point> polygon) {

        for (int i = 0; i < polygon.size() - 1; i++) {

            if (collisionABLineWithAnyEdge(polygon, i, i + 1) || collisionPointWithNonBelongEdge(polygon, i)) {
                return false;
            }

        }

        if(collisionPointWithNonBelongEdge(polygon, polygon.size() - 1)){
            return false;
        }

        return true;
    }

    private static boolean isConvex(ArrayList<Point> polygon){

        for (int i = 0; i < polygon.size() - 1; i++) {

            if (!allPointInOneHalfPlane(i, i + 1, polygon)) {
                return false;
            }
        }
        return true;
    }

    private static ArrayList<Integer> deleteAllPointsWhichAreNotVisibleFromStartPoint(
            ArrayList<Point> polygon, int indexStartPoint, ArrayList<Integer> indexViewPoints, boolean clockWise) {

        int sign = (clockWise) ? -1 : +1;
        double viewAngle = getAngle(polygon.get(indexStartPoint),
                polygon.get(getIndex(indexStartPoint + 1 * sign, polygon.size())),
                polygon.get(getIndex(indexStartPoint - 1 * sign, polygon.size())));

        for (int i = 0; i < indexViewPoints.size(); i++) {

            //remove points which have cross from start point over other edges
            if(collisionABLineWithAnyEdge(polygon, indexStartPoint, indexViewPoints.get(i))){
                indexViewPoints.remove(i);
                i--;
                continue;
            }

            //remove points which are not in angle between (indexPoint - 1)(indexPoint)(indexPoint + 1)
            double testMarkerAngle = getAngle(polygon.get(indexStartPoint),
                    polygon.get(getIndex(indexStartPoint + 1 * sign, polygon.size())),
                    polygon.get(indexViewPoints.get(i)));

            if(viewAngle < testMarkerAngle) {
                indexViewPoints.remove(i);
                i--;
            }
        }

        return indexViewPoints;
    }












    private static boolean intersectionOfLineSegment(Point A, Point B, Point C, Point D, boolean closedInterval) {

        if(A.equals(B) && C.equals(D)){
            return A.equals(C);
        }

        if(A.equals(B) && !C.equals(D)){
            
            if(!closedInterval){return false;}
            
            try{
                return pointInsideLineSegment(A, C, D, closedInterval);
            } catch (Exception ex){
                System.out.println("Point C and D are identical and go to pass through this if statement.");
            }
        }

        if(!A.equals(B) && C.equals(D)){
            
            if(!closedInterval){return false;}
            
            try{
                return pointInsideLineSegment(C, A, B, closedInterval);
            } catch (Exception ex){
                System.out.println("Point A and B are identical and go to pass through this if statement.");
            }
        }

        //!A.equals(B) && !C.equals(D)
        double numeratorA   = (C.y - A.y) * (D.x - C.x) + (C.x - A.x) * (C.y - D.y);
        double denominatorA = (B.y - A.y) * (D.x - C.x) + (B.x - A.x) * (C.y - D.y);

        double numeratorC   = (C.y - A.y) * (A.x - B.x) + (C.x - A.x) * (B.y - A.y);
        double denominatorC = (C.y - D.y) * (A.x - B.x) + (C.x - D.x) * (B.y - A.y);

        //parallel line
        if(denominatorA == 0 && denominatorC == 0){
            try {
                return pointInsideLineSegment(A, C, D, closedInterval) || pointInsideLineSegment(B, C, D, closedInterval) ||
                       pointInsideLineSegment(C, A, B, closedInterval) || pointInsideLineSegment(D, A, B, closedInterval);
            } catch (Exception ex){
                System.out.println("Very unlikely exception!!\n" + ex.getMessage());
            }
        }

        //anti-parallel line
        double tParameter = numeratorA / denominatorA;
        double sParameter = numeratorC / denominatorC;

        if(closedInterval) {
            return tParameter >= 0 && tParameter <= 1 && sParameter >= 0 && sParameter <= 1;
        } else {
            return tParameter > 0 && tParameter < 1 && sParameter > 0 && sParameter < 1;
        }
    }

    private static boolean intersectionOfOpenLineSegment(Point A, Point B, Point C, Point D){
        return intersectionOfLineSegment(A, B, C, D, false);
    }

    private static boolean intersectionOfClosedLineSegment(Point A, Point B, Point C, Point D){
        return intersectionOfLineSegment(A, B, C, D, true);
    }

    private static boolean pointInsideLineSegment(Point point, Point ALine, Point BLine, boolean closedInterval) throws Exception {
        double parameterByX = (point.x - ALine.x) / (double)(BLine.x - ALine.x);
        double parameterByY = (point.y - ALine.y) / (double)(BLine.y - ALine.y);

        //if (BLine.x - ALine.x = 0) => parameterByX is NaN
        //if (BLine.y - ALine.y = 0) => parameterByY is NaN
        //if parameterByX & parameterByY are NaN => A is identical to B => AB is not a line segment

        if(!Double.isFinite(parameterByX) && !Double.isFinite(parameterByY)){
            throw new Exception("Point ALine and BLine are identical");
        }

        //if parameterByX(Y) is NaN & parameterByY(X) is a number => line segment AB is parallel to Y(X) axis
        if(!Double.isFinite(parameterByX) && Double.isFinite(parameterByY)){

            if(closedInterval) {
                return ((point.y >= ALine.y && point.y <= BLine.y) ||
                        (point.y <= ALine.y && point.y >= BLine.y)) && point.x == ALine.x;
            } else {
                return ((point.y > ALine.y && point.y < BLine.y) ||
                        (point.y < ALine.y && point.y > BLine.y)) && point.x == ALine.x;
            }
        }

        if(Double.isFinite(parameterByX) && !Double.isFinite(parameterByY)){
            if(closedInterval) {
                return ((point.x >= ALine.x && point.x <= BLine.x) ||
                        (point.x <= ALine.x && point.x >= BLine.x)) && point.y == ALine.y;
            } else {
                return ((point.x > ALine.x && point.x < BLine.x) ||
                        (point.x < ALine.x && point.x > BLine.x)) && point.y == ALine.y;
            }
        }

        // if parameterByX & parameterByY are numbers =>
        // . parameterByX == parameterByY => normal way => parameterBy is between 0 and 1 => point is inside line segment => in other case point is outside line segment
        // . parameterByX != parameterByY => point is out of line

        // parameters are numbers in this case
        if(parameterByX == parameterByY){
            if(closedInterval) {
                return parameterByX >= 0 && parameterByX <= 1 && parameterByY >= 0 && parameterByY <= 1;
            } else {
                return parameterByX > 0 && parameterByX < 1 && parameterByY > 0 && parameterByY < 1;
            }
        } else {
            return false;
        }
    }

    private static int findStopMarker(ArrayList<Point> polygon, int startMarker, ArrayList<Integer> markers, boolean clockWise) {

        int AIndex = startMarker,
            BIndex = getIndex(startMarker - 1, polygon.size());

        markers = getMarkersUntil180(AIndex, BIndex, markers, polygon, clockWise);

        if(!markers.isEmpty()){
            return mostShortDistance(polygon, markers, startMarker);
        }


        return findIndexMark2(polygon, startMarker, clockWise);
    }

    private static int mostShortDistance(ArrayList<Point> polygon, ArrayList<Integer> markers, int startMarker) {

        int mostShortPointFromStartMarker = -1;

        for (int i = 0; i < markers.size(); i++) {

            if(startMarker == markers.get(i)){continue;}
            if(mostShortPointFromStartMarker == -1){mostShortPointFromStartMarker = markers.get(i); continue;}

            if(pointDistance(polygon.get(startMarker), polygon.get(mostShortPointFromStartMarker)) >
               pointDistance(polygon.get(startMarker), polygon.get(markers.get(i)))){

                mostShortPointFromStartMarker = markers.get(i);
            }
        }

        return mostShortPointFromStartMarker;
    }

    private static double pointDistance(Point A, Point B){
        return Math.sqrt(Math.pow(A.x - B.x, 2.0) + Math.pow(A.y - B.y, 2.0));
    }

    private static ArrayList<Integer> getMarkersUntil180(int AIndex, int BIndex, ArrayList<Integer> markers, ArrayList<Point> polygon, boolean clockWise) {

        ArrayList<Integer> markersUntil180 = new ArrayList<>();

        Point A = polygon.get(AIndex),
              B = polygon.get(BIndex), C;

        for (int i = 0; i < markers.size(); i++) {

            //If B is a marker, than we ignore it, because it is on the same edge.
            if(markers.get(i) == AIndex || markers.get(i) == BIndex){ continue; }

            C = polygon.get(markers.get(i));

            if(clockWise){
                if(rightOrientation == getOrientation(A, B, C)){ markersUntil180.add(markers.get(i)); }
            } else {
                if(leftOrientation  == getOrientation(A, B, C)){ markersUntil180.add(markers.get(i)); }
            }
        }

        return markersUntil180;
    }

    private static ArrayList<Integer> findMarkers(ArrayList<Point> polygon, boolean clockWise) {

        if(polygon.size() < 3){return null;}

        ArrayList<Integer> markers = new ArrayList<>();

        Point A, B, C;

        A = polygon.get(polygon.size() - 1);
        B = polygon.get(0);
        C = polygon.get(1);

        if( clockWise  && leftOrientation  == getOrientation(A, B, C)){ markers.add(0); }
        if(!clockWise  && rightOrientation == getOrientation(A, B, C)){ markers.add(0); }

        for (int i = 0; i < polygon.size() - 2; i++) {
            A = polygon.get(i);
            B = polygon.get(i+1);
            C = polygon.get(i+2);

            if( clockWise && leftOrientation  != getOrientation(A, B, C)){continue;}
            if(!clockWise && rightOrientation != getOrientation(A, B, C)){continue;}

            markers.add(i+1);
        }

        A = polygon.get(polygon.size() - 2);
        B = polygon.get(polygon.size() - 1);
        C = polygon.get(0);

        if( clockWise && leftOrientation  == getOrientation(A, B, C)){ markers.add(polygon.size() - 1); }
        if(!clockWise && rightOrientation == getOrientation(A, B, C)){ markers.add(polygon.size() - 1); }

        return markers;
    }



    private static boolean isClockWiseDirection(ArrayList<Point> polygon){

        int AIndex = -1, B1Index = -1, B2Index = -1, CIndex = -1;

        //looking for first line
        for (int i = 0; i < polygon.size() - 1; i++) {
            for (int j = i + 1; j < polygon.size(); j++) {
                if(allPointInOneHalfPlane(i, j, polygon)){
                    AIndex = i;
                    B1Index = j;
                    i = polygon.size();
                    break;
                }
            }
        }

        //looking for second line
        for (int i = B1Index; i < polygon.size() - 1; i++) {
            for (int j = i + 1; j < polygon.size(); j++) {
                if(allPointInOneHalfPlane(i, j, polygon) && lineOrientation != getOrientation(polygon.get(AIndex), polygon.get(i), polygon.get(j))){
                    B2Index = i;
                    CIndex = j;
                    i = polygon.size();
                    break;
                }
            }
        }

        return rightOrientation == getOrientation(polygon.get(AIndex), polygon.get(B1Index), polygon.get(B2Index), polygon.get(CIndex));
    }

    private static ArrayList<ArrayList<Point>> cutPolygonToTwoSubsets(ArrayList<Point> polygon, int index1, int index2) {


        if(index1 > index2){
            int tempIndex = index2;
            index2 = index1;
            index1 = tempIndex;
        }

        ArrayList<ArrayList<Point>> subPolygons = new ArrayList<>();

        ArrayList<Point> firstPolygon = new ArrayList<>();
        for (int i = 0; i <= index1; i++) {
            firstPolygon.add(polygon.get(i));
        }
        for (int i = index2; i < polygon.size(); i++) {
            firstPolygon.add(polygon.get(i));
        }


//        firstPolygon.addAll(polygon.subList(0, index1 + 1));
//        firstPolygon.addAll(polygon.subList(index2, polygon.size()));

        ArrayList<Point> secondPolygon = new ArrayList<>();
        for (int i = index1; i <= index2; i++) {
            secondPolygon.add(polygon.get(i));
        }

//        secondPolygon.addAll(polygon.subList(index1, index2 + 1));

        subPolygons.add(firstPolygon);
        subPolygons.add(secondPolygon);
        return subPolygons;
    }



    private static int findIndexMark1(ArrayList<Point> polygon, boolean clockWise){

        if(polygon.size() < 3){return -1;}

        Point A, B, C;

        A = polygon.get(polygon.size() - 1);
        B = polygon.get(0);
        C = polygon.get(1);

        if( clockWise  && leftOrientation  == getOrientation(A, B, C)){ return 0; }
        if(!clockWise  && rightOrientation == getOrientation(A, B, C)){ return 0; }

        for (int i = 0; i < polygon.size() - 2; i++) {
            A = polygon.get(i);
            B = polygon.get(i+1);
            C = polygon.get(i+2);

            if( clockWise && leftOrientation  != getOrientation(A, B, C)){continue;}
            if(!clockWise && rightOrientation != getOrientation(A, B, C)){continue;}

            return i+1;
        }

        A = polygon.get(polygon.size() - 2);
        B = polygon.get(polygon.size() - 1);
        C = polygon.get(0);

        if( clockWise && leftOrientation  == getOrientation(A, B, C)){ return polygon.size() - 1; }
        if(!clockWise && rightOrientation == getOrientation(A, B, C)){ return polygon.size() - 1; }

        return -1;
    }

    private static int findIndexMark2(ArrayList<Point> polygon, int mark1Index, boolean clockWise){

        int A_Ind = mark1Index,
            B_Ind = getIndex(mark1Index - 1, polygon.size()),
            C_TestInd,
            mover = 1;

        Point A = polygon.get(A_Ind),
              B = polygon.get(B_Ind), C0, C1, C2;

        do {

            C_TestInd = getIndex(B_Ind - mover++, polygon.size());

            C0 = polygon.get(C_TestInd);
            C1 = polygon.get(getIndex(C_TestInd - 1, polygon.size()));
            C2 = polygon.get(getIndex(C_TestInd - 2, polygon.size()));

            if(clockWise){
                if(leftOrientation  == getOrientation(B, A, C0))   { return getIndex(C_TestInd + 1, polygon.size()); } //more than 180°
                //if(rightOrientation == getOrientation(C0, C1, C2)) { return getIndex(C_TestInd - 1, polygon.size()); } //leave track to inside polynom
            } else {
                if(rightOrientation == getOrientation(B, A, C0))   { return getIndex(C_TestInd + 1, polygon.size()); } //more than 180°
                //if(leftOrientation  == getOrientation(C0, C1, C2)) { return getIndex(C_TestInd - 1, polygon.size()); } //leave track to inside polynom
            }

        } while(C_TestInd != A_Ind);


        return -1;
    }

    private static int getIndex(int chosenIndexCell, int length){

        int i = chosenIndexCell % length;
        int returnValue = (i < 0) ? i + length : i;

        return returnValue;
    }









    private static int getOrientation(Point A, Point B, Point C){ //clockwise

        return getOrientation(A, B, B, C);

        //        Point vectorAB = new Point(B.x - A.x, B.y - A.y);
        //        Point vectorBC = new Point(C.x - B.x, C.y - B.y);
        //        Point normalBC = new Point(-vectorBC.y, vectorBC.x);
        //
        //        int dotProd = dotProduct(vectorAB, normalBC);
        //
        //        return ( dotProd < 0) ? rightOrientation : (dotProd == 0) ? lineOrientation : leftOrientation ;
    }

    private static int getOrientation(Point A1, Point B1, Point A2, Point B2){
        Point vector1 = new Point(B1.x - A1.x, B1.y - A1.y);
        Point vector2 = new Point(B2.x - A2.x, B2.y - A2.y);
        Point normalVector2 = new Point(-vector2.y, vector2.x);

        int dotProd = dotProduct(vector1, normalVector2);

        return ( dotProd < 0) ? rightOrientation : (dotProd == 0) ? lineOrientation : leftOrientation ;
    }

    private static double getAngle(Point Origin, Point A, Point B){
        return getAngle(new Point(A.x - Origin.x, A.y - Origin.y),
                        new Point(B.x - Origin.x, B.y - Origin.y));
    }

    private static double getAngle(Point vectorA, Point vectorB){

        double returnAngle = 0;

        if(leftOrientation == getOrientation(vectorA, new Point(0,0), vectorB)){
            vectorA = new Point(-vectorA.x, -vectorA.y);
            returnAngle += 180;
        }

        double alphaRad = Math.acos(
                dotProduct(vectorA, vectorB) / (getVectorMagnitude(vectorA) * getVectorMagnitude(vectorB))
        );

        returnAngle += Math.toDegrees(alphaRad);

        return returnAngle;
    }

    private static int dotProduct(Point v1, Point v2){
        return v1.x * v2.x + v1.y * v2.y;
    }

    private static double getVectorMagnitude(Point vector){
        return Math.sqrt(dotProduct(vector, vector));
    }

    private static double getVectorMagnitude(Point2D vector){
        return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
    }



    private static Point2D getNormalizeVector(Point2D vector){
        double magnitude = getVectorMagnitude(vector);
        return  new Point2D(vector.getX() / magnitude, vector.getY() / magnitude);
    }




    // *****************************
    // isPolygon routines
    // *****************************

    private static boolean collisionABLineWithAnyEdge(ArrayList<Point> polygon, int indexA1, int indexB1){

        int indexA2, indexB2;

        if(indexA1 == indexB1){return collisionPointWithNonBelongEdge(polygon, indexA1);}

        for (int i = 0; i < polygon.size(); i++) {

            indexA2 = i;
            indexB2 = (i+1 >= polygon.size()) ? 0 : i+1;

            if((indexA2 == indexA1 && indexB2 == indexB1) || (indexA2 == indexB1 && indexB2 == indexA1)){continue;}

            if(indexA1 == indexA2 || indexA1 == indexB2 || indexB1 == indexA2 || indexB1 == indexB2){
                if(intersectionOfOpenLineSegment(polygon.get(indexA1), polygon.get(indexB1),
                        polygon.get(indexA2), polygon.get(indexB2))){
                    return true;
                }
                continue;
            }

            if(intersectionOfClosedLineSegment(polygon.get(indexA1), polygon.get(indexB1),
                    polygon.get(indexA2), polygon.get(indexB2))){
                return true;
            }
        }

        return false;
    }

    private static boolean collisionPointWithNonBelongEdge(ArrayList<Point> polygon, int indexPoint) {
        try {
            for (int A = 0, B; A < polygon.size(); A++) {
                B = (A + 1 >= polygon.size()) ? 0 : A + 1;

                if (indexPoint == A || indexPoint == B) {

                    if (pointInsideLineSegment(polygon.get(indexPoint), polygon.get(A), polygon.get(B), false)) {
                        return true;
                    }

                    continue;
                }

                if (pointInsideLineSegment(polygon.get(indexPoint), polygon.get(A), polygon.get(B), true)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            System.out.println("Very unlikely exception!!\n" + ex.getMessage());
        }
        return false;
    }


    // *****************************
    // isConvex routines
    // *****************************

    private static boolean allPointInOneHalfPlane(int indexPointA, int indexPointB, ArrayList<Point> polygon){


        int anotherIndexPoint = -1, tempOrientation;
        boolean leftHalfPlane = false;

        for (int i = 0; i < polygon.size(); i++) {

            if(i == indexPointA || i == indexPointB /* || i == anotherIndexPoint */ ){ continue; }

            if(anotherIndexPoint == -1){
                if(lineOrientation == (tempOrientation = getOrientation(polygon.get(indexPointA), polygon.get(indexPointB), polygon.get(i)))){
                    continue;
                }

                anotherIndexPoint = i;
                leftHalfPlane = leftOrientation == tempOrientation;
                continue;
            }

            if(leftHalfPlane){
                if(rightOrientation == getOrientation(polygon.get(indexPointA), polygon.get(indexPointB), polygon.get(i))) {
                    return false;
                }
            } else {
                if(leftOrientation == getOrientation(polygon.get(indexPointA), polygon.get(indexPointB), polygon.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }
}
