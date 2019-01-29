<p align="center">
    <a href="https://camerakit.website/jpegkit" target="_blank">
        <img alt='JpegKitKit Header' src='.repo/gh-readme-header.png' />
    </a>
</p>

<p align="center">
    <a href="https://www.wonderkiln.com" target="_blank">
        <img alt='JpegKit Header' src='.repo/gh-readme-wk.png'/>
    </a>
    <a href="https://play.google.com/store/apps/details?id=com.camerakit.jpegkit.demo&hl=en" target="_blank">
        <img alt='JpegKit Header' src='.repo/gh-readme-app.png'/>
    </a>
</p>

<p align="center">
    <a href="https://join-slack.camerakit.website"><img src="https://join-slack.camerakit.website/badge.svg" alt="Build Status"></a>
</p>

JpegKit bridges the libjpeg-turbo C++ library into android and wraps it with an easy to use class. You can currently:

- Retrieve metadata such as width and height
- Rotate the JPEG 90, 180, or 270 degrees
- Flip the JPEG horizontally
- Flip the JPEG vertically
- Crop the JPEG to any `Rect`

This is all done without decoding the JPEG to RGB. All operations on the JPEG are done in C++ space and does not touch the Java memory, allowing you to work with extremely large JPEGs without the risk of an `OutOfMemoryException`.

## The State Of The Union
We've been working on a major update to the JpegKit project that we will dub `v0.3.0`. This release separates concerns away from the single Jpeg class to several classes, each handling different functions of JpegKit. 

In a previous release we deprecated the Jpeg class with the intention of having the new functionality ready for the spotlight. 

However we discovered some bugs and issues after the initial release. Until `v0.3.0` is finalized, we've un-deprecated that Jpeg class and its other supporting classes. Below is the intended Setup and Usage for `v0.2.2`. 

## Setup
Add __JpegKit__ to the dependencies block in your `app` level `build.gradle`:

```groovy		
compile 'com.camerakit:jpegkit:0.2.2'
```

## Usage

The core of JpegKit is the Jpeg class. When creating an object of type `Jpeg`, the constructor expects a `byte[]`. In the future you'll be able to pass just a file.

### Constructor
First, create a `Jpeg`:

```java
import jpegkit.Jpeg;

//...

byte[] jpegBytes = ...;
Jpeg mJpeg = new Jpeg(jpegBytes);
```

One can then transform this Jpeg object with the transformations listed in the **Transformations** section below.


### Jpeg result
After you perform your transformations, you can get a new JPEG `byte[]` back:

```java
byte[] newJpegBytes = mJpeg.getJpeg();
```

The `getJpeg()` call can only be used once. At this point our C++ libjpeg-turbo wrapper encodes the new JPEG and also disposes of the original and clears any other memory it used.

Transformations are performed in C++ right when you make the method call, as opposed to doing all after you finish with `getJpeg()`. The transformations will be applied in the order you make the method calls.

## JpegImageView

`JpegImageView` is a view to display JpegKit's `Jpeg` objects.

Create a `JpegImageView` in **xml** as follows.

```xml
<jpegkit.JpegImageView
    android:id="@+id/jpegView"
    android:layout_width="200dp"
    android:layout_height="200dp" />
```

Access and set the JPEG from your Activity as follows.

```java
import jpegkit.JpegImageView;

//...

JpegImageView jpegView = findViewById(R.id.jpegView)
jpegView.setJpeg(mJpeg);
```

## Transformations
### Rotate

Acceptable parameters are `90`, `180`, or `270`.

```java
mJpeg.rotate(int rotation);
```

### Flip Horizontal or Vertical

Flip horizontal:

```java
mJpeg.flipHorizontal();
```

Flip horizontal:

```java
mJpeg.flipVertical();
```

### Crop

Crop extends an `android.graphics.Rect`.

```java
Rect cropRect = new Rect(left, top, right, bottom);
mJpeg.crop(cropRect);
```


## MetaData

You can retrieve a JPEGs width, height and size. This only decodes the JPEGs header information and is very fast.

```java
int width = mJpeg.getWidth();
int height = mJpeg.getHeight();
long size = mJpeg.getJpegSize();
```
---

## License
JpegKit is [MIT License](https://github.com/CameraKit/jpegkit-android/blob/master/LICENSE)
