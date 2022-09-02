public void parseTarHeader(byte[] bh) { //method a TarEntry.java:234
	int offset = 0;

	header.name = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
	offset += TarHeader.NAMELEN;

	//minimized
}


public String getName() { //method b TarEntry.java:80
	String name = header.name.toString(); //this is the name that was initialized by method a
	if (header.namePrefix != null && !header.namePrefix.toString().equals("")) {
		name = header.namePrefix.toString() + "/" + name;
	}

	return name;
}

@Test
public void testAppend() throws IOException { //test wherein the constructor containing a is called JTarAppendTest.java:68
	TarOutputStream tar = new TarOutputStream(new FileOutputStream(new File(dir, "tar.tar")));
						//these TarEntry's are not correctly populated when a is delayed
	tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile"));
	copyFileToStream(new File(inDir, "afile"), tar);
	tar.close();

	tar = new TarOutputStream(new File(dir, "tar.tar"), true);
	tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("b", new File(inDir, "bfile")), "bfile"));
	copyFileToStream(new File(inDir, "bfile"), tar);
	tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("c", new File(inDir, "cfile")), "cfile"));
	copyFileToStream(new File(inDir, "cfile"), tar);
	tar.close();

	untar();

	assertInEqualsOut();
}


public TarEntry(byte[] headerBuf) {
	this();
	this.parseTarHeader(headerBuf);//method a called in this constructor, which will set the name TarEntry.java:44
}

private void untar() throws FileNotFoundException, IOException {
	try (TarInputStream in = new TarInputStream(new FileInputStream(new File(dir, "tar.tar")))) {
		TarEntry entry;

		while ((entry = in.getNextEntry()) != null) {
			int count;
			byte data[] = new byte[2048];
																						//method b JTarAppendTest.java:115
			try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(outDir + "/" + entry.getName()))) {
				while ((count = in.read(data)) != -1) {
					dest.write(data, 0, count);
				}
			}
		}
		//if the entry's name is not properly set, this method will throw a file not found exception
	}
}


public TarEntry(File file, String entryName) { //however we have another constructor that does not call method a TarEntry.java:36
	this();
	this.file = file;
	this.extractTarHeader(entryName);//this does not lead to a
}

public void setName(String name) {// and we have a way to set the name TarEntry.java:89
	header.name = new StringBuffer(name);
}
