#include <jni.h>
#include <stdio.h>
#include <cstring>
#include <unistd.h>
#include <turbojpeg.h>

extern "C"
{
JNIEXPORT jobject JNICALL
Java_com_jpegkit_Jpeg_jniMount
        (JNIEnv *env, jobject obj, jbyteArray jpegBytes);

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniRelease
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT jbyteArray JNICALL
Java_com_jpegkit_Jpeg_jniGetJpegBytes
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT jlong JNICALL
Java_com_jpegkit_Jpeg_jniGetJpegSize
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT jint JNICALL
Java_com_jpegkit_Jpeg_jniGetWidth
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT jint JNICALL
Java_com_jpegkit_Jpeg_jniGetHeight
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniRotate
        (JNIEnv *env, jobject obj, jobject handle, jint degrees);

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniFlipHorizontal
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniFlipVertical
        (JNIEnv *env, jobject obj, jobject handle);

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniCrop
        (JNIEnv *env, jobject obj, jobject handle, jint left, jint top, jint right, jint bottom);
}

class Jpeg {
public:
    unsigned char *buffer;
    unsigned long size;

    tjhandle decompressor;
    tjhandle transformer;

    Jpeg() {
        buffer = NULL;
        size = 0;

        decompressor = NULL;
        transformer = NULL;
    }
};

JNIEXPORT jobject JNICALL
Java_com_jpegkit_Jpeg_jniMount
        (JNIEnv *env, jobject obj, jbyteArray jpegBytes) {
    int jpegSize = env->GetArrayLength(jpegBytes);

    unsigned char *jpegBuffer = tjAlloc(jpegSize);
    env->GetByteArrayRegion(jpegBytes, 0, jpegSize, reinterpret_cast<jbyte *>(jpegBuffer));

    Jpeg *jpeg = new Jpeg();

    jpeg->buffer = jpegBuffer;
    jpeg->size = (unsigned long) jpegSize;
    jpeg->decompressor = tjInitDecompress();
    jpeg->transformer = tjInitTransform();

    return env->NewDirectByteBuffer(jpeg, 0);
}


JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniRelease
        (JNIEnv *env, jobject obj, jobject handle) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    tjFree(jpeg->buffer);
    jpeg->buffer = NULL;
    jpeg->size = 0;

    tjDestroy(jpeg->decompressor);
    jpeg->decompressor = NULL;

    tjDestroy(jpeg->transformer);
    jpeg->transformer = NULL;
}

JNIEXPORT jbyteArray JNICALL
Java_com_jpegkit_Jpeg_jniGetJpegBytes
        (JNIEnv *env, jobject obj, jobject handle) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    jbyteArray array = env->NewByteArray((jsize) jpeg->size);
    env->SetByteArrayRegion(array, 0, (jsize) jpeg->size, reinterpret_cast<jbyte *>(jpeg->buffer));
    return array;
}

JNIEXPORT jlong JNICALL
Java_com_jpegkit_Jpeg_jniGetJpegSize
        (JNIEnv *env, jobject obj, jobject handle) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);
    return jpeg->size;
}

JNIEXPORT jint JNICALL
Java_com_jpegkit_Jpeg_jniGetWidth
        (JNIEnv *env, jobject obj, jobject handle) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    int width, height, subsampling;
    int status = tjDecompressHeader2(jpeg->decompressor, jpeg->buffer, jpeg->size, &width, &height, &subsampling);

    if (status != 0) {
        return -1;
    }

    return width;
}

JNIEXPORT jint JNICALL
Java_com_jpegkit_Jpeg_jniGetHeight
        (JNIEnv *env, jobject obj, jobject handle) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    int width, height, subsampling;
    int status = tjDecompressHeader2(jpeg->decompressor, jpeg->buffer, jpeg->size, &width, &height, &subsampling);

    if (status != 0) {
        return -1;
    }

    return height;
}

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniRotate
        (JNIEnv *env, jobject obj, jobject handle, jint degrees) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    tjtransform *transform = new tjtransform();
    if (degrees == 90) {
        transform->op = TJXOP_ROT90;
    } else if (degrees == 180) {
        transform->op = TJXOP_ROT180;
    } else if (degrees == 270) {
        transform->op = TJXOP_ROT270;
    }

    tjTransform(jpeg->transformer, jpeg->buffer, jpeg->size, 1, &jpeg->buffer, &jpeg->size, transform, 0);
}

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniFlipHorizontal
        (JNIEnv *env, jobject obj, jobject handle) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    tjtransform *transform = new tjtransform();
    transform->op = TJXOP_HFLIP;

    tjTransform(jpeg->transformer, jpeg->buffer, jpeg->size, 1, &jpeg->buffer, &jpeg->size, transform, 0);
}

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniFlipVertical
        (JNIEnv *env, jobject obj, jobject handle) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    tjtransform *transform = new tjtransform();
    transform->op = TJXOP_VFLIP;

    tjTransform(jpeg->transformer, jpeg->buffer, jpeg->size, 1, &jpeg->buffer, &jpeg->size, transform, 0);
}

JNIEXPORT void JNICALL
Java_com_jpegkit_Jpeg_jniCrop
        (JNIEnv *env, jobject obj, jobject handle, jint left, jint top, jint width, jint height) {
    Jpeg *jpeg = (Jpeg *) env->GetDirectBufferAddress(handle);

    tjtransform *transform = new tjtransform();
    tjregion cropRegion;
    cropRegion.x = left - (left % 16);
    cropRegion.y = top - (top % 16);
    cropRegion.w = width;
    cropRegion.h = height;

    transform->r = cropRegion;
    transform->options = TJXOPT_CROP;

    tjTransform(jpeg->transformer, jpeg->buffer, jpeg->size, 1, &jpeg->buffer, &jpeg->size, transform, 0);
}