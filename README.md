<div align="center"><img src="/screens/cover.png"/></div>

# MaterialShadows
[![Platform](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A library for seamlessly integrating Material shadows. 
The library takes existing material shadows to next level by adding the following features :

- <b>Convex shadows</b> : The shadows are not only rectangular or circular, they can take any convex shape depending on the view and its content.
- <b>Support for shadow offsets</b> : The library allows developers to set <b>X</b> and <b>Y</b> offset for the shadows.
- <b>Support for shadow intensity</b> : The library also has support for setting shadow intensity via `shadowAlpha` attribute.
- <b>Shadows for semi-transparent views</b> : The library allows shadows for semi-transparent views.
- <b>Support for Async Shadow calculations</b> : The library allows the operations to be async to avoid blocking the UI thread for long calculations.
- <b>Shadow animations</b> : The library supports fade out animation for shadow.

# Usage
Just add the following dependency in your app's `build.gradle`
```groovy
dependencies {
      implementation 'com.sdsmdg.harjot:materialshadows:1.2.5'
}
```

# Third Party Bindings
<b>[React Native](https://github.com/facebook/react-native)</b><br>
You may now use this library with `react-native` via the module [here](https://github.com/prscX/react-native-material-shadows), built by [Pranav Raj Singh Chauhan](https://github.com/prscX).

# How does this work ?
The `MaterialShadowViewWrapper` is an extension of `Relative Layout`. The `MaterialShadowFrameLayoutWrapper` is an extension of `FrameLayout`. Use any one of them as per your convenience.

All the child views go through the same process of generating shadow as given below : 
1. First a bitmap is generated from the drawing cache of the view.
2. The bitmap is traversed pixel by pixel to remove all transparent pixels and get a list of points corresponding to the actual outline of the content of the view.
3. Since the points corresponding to outline may give a concave path, hence <b>GrahamScan algorithm</b> is used to generate a convex hull of the outline points.
4. A path is created from the points of the resulting convex hull.
5. This path is passed to a `CustomViewOutlineProvider` object that is later attached to the view itself.
6. Hence we get a convex shadow for any type of view based on its content.

<b>P.S. : </b> All the calculations related to graham scan are done asynchronously by default. This behavior can be controlled by `calculateAsync` parameter. (Thanks [Yaroslav!](https://github.com/yarolegovich))

# Example Usage 1 (Simple)
#### XML
```xml
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
```xml
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
```xml
<com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:shadowOffsetX="15"
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

# Example Usage 4 (Semi-transparent views)
#### XML
```xml
<com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:shadowOffsetX="-30"
        app:shadowOffsetY="30">

        <ImageView
            android:layout_width="130dp"
            android:layout_height="130dp"
            android:elevation="10dp"
            android:background="#55000000" />

</com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper>
```
#### Result
<img src="/screens/example_4.png"/>

# Using MaterialShadows with custom ViewGroups
Since the `ShadowGenerator.java` encapsulates all the code related to the generation of convex shadows, it is really easy to plug in convex shadows with any custom ViewGroup or some platform ViewGroup like `LinearLayout` etc.

```java
public class CustomShadowWrapper extends CustomViewGroup {
      
      ShadowGenerator shadowGenerator;
      
      @Override
      protected void onLayout(boolean changed, int l, int t, int r, int b) {
          super.onLayout(changed, l, t, r, b);
          if (shadowGenerator == null) {
              shadowGenerator = new ShadowGenerator(this, offsetX, offsetY, shadowAlpha, shouldShowWhenAllReady, shouldCalculateAsync, shouldAnimateShadow, animationDuration);
          }
          shadowGenerator.generate();
      }
    
      @Override
      protected void onDetachedFromWindow() {
          super.onDetachedFromWindow();
          if (shadowGenerator != null) {
              shadowGenerator.releaseResources();
          }
      }
      
}
```

<b>Note : </b> Make sure to define all the 7 parameters required for `ShadowGenerator`, namely `offsetX`, `offsetY`, `shadowAlpha`, `shouldShowWhenAllReady`, `shouldCalculateAsync`, `shouldAnimateShadow`, `animationDuration` in the custom wrapper and handle changes in their values.

In case any parameter value changes, say `OffsetX`, add the following code inside the setter method for `OffsetX` :
```java
public void setOffsetX(float offsetX) {
      this.offsetX = offsetX;
      if (shadowGenerator != null) {
            shadowGenerator.setOffsetX(offsetX);
      }
}
```

If you want to add custom XML attributes with your `CustomShadowWrapper` class, add this to `attrs.xml` ([here](/materialshadows/src/main/res/values/attrs.xml)) and handle them in your `CustomShadowWrapper` class accordingly.
```xml
<declare-styleable name="CustomShadowWrapper">
        <attr name="shadowOffsetX" />
        <attr name="shadowOffsetY" />
        <attr name="shadowAlpha" />

        <attr name="calculateAsync" />
        <attr name="showWhenAllReady" />
        <attr name="animateShadow" />
        <attr name="animationDuration" />
    </declare-styleable>
```

See [MaterialShadowViewWrapper](/materialshadows/src/main/java/com/sdsmdg/harjot/materialshadows/MaterialShadowViewWrapper.java) for more details.

# Documentation
|XML attribute         |Java set methods              |Description                             | Default Value     |
|----------------------|------------------------------|----------------------------------------|-------------------|
|shadowOffsetX         |setOffsetX(...)               |Set the X-offset of the shadow          |0.0f               |
|shadowOffsetY         |setOffsetX(...)               |Set the Y-offset of the shadow          |0.0f               |
|shadowAlpha           |setShadowAlpha(...)           |Set the value of shadow intensity (alpha)|1.0f              |
|calculateAsync        |setShouldCalculateAsync(...)  |Set the flag for async shadow calculations. |true           |
|showWhenAllReady      |setShowShadowsWhenAllReady(...)|Set the flag for showing all shadows after all calculations are over|true|
|animateShadow         |setShouldAnimateShadow(...)   |Set the flag for shadow animation        |true              |
|animationDuration     |setAnimationDuration(...)     |Set the value of shadow animation duration.|300ms           |

# Limitations
1. Since the bitmap is traversed pixel by pixel, the performance for large views is bad. Hence the use of the library is limited to small views.
2. Currently the shadow is generated only for direct children of the `MaterialShadowViewWrapper`. Hence if the desired views are placed inside a Linear Layout or some other view group, then each view must be wrapped by a seperate `MaterialShadowViewWrapper` or `MaterialShadowFrameLayoutWrapper` or a custom view group wrapper may be implemented.
3. Each child of `MaterialShadowViewWrapper` or custom view group wrapper is assigned the same offset and shadow intensity. If fine control over every view's shadow is required then it must be wrapped inside its own `MaterialShadowViewWrapper` or `MaterialShadowFrameLayoutWrapper`.

# Credits
1. [Yaroslav](https://github.com/yarolegovich) : Implementation of asynchronous calculations and shadow animations.
2. [Pranav Raj Singh Chauhan](https://github.com/prscX) : Building react-native bridge plugin for the library.


# License
<b>MaterialShadows</b> is licensed under `MIT license`. View [license](LICENSE.md).
