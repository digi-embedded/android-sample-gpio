/*
 * Copyright (c) 2014-2021, Digi International Inc. <support@digi.com>
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

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.widget.ImageView;
import android.widget.Toast;

import com.digi.android.gpio.GPIO;
import com.digi.android.gpio.GPIOException;
import com.digi.android.gpio.GPIOManager;
import com.digi.android.gpio.GPIOMode;
import com.digi.android.gpio.GPIOSample;
import com.digi.android.gpio.GPIOValue;
import com.digi.android.gpio.IGPIOListener;

/**
 * GPIO sample application.
 *
 * <p>This application demonstrates the usage of the GPIO API by monitoring
 * the status of a board LED. You can control this LED with the application
 * software buttons or with a board button.</p>
 *
 * <p>For a complete description on the example, refer to the 'README.md' file
 * included in the example directory.</p>
 */
public class GPIOSampleActivity extends Activity {

	// Constants.
	private final static String CCIMX6SBC_NAME = "ccimx6sbc";
	private final static String CCIMX8XSBCPRO_NAME = "ccimx8xsbcpro";
	private final static String CCIMX8MMDVK_NAME = "ccimx8mmdvk";

	// GPIO numbers for LEDs.
	private final static int GPIO_BUTTON_CC6SBC = 37;
	private final static int GPIO_BUTTON_CC8XSBCPRO = 148;  // GPIO4_20
	private final static int GPIO_BUTTON_CC8MMDVK = 52;     // GPIO2_IO20
	private final static int GPIO_LED_CC6SBC = 34;
	private final static int GPIO_LED_CC8XSBCPRO = 479;     // PTD5
	private final static int GPIO_LED_CC8MMDVK = 51;        // GPIO2_IO19

	private final static int GPIO_BUTTON = getButtonGPIO();
	private final static int GPIO_LED = getLEDGPIO();

	// Action enumeration.
	private final static int PUSH_BUTTON_PRESSED = 0;
	private final static int PUSH_BUTTON_RELEASED = 1;
	final static int PUSH_SOFT_BUTTON_PRESSED = 2;
	final static int PUSH_SOFT_BUTTON_RELEASED = 3;

	// Variables.
	private GPIO pushLedGPIO;
	private GPIO pushButtonGPIO;

	private GPIOButton pushButton;

	private ImageView pushLed;

	private final IncomingHandler handler = new IncomingHandler(this);

	/**
	 * Handler to manage UI calls from different threads.
	 */
	static class IncomingHandler extends Handler {
		private final WeakReference<GPIOSampleActivity> wActivity;

