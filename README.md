SyncManagerAndroid-DemoGoogleTasks
==================================

Android Google Task app to demonstrate use of [SyncManagerAndroid][repo-syncman] library.

Overview
--------------------
Simple [Google Tasks API][gtasks] integrated app that demostrates synchronization of Google Task data between local SQLite database and remote RESTful Google Tasks API.


Some key Android concepts demonstrated
--------------------
* Android sync framework integration - with sync implementation provided by [SyncManagerAndroid][repo-syncman]
* SQLite database
* Retrofit REST client integration
* Custom ArrayAdapter

Libraries used
--------------------
* [SyncManagerAndroid][repo-syncman] - 2-way sync library
* [Retrofit][repo-retrofit] - REST client from [Square][gh-square]

[repo-syncman]: https://github.com/sschendel/SyncManagerAndroid
[repo-retrofit]: http://square.github.io/retrofit/
[gh-square]: http://square.github.io/
[gtasks]: https://developers.google.com/google-apps/tasks/
