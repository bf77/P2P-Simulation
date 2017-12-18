import javax.swing.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;

public class Simulation extends JPanel{

    public static void main(String[] args){
	JFrame frame = new JFrame();

	Simulation app = new Simulation();
	frame.getContentPane().add(app);

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setBounds(10, 10, 300, 200);
	frame.setTitle("title");
	frame.setVisible(true);
    }

    public void paintComponent(Graphics g){
	Graphics2D g2 = (Graphics2D)g;

	g2.draw(new Line2D.Double(20, 20, 160, 140));
    }
}
