
public class Page {

    public Page(int fn) {
        setFrameNumber(fn);
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int fn) {
        frameNumber = (fn >= 0) ? fn : 0;
    }
    private int frameNumber;

    /* Writing a getter and a setter for each boolean variable doesn't make any sense */
    public boolean valid = false,
            referenced = false,
            dirty = false;
}
