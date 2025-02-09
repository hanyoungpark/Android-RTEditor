# Android-RTEditor

The Android RTEditor is a rich text editor component for Android that can be used as a drop in for EditText.

![Images](http://www.1gravity.com/images/richtexteditor.png)

Features
--------

The editor offers the following <b>character formatting</b> options:

* **Bold**
* *Italic*
* <u>Underline</u>
* <strike>Strike through</strike>
* <sup>Superscript</sup>
* <sub>Subscript</sub>
* Different fonts
* Text size
* Text color
* Background color

It also supports the following <b>paragraph formatting</b>:

<ol><li>Numbered</li></ol>
<ul>
<li>Bullet points</li>
<ul style='list-style-type:none;'>Indentation</ul>
<div align="left">Left alignment</div>
<div align="center">Center alignment</div>
<div align="right">Right alignment</div>
</ul>

* [Links](http://www.1gravity.com)
* Images: [![Images](http://www.1gravity.com/smiley.jpg)](https://www.1gravity.com)
* Undo/Redo


Setup
-----
####**Dependencies**

Add this to your Gradle build file:
```
dependencies {
    compile 'com.1gravity:android-rteditor:1.1.7'
}
```

####**Theming**

The toolbar uses a couple of custom attributes that need to be defined or it will crash when being inflated.
You need to use a theme based on either RTE_ThemeLight or RTE_ThemeDark or define all rich text editor attributes (rte_toolbar_themes.xml) in your own theme. 

Make sure to call setTheme before setContentView (or set the theme in the manifest):

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set theme before calling setContentView!
    setTheme(R.style.ThemeLight);

    // set layout
    setContentView(R.layout.your_layout);
```

The 3 main components
---------------------
####**RTEditText**
is the EditText drop in component. Add it to your layout like you would EditText:
```xml
<com.onegravity.rteditor.RTEditText
    android:id="@+id/rtEditText"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:imeOptions="actionDone|flagNoEnterAction"
    android:inputType="textMultiLine|textAutoCorrect|textCapSentences" />
```
In code you would typically use methods to set and get the text content:
  * set text: <code>RTEditText.setRichTextEditing(true, "My content");</code>
  * get text: <code>RTEditText.getText(RTFormat.HTML)</code>

####**RTToolbar**
is an interface for the toolbar used to apply text and paragraph formatting and other features listed above. The actual RTToolbar implementation is in a separate module and is a scrollable ribbon but alternative implementations aren't too hard to realize (popup, action buttons, floating buttons...). The toolbar implementation is easy to integrate into your layout:
```xml
<include android:id="@+id/rte_toolbar_container" layout="@layout/rte_toolbar" />
```

 or if you want to have two ribbons for character and paragraph formatting:
```xml
<LinearLayout
    android:id="@+id/rte_toolbar_container"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <include layout="@layout/rte_toolbar_character" />
    <include layout="@layout/rte_toolbar_paragraph" />

</LinearLayout>
```

In code you'd typically not interact with the toolbar (see RTManager below for the one exception).

####**RTManager**
is the glue that holds the rich text editors (RTEditText), the toolbar and your app together. Each rich text editor and each toolbar needs to be registered with the RTManager before they are functional. Multiple editors and multiple toolbars can be registered. The RTManager is instantiated by your app in code usually in the onCreate passing in an RTApi object that gives the rich text editor access to its context (your app).
A typical initialization process looks like this (normally in the onCreate method):

```
// create RTManager
RTApi rtApi = new RTApi(this, new RTProxyImpl(this), new RTMediaFactoryImpl(this, true));
RTManager rtManager = new RTManager(rtApi, savedInstanceState);

// register toolbar
ViewGroup toolbarContainer = (ViewGroup) findViewById(R.id.rte_toolbar_container);
RTToolbar rtToolbar = (RTToolbar) findViewById(R.id.rte_toolbar);
if (rtToolbar != null) {
  rtManager.registerToolbar(toolbarContainer, rtToolbar);
}

// register editor & set text
RTEditText rtEditText = (RTEditText) findViewById(R.id.rtEditText);
rtManager.registerEditor(rtEditText, true);
rtEditText.setRichTextEditing(true, message);
```

To retrieve the edited text in html format you'd do:
```
String text = rtEditText.getText(RTFormat.HTML);
```

The RTManager also needs to be called in onSaveInstanceState and in onDestroy:
```
@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    mRTManager.onSaveInstanceState(outState);

    outState.putBoolean("mUseDarkTheme", mUseDarkTheme);
    outState.putBoolean("mSplitToolbar", mSplitToolbar);
}

@Override
public void onDestroy() {
    super.onDestroy();
    
    mRTManager.onDestroy(isFinishing());
}
```

The isSaved parameter passed into RTManager.onDestroy(boolean) is important. If it's true then media files inserted into the text (images at the moment) will remain untouched.
If the parameter is false (text content is dismissed), media files will be deleted. Note that the rich text editor copies the original file to a dedicated area according to the MediaFactory configuration, meaning the original will remain untouched.

RTApi
-----

If you read the previous section ("The 3 main components") you might have noticed the RTApi object.
The RTApi is a convenience class giving the various rich text editor components access to the application context and to RTProxy and RTMediaFactory methods.
```
RTApi rtApi = new RTApi(this, new RTProxyImpl(this), new RTMediaFactoryImpl(this, true));
```

####**Context**

The first parameter is merely a Context object (Application or Activity context).
The RTApi will only store the Application context so no issue with leaking the Activity context here.  

####**RTProxy**

The RTProxy allows the rich text editor to call Activity related methods like:
* startActivityForResult/runOnUiThread and Toast methods: for picking images to embed in the text
* Fragment related methods: for the link dialog (LinkFragment)

RTProxyImpl is the standard implementation for RTProxy and there's usually no need to use a custom implementation.
RTProxyImpl stores the Activity context in a SoftReference.

####**RTMediaFactory**

The most interesting class is RTMediaFactory.
By overriding it, different storage scenarios for embedded images (and potentially videos and audio files in the future) can be implemented (SQLite database, file system, cloud storage, access through ContentProvider etc.).

[More details can be found here](STORAGE.md)

Demo project
------------

The project consists of five different modules:
* **ColorPicker**: a color picker based on this: https://github.com/LarsWerkman/HoloColorPicker. The RTEditor uses an enhanced version that allows to enter ARGB values, includes a ColorPickerPreference that can be used in preference screens and shows a white and gray chessboard pattern behind the color visible when the the alpha channel is changed and the color becomes (partly) transparent.
* **MaterialDialog** a library based on this: https://github.com/afollestad/material-dialogs. The RTEditor uses a trimmed-down version because it only uses a subset of its functionality for the link dialog. You can easily remove this module and use the standard Android dialog instead.
* **RTEditor**: the actual rich text editor (excluding the toolbar implementation).
* **RTEditor-Toolbar**: the toolbar implementation.
* **RTEditor-Demo**: this module isn't part of the actual rich text editor component but contains a sample app that shows how to use the component.

The demo app can also be found on Google Play: [Demo App](https://play.google.com/store/apps/details?id=com.onegravity.rteditor.demo)

####**Fonts**

The rich text editor supports fonts that are part of the Android device it's running on. It's reading all ttf fonts in the /system/fonts, /system/font and /data/fonts and shows them in the editor. 

A lot of frequently used fonts have a copyright and can therefore not be included in this library but you can use any true type font you want by adding them to the assets folder of the demo app (just make sure you don't infringe on someone else's copyright).   
The fonts can be put anywhere in the assets folder (root or subdirectories). Since reading the directory structure of the assets folder during run-time is pretty slow (see [here](http://stackoverflow.com/a/12639530/534471)) a Gradle script generates an index of all ttf files during build time. 

Issues
------

If you have an issues with this library, please open a issue here: https://github.com/1gravity/Android-RTEditor/issues and provide enough information to reproduce it reliably. The following information needs to be provided:

* Which version of the SDK are you using?
* Which Android build are you using? (e.g. MPZ44Q)
* What device are you using?
* What steps will reproduce the problem? (Please provide the minimal reproducible test case.)
* What is the expected output?
* What do you see instead?
* Relevant logcat output.
* Optional: Link to any screenshot(s) that demonstrate the issue (shared privately in Drive.)
* Optional: Link to your APK (either downloadable in Drive or in the Play Store.)

License
-------

Copyright 2015 Emanuel Moecklin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
