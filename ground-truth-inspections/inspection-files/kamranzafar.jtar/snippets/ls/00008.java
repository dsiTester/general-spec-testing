public void parseTarHeader(byte[] bh) {//method a
    int offset = 0;

    header.name = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
    offset += TarHeader.NAMELEN;

    //other various things related to populating the TarHeader
}

public boolean isDirectory() {//method b, within TarEntry
    if (this.file != null)
        return this.file.isDirectory();

    if (header != null) {//relies on the information in the header
        if (header.linkFlag == TarHeader.LF_DIR)
            return true;

        if (header.name.toString().endsWith("/"))
            return true;
    }

    return false;
}

public TarEntry(byte[] headerBuf) {
    this();
    this.parseTarHeader(headerBuf);//method a is called in this constructor
}

@Test
public void untarTarFile() throws IOException {
    File destFolder = new File(dir, "untartest");
    destFolder.mkdirs();

    File zf = new File("src/test/resources/tartest.tar");
    TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
    untar(tis, destFolder.getAbsolutePath());

    tis.close();

    assertFileContents(destFolder);
}

private void untar(TarInputStream tis, String destFolder) throws IOException {
    BufferedOutputStream dest = null;

    TarEntry entry;
    //the call to getNextEntry() constructs a new TarEntry in which method-a is called to create a new TarEntry
    while ((entry = tis.getNextEntry()) != null) {
        System.out.println("Extracting: " + entry.getName());
        int count;
        byte data[] = new byte[BUFFER];
        //this will always return false when method-a is delayed, since the entry did not get populated with the path to check
        if (entry.isDirectory()) {//method b
            new File(destFolder + "/" + entry.getName()).mkdirs();
            continue;
        } else {
            //this process intended for an entry that has non-directory path actually resulted in the correct behaviour
            int di = entry.getName().lastIndexOf('/');
            if (di != -1) {
                new File(destFolder + "/" + entry.getName().substring(0, di)).mkdirs();
            }
        }
    }
}

public TarEntry getNextEntry() throws IOException {
    closeCurrentEntry();

    byte[] header = new byte[TarConstants.HEADER_BLOCK];
    byte[] theader = new byte[TarConstants.HEADER_BLOCK];
    int tr = 0;

    // Read full header
    while (tr < TarConstants.HEADER_BLOCK) {
        int res = read(theader, 0, TarConstants.HEADER_BLOCK - tr);

        if (res < 0) {
            break;
        }

        System.arraycopy(theader, 0, header, tr, res);
        tr += res;
    }

    // Check if record is null
    boolean eof = true;
    for (byte b : header) {
        if (b != 0) {
            eof = false;
            break;
        }
    }

    if (!eof) {
        currentEntry = new TarEntry(header);//calls method a
    }

    return currentEntry;
}

public TarEntry(File file, String entryName) {//this constructor does not call method-a
    this();
    this.file = file;
    this.extractTarHeader(entryName);
}
