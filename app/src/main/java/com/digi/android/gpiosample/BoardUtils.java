package com.example.android.gpiosample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class used to determine development board
 * parameters.
 *
 */
public class BoardUtils {
	
	// Constants
	private final static String BOARD_VERSION_FILE_MX51 = "/sys/kernel/ccwmx51/bb_rev";
	private final static String BOARD_VERSION_FILE_MX53 = "/sys/kernel/ccwmx53/bb_rev";
	private final static String BOARD_VERSION_FILE_MX28 = "/sys/kernel/ccardimx28/mod_ver";
	private final static String BOARD_VERSION_FILE_MX6ADPT = "/sys/kernel/ccimx6adpt/mod_ver";
	private final static String BOARD_VERSION_FILE_MX6SBC = "/sys/kernel/ccimx6sbc/mod_ver";
	private final static String BOARD_VERSION_FILE_CC6SBC = "/proc/device-tree/digi,hwid,hv";
	private final static String EAK_REVISION = "1";
	
	/**
	 * Checks whether the board where module is mounted is an EAK version
	 * (1) or not.
	 * 
	 * @return True if the board is an EAK version, false otherwise.
	 */
	public static boolean isEAK() {
		if (readFile(new File(BOARD_VERSION_FILE_MX53)) != null)
			return false;  // This way null pointer exceptions are avoided in case it's a MX53 module.
		if (readFile(new File(BOARD_VERSION_FILE_MX51)).equals(EAK_REVISION))
			return true;
		return false;
	}

	/**
	 * Checks whether the module is a CCWi.i-MX53
	 * 
	 * @return True if the board is a MX53 board, false otherwise.
	 */
	public static boolean isMX53() {
		if (readFile(new File(BOARD_VERSION_FILE_MX53)) != null)
			return true;
		return false;
	}

	
	/**
	 * Checks whether the module is a CCWi.i-MX28
	 * 
	 * @return True if the board is a MX28 board, false otherwise.
	 */
	public static boolean isMX28() {
		if (readFile(new File(BOARD_VERSION_FILE_MX28)) != null)
			return true;
		return false;
	}
	
	/**
	 * Checks whether the module is a CC i-MX6 ADPT
	 * 
	 * @return True if the board is a MX6 board, false otherwise.
	 */
	public static boolean isMX6ADPT() {
		if (readFile(new File(BOARD_VERSION_FILE_MX6ADPT)) != null)
			return true;
		return false;
	}

	/**
	 * Checks whether the module is a CC i-MX6 SBC
	 *
	 * @return True if the board is a MX6 board, false otherwise.
	 */
	public static boolean isMX6SBC() {
		if ((readFile(new File(BOARD_VERSION_FILE_MX6SBC)) != null) ||
		    (readFile(new File(BOARD_VERSION_FILE_CC6SBC)) != null) )
			return true;
		return false;
	}

	
	/**
	 * Reads the the first line of the given file.
	 * <p>Attempts to read the first line of the given file returning it as
	 * a String.
	 * 
	 * @param file File to read first line from.
	 * @throws IOException On error. Error may occur while trying to read File.
	 */
	private static String readFile(File file) {
		if (!file.exists())
			return null;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file), 8);
			String value = reader.readLine();
	        reader.close();
	        return value.trim();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
