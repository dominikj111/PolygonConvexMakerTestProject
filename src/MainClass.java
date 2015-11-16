import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by DEli on 16.4.2015.
 */
public class MainClass extends JFrame {

    private JPanel mainPanel, buttonPanel, polygonPanel, outputPanel;
    private JLabel labelOutputPointsCount, labelOutputLastPoint, areaSubPolygonsCount;

    public MainClass(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setFocusable(true);
        setResizable(false);

        initMainPanel();
        initButtonPanel();
        initPolygonPanel();
        initOutputPanel();

        ((PolygonPanel)polygonPanel).addPolygonPanelListener((pointListOfPolygon) -> {
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            StringBuilder sb3 = new StringBuilder();

            if(!((PolygonPanel)polygonPanel).wasSeparated()){
                sb3.append("No convex sub-polygons");
            } else {
                sb3.append("Sub-polygons: ").append(((PolygonPanel)polygonPanel).getCountOfSubPolygons());
            }


            if (pointListOfPolygon.isEmpty()) {
                sb1.append("No nodes");
                sb2.append(" ");
            } else {
                Point lastPoint = pointListOfPolygon.get(pointListOfPolygon.size() - 1);

                sb1.append("Nodes count: ").append(pointListOfPolygon.size());
                sb2.append("Last added point: [").append(lastPoint.x).append(",").append(lastPoint.y).append("]");
            }

            setOutput(sb1.toString(), sb2.toString(), sb3.toString());
        });

        mainPanel.add(buttonPanel);
        mainPanel.add(polygonPanel);
        mainPanel.add(outputPanel);

        add(mainPanel);
        pack();
        setLocation(centerLocation(getSize()));

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                ((PolygonPanel)polygonPanel).setControlPressed(e.getKeyCode() != KeyEvent.VK_CONTROL);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                ((PolygonPanel)polygonPanel).setControlPressed(e.getKeyCode() == KeyEvent.VK_CONTROL);
            }
        });
    }

    private Point centerLocation(Dimension size) {

        Dimension screen = getToolkit().getScreenSize();

        return new Point(
                (screen.width - size.width) / 2,
                (screen.height - size.height) / 2
        );

    }

    private void initOutputPanel() {
        this.outputPanel = new JPanel();
        this.outputPanel.setBackground(Color.darkGray);

        JPanel outUpPanel = new JPanel();

        outUpPanel.setLayout(new BoxLayout(outUpPanel, BoxLayout.Y_AXIS));
        outUpPanel.setBackground(Color.darkGray);

        labelOutputPointsCount = new JLabel();
        labelOutputPointsCount.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        labelOutputPointsCount.setForeground(Color.white);
        labelOutputPointsCount.setText("No nodes");

        labelOutputLastPoint = new JLabel();
        labelOutputLastPoint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        labelOutputLastPoint.setForeground(Color.white);
        labelOutputLastPoint.setText(" ");

        areaSubPolygonsCount = new JLabel();
        areaSubPolygonsCount.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        areaSubPolygonsCount.setForeground(Color.white);
        areaSubPolygonsCount.setText("No convex sub-polygons");

        outUpPanel.add(labelOutputPointsCount);
        outUpPanel.add(labelOutputLastPoint);
        outUpPanel.add(areaSubPolygonsCount);

        this.outputPanel.add(outUpPanel);
    }

    private void initPolygonPanel() {
        this.polygonPanel = new PolygonPanel();
        this.polygonPanel.setBackground(new Color(255, 255, 50));
    }

    private void initButtonPanel() {
        this.buttonPanel = new JPanel();
        this.buttonPanel.setBackground(Color.darkGray);

        JButton buttonReducePolygon = new JButton("Reduce Polygon");
        buttonReducePolygon.setFocusable(false);


        JButton buttonClear = new JButton("Clear Board");
        buttonClear.setFocusable(false);

        JButton buttonConvex = new JButton("Convex Polygon");
        buttonConvex.setFocusable(false);

        JButton switcherPolygon = new JButton("Switch Polygon");
        switcherPolygon.setFocusable(false);

        JButton getPolygon = new JButton("Get Polygon");
        getPolygon.setFocusable(false);

        this.buttonPanel.add(buttonConvex);
        this.buttonPanel.add(buttonReducePolygon);
        this.buttonPanel.add(buttonClear);
        this.buttonPanel.add(switcherPolygon);
        this.buttonPanel.add(getPolygon);

        buttonReducePolygon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                ((PolygonPanel) polygonPanel).reducePolygon();
            }
        });
        buttonClear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                ((PolygonPanel) polygonPanel).clear();
            }
        });
        buttonConvex.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                ((PolygonPanel) polygonPanel).convertToConvexPolygons();
            }
        });
        switcherPolygon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                ((PolygonPanel) polygonPanel).switchPolygon();
            }
        });
        getPolygon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                ((PolygonPanel) polygonPanel).copyPolygonToClipBoard();
            }
        });
    }

    private void initMainPanel() {
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
        this.mainPanel.setBackground(Color.YELLOW);
    }

    private void setOutput(String pointsCount, String lastPoints, String countSubPolygons){

        this.labelOutputPointsCount.setText(pointsCount);
        this.labelOutputLastPoint.setText(lastPoints);
        this.areaSubPolygonsCount.setText(countSubPolygons);
    }


    // ++++++++++++++++++++++++++++++++++++++
    // +    MAIN FUNCTION                   +
    // ++++++++++++++++++++++++++++++++++++++

    public static void main(String[] args) {


        SwingUtilities.invokeLater(()->{

            MainClass okno = new MainClass();
            okno.setVisible(true);

        });

    }

}
