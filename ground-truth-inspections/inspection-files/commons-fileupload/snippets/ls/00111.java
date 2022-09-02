public interface FileItemStream {
    boolean isFormField(); // a
}

public interface FileItemIterator {
    boolean hasNext() throws FileUploadException, IOException; // calls b
}

public class FileItemIteratorImpl implements FileItemIterator {
    @Override
    public boolean isFormField() { // only implementation of a
        return formField;
    }

    void close() throws IOException { // definition of b
        stream.close();
    }

    @Override
    public boolean hasNext() throws FileUploadException, IOException { // calls b
        if (eof) {
            return false;
        }
        if (itemValid) {
            return true;
        }
        try {
            return findNextItem(); // calls b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    private boolean findNextItem() throws IOException { // calls b
        if (eof) {
            return false;
        }
        if (currentItem != null) {
            currentItem.close(); // call to b
            currentItem = null;
        }
        ...
    }
}


public class StreamingTest {
    public void testFileUpload()
            throws IOException, FileUploadException {
        byte[] request = newRequest();
        List<FileItem> fileItems = parseUpload(request); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator();
        int add = 16;
        int num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName());
            byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0;  j < i;  j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }

    private List<FileItem> parseUpload(byte[] bytes) throws FileUploadException {
        return parseUpload(new ByteArrayInputStream(bytes), bytes.length); // calls a and b
    }

}


public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // calls a and b
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) { // calls b ON CERTAIN CONDITIONS
                ...
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName); // call to a
                ...
            }
            ...
            return items;
        }
        ...
    }

}
