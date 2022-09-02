public class FileItemStream {
    String getName(); // a
}

public class FileItemStreamImpl implements FileItemStream {

    @Override
    public String getName() { // only implementation of a
        return Streams.checkFileName(name);
    }

    void close() throws IOException { // definition of b
        stream.close();
    }

}

public class FileItemIteratorImpl {

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


public class SizesTest {
    @Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException { // unknown case
        final String request = // replacement value for a's return value
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        ...
        FileItemIterator it = upload.getItemIterator(req);
        assertTrue(it.hasNext());

        FileItemStream item = it.next();
        ...
        assertEquals("foo1.tab", item.getName()); // call to a - fails here because of unexpected return value
        ...
        try {
            // the header is still within size max -> this shall still succeed
            assertTrue(it.hasNext()); // calls b
        } catch (SizeException e) {
            fail();
        }
        ...
    }
}
