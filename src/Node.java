import java.awt.Point;

public class Node{

    //The threshold limit value of bandwidth
    double BW_tlv;
    //The number of packet
    int cache; 
    //x y on the figure
    Point pos;
    
    Point parent_pos;

    int child_num;

    Point[] child_pos;

    double pre_depart_timestamp;

    double timestamp_to_join;
    
    //Timestamp to start participating
    double timestamp; 

    int layer;

}
