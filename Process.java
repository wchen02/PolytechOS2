
import java.util.*;
import java.util.ArrayList;
import java.lang.*;

public class Process {

    private int pid, burst, numOfRef, penaltyTime;
    private ArrayList<Reference> refs;

    public Reference popRef() {
        Reference top = null;
        if (refs.size() > 0) {
            top = refs.get(0);
            refs.remove(0);
        }
        assert (top != null);
        return top;
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
        --penaltyTime;
        if (penaltyTime < 0) {
            penaltyTime = 0; // don't allow it to be negative, it doesn't make sense
        }
    }
 public void setPenaltyTime(int set) {
        penaltyTime = set;
        if (penaltyTime < 0) {
            penaltyTime = 0; // don't allow it to be negative, it doesn't make sense
        }
    }
    public Reference getReference(int index) {
        return refs.get(index);
    }

    public int getPenaltyTime() {
        return penaltyTime;
    }

    public Process(int pid, int burst, int numOfRef, ArrayList<Reference> refs) {
        this.pid = pid;
        this.burst = burst;
        this.numOfRef = numOfRef;
        this.refs = refs;
        penaltyTime = 0;            // this is the sum of missPenalty + dirtyPagePenalty
    }
}
