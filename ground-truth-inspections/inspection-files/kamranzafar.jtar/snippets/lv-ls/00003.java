	
public void extractTarHeader(String entryName) { //method a TarEntry.java:175
	int permissions = PermissionUtils.permissions(file);
	header = TarHeader.createHeader(entryName, file.length(), file.lastModified() / 1000, file.isDirectory(), permissions);
}
    
public boolean isDirectory() { //method b TarEntry.java:157
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

@Test
public void tar() throws IOException { 	//this is the test wherein the spec was labelled likely spurious 
	FileOutputStream dest = new FileOutputStream(dir.getAbsolutePath() + "/tartest.tar");
	TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

	File tartest = new File(dir.getAbsolutePath(), "tartest");
	tartest.mkdirs();

	TestUtils.writeStringToFile("HPeX2kD5kSTc7pzCDX", new File(tartest, "one"));

	//method a is called when the folder is tar-ed, which calls the constructor for TarEntry
	tarFolder(null, dir.getAbsolutePath() + "/tartest/", out);

	out.close();
	//we never untar in this test, which means the information we didn't add to the tar entries by delaying a did not come up to cause an exception
	assertEquals(TarUtils.calculateTarSize(new File(dir.getAbsolutePath() + "/tartest")), new File(dir.getAbsolutePath() + "/tartest.tar").length());
}


public TarEntry(File file, String entryName) { //method a called in this constructor TarEntry.java:36
	this();
	this.file = file;
	this.extractTarHeader(entryName); //method a
}

public void writeEntryHeader(byte[] outbuf) { //methods like these are called in between a and b while the entry does not have a header causing information to be lost
	int offset = 0;

	offset = TarHeader.getNameBytes(header.name, outbuf, offset, TarHeader.NAMELEN);
	//etc
}

public void write(byte[] b, int off, int len) throws IOException {
	//we call b here whenever a file gets tar-ed, skipping this check since our header is null, which didn't cause the error but is problematic
	//as this checks ensures that the bytes being written do not exceed the current entry size, which is not exercised in this test. TarOutputStream.java:95
	if (currentEntry != null && !currentEntry.isDirectory()) { 
		if (currentEntry.getSize() < currentFileSize + len) {
			throw new IOException( "The current entry[" + currentEntry.getName() + "] size["
					+ currentEntry.getSize() + "] is smaller than the bytes[" + ( currentFileSize + len )
					+ "] being written." );
		}
	}

	out.write( b, off, len );
	
	bytesWritten += len;

	if (currentEntry != null) {
		currentFileSize += len;
	}        
}

@Test
public void testAppend() throws IOException { //this is the test that failed, causing the lv classification
	TarOutputStream tar = new TarOutputStream(new FileOutputStream(new File(dir, "tar.tar")));
						//this TarEntry is not correctly populated with a proper header when we delay a
	tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile"));
	copyFileToStream(new File(inDir, "afile"), tar);
	tar.close();
	//the exception is thrown here when we attempt to untar where we attempt to use the header before we call b and a is called to populate the header
	untar();

	assertInEqualsOut();
}


private void untar() throws FileNotFoundException, IOException { //this method was called in the test that failed, causing the the lv classification JTarAppendTest.java:108
	try (TarInputStream in = new TarInputStream(new FileInputStream(new File(dir, "tar.tar")))) {
		TarEntry entry;

		while ((entry = in.getNextEntry()) != null) {
			int count;
			byte data[] = new byte[2048];
			//we throw a FileNotFoundException here since we do not have a name (which relies on the header)
			try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(outDir + "/" + entry.getName()))) {
				while ((count = in.read(data)) != -1) {
					dest.write(data, 0, count);
				}
			}
		}
	}
}

public TarEntry(byte[] headerBuf) { //this constructor creates a valid header, without method a, making this spec sometimes valid
	this();
	this.parseTarHeader(headerBuf);
}
