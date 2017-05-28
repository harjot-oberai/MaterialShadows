<div align="center"><img src="/screens/cover.png"/></div>

# MaterialShadows
[![Platform](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![by-SDS-MDG](https://img.shields.io/badge/by-SDS%2C%20MDG-blue.svg)](https://mdg.sdslabs.co)

A library for integrating Material shadows seamlessly. 
The library takes material shadows to next level by adding the following features :

- <b>Convex shadows</b> : The shadows are not only rectangular or circular, they can take any convex shape depending on the view and its content.
- <b>Support for shadow offsets</b> : The library allows developers to set <b>X</b> and <b>Y</b> offset for the shadows.
- <b>Support for shadow intensity</b> : The library also has support for setting shadow intensity via `shadowAlpha` attribute.
- <b>Shadows for semi-transparent views</b> : The library allows shadows for semi-transparent views.

# Usage
Just add the following dependency in your app's `build.gradle`
```
dependencies {
      compile 'com.sdsmdg.harjot:materialshadows:0.9.0'
}
```

# Example Usage 1 (Simple)
#### XML
```
<com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ImageView
            android:layout_width="130dp"
            android:layout_height="130dp"
            android:elevation="5dp"
            android:src="@drawable/poly" />

</com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper>
```
#### Result
<img src="/screens/example_1.png"/>

# Example Usage 2 (Offset)
#### XML
```
<com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:shadowOffsetX="-15"
        app:shadowOffsetY="30">

        <ImageView
            android:layout_width="130dp"
            android:layout_height="130dp"
            android:elevation="10dp"
            android:src="@drawable/poly" />

</com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper>
```
#### Result
<img src="/screens/example_2.png"/>

# License
<b>MaterialShadows</b> is licensed under `MIT license`. View [license](LICENSE.md).


