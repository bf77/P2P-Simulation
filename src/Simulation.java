import javax.swing.*;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.*;
import java.util.Random;
import java.util.ArrayList;

public class Simulation extends JPanel{

    int WIDTH = 1500;
    int HEIGHT = 800;
    float STROKE = 2.0f;

    int MAX_LAYER = 10;
    int MAX_NODE = 510;

    int PAIRS_NUM = ( MAX_NODE * (MAX_NODE - 1) )/2;

    //1~500 The Bound of the BW
    int BOUND = 499;

    long TIMESTAMP = 0;
    
    //Bandwidth
    double[] BW_PAIRS;

    //Origin Source
    OriginSrc OS;

    //ID of Origin Source
    int OS_ID;
    
    //Node
    Node[] NODES;

    //int MAX_CHILD = 20;

    int TMP_MAX_CHILD = 2;

    //The Bound of the time to join ( 0~10 min )
    int BOUND_TIME_JOIN = 1;

    double DEPART_INTERVAL;

    public static void main(String[] args){

	JFrame frame = new JFrame();

	Simulation sim = new Simulation();
	frame.getContentPane().add(sim);
	
	

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setBounds(10, 10, sim.WIDTH , sim.HEIGHT );
	frame.setTitle("Simulation");
	frame.setVisible(true);

	ArrayList<IntegerList> layer_list = new ArrayList<IntegerList>(sim.MAX_LAYER);
	sim.initArrayLL( layer_list , sim.MAX_LAYER );
		    
	System.out.println("aaaaa");
	//nodeParticipation( layer_list );

	//sim.TIMESTAMP = System.currentTimeMillis();
	sim.osInitialize();
	sim.nodesInitialize();
	
	long start_time,end_time;
	while(true){
	    
	    start_time = System.currentTimeMillis();
	    sim.nodeParticipation( layer_list );
	    try{
		
		Thread.sleep(1000);

		end_time = System.currentTimeMillis();
		sim.timestampUpdate(start_time,end_time);
	
		sim.repaint();
	
	    }catch(InterruptedException e){}	    
	    

	}

    }

    public void osInitialize(){

	//--Random--
	Random rnd = new Random();
	
	OS = new OriginSrc();

	OS.BW_tlv = rnd.nextDouble()*BOUND + 1;
	System.out.println( "Origin Source:TLV " + OS.BW_tlv );

	OS.child_num = 0;
	OS.child_id = new ArrayList<Integer>();

	OS.pos = new Point2D.Double( 0.0d , 0.0d );
	
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
	    NODES[i].BW_tlv = rnd.nextDouble()*BOUND + 1;

	    //0~x min
	    NODES[i].timestamp_to_join = rnd.nextDouble()*BOUND_TIME_JOIN;

	    //Init value
	    NODES[i].layer = 0;
       	    NODES[i].cache = 0;
	    NODES[i].pre_depart_timestamp = 0;
	    NODES[i].parent_id = -1;
	    NODES[i].child_num = 0;

	    //Timestamp
	    NODES[i].timestamp = TIMESTAMP;
	    
	    //The position
	    NODES[i].pos = new Point2D.Double( 0.0d , 0.0d );

	    //Initialize
	    NODES[i].child_id = new ArrayList<Integer>();

	    System.out.print("Node "+i+":TLV "+NODES[i].BW_tlv);
	    System.out.println(" Timestamp to join "+NODES[i].timestamp_to_join);

	}
	
    }


    public void timestampUpdate( long start ,long end ){

	TIMESTAMP += (end - start)/1000;
       
	System.out.println("Timestamp:"+TIMESTAMP+"s");

    }

    public void initArrayLL( ArrayList<IntegerList> ll , int length ){

	for( int i=0 ; i<length ; i++ )
	    ll.add( new IntegerList() );

    }
    
    public void nodeParticipation( ArrayList<IntegerList> layer_list ){

	System.out.println("Participating...");

	for( int id=0 ; id<MAX_NODE ; id++ ){

	    //Whether Nodes can join
	    if( TIMESTAMP < (NODES[id].timestamp_to_join*60) )
		continue;

	    //Whether there is a parent of the node
	    if( NODES[id].parent_id != -1 )
		continue;

	    //layer 1
	    if( OS.child_num < TMP_MAX_CHILD ){

		OS.child_num += 1;
		OS.child_id.add(id);

		NODES[id].parent_id = OS_ID;
		NODES[id].layer = 1;

		//Store node's id to the first layer
		layer_list.get(0).add(id);

		System.out.println("Node "+id+" on Layer 1");

	    }//layer 2~10
	    else{

		Boolean is_has_partipated = false;

		for( int layer=0 ; layer<MAX_LAYER ; layer++ ){
		    
		    //The number of nodes on the layer
		    int node_num_onlayer = layer_list.get(layer).size();

		    for( int id_onlayer=0 ; id_onlayer<node_num_onlayer ; id_onlayer++ ){

			int parent_id = layer_list.get(layer).get(id_onlayer);
			
			if( NODES[parent_id].child_num < TMP_MAX_CHILD ){
			  
			    //Processing to parent
			    NODES[parent_id].child_num += 1;
			    NODES[parent_id].child_id.add(id);
			    
			    //Processing to child
			    NODES[id].parent_id = parent_id;
			    NODES[id].layer = layer;
			    
			    //Regist id to layer_list
			    layer_list.get(layer+1).add(id);
			    
			    //Print
			    System.out.println("Node "+id+" on Layer "+(layer+2));
			    is_has_partipated = true;
			    break;

			}
	
		    }//end one layer's for

		    if( is_has_partipated )
			break;

		}//end layers' for

	    }//end else
		
	}//end all nodes for

	System.out.println("All nodes have participated...");

    }

    public void nodeStreaming(){


    }

    public void nodeReconnect(){


    }
    
    @Override
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

class IntegerList extends ArrayList<Integer>{}
