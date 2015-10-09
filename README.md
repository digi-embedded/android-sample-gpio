GPIO Sample Application
=======================

This application demonstrates the usage of the GPIO API by giving an example 
of how to control GPIO input and monitor GPIO output pin status.

Demo requirements
-----------------

To run this example you need:

* One compatible device to host the application.
* A USB connection between the device and the host PC in order to transfer and
  launch the application.
* Use EXP_GPIO0 on the GPIO expansion connector J30 of the SBC to switch the 
  state of the GPIO button.

Demo setup
----------

Make sure the hardware is set up correctly:

1. The device is powered on.
2. The device is connected directly to the PC by the micro USB cable.

Demo run
--------

The example is already configured, so all you need to do is to build and 
launch the project.
  
The application displays a representation of **User LED0** and **User BUTTON0**
of the development board.

Click the application button or the physical button of the development board to
turn on the application LED representation and the physical LED in the
development board.

Compatible with
---------------

* ConnectCore 6 SBC
* ConnectCore 6 SBC v2

License
-------

This software is open-source software. Copyright Digi International, 2014-2015.

This Source Code Form is subject to the terms of the Mozilla Public License,
v. 2.0. If a copy of the MPL was not distributed with this file, you can obtain
one at http://mozilla.org/MPL/2.0/.