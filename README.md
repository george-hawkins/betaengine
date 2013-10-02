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

Tools
-----

Once you've built the codebase you can run the various tools as follows.

### mDNS listener

    $ java -classpath 'bin:lib/*' net.betaengine.mdns.MDNSListener

### Smart Config responder

    $ java -classpath bin net.betaengine.smartconfig.device.Responder

### Smart Config desktop UI

    $ java -classpath 'bin:resources:lib/*' net.betaengine.smartconfig.desktop.Main
