package libjpeg

class TurboJpeg {

    companion object {

        const val TJ_SUBSAMP: Int = 5
        const val TJSAMP_444: Int = 0
        const val TJSAMP_422: Int = 1
        const val TJSAMP_420: Int = 2
        const val TJSAMP_GRAY: Int = 3
        const val TJSAMP_440: Int = 4

        const val TJ_NUMPF: Int = 11
        const val TJPF_RGB: Int = 0
        const val TJPF_BGR: Int = 1
        const val TJPF_RGBX: Int = 2
        const val TJPF_BGRX: Int = 3
        const val TJPF_XBGR: Int = 4
        const val TJPF_XRGB: Int = 5
        const val TJPF_GRAY: Int = 6
        const val TJPF_RGBA: Int = 7
        const val TJPF_BGRA: Int = 8
        const val TJPF_ABGR: Int = 9
        const val TJPF_ARGB: Int = 10

        const val TJFLAG_BOTTOMUP: Int = 2
        const val TJFLAG_FORCEMMX: Int = 8
        const val TJFLAG_FORCESSE: Int = 16
        const val TJFLAG_FORCESSE2: Int = 32
        const val TJFLAG_FORCESSE3: Int = 128
        const val TJFLAG_FASTUPSAMPLE: Int = 256
        const val TJFLAG_NOREALLOC: Int = 1024

        const val TJ_NUMXOP: Int = 8
        const val TJXOP_NONE: Int = 0
        const val TJXOP_HFLIP: Int = 1
        const val TJXOP_VFLIP: Int = 2
        const val TJXOP_TRANSPOSE: Int = 3
        const val TJXOP_TRANSVERSE: Int = 4
        const val TJXOP_ROT90: Int = 5
        const val TJXOP_ROT180: Int = 6
        const val TJXOP_ROT270: Int = 7

        const val TJXOPT_PERFECT: Int = 1
        const val TJXOPT_TRIM: Int = 2
        const val TJXOPT_CROP: Int = 4
        const val TJXOPT_GRAY: Int = 8
        const val TJXOPT_NOOUTPUT: Int = 16

        @JvmField
        val tjRedOffset: IntArray = intArrayOf(0, 2, 0, 2, 3, 1, 0, 0, 2, 3, 1)

        @JvmField
        val tjGreenOffset: IntArray = intArrayOf(1, 1, 1, 1, 2, 2, 0, 1, 1, 2, 2)

        @JvmField
        val tjBlueOffset: IntArray = intArrayOf(2, 0, 2, 0, 1, 3, 0, 2, 0, 1, 3)

        @JvmField
        val tjPixelSize: IntArray = intArrayOf(3, 3, 4, 4, 4, 4, 1, 4, 4, 4, 4)

        @JvmField
        val tjMCUWidth: IntArray = intArrayOf(8, 16, 16, 8, 8)

        @JvmField
        val tjMCUHeight: IntArray = intArrayOf(8, 8, 16, 8, 16)

        @JvmStatic
        external fun TJPAD(width: Int): Int

        @JvmStatic
        external fun TJSCALED(dimension: Int, scalefactor: IntArray): Int

        @JvmStatic
        external fun tjInitCompress(): Long

        @JvmStatic
        external fun tjCompress2(cmdHandle: Long,
                                 srcHandle: Long,
                                 width: Int,
                                 pitch: Int,
                                 height: Int,
                                 pixelFormat: Int,
                                 dstHandle: Long,
                                 jpegSizeDst: LongArray,
                                 jpegSubsamp: Int,
                                 jpegQual: Int,
                                 flags: Int): Int

        @JvmStatic
        external fun tjBufSize(width: Int, height: Int, jpegSubsamp: Int): Long

        @JvmStatic
        external fun tjBufSizeYUV(width: Int, height: Int, subsamp: Int): Long

        @JvmStatic
        external fun tjEncodeYUV2(cmdHandle: Long,
                                  srcHandle: Long,
                                  width: Int,
                                  pitch: Int,
                                  height: Int,
                                  pixelFormat: Int,
                                  dstHandle: Long,
                                  subsamp: Int,
                                  flags: Int): Int

        @JvmStatic
        external fun tjInitDecompress(): Long

        @JvmStatic
        external fun tjDecompressHeader2(cmdHandle: Long,
                                         srcHandle: Long,
                                         jpegSize: Long,
                                         outputs: IntArray): Int

        @JvmStatic
        external fun tjGetScalingFactors(): Array<IntArray>

        @JvmStatic
        external fun tjDecompress2(cmdHandle: Long,
                                   srcHandle: Long,
                                   jpegSize: Long,
                                   dstHandle: Long,
                                   width: Int,
                                   pitch: Int,
                                   height: Int,
                                   pixelFormat: Int,
                                   flags: Int): Int

        @JvmStatic
        external fun tjDecompressToYUV(cmdHandle: Long,
                                       srcHandle: Long,
                                       jpegSize: Long,
                                       dstHandle: Long,
                                       flags: Int): Int

        @JvmStatic
        external fun tjInitTransform(): Long

        @JvmStatic
        external fun tjTransform(cmdHandle: Long,
                                 srcHandle: Long,
                                 jpegSize: Long,
                                 n: Int,
                                 dstHandles: LongArray,
                                 sizeOutputs: LongArray,
                                 transforms: Array<Transform>,
                                 flags: Int): Int

        @JvmStatic
        external fun tjDestroy(cmdHandle: Long): Int

        @JvmStatic
        external fun tjAlloc(size: Int): Long

        @JvmStatic
        external fun tjFree(allocHandle: Long)

        @JvmStatic
        external fun tjGetErrorStr(): String?

        @JvmStatic
        external fun tjwAllocToDst(allocHandle: Long, dst: ByteArray)

        @JvmStatic
        external fun tjwSrcToAlloc(allocHandle: Long, src: ByteArray): Long

        init {
            System.loadLibrary("jpegkit")
        }

    }

    class Transform {

        @JvmField
        var r: Region? = null

        @JvmField
        var op: Int = 0

        @JvmField
        var options: Int = 0

        class Region(@JvmField var x: Int = 0,
                     @JvmField var y: Int = 0,
                     @JvmField var w: Int = 0,
                     @JvmField var h: Int = 0)

    }

}