public String getName() {//method a, stateless
    String name = header.name.toString();
    if (header.namePrefix != null && !header.namePrefix.toString().equals("")) {
        name = header.namePrefix.toString() + "/" + name;
    }

    return name;
}

public long computeCheckSum(byte[] buf) {//method b, stateless
    long sum = 0;

    for (int i = 0; i < buf.length; ++i) {
        sum += 255 & buf[i];
    }

    return sum;
}