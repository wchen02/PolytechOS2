
import java.util.ArrayList;

public class PageTable {

    public PageTable(int size){
        pageTable = new ArrayList<Page>(9);
        for(int i = 0; i <= 10; ++i)
        pageTable.add(new Page(i));
    }

    public void addPage(Page page) {
        pageTable.add(page);
    }

    public void removePage(Page page) {
        pageTable.remove(page);
    }

    public boolean contains(Page page) {
        return pageTable.contains(page);
    }

    public Page getPageAtIndex(int index) {
        assert index > 0 && index < 1000;
        //if(Clock.DEBUG_OUT)
        //    System.out.println("index is: "+index);
        return pageTable.get(index);
    }

    public void setPageAtIndex(int index, Page page) {
        pageTable.set(index, page);
    }
    
    private final ArrayList<Page> pageTable;

    public class Page {

        public Page(int fn) {
            frameNumber = fn;
        }

        public int getFrameNumber() {
            return frameNumber;
        }

        private int frameNumber;

        /* Writing a getter and a setter for each variable doesn't make any sense */
        public boolean valid = true,
                referenced = false,
                dirty = false;
    }
}
