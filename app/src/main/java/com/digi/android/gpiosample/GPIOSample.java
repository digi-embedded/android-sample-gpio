package com.example.android.gpiosample;

import java.util.Timer;
import java.util.TimerTask;

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
import android.widget.TextView;

/**
 * Sample application that demonstrates the usage of the GPIO
 * API by monitoring the status of Board User LED 1 . LEDs can
 * be controlled by using application software buttons or Board
 * User Button 1.
 * 
 * LED is controlled in a push mode: when button is pressed, LED 
 * is on and when button is released, LED is off.
 *
 */
public class GPIOSample extends Activity {
	
	// GPIO numbers for LEDs
	private final static int GPIO_BUTTON_1_MX51_EAK = 8;
	private final static int GPIO_BUTTON_1_MX51_JSK = 70;
	private final static int GPIO_BUTTON_1_MX53 = 96;
	private final static int GPIO_BUTTON_MX28 = 107;
	private final static int GPIO_BUTTON_1_MX6 = 36;
	private final static int GPIO_BUTTON_2_MX6 = 37;
	private final static int GPIO_LED_1_MX51 = 74;
	private final static int GPIO_LED_1_MX53 = 148;
	private final static int GPIO_LED_MX28 = 101;
	private final static int GPIO_LED_1_MX6 = 34;
	private final static int GPIO_LED_2_MX6 = 35;
	
	// Action enumeration
	private final static int PUSH_BUTTON_PRESSED = 0;
	private final static int PUSH_BUTTON_RELEASED = 1;
	private final static int PUSH_SOFT_BUTTON_PRESSED = 2;
	private final static int PUSH_SOFT_BUTTON_RELEASED = 3;
	
	// Variables
	private GPIO pushLedGPIO;
	private GPIO pushButtonGPIO;
	
	private ImageButton pushButton;
	
	private ImageView pushLed;
	
	private PushButtonTask pushButtonTask;
	
	private boolean running = false;

	private Timer timer = new Timer();
	private boolean timerRunning = false;
	
