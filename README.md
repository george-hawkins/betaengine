Beta-engine
===========

All net.betaengine code requires Java 7 or later.

This project depends on various jars and resources that are covered by their own licenses.
The origin of these items and the relevant licenses are covered in the file RESOURCES.txt.

The net.betaengine code and related resources are covered the Apache License, Version 2.0.
You may obtain a copy of this License at:

<http://www.apache.org/licenses/LICENSE-2.0>

Build
-----

The following sequence of commands will build the net.betaengine codebase on Linux or Mac OS X and should be easy to adapt for Windows.

    $ java -jar lib/jarjar-1.4.jar process assembly/rules.txt ti-resources/smartConfig.jar lib/com.ti.smartconfig-1.1.jar
    $ mkdir -p bin
    $ javac -d bin -classpath 'lib/*' $(find src -name '*.java')

The first step repackages the original TI smartConfig.jar file into a useable jar called com.ti.smartconfig-1.1.jar.

Tools
-----

Once you've built the codebase you can run the various tools as follows.

### mDNS listener

    $ java -classpath 'bin:lib/*' net.betaengine.mdns.MDNSListener

### Smart Config responder

    $ java -classpath bin net.betaengine.smartconfig.device.Responder <device-name>

If no device-name is provided then the default name "CC3000" is used.

### Smart Config desktop UI

    $ java -classpath 'bin:resources:lib/*' net.betaengine.smartconfig.desktop.Main

Decoder
-------

Running the decoder is a little more complex than the other tools.

It should be run in combination with tshark like so:

    $ tshark -o 'wlan.enable_decryption:FALSE' \
        -i en0 -I -f 'subtype qos-data' -Y 'wlan.fc.retry==0' -T fields \
        -e wlan.bssid -e radiotap.channel.freq -e wlan.sa -e wlan.da -e data.len 2> /dev/null \
        | java -classpath "bin:lib/*" net.betaengine.smartconfig.device.decoder.Consumer

Replace `en0` with the appropriate wifi device reported by:

    $ tshark -D

Note: stderr is redirected to `/dev/null` simply in order to throw away the frame count information that tshark reports.
It looks like one should be able to disable this output with `-Q` but this does not work with my version of tshark.
