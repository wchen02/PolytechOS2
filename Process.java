
import java.util.*;
import java.util.ArrayList;
import java.lang.*;

public class Process {
  public PageTable pt;          // future improvement, use actual encapsulation !
  public ProcessControlBlock pcb;

  public Process(PageTable pt, ProcessControlBlock pcb){
    this.pt = pt;
    this.pcb = pcb;
  }
  public Process(){
    pt = new PageTable();
    pcb = new ProcessControlBlock();
  }
}
