import java.awt.*;
import java.util.ArrayList;

/**
 * Created by DEli on 27.4.2015.
 */
public class PolygonNodesReducer {


    public static ArrayList<Point> reduceNodes(ArrayList<Point> beforePoints, double minDistToPass){


        ArrayList<Point> returnPointList = (ArrayList<Point>)beforePoints.clone();
        boolean deleted;

        do{

            deleted = false;

            for (int i = 0; i < returnPointList.size() - 2; i++) {
                if(getDistanceLinePoint(returnPointList.get(i), returnPointList.get(i + 1), returnPointList.get(i + 2)) <= minDistToPass){
                    returnPointList.remove(i + 1);
                    deleted = true;
                }
            }



        }while(deleted);

        return returnPointList;
    }

    private static double getDistanceLinePoint(Point PointLine1, Point PointLine2, Point C){

        Point A = PointLine1, B = PointLine2;

        /*
        * 1] identity test
        * 2] collinearity test
        * */
        if(A.equals(C) || (C.x - A.x)*(B.y - A.y) == (C.y - A.y)*(B.x - A.x)){
            return -1;
        }

        /*
        * normV -> normal vector
        * c -> ax + by + c = 0
        * a = normV.x
        * b = normV.y
        * C = [x,y]
        * */
        Point normV = new Point(B.y - A.y, A.x - B.x);
        double c = -1 * (normV.x * A.x + normV.y * A.y);

        return Math.abs(normV.x * C.x + normV.y * C.y + c) / Math.sqrt(normV.x * normV.x + normV.y * normV.y);
    }

}
