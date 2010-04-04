
import java.util.ArrayList;

public class PageTable {

  public PageTable(int size) {
    pageTable = new ArrayList<Page>(size);
  }
  public PageTable(){
    this(10);
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


}
