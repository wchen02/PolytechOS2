public class Process {
  public PageTable pt;          // future improvement, use actual encapsulation !
  public ProcessControlBlock pcb;

  public Process(PageTable pt, ProcessControlBlock pcb){
    this.pt = pt;
    this.pcb = pcb;
  }
}
