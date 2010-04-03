
import java.util.*;
import java.util.ArrayList;
import java.lang.*;

public class Process {

    private int pid, burst, numOfRef;
    private ArrayList<Reference> refs;

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

    public Process(int pid, int burst, int numOfRef, ArrayList<Reference> refs) {
        this.pid = pid;
        this.burst = burst;
        this.numOfRef = numOfRef;
        this.refs = refs;
    }
}