	// Handler to take care of UI actions called from other threads
	private Handler myHandler = new Handler() {
		
		/*
		 * (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch(msg.what) {
			// Check which kind of message was received to perform required actions.
			case (PUSH_BUTTON_PRESSED):
				performPushButtonGPIOAction(false);
				if (BoardUtils.isMX28() && !timerRunning) {
					timerRunning = true;
					timer.scheduleAtFixedRate(declareTimerTask(), 0, 5);
				}
				break;
			case (PUSH_SOFT_BUTTON_PRESSED):
				performPushButtonGPIOAction(false);
				break;
			case (PUSH_BUTTON_RELEASED):
			case (PUSH_SOFT_BUTTON_RELEASED):
				performPushButtonGPIOAction(true);
				break;
			}
		}
	};
	
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Initialize application graphics.
        initializeGraphics();
        // Initialize application GPIOs.
        initializeGPIOs();
        // Initialize application background task to check for interrupts on button GPIO.
		initializeTask();
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
		int screenHeight = size.x;
		int screenWidth = size.y;
		
		// Set maximum components size.
		pushLed.setMaxHeight((int)((float)screenHeight/2.75));
		pushLed.setMaxWidth(screenWidth/2);
		
		pushButton.setMaxHeight((int)((float)screenHeight/2.6));
		pushButton.setMaxWidth(screenWidth/2);
		
		// Add touch listeners to button.
		pushButton.setOnTouchListener(buttonTouchListener);

		if (BoardUtils.isMX28()) {
			TextView push_text = (TextView)findViewById(R.id.push_text);
			push_text.setText("User Button 1");
		}
    }
    
    /**
     * Initializes all the GPIOs that will be used in the application.
     */
    private void initializeGPIOs() {
    	try {
    		if (BoardUtils.isMX53()) {
    			pushButtonGPIO = new GPIO(GPIO_BUTTON_1_MX53, GPIO.MODE_INTERRUPT_EDGE_BOTH);
    			pushLedGPIO = new GPIO(GPIO_LED_1_MX53, GPIO.MODE_OUTPUT);    			
    		}
    		else if (BoardUtils.isMX28()) {
    			pushButtonGPIO = new GPIO(GPIO_BUTTON_MX28, GPIO.MODE_INTERRUPT_EDGE_FALLING);
    			pushLedGPIO = new GPIO(GPIO_LED_MX28, GPIO.MODE_OUTPUT_HIGH);
    		}
		else if (BoardUtils.isMX6ADPT()) {
    			pushButtonGPIO = new GPIO(GPIO_BUTTON_1_MX6, GPIO.MODE_INTERRUPT_EDGE_BOTH);
    			pushLedGPIO = new GPIO(GPIO_LED_1_MX6, GPIO.MODE_OUTPUT_HIGH);
    		}
		else if (BoardUtils.isMX6SBC()) {
			pushButtonGPIO = new GPIO(GPIO_BUTTON_2_MX6, GPIO.MODE_INTERRUPT_EDGE_BOTH);
			pushLedGPIO = new GPIO(GPIO_LED_1_MX6, GPIO.MODE_OUTPUT_HIGH);
		}
    		else {
        		if (BoardUtils.isEAK())
    				pushButtonGPIO = new GPIO(GPIO_BUTTON_1_MX51_EAK, GPIO.MODE_INTERRUPT_EDGE_BOTH);
    			else
    				pushButtonGPIO = new GPIO(GPIO_BUTTON_1_MX51_JSK, GPIO.MODE_INTERRUPT_EDGE_BOTH);
    			pushLedGPIO = new GPIO(GPIO_LED_1_MX51, GPIO.MODE_OUTPUT);
    		}
	        // Initialize LEDs to a known status (off)
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
    	// GPIOs have output to low level, so true means led off
	if (BoardUtils.isMX6SBC())
		pushLedGPIO.setState(false);
	else
		pushLedGPIO.setState(true);
    }
    
    /**
     * Initializes background asynchronous task that take care of listening
     * for interrupt events on board button and perform required actions
     * on LED GPIO and graphics. 
     */
    private void initializeTask() {
    	// Set global running variable to true.
    	running = true;
    	// Declare task
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
		} catch (InterruptedException e) {}
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
		if (BoardUtils.isMX6SBC())
			pushLedGPIO.setState(!state);
		else
			pushLedGPIO.setState(state);
    		// Change images
    		if (!state) {
    			// Switch button image.
    	    	pushButton.setImageResource(R.drawable.button_pressed);
    	    	// Switch LED image
				pushLed.setImageResource(R.drawable.led_on);
    		} else {
    			// Switch button image.
    	    	pushButton.setImageResource(R.drawable.button);
    	    	// Switch LED image
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
	        // Check which view was touch
	        case (R.id.push_button):
	        	// Check which action was performed
	        	if (action == MotionEvent.ACTION_DOWN)
	        		// Send event to handler to perform required actions.
	            	myHandler.sendEmptyMessage(PUSH_SOFT_BUTTON_PRESSED);
	        	else if (action == MotionEvent.ACTION_UP)
	            	// Send event to handler to perform required actions.
	            	myHandler.sendEmptyMessage(PUSH_SOFT_BUTTON_RELEASED);
	        	break;
	        }
	        return true;
		}
	};
	
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    protected void onDestroy() {
    	// Stop background task
    	stopTask();
    	try {
    		// Leave LED in a known state (off)
    		resetLedStatus();
		} catch (GPIOException e) {
			e.printStackTrace();
		}
		pushButtonGPIO = null;
		pushLedGPIO = null;
		super.onDestroy();
    }
    
    /**
     * Background asynchronous task that takes care of listening for
     * interrupt events on the board push button (User Button 1) and 
     * perform required actions.
     *
     */
    private class PushButtonTask extends AsyncTask<Void, Void, Void> {
    	
		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected Void doInBackground(Void... params) {
			while (running) {
				try {
					boolean state = pushButtonGPIO.waitForInterrupt(0, 100);
					if (running) {
						// Send event to handler to perform required actions.
						if (!state)
							myHandler.sendEmptyMessage(PUSH_BUTTON_PRESSED);
						else
							myHandler.sendEmptyMessage(PUSH_BUTTON_RELEASED);
					}
				} catch (GPIOException e) {
					e.printStackTrace();
				}
			}
			return (null);
		}
    }
    
    private TimerTask declareTimerTask() {
    	TimerTask timerTask = new TimerTask() {
    		public void run() {
    			try {
    				if (pushButtonGPIO.getState()) {
    					myHandler.sendEmptyMessage(PUSH_BUTTON_RELEASED);
    					this.cancel();
    					timerRunning = false;
    					return;
    				}
    			} catch (GPIOException e) {
    				e.printStackTrace();
    			} catch (NullPointerException e) {
    				e.printStackTrace();
    			}
    		}
    	};
    	return timerTask;
    }
}
