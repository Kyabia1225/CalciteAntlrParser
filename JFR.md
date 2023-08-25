Here's the JVM options to enable JFR and dumps on exit.

```
-XX:+FlightRecorder
-XX:StartFlightRecording=disk=true,dumponexit=true,filename=nukv_adaptor_test.jfr,settings=./My.jfc
-XX:FlightRecorderOptions=stackdepth=512
```

In terms of JMX, I found it not necessary to enable at local. Keep it here for documentation:
```
-Dcom.sun.management.jmxremote=true
-Dcom.sun.management.jmxremote.port=9010
-Dcom.sun.management.jmxremote.rmi.port=9010
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
```
