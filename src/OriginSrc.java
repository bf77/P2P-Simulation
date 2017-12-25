import java.awt.geom.*;
import java.util.ArrayList;

public class OriginSrc{

    double capacity;

    long timestamp;

    int child_num;

    int max_child_num;

    ArrayList<Integer> child_id;

    Point2D.Double pos;

    //[Byte/ms]
    double buffer;

    //Prev block id
    double prev_block_id;

    //Next block id
    double next_block_id;

}
