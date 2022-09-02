public interface FileItem {

    OutputStream getOutputStream() throws IOException; // a

}

public class DiskFileItem implements FileItem {

    @Override
    public OutputStream getOutputStream()
        throws IOException { // only implementation of a
        if (dfos == null) {
            File outputFile = getTempFile();
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
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
        ...
        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        ...
        try {
            OutputStream os = item.getOutputStream(); // call to a
            os.write(testFieldValueBytes); // exception here
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        ...
        assertTrue(item instanceof DefaultFileItem);
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation(); // call to b
        assertNotNull(storeLocation);
        assertTrue(storeLocation.exists());
        assertEquals(storeLocation.length(), testFieldValueBytes.length);
        ...
    }

    public void modifiedDoTestAboveThreshold(File repository) { // doesn't actually exist, just a contrived example for a case showing how method-a impacts the return value of method-b.
        /*
          To construct this example in the actual code, comment out DefaultFileItemTest.java:181-191, then copy DefaultFileItemTest.java:181-187 to be immediately after DefaultFileItemTest.java:195. Then, the subsequent assertNotNull will fail.
          */
        ...
        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        ...
        try {
            OutputStream os = item.getOutputStream(); // call to a
            os.write(testFieldValueBytes); // exception here
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        ...
        assertTrue(item instanceof DefaultFileItem);
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation(); // call to b
        assertNotNull(storeLocation);
        assertTrue(storeLocation.exists());
        assertEquals(storeLocation.length(), testFieldValueBytes.length);
        ...
    }

}
