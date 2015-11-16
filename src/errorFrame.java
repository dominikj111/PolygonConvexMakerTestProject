import javax.swing.*;
import java.awt.*;

/**
 * Created by DEli on 25.5.2015.
 */
public class errorFrame extends JFrame {

    public errorFrame(String message){

        JTextArea text = new JTextArea(message);
        JScrollPane scroll = new JScrollPane (text,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        scroll.setPreferredSize(new Dimension(700, 600));

        this.add(scroll);

        this.pack();
        this.setVisible(true);
    }
}
