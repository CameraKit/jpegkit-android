#include "platform.h"

typedef struct { tjhandle handle; } Command;

static tjtransform *getTransform(JNIEnv *env, jobject jTransform) {
    tjtransform *transform = new tjtransform();

    jclass jTransformCls = env->FindClass("libjpeg/TurboJpeg$Transform");

    jfieldID opFieldID = env->GetFieldID(jTransformCls, "op", "I");
    jfieldID optionsFieldID = env->GetFieldID(jTransformCls, "options", "I");

    transform->op = env->GetIntField(jTransform, opFieldID);
    transform->options = env->GetIntField(jTransform, optionsFieldID);

    jfieldID rFieldID = env->GetFieldID(jTransformCls, "r", "Llibjpeg/TurboJpeg$Transform$Region;");
    jobject jRegion = env->GetObjectField(jTransform, rFieldID);

    if (jRegion) {
        tjregion r;

        jclass jRegionCls = env->FindClass("libjpeg/TurboJpeg$Transform$Region");
        jfieldID rxFieldID = env->GetFieldID(jRegionCls, "x", "I");
        jfieldID ryFieldID = env->GetFieldID(jRegionCls, "y", "I");
        jfieldID rwFieldID = env->GetFieldID(jRegionCls, "w", "I");
        jfieldID rhFieldID = env->GetFieldID(jRegionCls, "h", "I");

        r.x = env->GetIntField(jRegion, rxFieldID);
        r.y = env->GetIntField(jRegion, ryFieldID);
        r.w = env->GetIntField(jRegion, rwFieldID);
        r.h = env->GetIntField(jRegion, rhFieldID);

        transform->r = r;
    }

    return transform;
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_TJPAD(JNIEnv *env, jclass clazz, jint width) {
    return TJPAD(width);
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_TJSCALED(JNIEnv *env, jclass clazz, jint dimension, jintArray scalefactor) {
    jint scalefactorElements[2];
    env->GetIntArrayRegion(scalefactor, 0, 2, scalefactorElements);
    tjscalingfactor factor = {scalefactorElements[0], scalefactorElements[1]};
    return TJSCALED(dimension, factor);
}

extern "C" JNIEXPORT jlong JNICALL
Java_libjpeg_TurboJpeg_tjInitCompress(JNIEnv *env, jclass clazz) {
    Command *command = new Command();
    command->handle = tjInitCompress();
    return (long) command;
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_tjCompress2(JNIEnv *env, jclass clazz, jlong cmdHandle, jlong srcHandle,
                                   jint width, jint pitch, jint height, jint pixelFormat,
                                   jlong dstHandle, jlongArray jpegSizeDst, jint jpegSubsamp,
                                   jint jpegQual, jint flags) {
    Command *command = (Command *) cmdHandle;

    Allocation *srcAlloc = (Allocation *) srcHandle;
    unsigned char *srcBuf = srcAlloc->bytes;

    Allocation *dstAlloc = (Allocation *) dstHandle;
    unsigned char *dstBuf = dstAlloc->bytes;

    unsigned long jpegSize;

    int status = tjCompress2(
            command->handle,
            srcBuf,
            width,
            pitch,
            height,
            pixelFormat,
            &dstBuf,
            &jpegSize,
            jpegSubsamp,
            jpegQual,
            flags
    );

    jlong jpegSizeDstElements[1];
    jpegSizeDstElements[0] = jpegSize;

    env->SetLongArrayRegion(jpegSizeDst, 0, 1, jpegSizeDstElements);

    return status;
}

extern "C" JNIEXPORT jlong JNICALL
Java_libjpeg_TurboJpeg_tjBufSize(JNIEnv *env, jclass clazz, jint width, jint height, jint jpegSubsamp) {
    return tjBufSize(width, height, jpegSubsamp);
}

extern "C" JNIEXPORT jlong JNICALL
Java_libjpeg_TurboJpeg_tjBufSizeYUV(JNIEnv *env, jclass clazz, jint width, jint height, jint subsamp) {
    return tjBufSizeYUV(width, height, subsamp);
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_tjEncodeYUV2(JNIEnv *env, jclass clazz, jlong cmdHandle, jlong srcHandle,
                                    jint width, jint pitch, jint height, jint pixelFormat,
                                    jlong dstHandle, jint subsamp, jint flags) {
    Command *command = (Command *) cmdHandle;

    Allocation *srcAlloc = (Allocation *) srcHandle;
    unsigned char *srcBuf = srcAlloc->bytes;

    Allocation *dstAlloc = (Allocation *) dstHandle;
    unsigned char *dstBuf = dstAlloc->bytes;

    int status = tjEncodeYUV2(
            command->handle,
            srcBuf,
            width,
            pitch,
            height,
            pixelFormat,
            dstBuf,
            subsamp,
            flags
    );

    return status;
}

extern "C" JNIEXPORT jlong JNICALL
Java_libjpeg_TurboJpeg_tjInitDecompress(JNIEnv *env, jclass clazz) {
    Command *command = new Command();
    command->handle = tjInitDecompress();
    return (long) command;
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_tjDecompressHeader2(JNIEnv *env, jclass clazz, jlong cmdHandle,
                                           jlong srcHandle, jlong jpegSize, jintArray outputs) {
    Command *command = (Command *) cmdHandle;
    Allocation *srcAlloc = (Allocation *) srcHandle;
    unsigned char *srcBuf = srcAlloc->bytes;

    int width;
    int height;
    int jpegSubsamp;

    int status = tjDecompressHeader2(
            command->handle,
            srcBuf,
            (unsigned long) jpegSize,
            &width,
            &height,
            &jpegSubsamp
    );

    jint outputsElements[3];
    outputsElements[0] = (jint) width;
    outputsElements[1] = (jint) height;
    outputsElements[2] = (jint) jpegSubsamp;

    env->SetIntArrayRegion(outputs, 0, 3, outputsElements);

    return status;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_libjpeg_TurboJpeg_tjGetScalingFactors(JNIEnv *env, jclass clazz) {
    int numscalingfactors;
    tjscalingfactor *scalingfactors = tjGetScalingFactors(&numscalingfactors);

    jclass intArrayCls = env->FindClass("[I");
    jobjectArray outputs = env->NewObjectArray(numscalingfactors, intArrayCls, NULL);

    for (int i = 0; i < numscalingfactors; i++) {
        tjscalingfactor scalingfactor = scalingfactors[i];
        jintArray scalingfactorOutputs = env->NewIntArray(2);

        int scalingfactorValues[] = {scalingfactor.num, scalingfactor.denom};
        env->SetIntArrayRegion(scalingfactorOutputs, 0, 2, scalingfactorValues);
        env->SetObjectArrayElement(outputs, i, scalingfactorOutputs);
    }

    return outputs;
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_tjDecompress2(JNIEnv *env, jclass clazz, jlong cmdHandle, jlong srcHandle,
                                     jlong jpegSize, jlong dstHandle, jint width, jint pitch,
                                     jint height, jint pixelFormat, jint flags) {
    Command *command = (Command *) cmdHandle;

    Allocation *srcAlloc = (Allocation *) srcHandle;
    unsigned char *srcBuf = srcAlloc->bytes;

    Allocation *dstAlloc = (Allocation *) dstHandle;
    unsigned char *dstBuf = dstAlloc->bytes;

    int status = tjDecompress2(
            command->handle,
            srcBuf,
            (unsigned long) jpegSize,
            dstBuf,
            width,
            pitch,
            height,
            pixelFormat,
            flags
    );

    return status;
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_tjDecompressToYUV(JNIEnv *env, jclass clazz, jlong cmdHandle, jlong srcHandle,
                                         jlong jpegSize, jlong dstHandle, jint flags) {
    Command *command = (Command *) cmdHandle;

    Allocation *srcAlloc = (Allocation *) srcHandle;
    unsigned char *srcBuf = srcAlloc->bytes;

    Allocation *dstAlloc = (Allocation *) dstHandle;
    unsigned char *dstBuf = dstAlloc->bytes;

    int status = tjDecompressToYUV(
            command->handle,
            srcBuf,
            (unsigned long) jpegSize,
            dstBuf,
            flags
    );

    return status;
}

extern "C" JNIEXPORT jlong JNICALL
Java_libjpeg_TurboJpeg_tjInitTransform(JNIEnv *env, jclass clazz) {
    Command *command = new Command();
    command->handle = tjInitTransform();
    return (long) command;
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_tjTransform(JNIEnv *env, jclass clazz, jlong cmdHandle, jlong srcHandle,
                                   jlong jpegSize, jint n, jlongArray dstHandles,
                                   jlongArray sizeOutputs, jobjectArray jTransforms, jint flags) {
    Command *command = (Command *) cmdHandle;

    Allocation *srcAlloc = (Allocation *) srcHandle;
    unsigned char *srcBuf = srcAlloc->bytes;

    unsigned char *dstBufs[n];
    jlong dstHandlesElements[n];

    env->GetLongArrayRegion(dstHandles, 0, n, dstHandlesElements);

    for (int i = 0; i < n; i++) {
        Allocation *dstAlloc = (Allocation *) dstHandlesElements[i];
        dstBufs[i] = dstAlloc->bytes;
    }

    unsigned long dstSizes[n];

    int numTransforms = env->GetArrayLength(jTransforms);
    tjtransform transforms[numTransforms];
    for (int i = 0; i < numTransforms; i++) {
        transforms[i] = *getTransform(env, env->GetObjectArrayElement(jTransforms, i));
    }

    int status = tjTransform(
            command->handle,
            srcBuf,
            (unsigned long) jpegSize,
            n,
            dstBufs,
            dstSizes,
            transforms,
            flags
    );

    jlong sizeOutputsElements[n];
    for (int i = 0; i < n; i++) sizeOutputsElements[i] = dstSizes[i];
    env->SetLongArrayRegion(sizeOutputs, 0, n, sizeOutputsElements);

    return status;
}

extern "C" JNIEXPORT jint JNICALL
Java_libjpeg_TurboJpeg_tjDestroy(JNIEnv *env, jclass clazz, jlong cmdHandle) {
    Command *command = (Command *) cmdHandle;
    int status = tjDestroy(command->handle);
    delete command;
    return status;
}

extern "C" JNIEXPORT jlong JNICALL
Java_libjpeg_TurboJpeg_tjAlloc(JNIEnv *env, jclass clazz, jint size) {
    Allocation *allocation = new Allocation();
    allocation->bytes = tjAlloc(size);
    return (long) allocation;
}

extern "C" JNIEXPORT void JNICALL
Java_libjpeg_TurboJpeg_tjFree(JNIEnv *env, jclass clazz, jlong allocHandle) {
    Allocation *allocation = (Allocation *) allocHandle;
    tjFree(allocation->bytes);
    delete allocation;
}

extern "C" JNIEXPORT jstring JNICALL
Java_libjpeg_TurboJpeg_tjGetErrorStr(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(tjGetErrorStr());
}

extern "C" JNIEXPORT jlong JNICALL
Java_libjpeg_TurboJpeg_tjwSrcToAlloc(JNIEnv *env, jclass clazz, jlong allocHandle, jbyteArray src) {
    Allocation *allocation = (Allocation *) allocHandle;
    int srcLength = env->GetArrayLength(src);
    allocation->bytes = tjAlloc(srcLength);
    env->GetByteArrayRegion(src, 0, srcLength, reinterpret_cast<jbyte *>(allocation->bytes));
    return (long) allocation;
}

extern "C" JNIEXPORT void JNICALL
Java_libjpeg_TurboJpeg_tjwAllocToDst(JNIEnv *env, jclass clazz, jlong allocHandle, jbyteArray dst) {
    Allocation *allocation = (Allocation *) allocHandle;
    int dstLength = env->GetArrayLength(dst);
    env->SetByteArrayRegion(dst, 0, dstLength, reinterpret_cast<jbyte *>(allocation->bytes));
}
