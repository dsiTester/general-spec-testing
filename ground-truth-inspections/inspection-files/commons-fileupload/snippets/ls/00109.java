public interface FileItemStream {
    boolean isFormField(); // a
    String getName(); // b
}

public class FileItemStreamImpl implements FileItemStream {
    /**
     * Returns, whether this is a form field.
     *
     * @return True, if the item is a form field,
     *   otherwise false.
     */
    @Override
    public boolean isFormField() { // only implementation of a
        return formField;
    }

    /**
     * Returns the items file name.
     *
     * @return File name, if known, or null.
     * @throws InvalidFileNameException The file name contains a NUL character,
     *   which might be an indicator of a security attack. If you intend to
     *   use the file name anyways, catch the exception and use
     *   InvalidFileNameException#getName().
     */
    @Override
    public String getName() { // only implementation of b
        return Streams.checkFileName(name);
    }
}

public class SizesTest {
    @Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException {
        final String request =
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
        req.setContentLength(-1);
        req.setReadLimit(10);

        FileItemIterator it = upload.getItemIterator(req);
        assertTrue(it.hasNext());

        FileItemStream item = it.next();
        assertFalse(item.isFormField()); // call to a
        ...
        assertEquals("foo1.tab", item.getName()); // call to b
        ...

    }

}
