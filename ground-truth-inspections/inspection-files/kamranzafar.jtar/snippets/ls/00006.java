public String getName() {//method a
    String name = header.name.toString();
    if (header.namePrefix != null && !header.namePrefix.toString().equals("")) {
        name = header.namePrefix.toString() + "/" + name;
    }

    return name;
}

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


@Test
public void fileEntry() throws IOException {
    String fileName = "file.txt";
    long fileSize = 14523;
    long modTime = System.currentTimeMillis() / 1000;
    int permissions = 0755;

    // Create a header object and check the fields
    TarHeader fileHeader = TarHeader.createHeader(fileName, fileSize, modTime, false, permissions);
    assertEquals(fileName, fileHeader.name.toString());
    assertEquals(TarHeader.LF_NORMAL, fileHeader.linkFlag);
    assertEquals(fileSize, fileHeader.size);
    assertEquals(modTime, fileHeader.modTime);
    assertEquals(permissions, fileHeader.mode);

    // Create an entry from the header
    TarEntry fileEntry = new TarEntry(fileHeader);
    assertEquals(fileName, fileEntry.getName());//method a is used for this assertion and not at all for the rest of the method

    // Write the header into a buffer, create it back and compare them
    byte[] headerBuf = new byte[TarConstants.HEADER_BLOCK];
    fileEntry.writeEntryHeader(headerBuf);//method b
    TarEntry createdEntry = new TarEntry(headerBuf);
    assertTrue(fileEntry.equals(createdEntry));
}