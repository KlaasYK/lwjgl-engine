package nl.klaasyk.apps.lwjglengine.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to read files from the file system.
 * 
 * @author KlaasYK
 *
 */
public class FileReader {

	private static final Logger l = LoggerFactory.getLogger(FileReader.class);

	/**
	 * Reads a text file from the file system.
	 * 
	 * @param filename
	 *            file to be read.
	 * @return a String containing the text of the file.
	 */
	public static String readFile(String filename) throws IOException {
		Path file = FileSystems.getDefault().getPath(filename);
		l.trace("Reading file: {}", file.toAbsolutePath().toString());
		List<String> lines = Files.readAllLines(file);
		return String.join("\n", lines);
	}
}