		IncomingHandler(GPIOSampleActivity activity) {
			wActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			GPIOSampleActivity gpioSample = wActivity.get();

			if (gpioSample == null)
				return;

			switch (msg.what) {
				case (PUSH_BUTTON_PRESSED):
				case (PUSH_SOFT_BUTTON_PRESSED):
					gpioSample.performPushButtonGPIOAction(GPIOValue.LOW);
					break;
				case (PUSH_BUTTON_RELEASED):
				case (PUSH_SOFT_BUTTON_RELEASED):
					gpioSample.performPushButtonGPIOAction(GPIOValue.HIGH);
					break;
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (GPIO_LED == -1 || GPIO_BUTTON == -1) {
			Toast.makeText(getApplicationContext(), "ERROR: Unknown board type",
					Toast.LENGTH_LONG).show();
			finish();
		}

		// Initialize application graphics.
		initializeGraphics();
		// Initialize application GPIOs.
		initializeGPIOs();
	}

	@Override
	protected void onDestroy() {
		try {
			// Leave LED in a known state (off).
			resetLedStatus();
		} catch (GPIOException e) {
			e.printStackTrace();
		}
		pushButtonGPIO = null;
		pushLedGPIO = null;
		super.onDestroy();
	}

	/**
	 * Initializes application graphics for the LED and Button images.
	 * This method also adds touch listener to the button.
	 */
	private void initializeGraphics() {
		// Declare views by retrieving them with the ID.
		pushLed = findViewById(R.id.push_led);
		pushButton = findViewById(R.id.push_button);
		ImageView boardImage = findViewById(R.id.board_image);

		// Check our screen size.
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int x = size.x;
		int y = size.y;

		// Set maximum components size.
		pushLed.setMaxHeight((int)((float)x/2.75));
		pushLed.setMaxWidth(y/2);
		
		pushButton.setMaxHeight((int)((float)x/2.6));
		pushButton.setMaxWidth(y/2);

		// Set correct board image.
		boardImage.setImageResource(getBoardImageResourceID());

		// Add touch listeners to button.
		pushButton.setHandler(handler);
	}

	/**
	 * Initializes all the GPIOs that will be used in the application.
	 */
	private void initializeGPIOs() {
		// Get the GPIO manager.
		GPIOManager gpioManager = new GPIOManager(this);
		try {
			pushButtonGPIO = gpioManager.createGPIO(GPIO_BUTTON, GPIOMode.INTERRUPT_EDGE_BOTH);
			pushLedGPIO = gpioManager.createGPIO(GPIO_LED, GPIOMode.OUTPUT_HIGH);
			// Initialize LEDs to a known status (off).
			resetLedStatus();
			// Subscribe a listener to receive GPIO value changes.
			pushButtonGPIO.registerListener(new IGPIOListener() {
				@Override
				public void valueChanged(GPIOSample sample) {
					if (sample.getGPIO() == pushButtonGPIO) {
						if (sample.getValue() == GPIOValue.LOW)
							handler.sendEmptyMessage(PUSH_SOFT_BUTTON_PRESSED);
						else
							handler.sendEmptyMessage(PUSH_SOFT_BUTTON_RELEASED);
					}
				}
			});
		} catch (GPIOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the LED to a known status (off).
	 * 
	 * @throws GPIOException If any error occurs while resetting the LED.
	 */
	private void resetLedStatus() throws GPIOException {
		// GPIOs have output to low level, so true means led off.
		pushLedGPIO.setValue(GPIOValue.LOW);
	}

	/**
	 * Performs the Push LED action with the given new value.
	 * 
	 * @param value New value of the push LED.
	 */
	private void performPushButtonGPIOAction(GPIOValue value) {
		try {
			switch (value) {
				case LOW:
					// Change GPIO LED value.
					pushLedGPIO.setValue(GPIOValue.HIGH);
					// Switch button image.
					pushButton.setImageResource(R.drawable.button_pressed);
					// Switch LED image.
					pushLed.setImageResource(R.drawable.led_on);
					break;
				case HIGH:
					// Change GPIO LED value.
					pushLedGPIO.setValue(GPIOValue.LOW);
					// Switch button image.
					pushButton.setImageResource(R.drawable.button);
					// Switch LED image.
					pushLed.setImageResource(R.drawable.led);
					break;
			}
		} catch (GPIOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the the resource ID of the board image to draw depending on the board the sample is
	 * running on.
	 *
	 * @return The resource ID of the board image to draw depending on the board the sample is
	 *         running on.
	 */
	private int getBoardImageResourceID() {
		if (Build.DEVICE.equals(CCIMX8XSBCPRO_NAME))
			return R.drawable.ccimx8x_sbc_pro_board;
		if (Build.DEVICE.equals(CCIMX8MMDVK_NAME))
			return R.drawable.ccimx8x_sbc_pro_board;
		if (Build.DEVICE.equals(CCIMX6SBC_NAME))
			return R.drawable.ccimx6_sbc_board;

		return R.drawable.digi_icon;
	}

	/**
	 * Returns the GPIO LED number based on the board the sample is running on.
	 *
	 * @return The GPIO LED number based on the board the sample is running on.
	 */
	private static int getLEDGPIO() {
		if (Build.DEVICE.equals(CCIMX8XSBCPRO_NAME))
			return GPIO_LED_CC8XSBCPRO;
		if (Build.DEVICE.equals(CCIMX8MMDVK_NAME))
			return GPIO_LED_CC8MMDVK;
		if (Build.DEVICE.equals(CCIMX6SBC_NAME))
			return GPIO_LED_CC6SBC;

		return -1;
	}

	/**
	 * Returns the GPIO BUTTON number based on the board the sample is running on.
	 *
	 * @return The GPIO BUTTON number based on the board the sample is running on.
	 */
	private static int getButtonGPIO() {
		if (Build.DEVICE.equals(CCIMX8XSBCPRO_NAME))
			return GPIO_BUTTON_CC8XSBCPRO;
		if (Build.DEVICE.equals(CCIMX8MMDVK_NAME))
			return GPIO_BUTTON_CC8MMDVK;
		if (Build.DEVICE.equals(CCIMX6SBC_NAME))
			return GPIO_BUTTON_CC6SBC;

		return -1;
	}
}
