import javax.swing.*;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.*;
import java.util.Random;

public class Simulation extends JPanel{

    int WIDTH = 1500;
    int HEIGHT = 800;
    float STROKE = 2.0f;

    int MAX_LAYER = 10;
    int MAX_NODE = 510;

    Node nodes[MAX_NODE];

    public static void main(String[] args){
	JFrame frame = new JFrame();

	Simulation sim = new Simulation();
	frame.getContentPane().add(sim);

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setBounds(10, 10, sim.WIDTH , sim.HEIGHT );
	frame.setTitle("Simulation");
	frame.setVisible(true);

	nodesInitialize();
	
    }
    
    public void nodesInitialize(){
	
	Random rnd = new Random();
	
	
	for( int i=0 ; i<MAX_NODE ; i++ ){
	
	    nodes[i] = new Node();
	    nodes[i].ts =  rnd.nextDouble(1000.0);
	    System.out.println("node "+i+":"+nodes[i].ts);
	}
	
	
    }
    
    public void paintComponent(Graphics g){

	Graphics2D g2 = (Graphics2D)g;

	//Antialiasing
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			    RenderingHints.VALUE_ANTIALIAS_ON);

	BasicStroke stroke = new BasicStroke(this.STROKE);
	g2.setStroke(stroke);

	g2.setPaint(Color.BLUE);
	Ellipse2D.Double origin_src = 
	    new Ellipse2D.Double( 675.0d, 10.0d , 35.0d , 35.0d );
	g2.fill(origin_src);

	g2.setPaint(Color.BLACK);
	for( int i=0 ; i<MAX_LAYER ; i++ ){
	    //g2.draw(new Line2D.Double( 50 ,70*(i+1) , this.WIDTH - 200 , 70*(i+1) ) );
	    g2.drawString("--Layer "+i+"--", this.WIDTH - 150 , 70*(i+1) );
	}
    }

}
