
public class Reference {

    private int address;
    private boolean readOrWrite; // read = true

    public Reference(int addr, boolean rw) {
        address = addr;
        readOrWrite = rw;
    }
}
