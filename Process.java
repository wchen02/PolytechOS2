
import java.util.*;
import java.util.ArrayList;
import java.lang.*;

public class Process {

  private int pid, burst, numOfRef, penaltyTime;
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
  public void setPenaltyTime(int penalty){
    penaltyTime = penalty;
    if(penaltyTime < 0 )penalty = 0; // don't allow it to be negative, it doesn't make sense
  }
  public Reference getReference(int index){
    if(index <= refs.size() ){
      return refs.get(index);
    }
    System.out.println("Index out of range ! index has to be less than: "+refs.size());
    return new Reference();     // dummy reference as error code.
  }
  public int getPenaltyTime(){ return penaltyTime;}

  public Process(int pid, int burst, int numOfRef, ArrayList<Reference> refs) {
    this.pid = pid;
    this.burst = burst;
    this.numOfRef = numOfRef;
    this.refs = refs;
    penaltyTime = 0;            // this is the sum of missPenalty + dirtyPagePenalty
  }
}
