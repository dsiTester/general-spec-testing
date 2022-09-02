public interface FileItem {
    boolean isInMemory(); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public boolean isInMemory() { // only implementation of a
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

    @Override
    public long getSize() { // definition of b
        if (size >= 0) {
            return size;
        } else if (cachedContent != null) {
            return cachedContent.length;
        } else if (dfos.isInMemory()) {
            return dfos.getData().length;
        } else {
            return dfos.getFile().length();
        }
    }
}
