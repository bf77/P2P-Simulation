import java.awt.geom.*;
import java.util.ArrayList;

public class Node{

    //The threshold limit value of bandwidth
    double capacity;

    //The number of packet
    double cache;
    
    //The cache rate
    double cache_rate;

    //x y on the figure
    Point2D.Double pos;

    int parent_id;

    int child_num;

    int max_child_num;
    
    ArrayList<Integer> child_id;

    long pre_depart_timestamp;

    long timestamp_to_join;
    
    //Timestamp to start participating ms
    long timestamp; 

    ArrayList<Integer> connected_list;

    int reconnect_count;

    int list_range;

    int layer;

    int color;

    double delay;

    boolean is_begin_playing;
    
    boolean is_begin_streaming;

    //Start block id
    double first_block_id;

    //prev block id
    double prev_block_id;

    //Next block id
    double next_block_id;

    double played_buffer;

    double total_buffer;

    double max_down_Bpms;

}
