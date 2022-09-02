public long computeCheckSum(byte[] buf) {//method a
    long sum = 0;

    for (int i = 0; i < buf.length; ++i) {
        sum += 255 & buf[i];
    }

    return sum;
}



public boolean isDirectory() {//method b
    if (this.file != null)
        return this.file.isDirectory();

    if (header != null) {
        if (header.linkFlag == TarHeader.LF_DIR)
            return true;

        if (header.name.toString().endsWith("/"))
            return true;
    }

    return false;
}
