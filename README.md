GPIO Sample Application
=======================

This application demonstrates the usage of the GPIO API by giving an example
of how to control GPIO input and monitor GPIO output pin status.

Demo requirements
-----------------

To run this example you need:

* A compatible development board to host the application.
* A USB connection between the board and the host PC in order to transfer and
  launch the application.
* For ConnectCore 6 SBC use EXP_GPIO0 on the GPIO expansion connector J30 of
  the SBC to switch the state of the GPIO button.
* For ConnectCore 8X SBC Pro use:
     * GPIO4_20 on the expansion connector J20 (pin 13) as GPIO button.
     * User LED 0 (PTD5)
* For ConnectCore 8M Mini Development Kit use:
     * User button 2 (GPIO2_IO20)
     * User LED 3 (GPIO2_IO19)

Demo setup
----------

Make sure the hardware is set up correctly:

1. The development board is powered on.
2. The board is connected directly to the PC by the micro USB cable.

Demo run
--------

The example is already configured, so all you need to do is to build and
launch the project.

The application displays a representation of a **User LED** and **User BUTTON**
in the development board.

Click the application button or the physical button of the development board to
turn on the application LED representation and the physical LED in the
development board.

Compatible with
---------------

* ConnectCore 6 SBC
* ConnectCore 6 SBC v3
* ConnectCore 8X SBC Pro
* ConnectCore 8M Mini Development Kit

License
-------

Copyright (c) 2014-2025, Digi International Inc. <support@digi.com>

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
