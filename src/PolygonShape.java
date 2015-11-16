import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by domino on 4. 5. 2015.
 */
public class PolygonShape{

    private boolean[][] shapeMask;


    public PolygonShape(ArrayList<Point> polygon){
        shapeMask = getPolygonMask(polygon);
    }


    public boolean contains(int x, int y) {
        return this.shapeMask[y][x];
    }

    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }



    private boolean[][] getPolygonMask(ArrayList<Point> polygon){

        //max x y
        int maxX = 0, maxY = 0;
        for (Point p : polygon){
            maxX = (p.x > maxX) ? p.x : maxX;
            maxY = (p.y > maxY) ? p.y : maxY;
        }

        int width  = maxX + 1,
                height = maxY + 1;


        boolean[][] returnMask = new boolean[height][width];


        BufferedImage imageMask = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //Color backColor = Color.black;
        Color fillColor = Color.white;

        Graphics2D g = imageMask.createGraphics();
        g.setColor(fillColor);
        fillPolygon(g, polygon);

        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                returnMask[row][column] = fillColor.equals(new Color(imageMask.getRGB(column, row)));
            }
        }


        return returnMask;
    }

    private void fillPolygon(Graphics g, ArrayList<Point> polygon){

        int[] x = new int[polygon.size()];
        int[] y = new int[polygon.size()];

        Point tempPoint;

        for (int i = 0; i < polygon.size(); i++) {

            tempPoint = polygon.get(i);

            x[i] = tempPoint.x;
            y[i] = tempPoint.y;
        }

        g.fillPolygon(x, y, polygon.size());
    }
}
