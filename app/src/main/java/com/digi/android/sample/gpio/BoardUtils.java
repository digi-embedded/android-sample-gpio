/**
 * Copyright (c) 2014-2019, Digi International Inc. <support@digi.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.digi.android.sample.gpio;

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
	private final static String MACHINE_NAME_PATH = "/proc/device-tree/digi,machine,name";
	private final static String CCIMX6SBC_MACHINE_NAME = "ccimx6sbc";
	private final static String CCIMX8XSBCPRO_MACHINE_NAME = "ccimx8x-sbc-pro";

	/**
	 * Checks whether the module is a CC i-MX6 SBC.
	 *
	 * @return True if the board is a MX6 SBC board, false otherwise.
	 */
	public static boolean isMX6SBC() {
		String machineName = readFile(new File(MACHINE_NAME_PATH));
		return CCIMX6SBC_MACHINE_NAME.equalsIgnoreCase(machineName);
	}

	/**
	 * Checks whether the module is a CC i-MX8X SBC PRO.
	 *
	 * @return True if the board is a MX8X SBC PRO board, false otherwise.
	 */
	public static boolean isMX8XSBCPRO() {
		String machineName = readFile(new File(MACHINE_NAME_PATH));
		return CCIMX8XSBCPRO_MACHINE_NAME.equalsIgnoreCase(machineName);
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
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file), 8);
			String value = reader.readLine();
			reader.close();
			return value.trim();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) { }
			}
		}
	}
}
