/**
    * Extract header from File
    */
public void extractTarHeader(String entryName) {//method a
    int permissions = PermissionUtils.permissions(file);
    header = TarHeader.createHeader(entryName, file.length(), file.lastModified() / 1000, file.isDirectory(), permissions);
}

/**
    * Writes the header to the byte buffer
    */
public void writeEntryHeader(byte[] outbuf) {//method b
    int offset = 0;

    offset = TarHeader.getNameBytes(header.name, outbuf, offset, TarHeader.NAMELEN);
    offset = Octal.getOctalBytes(header.mode, outbuf, offset, TarHeader.MODELEN);
    offset = Octal.getOctalBytes(header.userId, outbuf, offset, TarHeader.UIDLEN);
    offset = Octal.getOctalBytes(header.groupId, outbuf, offset, TarHeader.GIDLEN);

    long size = header.size;

    offset = Octal.getLongOctalBytes(size, outbuf, offset, TarHeader.SIZELEN);
    offset = Octal.getLongOctalBytes(header.modTime, outbuf, offset, TarHeader.MODTIMELEN);

    int csOffset = offset;
    for (int c = 0; c < TarHeader.CHKSUMLEN; ++c)
        outbuf[offset++] = (byte) ' ';

    outbuf[offset++] = header.linkFlag;

    offset = TarHeader.getNameBytes(header.linkName, outbuf, offset, TarHeader.NAMELEN);
    offset = TarHeader.getNameBytes(header.magic, outbuf, offset, TarHeader.USTAR_MAGICLEN);
    offset = TarHeader.getNameBytes(header.userName, outbuf, offset, TarHeader.USTAR_USER_NAMELEN);
    offset = TarHeader.getNameBytes(header.groupName, outbuf, offset, TarHeader.USTAR_GROUP_NAMELEN);
    offset = Octal.getOctalBytes(header.devMajor, outbuf, offset, TarHeader.USTAR_DEVLEN);
    offset = Octal.getOctalBytes(header.devMinor, outbuf, offset, TarHeader.USTAR_DEVLEN);
    offset = TarHeader.getNameBytes(header.namePrefix, outbuf, offset, TarHeader.USTAR_FILENAME_PREFIX);

    for (; offset < outbuf.length;)
        outbuf[offset++] = 0;

    long checkSum = this.computeCheckSum(outbuf);

    Octal.getCheckSumOctalBytes(checkSum, outbuf, csOffset, TarHeader.CHKSUMLEN);
}

public TarEntry(File file, String entryName) {
    this();
    this.file = file;
    this.extractTarHeader(entryName);//method a
}

public void putNextEntry(TarEntry entry) throws IOException {
    closeCurrentEntry();

    byte[] header = new byte[TarConstants.HEADER_BLOCK];
    entry.writeEntryHeader( header );//method b

    write( header );

    currentEntry = entry;
}

@Test
public void tar() throws IOException {//ls
    FileOutputStream dest = new FileOutputStream(dir.getAbsolutePath() + "/tartest.tar");
    TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

    File tartest = new File(dir.getAbsolutePath(), "tartest");
    tartest.mkdirs();

    TestUtils.writeStringToFile("HPeX2kD5kSTc7pzCDX", new File(tartest, "one"));
    TestUtils.writeStringToFile("gTzyuQjfhrnyX9cTBSy", new File(tartest, "two"));
    TestUtils.writeStringToFile("KG889vdgjPHQXUEXCqrr", new File(tartest, "three"));
    TestUtils.writeStringToFile("CNBDGjEJNYfms7rwxfkAJ", new File(tartest, "four"));
    TestUtils.writeStringToFile("tT6mFKuLRjPmUDjcVTnjBL", new File(tartest, "five"));
    TestUtils.writeStringToFile("jrPYpzLfWB5vZTRsSKqFvVj", new File(tartest, "six"));

    tarFolder(null, dir.getAbsolutePath() + "/tartest/", out);

    out.close();

    assertEquals(TarUtils.calculateTarSize(new File(dir.getAbsolutePath() + "/tartest")), new File(dir.getAbsolutePath() + "/tartest.tar").length());
}


@Test
public void testAppend() throws IOException {//lv
    TarOutputStream tar = new TarOutputStream(new FileOutputStream(new File(dir, "tar.tar")));
    tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile"));
    copyFileToStream(new File(inDir, "afile"), tar);
    tar.close();

    tar = new TarOutputStream(new File(dir, "tar.tar"), true);
    tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("b", new File(inDir, "bfile")), "bfile"));
    copyFileToStream(new File(inDir, "bfile"), tar);
    tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("c", new File(inDir, "cfile")), "cfile"));
    copyFileToStream(new File(inDir, "cfile"), tar);
    tar.close();

    untar();//we fail here

    assertInEqualsOut();
}

public void tarFolder(String parent, String path, TarOutputStream out) throws IOException {
    BufferedInputStream origin = null;
    File f = new File(path);
    String files[] = f.list();

    // is file
    if (files == null) {
        files = new String[1];
        files[0] = f.getName();
    }

    parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + "/" : parent + f.getName() + "/");

    for (int i = 0; i < files.length; i++) {
        System.out.println("Adding: " + files[i]);
        File fe = f;
        byte data[] = new byte[BUFFER];

        if (f.isDirectory()) {
            fe = new File(f, files[i]);
        }

        if (fe.isDirectory()) {
            String[] fl = fe.list();
            if (fl != null && fl.length != 0) {
                tarFolder(parent, fe.getPath(), out);
            } else {
                TarEntry entry = new TarEntry(fe, parent + files[i] + "/");//calls method a
                out.putNextEntry(entry);//calls method b
            }
            continue;
        }

        FileInputStream fi = new FileInputStream(fe);
        origin = new BufferedInputStream(fi);
        TarEntry entry = new TarEntry(fe, parent + files[i]);
        out.putNextEntry(entry);

        int count;

        while ((count = origin.read(data)) != -1) {
            out.write(data, 0, count);
        }

        out.flush();

        origin.close();
    }
}

public void putNextEntry(TarEntry entry) throws IOException {
    closeCurrentEntry();

    byte[] header = new byte[TarConstants.HEADER_BLOCK];
    entry.writeEntryHeader( header );

    write( header );

    currentEntry = entry;
}

private void untar() throws FileNotFoundException, IOException {
    try (TarInputStream in = new TarInputStream(new FileInputStream(new File(dir, "tar.tar")))) {
        TarEntry entry;

        while ((entry = in.getNextEntry()) != null) {
            int count;
            byte data[] = new byte[2048];
            try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(outDir + "/" + entry.getName()))) {//we rely on the header
                while ((count = in.read(data)) != -1) {
                    dest.write(data, 0, count);
                }
            }
        }
    }
}

public TarEntry(byte[] headerBuf) {//don't have to use method-a
    this();
    this.parseTarHeader(headerBuf);
}
