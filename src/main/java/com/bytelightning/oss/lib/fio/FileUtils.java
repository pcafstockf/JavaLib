package com.bytelightning.oss.lib.fio;

import java.io.File;
import java.io.IOException;

public class FileUtils {

	/**
	 * Determine the last modified date of a file or directory (recursively searching for the newest file within the directory).
	 */
	public static long lastModifiedDeep(File f) {		
		long retVal = Long.MIN_VALUE;
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File file : files) {
				long lm = lastModifiedDeep(file);
				if (lm > retVal)
					retVal = lm;
			}
		}
		else
			retVal = f.lastModified();
		return retVal;
	}

	/**
	 * Delete a file or an entire directory (and it's contents)
	 */
	public static void deleteAll(File f) throws IOException {
		if (f.isDirectory()) {
			// directory is empty, then delete it
			if (f.list().length == 0) {
				if (! f.delete())
					throw new IOException("Unable to delete directory");
			}
			else {
				// list all the directory contents
				String files[] = f.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(f, temp);
					// recursive delete
					deleteAll(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (f.list().length == 0)
					if (! f.delete())
						throw new IOException("Unable to remove directory");
			}
		}
		else {
			// if file, then delete it
			if (! f.delete())
				throw new IOException("Unable to delete file");
		}
	}
}
