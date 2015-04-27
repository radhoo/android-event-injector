As a follow up to an article that I wrote "Programmatically Injecting Events on Android" (available on: http://www.pocketmagic.net/2012/04/injecting-events-programatically-on-android ), here's a bit of code in a form of a native library with a JNI wrapper to make things easier.

The approach is to search and find all the /dev/input/eventX files, each representing a event device node, gather all the relevant data (including name), and leave it to the user to choose where to inject the events.

The searching process requires read permission on the event device nodes , while the injection of events requires writing permissions. To solve these requirements, we rely to having root access, to modify the event device node files from 660 to 666 (adding +rw for Other).

The permission change is all that we need root for.

More details available on my blog: http://www.pocketmagic.net/2013/01/programmatically-injecting-events-on-android-part-2


![http://www.pocketmagic.net/wp-content/uploads/2013/01/header_android_inject_events.jpg](http://www.pocketmagic.net/wp-content/uploads/2013/01/header_android_inject_events.jpg)