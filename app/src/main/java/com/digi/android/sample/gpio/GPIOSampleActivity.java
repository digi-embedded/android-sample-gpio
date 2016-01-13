/**
 * Copyright (c) 2014-2016 Digi International Inc.,
 * All rights not expressly granted are reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
 * =======================================================================
 */

package com.digi.android.sample.gpio;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
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
 * <p>This example demonstrates the usage of the GPIO
 * API by monitoring the status of Board User LED 0. LEDs can
 * be controlled by using application software buttons or Board
 * User Button 0.</p>
 *
 * <p>For a complete description on the example, refer to the 'README.md' file
 * included in the example directory.</p>
 */
public class GPIOSampleActivity extends Activity {
	
	// Constants.
	
	// GPIO numbers for LEDs.
	private final static int GPIO_BUTTON = 37;
	private final static int GPIO_LED = 34;

	// Action enumeration.
	private final static int PUSH_BUTTON_PRESSED = 0;
	private final static int PUSH_BUTTON_RELEASED = 1;
	private final static int PUSH_SOFT_BUTTON_PRESSED = 2;
	private final static int PUSH_SOFT_BUTTON_RELEASED = 3;

	// Variables.
	private GPIO pushLedGPIO;
	private GPIO pushButtonGPIO;

	private ImageButton pushButton;

	private ImageView pushLed;

	private IncomingHandler handler = new IncomingHandler(this);

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
		// Check if the module is a CC i-MX6 SBC.
		if (!BoardUtils.isMX6SBC()) {
			Toast.makeText(getApplicationContext(), getString(R.string.ModuleError),
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
		pushLed = (ImageView)findViewById(R.id.push_led);
		pushButton = (ImageButton)findViewById(R.id.push_button);
		
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
		
		// Add touch listeners to button.
		pushButton.setOnTouchListener(buttonTouchListener);
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
	 * @throws GPIOException
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
	 * Listener to wait for touch events to occur on both buttons.
	 */
	private OnTouchListener buttonTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			switch(v.getId()) {
				// Check which view was touch.
				case (R.id.push_button):
					// Check which action was performed.
					if (action == MotionEvent.ACTION_DOWN)
						// Send event to handler to perform required actions.
						handler.sendEmptyMessage(PUSH_SOFT_BUTTON_PRESSED);
					else if (action == MotionEvent.ACTION_UP)
						// Send event to handler to perform required actions.
						handler.sendEmptyMessage(PUSH_SOFT_BUTTON_RELEASED);
					break;
			}
			return true;
		}
	};
}
