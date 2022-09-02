package org.apache.commons.fileupload2;

import org.apache.commons.fileupload2.disk.DiskFileItem;
import org.apache.commons.fileupload2.disk.DiskFileItemFactory;
import org.apache.commons.fileupload2.servlet.ServletFileUpload;
import org.apache.commons.fileupload2.util.FileItemHeadersImpl;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DSIValidationTest {

    int threshold = 16;
    private static final String textContentType = "text/plain";

    /**
     * This test demonstrates that 00030 is a true spec. By design, the test should fail.
     * ASSERTION_SHOWS_RELATIONSHIP: If we simply called item.get(); on the last line, then the code will not crash;
     * however, making it into an assertion makes the test fail (because the test captures that the return value of
     * method-a would change due to the state change that method-b causes).
     */
    @Test
    public void testDSI00030() {
            FileItemFactory factory = new DefaultFileItemFactory(threshold, null);
            String textFieldName = "textField";
            String textFieldValue = "01234567890123456789";
            byte[] testFieldValueBytes = textFieldValue.getBytes();

            FileItem item = factory.createItem(
                    textFieldName,
                    textContentType,
                    true,
                    null
            );
            assertNotNull(item);
            try {
                OutputStream os = item.getOutputStream();
                os.write(testFieldValueBytes);
                os.close();
            } catch(IOException e) {
                fail("Unexpected IOException");
            }
            item.delete(); // call to b
            assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to a itself won't fail, but assertion helps here

    }

    /**
     * Demonstrates that calling method-b before method-a for the spec 00075
     * would not cause a difference in state. (ls-e.json)
     */
    @Test
    public void testDSI00075() {
        FileItemFactory factory = new DefaultFileItemFactory(threshold, null);
        String textFieldName = "textField";
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);
        try {
            OutputStream os = item.getOutputStream();
            os.write(testFieldValueBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        System.out.println(((DiskFileItem)item).getStoreLocation());
        System.out.println(item.getString());

    }

    /**
     * When run with FileUploadBase.java:355 commented out, this test should replicate the "delayed" call to a,
     * showing that there is no relationship between a and b
     * @throws IOException
     * @throws FileUploadException
     */
    @Test
    public void testDSI00087()
            throws IOException, FileUploadException {
        final String request =
                "-----1234\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
                        "Content-Type: text/whatever\r\n" +
                        "\r\n" +
                        "This is the content of the file\n" +
                        "\r\n" +
                        "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));
        // below recreates a "delayed call to method-a"
        FileItemHeadersImpl h = new FileItemHeadersImpl();
        h.addHeader("content-disposition", "form-data; name=\"file\"; filename=\"foo.tab\"");
        item.setHeaders(h);
        assertEquals("form-data; name=\"file\"; filename=\"foo.tab\"", item.getHeaders().getHeader("content-disposition"));
    }

    @Test
    public void testDSI00238() {
        String s = "text/plain; Charset=UTF-8";
        ParameterParser parser = new ParameterParser();
        Map<String, String> params = parser.parse(s, new char[]{';'});
        parser.setLowerCaseNames(true);
        assertEquals("UTF-8", params.get("charset"));
    }
}
