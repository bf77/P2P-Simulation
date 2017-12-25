import java.awt.geom.*;
import java.util.ArrayList;

public class OriginSrc{

    double capacity;

    long timestamp;

    int child_num;

    ArrayList<Integer> child_id;

    Point2D.Double pos;

    //[Byte/ms]
    double buffer;

    //Start block id
    long next_block_id;

    //End block id
    long end_block_id;

}
