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

    static final int WIDTH = 1900;
    static final int HEIGHT = 800;
    static final float STROKE = 2.0f;

    static final int MAX_NODE = 1022;

    //Including Origin Source
    static final int PAIRS_NUM = ( MAX_NODE * (MAX_NODE + 1) )/2;

    //1~200 The Bound of the BW
    static final int BOUND = 199;

    //20~200 The Bound of the BW
    static final int OS_BOUND = 180;

    static final int MAX_PACKET_BYTE = 1500;

    int MAX_LAYER = 0;

    int CAPACITY = 200;
    
    int CAPACITY_INITVALUE = 100;

    long TIMESTAMP = 0;

    long CURRENT_TIME = 0;

    long PRE_TIMESTAMP;
    
    //Bandwidth
    double[] BW_PAIRS;

    //Origin Source
    OriginSrc OS;

    //ID of Origin Source
    static final int OS_ID = MAX_NODE;

    //for eclipse
    static final double OS_R = 30.0d;

    //10Mbps ->  buffer[Byte/ms]
    static final double BUFFER = 20*1000*1.0 / 8;
    
    //Node
    Node[] NODES;

    //for eclipse
    static final double NODE_R = 5.0d;

    static final int TMP_MAX_CHILD = 2;

    static final double CACHE_TLV = 500.0d;

    //The Bound of the time to join ( 0~10 min )
    static final int BOUND_TIME_JOIN = 1000 * 60;

    double DEPART_INTERVAL;

    ArrayList<IntegerList> LAYER_LIST;

    public static void main(String[] args){

	JFrame frame = new JFrame();

	Simulation sim = new Simulation();
	frame.getContentPane().add(sim);	

	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setBounds(10, 10, sim.WIDTH , sim.HEIGHT );
	frame.setTitle("Simulation");
	frame.setVisible(true);

	sim.LAYER_LIST = new ArrayList<IntegerList>(sim.MAX_LAYER);
	sim.initLayerList();
		    
	sim.osInitialize();
	sim.nodesInitialize();
	
	sim.TIMESTAMP = System.currentTimeMillis();
	while(true){
	    
	    if( sim.PRE_TIMESTAMP != sim.TIMESTAMP ){
		
		long dt_ms = sim.TIMESTAMP - sim.PRE_TIMESTAMP;
		sim.nodeParticipation( dt_ms );
		sim.nodeStreaming( dt_ms );

	    }

	    sim.repaint();
	    sim.timestampUpdate();

	}

    }

    public void osInitialize(){

	//--Random--
	Random rnd = new Random();
	
	OS = new OriginSrc();

	OS.capacity = rnd.nextDouble()*CAPACITY + CAPACITY_INITVALUE;
	System.out.println( "Origin Source: capacity " + OS.capacity );

	OS.child_num = 0;
	OS.child_id = new ArrayList<Integer>();

	OS.pos = new Point2D.Double( 0.0d , 0.0d );

	OS.prev_block_id = 0.0d;
	OS.next_block_id = 0.0d;

	OS.max_child_num = (int)( (OS.capacity * 1000 / 8) / BUFFER );

	OS.buffer = BUFFER;
	
    }
    
    public void nodesInitialize(){
	
	//--Random--
	Random rnd = new Random();

	//-Bandwidth between pairs--
	BW_PAIRS = new double[PAIRS_NUM];

	int cnt = 0;

	//Including Origin Source
	for( int i=0 ; i<(MAX_NODE) ; i++ ){

	    for( int j=(i+1) ; j<(MAX_NODE + 1) ; j++ ){
		
		//1~X Mbps
		BW_PAIRS[cnt] = rnd.nextDouble()*BOUND + 1;
		//System.out.println("Pairs ["+i+","+j+"]:"+BW_PAIRS[i]);
		cnt++;

	    }

	}

	//--Node--
	NODES = new Node[MAX_NODE];
	
	for( int i=0 ; i<MAX_NODE ; i++ ){

	    //init
	    NODES[i] = new Node();

	    //100~500 Mbps
	    NODES[i].capacity = rnd.nextDouble()*CAPACITY + CAPACITY_INITVALUE;

	    //0~x min
	    NODES[i].timestamp_to_join = rnd.nextInt(BOUND_TIME_JOIN);

	    //Color
	    NODES[i].color = rnd.nextInt()*10000000;

	    //Init value
	    NODES[i].layer = 0;
       	    NODES[i].cache = 0.0d;
	    NODES[i].played_buffer = 0.0d;
	    NODES[i].total_buffer = 0.0d;
	    NODES[i].pre_depart_timestamp = 0;
	    NODES[i].parent_id = -1;
	    NODES[i].child_num = 0;
	    NODES[i].max_child_num = (int)( (NODES[i].capacity * 1000 / 8) / BUFFER ) - 1 ;
	    NODES[i].first_block_id = -1.0d;
	    NODES[i].prev_block_id = -1.0d;
	    NODES[i].next_block_id = -1.0d;
	    NODES[i].delay = 0.0d;
	    NODES[i].max_down_Bpms = 0.0d;
	    NODES[i].is_begin_playing = false;
	    NODES[i].is_begin_streaming = false;
	    

	    //Timestamp
	    NODES[i].timestamp = TIMESTAMP;
	    
	    //The position
	    NODES[i].pos = new Point2D.Double( 0.0d , 0.0d );

	    //Initialize
	    NODES[i].child_id = new ArrayList<Integer>();

	    System.out.print("Node "+i+":capacity "+NODES[i].capacity);
	    System.out.println(" Timestamp to join "+NODES[i].timestamp_to_join);

	}
	
    }


    public void timestampUpdate(){

	PRE_TIMESTAMP = TIMESTAMP;
	TIMESTAMP = System.currentTimeMillis(); 

	//Timestamp have not been changed
	if( PRE_TIMESTAMP == TIMESTAMP )
	    return;

	CURRENT_TIME += TIMESTAMP - PRE_TIMESTAMP; 
	
	//System.out.println("Time:"+CURRENT_TIME+"ms "+CURRENT_TIME/1000+"s");
	//System.out.println("Timestamp:"+TIMESTAMP+"ms");
	//System.out.println("Previous timestamp:"+PRE_TIMESTAMP+"ms");

    }

    public void initLayerList(){

	
	LAYER_LIST.add( new IntegerList() );

	//layer 0
	LAYER_LIST.get(0).add(OS_ID);

    }
    
    public void nodeParticipation( long dt_ms ){

	//System.out.println("Participating...");

	Random rnd = new Random();
	int rnd_int=0;
	int next_id=0;

	for( int id=0 ; id<MAX_NODE ; id++ ){

	    //Whether Nodes can join
	    if( CURRENT_TIME < NODES[id].timestamp_to_join )
		continue;
	    
	    //Whether there is a parent of the node
	    if( NODES[id].parent_id != -1 )
		continue;
	    
	    for( int layer=0 ; layer<=MAX_LAYER ; layer++ ){
		
		//layer 1
		if( layer==0 ){
		    
		    OS.prev_block_id = (CURRENT_TIME - dt_ms) * BUFFER / MAX_PACKET_BYTE;
		    OS.next_block_id = CURRENT_TIME * BUFFER / MAX_PACKET_BYTE;
		    
		    rnd_int = rnd.nextInt( OS.child_num + 1 );
		    System.out.println("rnd:"+rnd_int+" layer:"+layer);
		    
		    //[rnd_int]==0 -> Set as my child
		    if( rnd_int > 0 ){
			
			next_id = OS.child_id.get(rnd_int-1);
			continue;
			
		    }
			
		    if( OS.child_num < OS.max_child_num ){
						
			OS.child_num += 1;
			OS.child_id.add(id);
			
			NODES[id].parent_id = OS_ID;
			NODES[id].layer = layer + 1;
			NODES[id].first_block_id = OS.next_block_id;
			NODES[id].prev_block_id = NODES[id].first_block_id;
			
			//Update layer list
			if( (layer+1) > MAX_LAYER ){
			    
			    LAYER_LIST.add( new IntegerList() );
			    MAX_LAYER++;
			    
			}
			
			//Store node's id to the first layer
			LAYER_LIST.get(layer+1).add(id);
			
			System.out.println("Node "+id+" on Layer "+ (layer+1));
			break;

		    }
		    
		}//layer 2~10
		else{
		    
		    
		    int parent_id = next_id;
		    System.out.println("parent_id:"+parent_id+" layer:"+layer);
		    
		    //Proceccing related to random
		    rnd_int = rnd.nextInt( NODES[parent_id].child_num + 1 );
		    
		    if( rnd_int > 0 ){
			
			next_id = NODES[parent_id].child_id.get(rnd_int-1);
			System.out.println("next layer:"+NODES[next_id].layer);
			continue;
			
		    }
		    
		    //Check cache
		    if( NODES[parent_id].cache < CACHE_TLV )	    
			break;
		    
		    //Check the number of children 
		    if( NODES[parent_id].child_num < NODES[parent_id].max_child_num ){
			
			//Processing to parent
			NODES[parent_id].child_num += 1;
			NODES[parent_id].child_id.add(id);
			
			//Processing to child
			NODES[id].parent_id = parent_id;
			NODES[id].layer = layer + 1;
			NODES[id].first_block_id = NODES[parent_id].next_block_id;
			NODES[id].prev_block_id = NODES[id].first_block_id;
			
			//Update layer list
			if( (layer+1) > MAX_LAYER ){
			    
			    LAYER_LIST.add( new IntegerList() );
			    MAX_LAYER++;
			    
			}
			
			//Regist id to layer_list
			LAYER_LIST.get(layer+1).add(id);
			
			//Print
			System.out.println("Node "+id+" on Layer "+(layer+1));
			
		    }

		    break;
		   
		    
		}//end else layer!=0
		
	    }//end layers' for
	    
	}//end all nodes for
	
	//System.out.println("All nodes have participated...");
	
    }
    
    
    public double nodeCombinationBW( int n , int m ){

	int ret = 0;

	if( n < m ){

	    for( int i=0 ; i<n ; i++ )
		ret += ( MAX_NODE - i );

	    ret += (m - n);
	    
	}   
	else{
	    
	    for( int i=0 ; i<m ; i++ )
		ret += ( MAX_NODE - i );

	    ret += (n - m);

	}

	return BW_PAIRS[ret];

    }
    
    

    public void nodeStreaming( long dt_ms ){
		
	for(int layer=0 ; layer<=MAX_LAYER ; layer++ ){
	    
	    int node_num_onlayer = LAYER_LIST.get(layer).size();
	    
	    //-- Layer 0 (Origin Source) --
	    if( layer==0 ){
		
		double max_capacity_Bpms = ( OS.capacity * 1000 / 8 ) / OS.child_num;
		
		//Processing to calculate cache
		for( int i=0 ; i<OS.child_num ; i++ ){
		    
		    int child_id = OS.child_id.get(i);
		    double pair_Bpms = nodeCombinationBW( child_id , OS_ID ) * 1000 / 8 ;

		    //Determine the min value
		    NODES[child_id].max_down_Bpms = Math.min( max_capacity_Bpms , pair_Bpms );
		    NODES[child_id].max_down_Bpms = Math.min( NODES[child_id].max_down_Bpms , BUFFER );
		    
		    
		}//end for childnum
		
	    }//-- Layer 1~X --
	    else{
		
		for( int id_onlayer=0 ; id_onlayer<node_num_onlayer ; id_onlayer++ ){
		    
		    //--Processing for the node id 

		    //ID of node
		    int id = LAYER_LIST.get(layer).get(id_onlayer);

		    //The number of nodes's child and the number of the parent 
		    double max_capacity_Bpms = ( NODES[id].capacity * 1000 / 8 ) / ( NODES[id].child_num + 1 );

		    //--Check processing to participate--
		    if( !NODES[id].is_begin_streaming ){
			
			NODES[id].is_begin_streaming = true;
			System.out.println("NODE "+id+": Begin streaming ");
			continue;

		    }
		    
		    //--Buffer processing--
		    double downloaded_data = Math.min( max_capacity_Bpms , NODES[id].max_down_Bpms ) * dt_ms;
		    NODES[id].total_buffer += downloaded_data;

		    double total_block_id = NODES[id].total_buffer / MAX_PACKET_BYTE;

		    NODES[id].prev_block_id = NODES[id].next_block_id;
		    NODES[id].next_block_id = NODES[id].first_block_id + total_block_id;

		    //--Cache processing--
		    if( NODES[id].is_begin_playing ){
			
			NODES[id].played_buffer += BUFFER * dt_ms;
			NODES[id].cache =  ( NODES[id].total_buffer - NODES[id].played_buffer ) / MAX_PACKET_BYTE;
			
		    }else{
			
			NODES[id].cache = total_block_id;

			if( NODES[id].cache >= CACHE_TLV ){
		
			    NODES[id].is_begin_playing = true;
			
			}
			
		    }
		    		    
		    //For output
		    //if( layer==1 )
			//printNode(id);


		    //--Processing for the children ids that the node id has 

		    for( int i=0 ; i<NODES[id].child_num ; i++ ){
			
			int child_id = NODES[id].child_id.get(i);
			double pair_Bpms = nodeCombinationBW( child_id , id ) * 1000 / 8 ;

			//Determine the min value
			NODES[child_id].max_down_Bpms = Math.min( max_capacity_Bpms , BUFFER );
			NODES[child_id].max_down_Bpms = Math.min( NODES[child_id].max_down_Bpms , pair_Bpms );
			
		    }//end i for
		    
		}// end id_onlayer for
		
	    }//end else
	    
	}//end layer 
	
    }//end function
    
    /*
    public void nodeReconnect(){

	for( int layer=0; layer<MAX_LAYER ; layer++ ){

	    int node_num_onlayer = LAYER_LIST.get(layer).size();
		
	    for( int id_onlayer=0 ; id_onlayer<node_num_onlayer ; id_onlayer++ ){

		int id = LAYER_LIST.get(layer).get(id_onlayer);
		if( !(NODES[id].is_begin_stream) )
		    continue;
		
		if( NODES[id].cache < CACHE_TLV ){

		    
   
		}

	    }

	}

    }
    */

    public void printNode( int id ){
	
	System.out.println("------------------------ Node "+id+" ------------------------");
	System.out.println("Capacity:"+NODES[id].capacity);
	System.out.println("Total buffer:"+NODES[id].total_buffer);
	System.out.println("Cache:"+NODES[id].cache);
	System.out.println("Played buffer:"+NODES[id].played_buffer);
	System.out.println("The first block id:"+NODES[id].first_block_id);
	System.out.println("The previous block id:"+NODES[id].prev_block_id);
	System.out.println("The next block id:"+NODES[id].next_block_id);
	System.out.println("Max download Byte per ms:"+NODES[id].max_down_Bpms);
	System.out.println("Streaming flag:"+NODES[id].is_begin_streaming);
	System.out.println("Playing flag:"+NODES[id].is_begin_playing);
	System.out.println();

    }

    public void printNode( int id , long dt_ms ){

	System.out.println("------------------------ Node "+id+" ------------------------");
	System.out.println("Capacity:"+NODES[id].capacity);
	System.out.println("Total buffer:"+NODES[id].total_buffer);
	System.out.println("Cache:"+NODES[id].cache);
	System.out.println("Played buffer for dt:"+(BUFFER*dt_ms/MAX_PACKET_BYTE));
	System.out.println("The first block id:"+NODES[id].first_block_id);
	System.out.println("The previous block id:"+NODES[id].prev_block_id);
	System.out.println("The next block id:"+NODES[id].next_block_id);
	System.out.println("Max download Byte per ms:"+NODES[id].max_down_Bpms);
	System.out.println("Streaming flag:"+NODES[id].is_begin_streaming);
	System.out.println("Playing flag:"+NODES[id].is_begin_playing);
	System.out.println();

    }

    @Override
    public void paintComponent(Graphics g){

	//Clear the window
	super.paintComponent(g);

	Random rnd = new Random();

	Graphics2D g2 = (Graphics2D)g;

	//Antialiasing
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			    RenderingHints.VALUE_ANTIALIAS_ON);

	BasicStroke stroke = new BasicStroke(this.STROKE);
	g2.setStroke(stroke);	
	
	for( int layer=0 ; layer<= this.MAX_LAYER ; layer++ ){


	    if( layer==0 ){
		
		//Set the position of Origin Source
		OS.pos.setLocation( 50.0d + (this.WIDTH -150)/2.0 , 30.0d );

		//g2.setPaint(Color.BLACK);
		//g2.drawString("--Layer "+layer+"--", this.WIDTH - 150 , 70*layer );
		
		g2.setPaint(Color.BLUE);
		Ellipse2D.Double origin_src = 
		    new Ellipse2D.Double( this.OS.pos.getX() - (this.OS_R/2) , this.OS.pos.getY() - (this.OS_R/2), this.OS_R , this.OS_R );
		g2.fill(origin_src);
		
	    }else{

		g2.setPaint(Color.BLACK);
		g2.drawString("--Layer "+layer+"--", this.WIDTH - 150 , 70*layer );
		
		int node_num_onlayer = this.LAYER_LIST.get(layer).size();
		double width_onlayer = (this.WIDTH-150-50)*1.0 / (node_num_onlayer+1);
		
		for( int id_onlayer=0; id_onlayer<node_num_onlayer ; id_onlayer++ ){
		    
		    int id = this.LAYER_LIST.get(layer).get(id_onlayer);
		    
		    g2.setPaint(new Color(NODES[id].color));
		    
		    this.NODES[id].pos.setLocation( 50.0d + (id_onlayer+1)*width_onlayer , 70*layer );
		    
		    int parent_id = this.NODES[id].parent_id;
		    
		    if( parent_id == this.OS_ID ){
			
			g2.draw(new Line2D.Double( this.NODES[id].pos.getX() , this.NODES[id].pos.getY() , this.OS.pos.getX() , this.OS.pos.getY() ));
			
		    }else{
			
			g2.draw(new Line2D.Double( this.NODES[id].pos.getX() , this.NODES[id].pos.getY() , this.NODES[parent_id].pos.getX() , this.NODES[parent_id].pos.getY() ));
			
		    }		
		    
		    Ellipse2D.Double node = 
			new Ellipse2D.Double( this.NODES[id].pos.getX() - (this.NODE_R/2), this.NODES[id].pos.getY() - (this.NODE_R/2), this.NODE_R , this.NODE_R );
		    g2.fill(node);
		    
		}//end id_onlayer for
		
	    }//end else
	    
	}//end layer for

    }//end paintComponent()

}//end class

class IntegerList extends ArrayList<Integer>{}
