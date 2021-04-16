/*
 * Copyright (c) 2021, Digi International Inc. <support@digi.com>
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

import static com.digi.android.sample.gpio.GPIOSampleActivity.PUSH_SOFT_BUTTON_PRESSED;
import static com.digi.android.sample.gpio.GPIOSampleActivity.PUSH_SOFT_BUTTON_RELEASED;

public class GPIOButton extends ImageButton {
	private GPIOSampleActivity.IncomingHandler handler;

	public GPIOButton(Context context) {
		super(context);
	}

	public GPIOButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GPIOButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		// Listening for the down and up touch events
		switch (event.getAction()) {
			// Check which action was performed.
			case MotionEvent.ACTION_DOWN:
				// Send event to handler to perform required actions.
				handler.sendEmptyMessage(PUSH_SOFT_BUTTON_PRESSED);
				return true;
			case MotionEvent.ACTION_UP:
				// Send event to handler to perform required actions.
				handler.sendEmptyMessage(PUSH_SOFT_BUTTON_RELEASED);
				performClick(); // Call this method to handle the response, and
								// thereby enable accessibility services to
								// perform this action for a user who cannot
								// click the touchscreen.
				return true;
		}

		return false;
	}

	@Override
	public boolean performClick() {
		// Calls the super implementation, which generates an AccessibilityEvent
		// and calls the onClick() listener on the view, if any
		super.performClick();

		return true;
	}

	public void setHandler(GPIOSampleActivity.IncomingHandler handler) {
		this.handler = handler;
	}
}
