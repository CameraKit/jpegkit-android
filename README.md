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

## Setup
Add __JpegKit__ to the dependencies block in your `app` level `build.gradle`:

```groovy		
compile 'com.camerakit:jpegkit:0.0.1'
```

## Usage

JpegKit currently expects a JPEG parameter in the form of a `byte[]`. In the future you'll be able to just pass a file.

First, create a `JpegTransformer`:

```java
byte[] jpeg = ...;
JpegTransformer jpegTransformer = new JpegTransformer(jpeg);
```

After you perform your transformations, you can get a new JPEG `byte[]` back:

```java
byte[] newJpeg = jpegTransformer.getJpeg();
```

The `getJpeg()` call can only be used once. At this point our C++ libjpeg-turbo wrapper encodes the new JPEG and also disposes of the original and clears any other memory it used.

Transformations are performed in C++ right when you make the method call, as opposed to doing all after you finish with `getJpeg()`. The transformations will be applied in the order you make the method calls.

### MetaData

You can currently retrieve a JPEGs width and height. This only decodes the JPEGs header information and is very fast.

```java
int width = jpegTransformer.getWidth();
int height = jpegTransformer.getHeight();
```

### Rotate

Acceptable parameters are `90`, `180`, or `270`.

```java
jpegTransformer.rotate(90);
```

### Flip Horizontal or Vertical

Flip horizontal:

```java
jpegTransformer.flipHorizontal();
```

Flip horizontal:

```java
jpegTransformer.flipVertical();
```

### Crop

Crop extends an `android.graphics.Rect`.

```java
Rect cropRect = new Rect(left, top, right, bottom);
jpegTransformer.crop(cropRect);
```

---

## License
JpegKit is [MIT License](https://github.com/CameraKit/jpegkit-android/blob/master/LICENSE)
