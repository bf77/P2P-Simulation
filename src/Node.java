public class Node(){

    //Mbps
    double bandwidth;
    //The number of packet
    int cache;
 
    //x y on the figure
    Point point;
    
    Point parent_point;

    int child_num;

    Point[] child_points;

    double pre_depart_timestamp;

    //Timestamp to start participating
    double timestamp;

    int layer;

}
