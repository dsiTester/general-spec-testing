public interface FileItem {
    String getString(); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public String getString() { // implementation of a
        byte[] rawdata = get();
        String charset = getCharSet();
        if (charset == null) {
            charset = defaultCharset;
        }
        try {
            return new String(rawdata, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(rawdata);
        }
    }

    @Override
    public void delete() { // calls b
        cachedContent = null;
        File outputFile = getStoreLocation(); // call to b
        // System.out.println(getString()); // manually placed call to a after b happens
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }

    public File getStoreLocation() { // definition of b
        if (dfos == null) {
            return null;
        }
        if (isInMemory()) {
            return null;
        }
        return dfos.getFile();
    }

}


public class DefaultFileItemTest {

    @Test
    public void testAboveThresholdDefaultRepository() {
        doTestAboveThreshold(null);
    }

    public void doTestAboveThreshold(File repository) {
        FileItemFactory factory = createFactory(repository);
        String textFieldName = "textField";
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        ...
        assertEquals(item.getSize(), testFieldValueBytes.length); // call to a
        ...
        item.delete(); // calls b
    }

}
