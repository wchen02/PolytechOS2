
public class ProcessControlBlock {

    //public methods
    public ProcessControlBlock(int id, PageTable pt) {
        this.id = id;
        this.pt = pt;
    }
    // private methods
    // private member fields
    private PageTable pt = null;
    private int id = 0, burst = 0;
}
