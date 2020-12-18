package miniplc0java.analysizer;
import java.util.ArrayList;

public class BasicBlock {
    ArrayList<Integer> phaseIndexs = new ArrayList<>();
    ArrayList<Integer> jumpTo = new ArrayList<>();
    boolean hasReturn = false;
}
