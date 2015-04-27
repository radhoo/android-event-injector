# Introduction #

As a follow up to an article that I wrote "Programmatically Injecting Events on Android" (available on: http://www.pocketmagic.net/2012/04/injecting-events-programatically-on-android ), here's a bit of code in a form of a native library with a JNI wrapper to make things easier.

The approach is to search and find all the /dev/input/eventX files, each representing a event device node, gather all the relevant data (including name), and leave it to the user to choose where to inject the events.

The searching process requires read permission on the event device nodes , while the injection of events requires writing permissions. To solve these requirements, we rely to having root access, to modify the event device node files from 660 to 666 (adding +rw for Other).

The permission change is all that we need root for.

More details available in the original article at: http://www.pocketmagic.net/2012/04/injecting-events-programatically-on-android/


# Details #

Details will follow soon.
[[Image:![http://android-event-injector.googlecode.com/files/device-2013-01-23-192014.png](http://android-event-injector.googlecode.com/files/device-2013-01-23-192014.png)]]