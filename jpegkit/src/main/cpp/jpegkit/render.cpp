#include "platform.h"
#include <memory>
#include <android/native_window.h>
#include <android/native_activity.h>
#include <android/surface_texture.h>
#include <android/native_window_jni.h>

extern "C" JNIEXPORT void JNICALL
Java_jpegkit_JpegView_renderJpeg(JNIEnv *env, jobject obj, jobject surface, jlong allocHandle, jlong jpegSize, jint width, jint height) {
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_setBuffersGeometry(window, width, height, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer windowBuffer;
    if (ANativeWindow_lock(window, &windowBuffer, NULL) == 0) {
        Allocation *alloc = (Allocation *) allocHandle;
        unsigned char *jpegBuf = alloc->bytes;
        memcpy(windowBuffer.bits, jpegBuf, (size_t) jpegSize);
        ANativeWindow_unlockAndPost(window);
    }
}


