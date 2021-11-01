#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <jpeglib.h>
#include <setjmp.h>
#include "log.h"

typedef u_int8_t BYTE;

int jpegCompress(BYTE *data, uint32_t width, uint32_t height, jint quality, const char *file,
                 jboolean optimize);

struct my_error_mgr {
    struct jpeg_error_mgr pub;
    jmp_buf setjmp_buffer;
};
typedef struct my_error_mgr *my_error_ptr;

// 使用libjpeg解压文件时难免产生错误，原因可能是图片文件损坏、io错误、内存不足等等。
// 默认的错误处理函数会调用exit()函数，导致整个进程结束，这对用户来说是非常不友好的。
// 我们需要注册自定义错误处理函数，改变此行为。
METHODDEF(void)
my_error_exit(j_common_ptr cinfo) {
    my_error_ptr myerr = (my_error_ptr) cinfo->err;
    (*cinfo->err->output_message)(cinfo);
    LOGW("jpeg_message_table[%d]:%s",
         myerr->pub.msg_code, myerr->pub.jpeg_message_table[myerr->pub.msg_code]);
    longjmp(myerr->setjmp_buffer, 1);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_wangxingxing_imagecompressor_CompressUtils_nativeCompress(JNIEnv *env, jobject thiz,
                                                                   jobject bitmap, jint quality,
                                                                   jstring dest_file_path,
                                                                   jboolean optimize) {
    const char *destFile = env->GetStringUTFChars(dest_file_path, NULL);
    AndroidBitmapInfo bitmapInfo;
    int ret;
    BYTE *pixelsColor;
    // 把Bitmap对象中的像素数据，获取出来，得到ARGB
    // 解码Android Bitmap信息
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGD("AndroidBitmap_getInfo() failed error=%d", ret);
        return ret;
    }

    // 通过Android Native层的 API 获取 Bitmap对象对应的像素数据
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixelsColor)) < 0) {
        LOGD("AndroidBitmap_lockPixels() failed error=%d", ret);
        return ret;
    }

    BYTE r, g, b;
    int color;
    // width * height 个像素，每个像素3个字节
    BYTE *data = (BYTE *) malloc(bitmapInfo.width * bitmapInfo.height * 3);
    BYTE *tmpData = data;
    // 从pixelsColor数组中取出像素数据，获取RGB的值
    for (int i = 0; i < bitmapInfo.height; ++i) {
        for (int j = 0; j < bitmapInfo.width; ++j) {
            //只处理 RGBA_8888
            if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
                // 0x2312faff  -> 588446463
                // 一个int 4个字节，读取得到的颜色值为一个int值，也就是color的值
                color = (*(int *) (pixelsColor));
                // 从 color 值中读取 RGBA的值
                b = (color >> 16) & 0xFF;
                g = (color >> 8) & 0xFF;
                r = (color >> 0) & 0xFF;
                *data = r;
                *(data + 1) = g;
                *(data + 2) = b;

                data += 3;
                // 移动步长4个字节
                pixelsColor += 4;
            } else {
                return -2;
            }
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    // 把像素数据，给到libjpeg去进行压缩，压缩完成之后，libjpeg写入磁盘文件
    ret = jpegCompress(tmpData, bitmapInfo.width, bitmapInfo.height, quality, destFile, optimize);
    free((void *) tmpData);

    env->ReleaseStringUTFChars(dest_file_path, destFile);
    return 0;
}

// 传入像素数组，通过libjpeg压缩生成一个本地文件
int jpegCompress(BYTE *data, uint32_t width, uint32_t height, jint quality, const char *file, jboolean optimize) {
    jpeg_compress_struct jcs;
    //自定义的error
    my_error_mgr jem;

    jcs.err = jpeg_std_error(&jem.pub);
    jem.pub.error_exit = my_error_exit;

    // 建立setjmp返回上下文，以供my_error_exit使用
    if (setjmp(jem.setjmp_buffer)) {
        return 0;
    }

    //为jcs分配空间并初始化
    jpeg_create_compress(&jcs);
    //打开文件
    FILE *f = fopen(file, "wb");
    if (f == NULL) {
        return 0;
    }

    //指定压缩数据源
    jpeg_stdio_dest(&jcs, f);
    jcs.image_width = width;
    jcs.image_height = height;

    // false 代表使用Huffman算法
    jcs.arith_code = false;
    // 色彩通道数（每像素采样)，1代表灰度图，3代表彩色位图图像
    int nComponent = 3;
    jcs.input_components = nComponent;
    // JCS_GRAYSCALE表示灰度图，JCS_RGB代表彩色位图图像
    jcs.in_color_space = JCS_RGB;

    jpeg_set_defaults(&jcs);
    // 如果设置optimize_coding为TRUE，将会使得压缩图像过程中基于图像数据计算哈弗曼表
    jcs.optimize_coding = optimize;

    //为压缩设定参数，包括图像大小，颜色空间
    jpeg_set_quality(&jcs, quality, true);
    //开始压缩
    jpeg_start_compress(&jcs, true);
    // 一行像素的字节个数
    int row_stride = jcs.image_width * nComponent;
    JSAMPROW row_point[1];
    // 从 data 数组中读取 rgb数据，一行一行读取，一行的数据总量为 image_width * 3 （一个像素，有RGB 3个字节）
    while (jcs.next_scanline < jcs.image_height) {
        row_point[0] = &data[jcs.next_scanline * row_stride];
        jpeg_write_scanlines(&jcs, row_point, 1);
    }

    if (jcs.optimize_coding) {
        LOGI("使用了哈夫曼算法完成压缩");
    } else {
        LOGI("未使用哈夫曼算法");
    }
    //压缩完毕
    jpeg_finish_compress(&jcs);
    //释放资源
    jpeg_destroy_compress(&jcs);
    fclose(f);
    return 1;
}