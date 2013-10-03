IMPORTANT
=========

You must be using Java 7 or later, to check your version run:

$ java -version

And confirm that the version number starts with 1.7 (or a higher number e.g. 1.8).

Before you can use the new Smart Config interface you must repackage the TI Smart Config library into a useable form.

This only needs to be done once. You can do it either by double clicking repackage.jar or like so from the command line:

$ java -jar repackage.jar

Once this is done you can run the Smart Config interface by double clicking net.betaengine.smartconfig-ui.jar or like so from the command line:

$ java -jar net.betaengine.smartconfig-ui.jar

Note: running executable jars by double clicking them may be disabled in certain environments in which case you will need to run them from the command line as shown above.

If your desktop or laptop is connected to your wireless access point using 802.11n then the CC3000 enabled device may not be able to receive the setup information.

If you are using 802.11n then I would really appreciates a report on whether setup was successful or not here:

  http://depletionregion.blogspot.ch/2013/09/determining-80211-protocol-b-g-or-n.html

This blog post discusses how to determine if you really are using 802.11n. So please double check this and please include details of your setup, e.g. "Mid 2012 Macbook Air running OS X 10.8.4 and using built-in wifi" or "Raspberry Pi model B running 2013-09-10-wheezy-raspbian with Wi-Pi USB wifi dongle."

ORIGIN AND LICENSES
-------------------

For information on the origin of the various jars and other resources and on the licenses that cover them please see:

  https://github.com/george-hawkins/betaengine/blob/master/RESOURCES.txt
