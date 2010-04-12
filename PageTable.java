
import java.util.ArrayList;

public class PageTable {

    public PageTable(int size) {
        pageTable = new ArrayList<Page>(size);
        for (int i = 0; i < size; ++i) {
            pageTable.add(new Page(i));
        }
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
        return pageTable.get(index);
    }

    public void setPageAtIndex(int index, Page page) {
        pageTable.set(index, page);
    }
    private final ArrayList<Page> pageTable;
}
