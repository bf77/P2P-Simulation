import java.awt.Point;
import java.awt.geom.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class Simulation{

    static final int WIDTH = 1900;
    static final int HEIGHT = 800;
    static final float STROKE = 2.0f;

    static final int MAX_NODE = 80000;

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
    HashMap<Integer,Double> BW_PAIRS;

    //Origin Source
    OriginSrc OS;

    //ID of Origin Source
    static final int OS_ID = MAX_NODE;

    //for eclipse
    static final double OS_R = 30.0d;

    //10Mbps ->  buffer[Byte/ms]
    static final double BUFFER = 12*1000*1.0 / 8;
    
    //Node
    Node[] NODES;

    //for eclipse
    static final double NODE_R = 5.0d;

    static final int TMP_MAX_CHILD = 2;

    static final double CACHE_TLV = 2400.0d;

    static final double DEFAULT_CACHE = 3000.0d;

    static final double CACHE_RATE_TLV = 0.99d;

    //The Bound of the time to join ( 0~10 min )
    static final int BOUND_TIME_JOIN = 1000 * 900;

    static final long PROCESS_INTERVAL = 10L;

    //ms
    long DEPART_INTERVAL = 5;

    ArrayList<IntegerList> LAYER_LIST;

    public static void main(String[] args){
	    
	Simulation sim = new Simulation();

	sim.LAYER_LIST = new ArrayList<IntegerList>(sim.MAX_LAYER);
	sim.initLayerList();
	
	sim.osInitialize();
	sim.nodesInitialize();
	
	sim.TIMESTAMP = System.currentTimeMillis();
	while(true){
	    
	    //long dt_ms = sim.PROCESS_INTERVAL;

	    if( sim.PRE_TIMESTAMP != sim.TIMESTAMP ){
		
		long dt_ms = sim.TIMESTAMP - sim.PRE_TIMESTAMP;
		sim.nodeParticipation( dt_ms );
		sim.nodeReconnect( dt_ms );
		sim.nodeStreaming( dt_ms );
		
	    }
	    
	    sim.timestampUpdate();
	    
	}
	
	
    }
    
    public void osInitialize(){

	//--Random--
	Random rnd = new Random();
	
	OS = new OriginSrc();

	OS.capacity = rnd.nextDouble()*CAPACITY + CAPACITY_INITVALUE;
	//System.out.println( "Origin Source: capacity " + OS.capacity );

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
	BW_PAIRS = new HashMap<Integer,Double>();

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
	    NODES[i].cache_rate = 0.0d;
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
	    NODES[i].reconnect_count = 0;
	    NODES[i].list_range = 1;
	    NODES[i].is_begin_playing = false;
	    NODES[i].is_begin_streaming = false;

	    //Recconect
	    NODES[i].connected_list = new ArrayList<Integer>();
	    NODES[i].connected_list.add(i);

	    //Timestamp
	    NODES[i].timestamp = TIMESTAMP;
	    
	    //The position
	    NODES[i].pos = new Point2D.Double( 0.0d , 0.0d );

	    //Initialize
	    NODES[i].child_id = new ArrayList<Integer>();

	    //System.out.print("Node "+i+":capacity "+NODES[i].capacity);
	    //System.out.println(" Timestamp to join "+NODES[i].timestamp_to_join);

	}
	
    }


    public void timestampUpdate(){

	PRE_TIMESTAMP = TIMESTAMP;
	TIMESTAMP = System.currentTimeMillis(); 

	//Timestamp have not been changed
	if( PRE_TIMESTAMP == TIMESTAMP )
	    return;

	CURRENT_TIME += TIMESTAMP - PRE_TIMESTAMP; 
	//CURRENT_TIME += dt_ms;

	if( CURRENT_TIME > BOUND_TIME_JOIN )
	    System.out.println("Time:"+CURRENT_TIME+"ms "+CURRENT_TIME/1000+"s");

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

	OS.prev_block_id = (CURRENT_TIME - dt_ms) * BUFFER / MAX_PACKET_BYTE;
	OS.next_block_id = CURRENT_TIME * BUFFER / MAX_PACKET_BYTE;

	for( int id=0 ; id<MAX_NODE ; id++ ){
	    
	    next_id = -1;

	    //Whether Nodes can join
	    if( CURRENT_TIME < NODES[id].timestamp_to_join )
		continue;
	    
	    //Whether there is a parent of the node
	    if( NODES[id].parent_id != -1 )
		continue;
	    
	    for( int layer=0 ; layer<=MAX_LAYER ; layer++ ){
		
		//layer 1
		if( layer==0 ){
		    
		    rnd_int = rnd.nextInt( OS.child_num + 1 );
		    //System.out.println("rnd:"+rnd_int+" layer:"+layer);
		    
		    //[rnd_int]==0 -> Set as OS's child
		    if( rnd_int > 0 ){
			
			next_id = OS.child_id.get(rnd_int-1);
			continue;
			
		    }
			
		    if( OS.child_num < OS.max_child_num ){
						
			OS.child_num += 1;
			OS.child_id.add(id);
			
			NODES[id].parent_id = OS_ID;
			NODES[id].layer = 1;
			NODES[id].first_block_id = OS.next_block_id;
			NODES[id].prev_block_id = NODES[id].first_block_id;
			NODES[id].next_block_id = NODES[id].first_block_id;
			NODES[id].pre_depart_timestamp = CURRENT_TIME;
			NODES[id].connected_list.add(OS_ID);
			
			//Update layer list
			if( 1 > MAX_LAYER ){
			    
			    LAYER_LIST.add( new IntegerList() );
			    MAX_LAYER++;
			    
			}
			
			//Store node's id to the first layer
			LAYER_LIST.get(1).add(id);
			if( !BW_PAIRS.containsKey( nodeCombinationKey( id , OS_ID ) ) )
			    BW_PAIRS.put( nodeCombinationKey( id , OS_ID ) , rnd.nextDouble()*BOUND + 1 );
			//System.out.println("id:"+id+" parent_id:"+OS_ID);

			//System.out.println("Node "+id+" on Layer "+ 1);
			break;

		    }else{
			
			//System.out.println("The children status of OS is full ...");
			rnd_int = rnd.nextInt( OS.child_num ) + 1;
			//System.out.println("rnd:"+rnd_int+" layer:"+layer);
			next_id = OS.child_id.get(rnd_int-1);
			continue;

		    }
		    
		}//layer 2~10
		else{
		    		    
		    int parent_id = next_id;
		    //System.out.println("parent_id:"+parent_id+" layer:"+layer);
		    
		    //Proceccing related to random
		    rnd_int = rnd.nextInt( NODES[parent_id].child_num + 1 );
		    
		    if( rnd_int > 0 ){
			
			next_id = NODES[parent_id].child_id.get(rnd_int-1);
			//System.out.println("next layer:"+NODES[next_id].layer);
			continue;
			
		    }
		    
		    //Check cache
		    if( NODES[parent_id].cache < DEFAULT_CACHE )	    
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
			NODES[id].next_block_id = NODES[id].first_block_id;
			NODES[id].pre_depart_timestamp = CURRENT_TIME;
			NODES[id].connected_list.add(parent_id);
			
			//Update layer list
			if( (layer+1) > MAX_LAYER ){
			    
			    LAYER_LIST.add( new IntegerList() );
			    MAX_LAYER++;
			    
			}
			
			//Regist id to layer_list
			LAYER_LIST.get(layer+1).add(id);
			if( !BW_PAIRS.containsKey( nodeCombinationKey( id , parent_id ) ) )
			    BW_PAIRS.put( nodeCombinationKey( id , parent_id ) , rnd.nextDouble()*BOUND + 1 );
			//System.out.println("id:"+id+" parent_id:"+parent_id);
			//Print
			//System.out.println("Node "+id+" on Layer "+(layer+1));
			
		    }else{

			//System.out.println("The number of children Node "+parent_id+" has is "+NODES[parent_id].child_num+" ...");
			//System.out.println("The children status of Node "+parent_id+" is full ...");
			rnd_int = rnd.nextInt( NODES[parent_id].child_num ) + 1;
			//System.out.println("rnd:"+rnd_int+" layer:"+layer);
			next_id = NODES[parent_id].child_id.get(rnd_int-1);
			continue;

		    }

		    break;
		   
		    
		}//end else layer!=0
		
	    }//end layers' for
	    
	}//end all nodes for
	
	//System.out.println("All nodes have participated...");
	
    }
    
    public int idGeneratorExceptlist( int base_layer , int range , ArrayList<Integer> except_list ){

	Random rnd = new Random();
	int id = -1;

	ArrayList<Integer> connect_list = new ArrayList<Integer>();

	for( int layer=(base_layer-range) ; layer<=(base_layer+range) ; layer++ ){

	    if( layer > MAX_LAYER )
		break;

	    if( layer < 0 )
		continue;

	    for( int i=0 ; i<LAYER_LIST.get(layer).size() ; i++ )
		connect_list.add( LAYER_LIST.get(layer).get(i) );

	}

	for( int i=0 ; i<connect_list.size() ; i++ ){
	    
	    if( except_list.contains( connect_list.get(i) ) ){
		
		//System.out.println("remove:"+connect_list.get(i));
		connect_list.remove(i);
		i--;
		
	    }
	    
	}
	
	//System.out.print("connect_list:");
	//printList(connect_list);

	if( connect_list.size() == 0 )
	    return -1;

	
	int id_rnd = rnd.nextInt( connect_list.size() );
	id = connect_list.get(id_rnd);
	
	return id;

    }

    public void printList( ArrayList<Integer> list ){

	for( int i=0 ; i<list.size() ; i++ ){
	    System.out.print( list.get(i)+" " );
	}

	System.out.println();
    }

    public boolean layerAjustment( int id ){

	boolean is_have_children = false;
	int parent_id = NODES[id].parent_id;

	//Depart form the current layer
	int id_onlayer = LAYER_LIST.get(NODES[id].layer).indexOf(id);
	LAYER_LIST.get(NODES[id].layer).remove( id_onlayer );

	if( parent_id == OS_ID )
	    NODES[id].layer = 1;
	else
	    NODES[id].layer = NODES[parent_id].layer + 1;

	//Update layer list
	if( NODES[id].layer > MAX_LAYER ){
	    
	    LAYER_LIST.add( new IntegerList() );
	    MAX_LAYER++;
	    
	}

	//Regist id to layer_list
	LAYER_LIST.get(NODES[id].layer).add(id);

	if( NODES[id].child_num > 0 )
	    is_have_children = true;

	return is_have_children;

    }

    public void nodeReconnect( long dt_ms ){

       	//System.out.println("nodeReconnect()...");

	Random rnd = new Random();

	if( MAX_LAYER == 0 )
	    return;

	ArrayList<Integer> reconnect_list = new ArrayList<Integer>(); 

	for( int layer=1 ; layer<=MAX_LAYER ; layer++ ){

	    for( int id_onlayer=0; id_onlayer<LAYER_LIST.get(layer).size() ; id_onlayer++ ){

		int id = LAYER_LIST.get(layer).get(id_onlayer);

		if( NODES[id].cache_rate == CACHE_RATE_TLV ||
		    !((NODES[id].cache < CACHE_TLV) && NODES[id].is_begin_playing) ||
		    (CURRENT_TIME - NODES[id].pre_depart_timestamp) < DEPART_INTERVAL ){

		    continue;

		}
		else{
		    
		    //System.out.println("Node "+id+" cache_rate :"+NODES[id].cache_rate);
		    //printNode(id);
		    reconnect_list.add(id);
		    
		}

	    }

	}//end layer for

	for( int id_onlist=0 ; id_onlist<reconnect_list.size() ; id_onlist++ ){

	    int id = reconnect_list.get(id_onlist);	    
	    boolean is_connect_successful = false;

	    ArrayList<Integer> except_list = new ArrayList<Integer>();

	    //Initialized
	    for( int i=0 ; i<NODES[id].connected_list.size() ; i++ )
		except_list.add(NODES[id].connected_list.get(i));

	    //--Connect--
	    while( !is_connect_successful ){

		//Candidate id
		int candidate_id = idGeneratorExceptlist( NODES[id].layer , NODES[id].list_range , except_list );

		if( candidate_id == -1 ){

		    //System.out.println("Not found candidate id...");
		    NODES[id].reconnect_count++;
		    NODES[id].list_range++;

		    NODES[id].list_range = Math.max( NODES[id].list_range , MAX_LAYER - 1 );
		    //System.exit(0);
		    break;

		}else{

		    //System.out.println("candidate id "+candidate_id);

		}

		if( candidate_id == OS_ID ){

		    if( OS.child_num < OS.max_child_num ){

			//Depart form the current layer
			int id_onlayer = LAYER_LIST.get(NODES[id].layer).indexOf(id);
			LAYER_LIST.get(NODES[id].layer).remove( id_onlayer );
			
			//Processing for old parent
			int parent_id = NODES[id].parent_id;
			
			//Processing to old parent
			NODES[parent_id].child_num -= 1;
			int id_onparent = NODES[parent_id].child_id.indexOf(id);
			NODES[parent_id].child_id.remove(id_onparent);
			
			//Processing to new parent
			OS.child_num += 1;
			OS.child_id.add(id);
			
			//Processing to child
			NODES[id].parent_id = candidate_id;
			NODES[id].layer = 1;
			//NODES[id].first_block_id = NODES[parent_id].next_block_id;
			//NODES[id].prev_block_id = NODES[id].first_block_id;
			NODES[id].pre_depart_timestamp = CURRENT_TIME;
			
			//Regist id to layer_list
			LAYER_LIST.get(NODES[id].layer).add(id);
			if( !BW_PAIRS.containsKey( nodeCombinationKey( id , OS_ID ) ) )
			    BW_PAIRS.put( nodeCombinationKey( id , OS_ID ) , rnd.nextDouble()*BOUND + 1 );
			
			//System.out.println("id:"+id+" parent_id:"+OS_ID);

			//Print
			//System.out.println("Node "+id+" on Layer "+ NODES[id].layer);
			is_connect_successful = true;
			NODES[id].connected_list.add(candidate_id);

			NODES[id].reconnect_count++;
			
			//The status of layerAjustment()
			boolean status = true;
			ArrayList<Integer> ajustment_list = new ArrayList<Integer>();
			ajustment_list.add(id);
			
			for( int i=0 ; i < ajustment_list.size() ; i++ ){

			    int id_on_alist = ajustment_list.get(i);

			    for( int id_onnode=0 ; id_onnode < NODES[id_on_alist].child_num ; id_onnode++ ){
			    
				if( layerAjustment( NODES[id_on_alist].child_id.get(id_onnode) ) )
				    ajustment_list.add(NODES[id_on_alist].child_id.get(id_onnode));
				
			    }

			}
							    
		    }else{
			
			if( !NODES[id].connected_list.contains(candidate_id) )
			    NODES[id].connected_list.add(candidate_id);
			
			if( !except_list.contains(candidate_id) )
			    except_list.add(candidate_id);

			NODES[id].reconnect_count++;
		
		    }

		
		}//Node
		else{

		    //Check cache
		    if( NODES[candidate_id].cache < DEFAULT_CACHE ){	    
			
			if( !except_list.contains(candidate_id) )
			    except_list.add(candidate_id);
			
			NODES[id].reconnect_count++;

			continue;
		    
		    }

		    //Check the number of children 
		    if( NODES[candidate_id].child_num < NODES[candidate_id].max_child_num ){
			
			//Depart form the current layer
			int id_onlayer = LAYER_LIST.get(NODES[id].layer).indexOf(id);
			LAYER_LIST.get(NODES[id].layer).remove( id_onlayer );
			
			//Processing for old parent
			int parent_id = NODES[id].parent_id;
			
			if( parent_id == OS_ID ){
			    
			    OS.child_num -= 1;
			    int id_onparent = OS.child_id.indexOf(id);
			    OS.child_id.remove(id_onparent);
			    
			}
			else{
			    
			    NODES[parent_id].child_num -= 1;
			    int id_onparent = NODES[parent_id].child_id.indexOf(id);
			    NODES[parent_id].child_id.remove(id_onparent);
			    
			}
			
			//Processing to new parent
			NODES[candidate_id].child_num += 1;
			NODES[candidate_id].child_id.add(id);
			
			//Processing to child
			NODES[id].parent_id = candidate_id;
			NODES[id].layer = NODES[candidate_id].layer + 1;
			NODES[id].pre_depart_timestamp = CURRENT_TIME;
			
			//Update layer list
			if( NODES[id].layer > MAX_LAYER ){
			    
			    LAYER_LIST.add( new IntegerList() );
			    MAX_LAYER++;
			    
			}
			
			//Regist id to layer_list
			LAYER_LIST.get(NODES[id].layer).add(id);
			if( !BW_PAIRS.containsKey( nodeCombinationKey( id , candidate_id ) ) )
			    BW_PAIRS.put( nodeCombinationKey( id , candidate_id ) , rnd.nextDouble()*BOUND + 1 );
			
			//System.out.println("id:"+id+" parent_id:"+parent_id);

			//Print
			//System.out.println("Node "+id+" on Layer "+ NODES[id].layer);
			is_connect_successful = true;
			NODES[id].connected_list.add(candidate_id);
			
			NODES[id].reconnect_count++;

			//The status of layerAjustment()
			boolean status = true;
			ArrayList<Integer> ajustment_list = new ArrayList<Integer>();
			ajustment_list.add(id);
			
			for( int i=0 ; i < ajustment_list.size() ; i++ ){

			    int id_on_alist = ajustment_list.get(i);

			    for( int id_onnode=0 ; id_onnode < NODES[id_on_alist].child_num ; id_onnode++ ){
			    
				if( layerAjustment( NODES[id_on_alist].child_id.get(id_onnode) ) )
				    ajustment_list.add(NODES[id_on_alist].child_id.get(id_onnode));
				
			    }

			}
			
		    }
		    else{
			
			if( !NODES[id].connected_list.contains(candidate_id) )
			    NODES[id].connected_list.add(candidate_id);
			
			if( !except_list.contains(candidate_id) )
			    except_list.add(candidate_id);

			NODES[id].reconnect_count++;

		    }//end else
		    
		}//end else
								
	    }//end while
	    
	}//end reconnect list for

	//System.out.println("nodeReconnect() end...");

    }//end function
	
    public double nodeCombinationBW( int n , int m ){

	//System.out.println("start");

	if( !BW_PAIRS.containsKey(nodeCombinationKey(n,m))){
	    System.out.println("n:"+n+" m:"+m);
	    System.out.println("Key not found"+nodeCombinationKey(n,m));
	    System.exit(1);
	}

	//System.out.println("end");

	return BW_PAIRS.get(nodeCombinationKey(n,m));

    }
    
    public int nodeCombinationKey( int n , int m ){

	int ret = 0;

	if( n < m ){

	    for( int i=0 ; i<n ; i++ )
		ret += ( (MAX_NODE+1) - i );

	    ret += (m - n);
	    
	}   
	else{
	    
	    for( int i=0 ; i<m ; i++ )
		ret += ( (MAX_NODE+1) - i );

	    ret += (n - m);

	}

	//System.out.println("Key "+ret+"n:"+n+" m:"+m);

	return ret;

    }
    

    public void nodeStreaming( long dt_ms ){
	
	//System.out.println("nodeStreaming()...");

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
			//System.out.println("NODE "+id+": Begin streaming ");
			continue;

		    }
		    
		    //--Buffer processing--
		    double downloaded_data_id;
		    double total_block_id = -1;
		    double dt_id = 0.0d;
		    int parent_id = NODES[id].parent_id;

		 

		    if( NODES[id].reconnect_count > 0 ){

			double current_Bpms;
		
			current_Bpms = Math.min( max_capacity_Bpms , NODES[id].max_down_Bpms );
			downloaded_data_id = current_Bpms * dt_ms / MAX_PACKET_BYTE;
			
			if( parent_id == OS_ID ){
			    
			    dt_id = OS.next_block_id - NODES[id].next_block_id;
			    
			    //Check the limit value
			    if( dt_id < downloaded_data_id ){
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id = OS.next_block_id;
				
				if( NODES[id].is_begin_playing ){
				    
				    NODES[id].total_buffer = NODES[id].played_buffer - (BUFFER * dt_ms) + (DEFAULT_CACHE * MAX_PACKET_BYTE);
				    total_block_id = Math.ceil(NODES[id].total_buffer / MAX_PACKET_BYTE);
				}
				else{
				    NODES[id].total_buffer = DEFAULT_CACHE * MAX_PACKET_BYTE;
				    total_block_id = DEFAULT_CACHE;
				}
				
			    }//Within limit 
			    else{
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id += downloaded_data_id;
				
				NODES[id].total_buffer += current_Bpms * dt_ms;
				total_block_id = NODES[id].total_buffer / MAX_PACKET_BYTE;
				
			    }			    
			    
			}//Node
			else{
			    
			    dt_id = NODES[parent_id].next_block_id - NODES[id].next_block_id;
			    
			    //Check the limit value
			    if( dt_id < downloaded_data_id ){
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id = NODES[parent_id].next_block_id;
				
				if( NODES[id].is_begin_playing ){
				    
				    NODES[id].total_buffer = NODES[id].played_buffer + (BUFFER * dt_ms) + (DEFAULT_CACHE * MAX_PACKET_BYTE);
				    total_block_id = Math.ceil(NODES[id].total_buffer / MAX_PACKET_BYTE);
				}
				else{
				    NODES[id].total_buffer = DEFAULT_CACHE * MAX_PACKET_BYTE;
				    total_block_id = DEFAULT_CACHE;
				}
				
			    }//Within limit 
			    else{
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id += downloaded_data_id;
				
				NODES[id].total_buffer += current_Bpms * dt_ms;
				total_block_id = NODES[id].total_buffer / MAX_PACKET_BYTE;
				
			    }
			    
			}//end node
			
		    }//Cache ok
		    else{
			
			double current_Bpms;
			current_Bpms = Math.min( max_capacity_Bpms , NODES[id].max_down_Bpms );
			current_Bpms = Math.min( current_Bpms , BUFFER );
			downloaded_data_id = current_Bpms * dt_ms / MAX_PACKET_BYTE;			    
			
			//OS
			if( parent_id == OS_ID ){
			    
			    dt_id = OS.next_block_id - NODES[id].next_block_id;
			    
			    //Check the limit value
			    if( dt_id < downloaded_data_id ){
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id = OS.next_block_id;
				
				if( NODES[id].is_begin_playing ){
				    
				    NODES[id].total_buffer = NODES[id].played_buffer + (BUFFER * dt_ms) + (DEFAULT_CACHE * MAX_PACKET_BYTE);
				    total_block_id = Math.ceil(NODES[id].total_buffer / MAX_PACKET_BYTE);
				}
				else{
				    NODES[id].total_buffer = DEFAULT_CACHE * MAX_PACKET_BYTE;
				    total_block_id = DEFAULT_CACHE;
				}
				
			    }//Within limit 
			    else{
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id += downloaded_data_id;
				
				NODES[id].total_buffer += current_Bpms * dt_ms;
				total_block_id = NODES[id].total_buffer / MAX_PACKET_BYTE;
				
			    }			    
			    
			}//Node
			else{
			    
			    dt_id = NODES[parent_id].next_block_id - NODES[id].next_block_id;
			    
			    //Check the limit value
			    if( dt_id < downloaded_data_id ){
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id = NODES[parent_id].next_block_id;
				
				if( NODES[id].is_begin_playing ){
				    
				    NODES[id].total_buffer = NODES[id].played_buffer + (BUFFER * dt_ms) + (DEFAULT_CACHE * MAX_PACKET_BYTE);
				    total_block_id = Math.ceil(NODES[id].total_buffer / MAX_PACKET_BYTE);
				}
				else{
				    NODES[id].total_buffer = DEFAULT_CACHE * MAX_PACKET_BYTE;
				    total_block_id = DEFAULT_CACHE;
				}
				
			    }//Within limit 
			    else{
				
				NODES[id].prev_block_id = NODES[id].next_block_id;
				NODES[id].next_block_id += downloaded_data_id;
				
				NODES[id].total_buffer += current_Bpms * dt_ms;
				total_block_id = NODES[id].total_buffer / MAX_PACKET_BYTE;
				
			    }			    
			    
			}//end OS or not
			
		    }
		    
		    if( total_block_id < 0 ){

			printNode(id);
			printNode(NODES[id].parent_id);
			System.out.println("total_block_id :"+total_block_id);
			System.out.println("Node re count " + NODES[id].reconnect_count );
			System.exit(1);

		    }
		
		    if( NODES[id].cache < 0 ){

			printNode(id);
			printNode(NODES[id].parent_id);
			System.out.println("Node re count " + NODES[id].reconnect_count );
			System.out.println("Cache 0");
			System.exit(1);
			
		    }
		    

		    //--Cache processing--
		    if( NODES[id].is_begin_playing ){
			
			NODES[id].played_buffer += BUFFER * dt_ms;
			double prev_cache = NODES[id].cache;
			NODES[id].cache = ( NODES[id].total_buffer - NODES[id].played_buffer ) / MAX_PACKET_BYTE;

			if(NODES[id].cache_rate > 0.99) 
			    NODES[id].cache_rate = 1.0d;
			
			NODES[id].cache_rate = (NODES[id].cache - prev_cache) / ( BUFFER * dt_ms ) + 1;
			NODES[id].cache_rate = Math.min(1,NODES[id].cache_rate);
			NODES[id].cache_rate = Math.max(0,NODES[id].cache_rate);
			//System.out.println( "NODE cache rate:" + NODES[id].cache_rate);
			
		    }else{
			
			double prev_cache = NODES[id].cache;
			NODES[id].cache = total_block_id;
			NODES[id].cache_rate = (NODES[id].cache - prev_cache) / ( BUFFER * dt_ms ) + 1;

			if(NODES[id].cache_rate > 0.99) 
			    NODES[id].cache_rate = 1.0d;

			NODES[id].cache_rate = Math.min(1,NODES[id].cache_rate);
			NODES[id].cache_rate = Math.max(0,NODES[id].cache_rate);
			//System.out.println( "NODE cache rate:" + NODES[id].cache_rate);
			
			//printNode(id);
			//System.out.println( "dt_id" + dt_id);

			if( NODES[id].cache >= DEFAULT_CACHE ){
		
			    NODES[id].is_begin_playing = true;
			
			}
			
		    }
		    		    
		    //--Processing for the children ids that the node id has 
		    
		    for( int i=0 ; i<NODES[id].child_num ; i++ ){
			
			int child_id = NODES[id].child_id.get(i);
			//printNode(child_id);
			double pair_Bpms = nodeCombinationBW( child_id , id ) * 1000 / 8 ;
			
			//Determine the min value
			//NODES[child_id].max_down_Bpms = Math.min( max_capacity_Bpms , BUFFER );
			NODES[child_id].max_down_Bpms = Math.min( max_capacity_Bpms , pair_Bpms );
			
		    }//end i for
		    
		}// end id_onlayer for
		
	    }//end else
	    
	}//end layer 
	
	//System.out.println("nodeStreaming() end...");

    }//end function
    
    public void printNode( int id ){
	
	System.out.println("------------------------ Node "+id+" ------------------------");
	System.out.println("Capacity:"+NODES[id].capacity);
	System.out.println("Total buffer:"+NODES[id].total_buffer);
	System.out.println("Cache:"+NODES[id].cache);
	System.out.println("Cache rate:"+NODES[id].cache_rate);
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
    
}//end class

class IntegerList extends ArrayList<Integer>{}
