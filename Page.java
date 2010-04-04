package hw02;

public class Page {

  public Page(int fn) {
    frameNumber = fn;
  }

  public int getFrameNumber() {
    return frameNumber;
  }
  private int frameNumber;

  /* Writing a getter and a setter for each variable doesn't make any sense */
  public boolean valid = false,
    referenced = false,
    dirty = false;
}
