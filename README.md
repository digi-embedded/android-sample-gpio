GPIO Sample Application
=======================

This application demonstrates the usage of the GPIO API by giving an example 
of how to control GPIO input and monitor GPIO output pin status.

Demo requirements
-----------------

To run this example you need:

* One compatible device to host the application.
* Network connection between the device and the host PC in order to transfer and
  launch the application.
* Establish remote target connection to your Digi hardware before running this 
  application.
* Use EXP_GPIO0 on the GPIO expansion connector J30 of the SBC to switch the 
  state of the GPIO button.

Demo setup
----------

Make sure the hardware is set up correctly:

1. The device is powered on.
2. The device is connected directly to the PC or to the Local Area Network (LAN)
   by the Ethernet cable.

Demo run
--------

The example is already configured, so all you need to do is to build and 
launch the project.
  
The application displays a representation of **User LED1** and **User BUTTON1**
of the development board.

Click the application button or the physical button of the development board to
turn on the application LED representation and the physical LED in the
development board.

Tested on
---------

* ConnectCore Wi-i.MX51
* ConnectCore Wi-i.MX53
* ConnectCard for i.MX28
* ConnectCore 6 Adapter Board
* ConnectCore 6 SBC
* ConnectCore 6 SBC v2