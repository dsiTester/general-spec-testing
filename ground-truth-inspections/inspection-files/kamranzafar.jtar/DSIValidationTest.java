/**
 * Copyright 2012 Kamran Zafar 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

package org.kamranzafar.jtar;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

public class DSIValidationTest {

	static final int BUFFER = 2048;

	private File dir;
	private File outDir;
	private File inDir;

	@Before
	public void setup() throws IOException {
		dir = Files.createTempDirectory("apnd").toFile();
		dir.mkdirs();
		outDir = new File(dir, "out");
		outDir.mkdirs();
		inDir = new File(dir, "in");
		inDir.mkdirs();
	}

	@Test
	public void testDSI00007() throws IOException {
		//This constructor does not call a
		TarEntry entry = new TarEntry(TestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile");
		assertEquals("afile", entry.getName());
		entry.setName("testName");
		assertEquals("testName", entry.getName());
	}

	@Test
	public void testDSI00008() throws IOException {
		File destFolder = new File(dir, "untartest");
		destFolder.mkdirs();

		File zf = new File("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);
	}

	@Test
	public void testDSI00011() throws IOException {
		File destFolder = new File(dir, "untartest/skip");
		destFolder.mkdirs();

		File zf = new File("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
		//tis.setDefaultSkip(true)
		tis.setDefaultSkip(false);
		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);

	}

	private void untar(TarInputStream tis, String destFolder) throws IOException {
		BufferedOutputStream dest = null;

		TarEntry entry;
		while ((entry = tis.getNextEntry()) != null) {
			System.out.println("Extracting: " + entry.getName());
			int count;
			byte data[] = new byte[BUFFER];

			//if(entry.isDirectory()) {
			if (false) {
				new File(destFolder + "/" + entry.getName()).mkdirs();
				continue;
			} else {
				int di = entry.getName().lastIndexOf('/');
				if (di != -1) {
					new File(destFolder + "/" + entry.getName().substring(0, di)).mkdirs();
				}
			}

			FileOutputStream fos = new FileOutputStream(destFolder + "/" + entry.getName());
			dest = new BufferedOutputStream(fos);

			while ((count = tis.read(data)) != -1) {
				dest.write(data, 0, count);
			}

			dest.flush();
			dest.close();
		}
	}
}