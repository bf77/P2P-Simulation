import java.awt.geom.*;
import java.util.ArrayList;

public class Node{

    //The threshold limit value of bandwidth
    double capacity;

    //The number of packet
    int cache; 

    //x y on the figure
    Point2D.Double pos;

    int parent_id;

    int child_num;

    ArrayList<Integer> child_id;

    long pre_depart_timestamp;

    long timestamp_to_join;
    
    //Timestamp to start participating ms
    long timestamp; 

    int layer;

    int color;

    double delay;

    boolean is_begin_play;
    
    boolean is_begin_streaming;

    //Start block id
    long next_block_id;

    //End block id
    long end_block_id;

    double total_buffer;

    double max_down_Bpms;

}
