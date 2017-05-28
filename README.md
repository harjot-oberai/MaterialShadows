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
      compile 'com.sdsmdg.harjot:materialshadows:1.0.0'
}
```

# How does this work ?
The `MaterialShadowViewWrapper` is an extension of `Relative Layout`. All the child views go through the same process of generating shadow as given below : 
1. First a bitmap is generated from the drawing cache of the view.
2. The bitmap is traversed pixel by pixel to remove all transparent pixels and get a list of points corresponding to the actual outline of the content of the view.
3. Since the points corresponding to outline may give a concave path, hence <b>GrahamScan algorithm</b> is used to generate a convex hull of the outline points.
4. A path is created from the points from the resulting convex hull.
5. This path is passed to a `CustomViewOutlineProvider` object that is later attached to the view itself.
6. Hence we get a convex shadow for any type of view based on its content.

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

# Example Usage 3 (Shadow intensity)
#### XML
```
<com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:shadowOffsetX="-15"
        app:shadowOffsetY="30"
        app:shadowAlpha="0.9">

        <ImageView
            android:layout_width="130dp"
            android:layout_height="130dp"
            android:elevation="10dp"
            android:src="@drawable/poly" />

</com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper>
```
#### Result
<img src="/screens/example_3.png"/>

# Documentation
|XML attribute         |Description                             | Default Value     |
|----------------------|----------------------------------------|-------------------|
|shadowOffsetX         |Set the X-offset of the shadow          |0.0f               |
|shadowOffsetY         |Set the Y-offset of the shadow          |0.0f               |
|shadowAlpha           |Set the value of shadow intensity (alpha)        |1.0f      |

# Limitations
1. Since the bitmap is traversed pixel by pixel, the performance for large views is bad. Hence the use of the library is limited to small views.
2. Currently the shadow is generated only for direct children of the `MaterialShadowViewWrapper`. Hence if the desired are views are placed inside a Linear Layout or some other view group, then each view must be wrapped by seperate `MaterialShadowViewWrapper`. This doesn't affect performance as the number of operations are still the same, but affects the quality of code.
3. Each child of `MaterialShadowViewWrapper` is assigned same offset and shadow intensity. If fine control over every view's shadow is required then it must be wrapped inside its own `MaterialShadowViewWrapper`. Again this doesn't affect the performance, just the quality of code.

# License
<b>MaterialShadows</b> is licensed under `MIT license`. View [license](LICENSE.md).


