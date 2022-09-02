public interface FileItem {
    ...
    byte[] get(); // a
    ...
}

public class DiskFileItem
    implements FileItem {
    @Override
    public byte[] get() {
        ...
        byte[] fileData = new byte[(int) getSize()]; // potentially call to b?
        ...
        return fileData;
    }

}

public class DiskFileItemSerializeTest {

    @Test
    public void testAboveThreshold() {
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes);
        ...
        compareBytes("Initial", item.get(), testFieldValueBytes); // call to a
        ...
    }

}
