import javax.swing.*;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.*;
import java.util.Random;

public class Simulation extends JPanel{

    int WIDTH = 1500;
    int HEIGHT = 800;
    float STROKE = 2.0f;

    int MAX_LAYER = 10;
    int MAX_NODE = 510;

    int PAIRS_NUM = ( MAX_NODE * (MAX_NODE - 1) )/2;

    //1~1000
    int BOUND = 499;

    //Bandwidth
    double[] BW_PAIRS;
    //Node
    Node[] NODES;

    int MAX_CHILD = 20;

    public static void main(String[] args){

	JFrame frame = new JFrame();

	Simulation sim = new Simulation();
	frame.getContentPane().add(sim);

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setBounds(10, 10, sim.WIDTH , sim.HEIGHT );
	frame.setTitle("Simulation");
	frame.setVisible(true);

	sim.nodesInitialize();
	
    }
    
    public void nodesInitialize(){
	
	//--Random--
	Random rnd = new Random();

	//-Bandwidth between pairs--
	BW_PAIRS = new double[PAIRS_NUM];

	for( int i=0 ; i<(MAX_NODE-1) ; i++ ){

	    for( int j=(i+1) ; j<MAX_NODE ; j++ ){
		
		//1~500 Mbps
		BW_PAIRS[i] = rnd.nextDouble()*BOUND + 1;
		System.out.println("Pairs ["+i+","+j+"]:"+BW_PAIRS[i]);

	    }

	}

	//--Node--
	NODES = new Node[MAX_NODE];
	
	for( int i=0 ; i<MAX_NODE ; i++ ){

	    //init
	    NODES[i] = new Node();

	    //1~500 Mbps
	    NODES[i].BW_tlv =  rnd.nextDouble()*BOUND + 1;

	    //Init value
	    NODES[i].layer = 0;
       	    NODES[i].cache = 0;
	    NODES[i].timestamp = 0;
	    NODES[i].pre_depart_timestamp = 0;

	    //The position
	    NODES[i].pos = new Point(0,0);
	    //The position of parent
	    NODES[i].parent_pos = new Point(0,0);
	    //Init value
	    NODES[i].child_num = 0;
	    //The position of children
	    NODES[i].child_pos = new Point[MAX_CHILD];
	    for( int j=0 ; j<MAX_CHILD ; j++ )
		NODES[i].child_pos[j] = new Point(0,0);

	    System.out.println("Node "+i+":"+NODES[i].BW_tlv);

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
