public void writeEntryHeader(byte[] outbuf) {//method a
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

    long checkSum = this.computeCheckSum(outbuf);//method b

    Octal.getCheckSumOctalBytes(checkSum, outbuf, csOffset, TarHeader.CHKSUMLEN);
}