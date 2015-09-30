GPIO Sample Application
=======================

This application demonstrates the usage of the GPIO APIs by giving
an example of how to control GPIO input and monitor GPIO output pin
status.

Demo requeriments
-----------------

To run this example you will need:
    - One compatible device to host the application.
    - Network connection between the device and the host PC in order to
      transfer and launch the application.
    - Establish remote target connection to your Digi hardware before running
      this application.
    - Use EXP_GPIO0 on the GPIO expansion connector J30 of the SBC to switch
      the state of the GPIO button.

Demo setup
----------

 Make sure the hardware is set up correctly:
    - The device is powered on.
    - The device is connected directly to the PC or to the Local 
      Area Network (LAN) by the Ethernet cable.

Demo run
--------

The example is already configured, so all you need to do is to build and 
launch the project.
  
While it is running, the application will display a visual representation
of the User LED1 and User BUTTON1 of the development board. Pressing the
software button or the physical button from the development board will 
cause both the software LED and the physical development board LED to 
light.

Tested on
---------

ConnectCore Wi-i.MX51
ConnectCore Wi-i.MX53
ConnectCard for i.MX28
ConnectCore 6 Adapter Board
ConnectCore 6 SBC
ConnectCore 6 SBC v2