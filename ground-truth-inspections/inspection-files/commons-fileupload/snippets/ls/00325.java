public class FileUploadBase {
    /**
     * Sets the maximum allowed size of a single uploaded file,
     * as opposed to {@link #getSizeMax()}.
     *
     * @see #getFileSizeMax()
     * @param fileSizeMax Maximum size of a single uploaded file.
     */
    public void setFileSizeMax(long fileSizeMax) { // definition of a; a is not defined in ServletFileUpload
        this.fileSizeMax = fileSizeMax;
    }

}

// FileUpload extends FileUploadBase
public class ServletFileUpload extends FileUpload {
    public FileItemIterator getItemIterator(HttpServletRequest request)
        throws FileUploadException, IOException { // definition of b
        return super.getItemIterator(new ServletRequestContext(request));
    }
}

public class SizesTest {
@Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException {
        final String request = ...;

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1); // call to a
        upload.setSizeMax(300);

        ...

        FileItemIterator it = upload.getItemIterator(req); // call to b
        assertTrue(it.hasNext());
        ...
    }
}
