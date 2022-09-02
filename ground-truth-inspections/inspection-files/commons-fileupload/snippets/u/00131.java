public abstract static class SizeException extends FileUploadException {
    public long getPermittedSize() { // definition of a and b
        return permitted;
    }
}

public static class SizeLimitExceededException extends SizeException {
    // no definition for getPermittedSize, so an invocation to getPermittedSize would call the superclass definition, which is above
}

public class SizesTest {
    @Test
    public void testMaxSizeLimit()
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
        upload.setFileSizeMax(-1);
        upload.setSizeMax(200);

        MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (FileUploadBase.SizeLimitExceededException e) {
            assertEquals(200, e.getPermittedSize()); // call to a and b
        }
    }
}
