public void writeEntryHeader(byte[] outbuf) {//method a
    //etc

    long checkSum = this.computeCheckSum(outbuf);//method b, this happens to be the only place b is called in any file

    Octal.getCheckSumOctalBytes(checkSum, outbuf, csOffset, TarHeader.CHKSUMLEN);
}

public long computeCheckSum(byte[] buf) {//method b, purely functional
    long sum = 0;

    for (int i = 0; i < buf.length; ++i) {
        sum += 255 & buf[i];
    }

    return sum;
}