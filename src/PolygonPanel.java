import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by DEli on 16.4.2015.
 */
public class PolygonPanel extends javax.swing.JPanel {



    private int RowsCount, ColumnsCount, GridWidth, GridHeight;
    private int currentMouseColumnLoc, currentMouseRowLoc, mouseX, mouseY;
    private Point mousePanelPosition;

    private boolean controlKeyPressed;

    private boolean[][] markedPoints;

    private ArrayList<Point> polygonPoints;
    private ArrayList<ArrayList<Point>> convexPolygons;

    private BufferedImage iconPlus, iconMinus;

    private boolean drawFirstConvexPolygon = false;
    private char[] countOfSubpolygons;

    public PolygonPanel() {

        //LITERALS
        int rowsCount = 48, columnsCount = 64, cellWidth = 10, cellHeight = 10;

        setPreferredSize(new Dimension(columnsCount * cellWidth, rowsCount * cellHeight));
        setFieldDimension(rowsCount, columnsCount);
        initImages();

        this.polygonPoints = new ArrayList<>();
        this.markedPoints = new boolean[this.RowsCount][this.ColumnsCount];

        //**************************
        //ADD ERROR POLYGON FOR TEST
        //TODO






        //**************************


        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseMove(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                mouseWasPressed(e);
            }
        });


        Thread paintThread = new Thread(() -> {
            while (true){
                repaint();
                try { Thread.sleep(1000/30);}catch (InterruptedException iex){}
            }
        });
        paintThread.setDaemon(true);
        paintThread.start();
    }


    public void setFieldDimension(int rowCount, int columnsCount){

        this.RowsCount = rowCount;
        this.ColumnsCount = columnsCount;

        //CARRY ON
        this.GridHeight = getPreferredSize().height / rowCount;
        this.GridWidth  = getPreferredSize().width  / columnsCount;


    }

    public void setControlPressed(boolean value){
        this.controlKeyPressed = value;
    }

    public void clear(){

        boolean[][] cleanBoard = new boolean[this.RowsCount][this.ColumnsCount];

        for (int row = 0; row < this.RowsCount; row++) {
            for (int column = 0; column < this.ColumnsCount; column++) {
                cleanBoard[row][column] = false;
            }
        }


        this.markedPoints = cleanBoard;
        this.polygonPoints.clear();

        this.convexPolygons = null;

        firePolygonWasChanged();
    }

    public void reducePolygon() {

        ArrayList<Point> newPoints = PolygonNodesReducer.reduceNodes(this.polygonPoints, 0.5);

        this.polygonPoints = newPoints;
        this.markedPoints = new boolean[this.RowsCount][this.ColumnsCount];
        for (Point newP : this.polygonPoints){
            this.markedPoints[newP.y][newP.x] = true;
        }

        firePolygonWasChanged();
    }

    public void convertToConvexPolygons(){


        try {
            this.convexPolygons = PolygonConvexMaker.breakToConvexSubPolygons(this.polygonPoints);
            this.drawFirstConvexPolygon = false;
        } catch (Exception ex){

            String errorMessage = "";

            errorMessage += ex.getMessage();
            errorMessage += "\n";

            StackTraceElement[] pole = ex.getStackTrace();
            for (int i = 0; i < pole.length; i++) {
                errorMessage += pole[i].toString();
                errorMessage += "\n";
            }

            errorMessage += "------------------------\nPOLYGON: \n\n";

            for (int i = 0; i < this.polygonPoints.size(); i++) {
                errorMessage += "this.polygonPoints.add(new Point(" + this.polygonPoints.get(i).x + "," + this.polygonPoints.get(i).y + "));";
                errorMessage += "\n";
            }

            System.out.println(errorMessage);

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errorMessage), null);
            JFrame okno = new errorFrame(errorMessage);
            JOptionPane.showMessageDialog(this, "Zprava byla zkopirovana do schranky", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        firePolygonWasChanged();
    }

    public void switchPolygon(){
        this.drawFirstConvexPolygon = !this.drawFirstConvexPolygon;
    }



    private ArrayList<Point> generateInsidePolygon(ArrayList<Point> polygon, int margin){


        //polygon must has at least 3 points
        if(polygon.size() < 3){
            System.out.println("Can't solve inside polygon for non-polygon (each polygon must has at least 3 points.)");
            return null;
        }

        ArrayList<Point> returnPolygon = new ArrayList<>();
        PolygonShape polShape = new PolygonShape(polygon);

        returnPolygon.add(generateInsidePoint(polShape, polygon.get(polygon.size() - 1), polygon.get(0), polygon.get(1), margin));

        for (int i = 1; i < polygon.size() - 1; i++) {
            returnPolygon.add(generateInsidePoint(polShape, polygon.get(i - 1), polygon.get(i), polygon.get(i + 1), margin));
        }

        returnPolygon.add(generateInsidePoint(polShape, polygon.get(polygon.size() - 2), polygon.get(polygon.size() - 1), polygon.get(0), margin));

        return returnPolygon;
    }

    private Point generateInsidePoint(PolygonShape polShape, Point A, Point B, Point C, int magnitude){

        Point vector1, vector2, result;

        vector1 = makeVector(B, A, -1);
        vector2 = makeVector(B, C, -1);

        result = addVector(vector1, vector2);
        result = changeVectorMagnitude(result, magnitude);
        result = (polShape.contains(addVector(B, result))) ? addVector(B, result) : new Point(-result.x, -result.y);

        return result;
    }



    private Point addVector(Point vector1, Point vector2){
        return new Point(vector1.x + vector2.x, vector1.y + vector2.y);
    }

    private Point makeVector(Point start, Point stop, int Magnitude){
        Point vector = new Point(stop.x - start.x, stop.y - start.y);

        if(Magnitude <= 0){return vector;}

        return changeVectorMagnitude(vector, Magnitude);
    }

    private Point changeVectorMagnitude(Point vector, int Magnitude){
        double vectorMagnitude = Math.sqrt(vector.x * vector.x + vector.y * vector.y);

        return new Point(
                (int)Math.round(vector.x * (Magnitude / vectorMagnitude)),
                (int)Math.round(vector.y * (Magnitude / vectorMagnitude))
        );
    }





    private ArrayList<PolygonPanelListener> polListeners = new ArrayList<>();

    public void addPolygonPanelListener(PolygonPanelListener list){
        polListeners.add(list);
    }

    private void firePolygonWasChanged(){


        //polListeners.forEach(PolygonPanelListener::polygonWasChanged);
        polListeners.forEach((list) -> list.polygonWasChanged(this.polygonPoints));
    }



    private void mouseMove(MouseEvent e){

        this.mousePanelPosition = e.getPoint();

        int MouseColumnLoc = e.getX() / this.GridWidth;
        int MouseRowLoc    = e.getY() / this.GridHeight;

        if(MouseColumnLoc >= this.markedPoints[0].length || MouseRowLoc >= this.markedPoints.length){
            return;
        }

        this.currentMouseColumnLoc = MouseColumnLoc;
        this.currentMouseRowLoc = MouseRowLoc;

        this.mouseX = e.getX();
        this.mouseY = e.getY();


        if(this.controlKeyPressed){
            addCurrentPoint();
        }
    }

    private void mouseWasPressed(MouseEvent e){

        if(this.markedPoints[this.currentMouseRowLoc][this.currentMouseColumnLoc]){
            removeCurrentPoint();
        } else {
            addCurrentPoint();
        }
    }



    private void addCurrentPoint(){
        this.markedPoints[this.currentMouseRowLoc][this.currentMouseColumnLoc] = true;

        Point tempPoint = new Point(this.currentMouseColumnLoc, this.currentMouseRowLoc);

        if(this.polygonPoints.contains(tempPoint)){ return; }

        this.polygonPoints.add(tempPoint);

        firePolygonWasChanged();
    }

    private void removeCurrentPoint(){
        this.markedPoints[this.currentMouseRowLoc][this.currentMouseColumnLoc] = false;
        this.polygonPoints.remove(new Point(this.currentMouseColumnLoc, this.currentMouseRowLoc));

        firePolygonWasChanged();
    }

    private void initImages(){
        try{

            URL imagePlusUrl  = getClass().getResource("plus.png");
            URL imageMinusUrl = getClass().getResource("minus.png");

            if(imagePlusUrl == null || imageMinusUrl == null){
                System.out.println("Cant get image resource.");
            }

            this.iconPlus  = (imagePlusUrl  != null) ? ImageIO.read(imagePlusUrl)  : null;
            this.iconMinus = (imageMinusUrl != null) ? ImageIO.read(imageMinusUrl) : null;

        } catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }



    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if(drawFirstConvexPolygon){
            if(this.convexPolygons != null) {
                for (ArrayList<Point> pol : this.convexPolygons){
                    g.setColor(Color.green);
                    fillPolygon(g, pol);
                    g.setColor(Color.black);
                    drawPolygon(g,pol);
                }
            }

            g.setColor(Color.darkGray);
            fillPolygon(g, this.polygonPoints);
        } else {
            g.setColor(Color.darkGray);
            fillPolygon(g, this.polygonPoints);

            if(this.convexPolygons != null) {
                for (ArrayList<Point> pol : this.convexPolygons){
                    g.setColor(Color.green);
                    fillPolygon(g, pol);
                    g.setColor(Color.black);
                    drawPolygon(g,pol);
                }
            }
        }








        if(this.markedPoints[this.currentMouseRowLoc][this.currentMouseColumnLoc]){
            g.setColor(new Color(136, 0, 0));
        } else {
            g.setColor(new Color( 0,136, 0));
        }

        g.fillRect(this.currentMouseColumnLoc * this.GridWidth,
                   this.currentMouseRowLoc    * this.GridHeight, this.GridWidth, this.GridHeight);

        g.setColor(Color.black);
        for (int row = 0; row < this.RowsCount; row++) {
            for (int column = 0; column < this.ColumnsCount; column++) {

                if(this.markedPoints[row][column]){
                    g.fillOval(
                            column * this.GridWidth  + this.GridWidth  / 2 - 2,
                            row    * this.GridHeight + this.GridHeight / 2 - 2,
                            4, 4
                    );
                }
            }
        }

        if(this.markedPoints[this.currentMouseRowLoc][this.currentMouseColumnLoc] && !this.controlKeyPressed) {
            drawIcon(g, this.iconMinus);
        }
        if(!this.markedPoints[this.currentMouseRowLoc][this.currentMouseColumnLoc]) {
            drawIcon(g, this.iconPlus);
        }

        drawInfoPoint(g, this.currentMouseColumnLoc, this.currentMouseRowLoc, this.mouseX, this.mouseY);

    }

    private void drawIcon(Graphics g, BufferedImage img){

        if(this.mousePanelPosition == null){
            return;
        }

        Point p = getIconPosition(this.getPreferredSize(), new Dimension(img.getWidth(), img.getHeight()), this.mousePanelPosition);

        g.drawImage(
                img,
                this.mousePanelPosition.x + p.x - img.getWidth() / 2,
                this.mousePanelPosition.y + p.y - img.getHeight() / 2,
                img.getWidth(), img.getHeight(), null);
    }

    private void drawInfoPoint(Graphics g, int x, int y, int mouseX, int mouseY){

        g.setColor(Color.black);
        g.fillRect(mouseX + 25, mouseY, 45, 23);

        g.setColor(Color.yellow);
        g.drawString("[" + x + "," + y + "]", mouseX + 30, mouseY + 15);

    }

    private void fillPolygon(Graphics g, ArrayList<Point> polygon){

        int[] x = new int[polygon.size()];
        int[] y = new int[polygon.size()];

        Point tempPoint;

        for (int i = 0; i < polygon.size(); i++) {

            tempPoint = polygon.get(i);

            x[i] = tempPoint.x * this.GridWidth  + this.GridWidth  / 2;
            y[i] = tempPoint.y * this.GridHeight + this.GridHeight / 2;
        }

        g.fillPolygon(x, y, polygon.size());
    }

    private void drawPolygon(Graphics g, ArrayList<Point> polygon){
        int[] x = new int[polygon.size()];
        int[] y = new int[polygon.size()];

        Point tempPoint;

        for (int i = 0; i < polygon.size(); i++) {

            tempPoint = polygon.get(i);

            x[i] = tempPoint.x * this.GridWidth  + this.GridWidth  / 2;
            y[i] = tempPoint.y * this.GridHeight + this.GridHeight / 2;
        }

        g.drawPolygon(x, y, polygon.size());
    }

    //LITERAL

    private final int iconDistanceFromMouse = 20;
    private Point lastIconLocation;

    private final Point[]  iconPositions = new Point[]{
            new Point(-1, -1), new Point(+1, -1), new Point(-1, +1), new Point(+1, +1)
    };

    private Point getSmallestDistanceAroundMouseFromCenter(Point centerLocation, Point mousePosition, int distanceFromMouse){
        int smallestDistanceIndex = 0;
        double tempDistance, returnDistance = Double.MAX_VALUE;

        for (int i = 0; i < iconPositions.length; i++) {

            tempDistance = centerLocation.distance(mousePosition.x + iconPositions[i].x, mousePosition.y + iconPositions[i].y);


            if(tempDistance < returnDistance){
                returnDistance = tempDistance;
                smallestDistanceIndex = i;
            }
        }


        return new Point(
                iconPositions[smallestDistanceIndex].x * distanceFromMouse,
                iconPositions[smallestDistanceIndex].y * distanceFromMouse);
    }

    private Point getIconPosition(Dimension panelSize, Dimension iconSize, Point mousePosition){

        if(lastIconLocation == null){
            lastIconLocation = getSmallestDistanceAroundMouseFromCenter(
                    new Point(panelSize.width / 2, panelSize.height / 2),
                    mousePosition,
                    iconDistanceFromMouse
            );

            return lastIconLocation;
        }

        Rectangle globalIconBound = new Rectangle( mousePosition.x + lastIconLocation.x,
                                                   mousePosition.y + lastIconLocation.y,
                                                   iconSize.width, iconSize.height);

        if( globalIconBound.x <= 0 || globalIconBound.y <= 0 ||
            (globalIconBound.x + globalIconBound.width) >= panelSize.width ||
            (globalIconBound.y + globalIconBound.height) >= panelSize.height){

            lastIconLocation = getSmallestDistanceAroundMouseFromCenter(
                    new Point(panelSize.width / 2, panelSize.height / 2),
                    mousePosition,
                    iconDistanceFromMouse
            );
        }

        return lastIconLocation;
    }


    public void copyPolygonToClipBoard() {


        StringBuilder sb = new StringBuilder();

        for (Point p : this.polygonPoints){
            sb.append("this.polygonPoints.add(new Point(").append(p.x).append(",").append(p.y).append("));\n");
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);

        JOptionPane.showMessageDialog(this, "Polygon byl zkopirovan do schranky.");
    }

    public boolean wasSeparated() {

        if(this.convexPolygons == null){return false;}

        return  !this.convexPolygons.isEmpty();
    }

    public int getCountOfSubPolygons() {
        return this.convexPolygons.size();
    }
}
