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

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.gpio.GPIO;
import android.gpio.GPIOException;
import android.graphics.Point;
import android.os.AsyncTask;
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
public class GPIOSample extends Activity {
	
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

	private PushButtonTask pushButtonTask;
	
	private boolean running = false;

	private IncomingHandler handler = new IncomingHandler(this);

	/**
	 * Handler to manage UI calls from different threads.
	 */
	static class IncomingHandler extends Handler {
		private final WeakReference<GPIOSample> wActivity;

		IncomingHandler(GPIOSample activity) {
			wActivity = new WeakReference<GPIOSample>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			GPIOSample gpioSample = wActivity.get();

			if (gpioSample == null)
				return;

			switch (msg.what) {
				case (PUSH_BUTTON_PRESSED):
					gpioSample.performPushButtonGPIOAction(false);
					break;
				case (PUSH_SOFT_BUTTON_PRESSED):
					gpioSample.performPushButtonGPIOAction(false);
					break;
				case (PUSH_BUTTON_RELEASED):
				case (PUSH_SOFT_BUTTON_RELEASED):
					gpioSample.performPushButtonGPIOAction(true);
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
		// Initialize application background task to check for interrupts on button GPIO.
		initializeTask();
	}

	@Override
	protected void onDestroy() {
		// Stop background task.
		stopTask();
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
		try {
			pushButtonGPIO = new GPIO(GPIO_BUTTON, GPIO.MODE_INTERRUPT_EDGE_BOTH);
			pushLedGPIO = new GPIO(GPIO_LED, GPIO.MODE_OUTPUT_HIGH);
			// Initialize LEDs to a known status (off).
			resetLedStatus();
		} catch (GPIOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Sets the LED to a known status (off).
	 * 
	 * @throws GPIOException
	 */
	private void resetLedStatus() throws GPIOException {
		// GPIOs have output to low level, so true means led off.
		pushLedGPIO.setState(false);
	}
	
	/**
	 * Initializes background asynchronous task that take care of listening
	 * for interrupt events on board button and perform required actions
	 * on LED GPIO and graphics. 
	 */
	private void initializeTask() {
		// Set global running variable to true.
		running = true;
		// Declare task.
		pushButtonTask = new PushButtonTask();
		// Start task.
		pushButtonTask.execute();
	}
	
	/**
	 * Stop asynchronous background task that were taking care of 
	 * checking GPIO button interrupt events.
	 */
	private void stopTask() {
		// Set global running variable to false.
		running = false;
		// Stop waiting for interrupt on GPIO.
		pushButtonGPIO.stopWaitingForInterrupt();
		// Give time to propagate stop request.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Cancel tasks.
		pushButtonTask.cancel(true);
		pushButtonTask = null;
	}
	
	/**
	 * Performs the Push LED action with the given new state.
	 * 
	 * @param state New state of the push LED.
	 */
	private void performPushButtonGPIOAction(boolean state) {
		try {
			// Change GPIO LED state.
			pushLedGPIO.setState(!state);
			// Change images.
			if (!state) {
				// Switch button image.
				pushButton.setImageResource(R.drawable.button_pressed);
				// Switch LED image.
				pushLed.setImageResource(R.drawable.led_on);
			} else {
				// Switch button image.
				pushButton.setImageResource(R.drawable.button);
				// Switch LED image.
				pushLed.setImageResource(R.drawable.led);
			}
		} catch (GPIOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Listener to wait for touch events to occur on both buttons.
	 */
	private OnTouchListener buttonTouchListener = new OnTouchListener() {
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
	
	/**
	 * Background asynchronous task that takes care of listening for
	 * interrupt events on the board push button (User Button 1) and 
	 * perform required actions.
	 *
	 */
	private class PushButtonTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			while (running) {
				try {
					boolean state = pushButtonGPIO.waitForInterrupt(0, 100);
					if (running) {
						// Send event to handler to perform required actions.
						if (!state)
							handler.sendEmptyMessage(PUSH_BUTTON_PRESSED);
						else
							handler.sendEmptyMessage(PUSH_BUTTON_RELEASED);
					}
				} catch (GPIOException e) {
					e.printStackTrace();
				}
			}
			return (null);
		}
	}
}
