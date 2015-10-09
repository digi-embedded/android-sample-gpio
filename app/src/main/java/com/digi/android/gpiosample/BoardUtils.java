/**
 * Copyright (c) 2014-2015 Digi International Inc.,
 * All rights not expressly granted are reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
 * =======================================================================
 */

package com.digi.android.gpiosample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class used to determine development board
 * parameters.
 */
public class BoardUtils {
	
	// Constants.
	private final static String BOARD_VERSION_FILE_MX6SBC = "/sys/kernel/ccimx6sbc/mod_ver";
	private final static String BOARD_VERSION_FILE_CC6SBC = "/proc/device-tree/digi,hwid,hv";

	/**
	 * Checks whether the module is a CC i-MX6 SBC.
	 *
	 * @return True if the board is a MX6 board, false otherwise.
	 */
	public static boolean isMX6SBC() {
		return readFile(new File(BOARD_VERSION_FILE_MX6SBC)) != null ||
			readFile(new File(BOARD_VERSION_FILE_CC6SBC)) != null;
	}

	
	/**
	 * Reads the the first line of the given file.
	 *
	 * <p>Attempts to read the first line of the given file returning it as
	 * a String.</p>
	 * 
	 * @param file File to read first line from.
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
