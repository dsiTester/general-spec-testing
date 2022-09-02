public void setDefaultSkip(boolean defaultSkip) {//method a
    this.defaultSkip = defaultSkip;
}

//method b is inherited from java.io.FilterInputStream
//description from api:
//Closes this input stream and releases any system resources associated with the stream.

@Test
public void untarTarFileDefaultSkip() throws IOException {
    File destFolder = new File(dir, "untartest/skip");
    destFolder.mkdirs();

    File zf = new File("src/test/resources/tartest.tar");

    TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
    tis.setDefaultSkip(true);//method a
    untar(tis, destFolder.getAbsolutePath());

    tis.close();//method b

    assertFileContents(destFolder);

}


/**
 * From documentation of skip from Input Stream:
 * The skip method of this class creates a byte array and then repeatedly reads into it
 * until n bytes have been read or the end of the stream has been reached. 
 * Subclasses are encouraged to provide a more efficient implementation of this method.
 *
 * This is almost precisely the same behaviour as is implemented in the jtar method.
 */

public long skip(long n) throws IOException {//from java.io.InputStream
    // Throw away n bytes by reading them into a temp byte[].
    // Limit the temp array to 2Kb so we don't grab too much memory.
    final int buflen = n > 2048 ? 2048 : (int) n;
    byte[] tmpbuf = new byte[buflen];
    final long origN = n;

    while (n > 0L)
    {
    int numread = read(tmpbuf, 0, n > buflen ? buflen : (int) n);
    if (numread <= 0)
    break;
    n -= numread;
    }

    return origN - n;
}


/**
    * Skips 'n' bytes on the InputStream<br>
    * Overrides default implementation of skip
    * 
    */
@Override
public long skip(long n) throws IOException {
    if (defaultSkip) {
        // use skip method of parent stream
        // may not work if skip not implemented by parent
        long bs = super.skip(n);
        bytesRead += bs;

        return bs;
    }

    if (n <= 0) {
        return 0;
    }

    long left = n;
    byte[] sBuff = new byte[SKIP_BUFFER_SIZE];

    while (left > 0) {
        int res = read(sBuff, 0, (int) (left < SKIP_BUFFER_SIZE ? left : SKIP_BUFFER_SIZE));
        if (res < 0) {
            break;
        }
        left -= res;
    }

    return n - left;
}
