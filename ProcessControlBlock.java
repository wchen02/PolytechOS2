
import java.util.ArrayList;

public class ProcessControlBlock {

    private int pid, burst, numOfRef, penaltyTime;
    private ArrayList<Reference> refs;

    public ProcessControlBlock(int pid, int burst, int numOfRef, ArrayList<Reference> refs) {
        this.pid = pid;
        this.burst = burst;
        this.numOfRef = numOfRef;
        this.refs = refs;
        penaltyTime = 0; // this is the sum of missPenalty + dirtyPagePenalty
    }

    public void popRef() {
        if (refs.size() > 0) {
            refs.remove(0);
        }
    }

    public Reference topRef() {
        Reference top = null;
        if (refs.size() > 0) {
            top = refs.get(0);
        }
        assert (top != null);
        return top;
    }

    public int getPid() {
        return pid;
    }

    public int getBurst() {
        return burst;
    }

    public int getNumOfRef() {
        return numOfRef;
    }

    public void decNumOfRef() {
        --numOfRef;
        assert (numOfRef >= 0);
    }

    public void decPenaltyTime() {
        penaltyTime -= (penaltyTime > 0) ? 1 : 0;
    }

    public void setPenaltyTime(int set) {
        penaltyTime = (set >= 0) ? set : 0;
    }

    public Reference getReference(int index) {
        return refs.get(index);
    }

    public int getPenaltyTime() {
        return penaltyTime;
    }
}
