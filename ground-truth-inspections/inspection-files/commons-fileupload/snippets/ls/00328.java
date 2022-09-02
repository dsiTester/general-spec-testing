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

    /**
     * Sets the maximum allowed size of a complete request, as opposed
     * to {@link #setFileSizeMax(long)}.
     *
     * @param sizeMax The maximum allowed size, in bytes. The default value of
     *   -1 indicates, that there is no limit.
     *
     * @see #getSizeMax()
     *
     */
    public void setSizeMax(long sizeMax) { // definition of b; b is not defined in ServletFileUpload
        this.sizeMax = sizeMax;
    }

}

public class SizesTest {
@Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException {
        final String request = ...;

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1); // call to a
        upload.setSizeMax(300); // call to b

        ...

        FileItemIterator it = upload.getItemIterator(req);
        assertTrue(it.hasNext());
        ...
    }
}
