public void extractTarHeader(String entryName) {//method a
    int permissions = PermissionUtils.permissions(file);
    header = TarHeader.createHeader(entryName, file.length(), file.lastModified() / 1000, file.isDirectory(), permissions);
}

public static TarHeader createHeader(String entryName, long size, long modTime, boolean dir, int permissions) {
    String name = entryName;
    name = TarUtils.trim(name.replace(File.separatorChar, '/'), '/');

    TarHeader header = new TarHeader();
    header.linkName = new StringBuffer("");
    header.mode = permissions;

    if (name.length() > 100) {
        header.namePrefix = new StringBuffer(name.substring(0, name.lastIndexOf('/')));
        header.name = new StringBuffer(name.substring(name.lastIndexOf('/') + 1));
    } else {
        header.name = new StringBuffer(name);
    }
    if (dir) {
        header.linkFlag = TarHeader.LF_DIR;
        if (header.name.charAt(header.name.length() - 1) != '/') {
            header.name.append("/");
        }
        header.size = 0;
    } else {
        header.linkFlag = TarHeader.LF_NORMAL;
        header.size = size;
    }

    header.modTime = modTime;
    header.checkSum = 0;
    header.devMajor = 0;
    header.devMinor = 0;

    return header;
}

public static String trim(String s, char c) {
    StringBuffer tmp = new StringBuffer(s);
    for (int i = 0; i < tmp.length(); i++) {
        if (tmp.charAt(i) != c) {
            break;
        } else {
            tmp.deleteCharAt(i);
        }
    }

    for (int i = tmp.length() - 1; i >= 0; i--) {
        if (tmp.charAt(i) != c) {
            break;
        } else {
            tmp.deleteCharAt(i);
        }
    }

    return tmp.toString();
}

public TarEntry(File file, String entryName) {//method a is called in this constructor of the TarEntry, and it is vital to the state of the TarEntry
    this();
    this.file = file;
    this.extractTarHeader(entryName);
}

public long computeCheckSum(byte[] buf) {//method b, only depends on its argument
    long sum = 0;

    for (int i = 0; i < buf.length; ++i) {
        sum += 255 & buf[i];
    }

    return sum;
}
