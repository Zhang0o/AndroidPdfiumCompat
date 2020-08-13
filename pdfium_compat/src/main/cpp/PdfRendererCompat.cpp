//
// Created by ober on 20-8-11.
//

extern "C" {
#include <unistd.h>
#include <sys/stat.h>
}

#include <cstring>
#include <cerrno>
#include "PdfRendererCompat.h"
#include "log_def.h"
#include <android/bitmap.h>
#include <cstdlib>

static const int RENDER_MODE_FOR_DISPLAY = 1;
static const int RENDER_MODE_FOR_PRINT = 2;

static int sUnmatchedPdfiumInitRequestCount = 0;

typedef struct RGB {
    uint8_t r;
    uint8_t g;
    uint8_t b;
} RGB;

inline int jniThrowException(JNIEnv *env, const char* className, const char* msg) {

    jclass exceptionClass = env->FindClass(className);

    if (exceptionClass == NULL) {
        return -1;
    }

    if (env->ThrowNew(exceptionClass, msg) != JNI_OK) {

    }

    return 0;
}

static int getBlock(void* param, unsigned long position, unsigned char* outBuffer,
             unsigned long size) {
    const int fd = (int) ((long)param);
    const int readCount = pread(fd, outBuffer, size, position);
    if (readCount < 0) {
        LOGE("Cannot read from file descriptor. Error:%d", errno);
        return 0;
    }
    return 1;
}

static void initializeLibraryIfNeeded(JNIEnv* env) {
    if (sUnmatchedPdfiumInitRequestCount == 0) {
        FPDF_InitLibrary();
    }
    sUnmatchedPdfiumInitRequestCount++;
}

static void destroyLibraryIfNeeded(JNIEnv* env, bool handleError) {
    if (sUnmatchedPdfiumInitRequestCount == 1) {
        FPDF_DestroyLibrary();
    }
    sUnmatchedPdfiumInitRequestCount--;
}

static bool forwardPdfiumError(JNIEnv* env) {
    long error = FPDF_GetLastError();
    LOGE("forwardPdfiumError %ld", error);
    switch (error) {
        case FPDF_ERR_SUCCESS:
            return false;
        case FPDF_ERR_FILE:
            jniThrowException(env, "java/io/IOException", "file not found or cannot be opened");
            break;
        case FPDF_ERR_FORMAT:
            jniThrowException(env, "java/io/IOException", "file not in PDF format or corrupted");
            break;
        case FPDF_ERR_PASSWORD:
            jniThrowException(env, "java/lang/SecurityException",
                              "password required or incorrect password");
            break;
        case FPDF_ERR_SECURITY:
            jniThrowException(env, "java/lang/SecurityException", "unsupported security scheme");
            break;
        case FPDF_ERR_PAGE:
            jniThrowException(env, "java/io/IOException", "page not found or content error");
            break;
#ifdef PDF_ENABLE_XFA
        case FPDF_ERR_XFALOAD:
            jniThrowException(env, "java/lang/Exception", "load XFA error");
            break;
        case FPDF_ERR_XFALAYOUT:
            jniThrowException(env, "java/lang/Exception", "layout XFA error");
            break;
#endif  // PDF_ENABLE_XFA
        case FPDF_ERR_UNKNOWN:
        default:
            jniThrowException(env, "java/lang/Exception", "unknown error");
    }
    return true;
}

FPDF_DOCUMENT PdfRendererCompat::create(JNIEnv *env, int fd) {
    struct stat file_state;

    long file_len;
    if(fstat(fd, &file_state) >= 0){
        file_len = (long)(file_state.st_size);
    } else {
        LOGE("Error getting file size");
        return 0;
    }

    LOGD("get pdf file lenght=%ld", file_len);

    initializeLibraryIfNeeded(env);

    FPDF_FILEACCESS loader;
    loader.m_FileLen = file_len;
    loader.m_Param = (void*) fd;
    loader.m_GetBlock = &getBlock;

    FPDF_DOCUMENT document = FPDF_LoadCustomDocument(&loader, NULL);
    LOGD("FPDF_LoadCustomDocument %lx", (long)document);
    if(!document) {
        forwardPdfiumError(env);
        destroyLibraryIfNeeded(env, false);
        return 0;
    }

    return document;
}

void PdfRendererCompat::close(JNIEnv *env, FPDF_DOCUMENT document) {
    FPDF_CloseDocument(document);
    destroyLibraryIfNeeded(env, true);
}

int PdfRendererCompat::getPageCount(JNIEnv *env, FPDF_DOCUMENT document) {

    return FPDF_GetPageCount(document);
}

bool PdfRendererCompat::scaleForPrinting(JNIEnv *env, FPDF_DOCUMENT document) {
    FPDF_BOOL print_scaling = FPDF_VIEWERREF_GetPrintScaling(document);

    return print_scaling ? 1 : 0;
}

void
PdfRendererCompat::renderPage(JNIEnv *env, FPDF_DOCUMENT document, FPDF_PAGE page, jobject bitmap,
                              const float *clip_data, const float *transform_data, jint render_mode) {

    LOGD("renderPage");
    int bmp_format;
    int width, height, stride;
    int ret;

    {
        AndroidBitmapInfo info;

        if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) != 0) {
            LOGE("AndroidBitmap_getInfo failed %d", ret);
            return;
        }

        width = info.width;
        height = info.height;
        bmp_format = info.format;
        stride = info.stride;
        LOGD("bitmap info width=%d height=%d stride=%d", width, height, stride);
    }

    if(bmp_format != ANDROID_BITMAP_FORMAT_RGBA_8888 && bmp_format != ANDROID_BITMAP_FORMAT_RGB_565){
        LOGE("Bitmap format must be RGBA_8888 or RGB_565");
        return;
    }

    void* addr;
    if( (ret = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0){
        LOGE("AndroidBitmap_lockPixels failed: %s", strerror(ret * -1));
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    }

    void *fpdf_bmp_data;
    int fpdf_bmp_format;
    int fpdf_bmp_stride;

    if(bmp_format == ANDROID_BITMAP_FORMAT_RGB_565) {
        fpdf_bmp_data = malloc(width * height * 24);
        fpdf_bmp_format = FPDFBitmap_BGR;
        fpdf_bmp_stride = width * 3;
    } else {
        fpdf_bmp_data = addr;
        fpdf_bmp_format = FPDFBitmap_BGRA;
        fpdf_bmp_stride = stride;
    }

    FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx(width, height, fpdf_bmp_format, fpdf_bmp_data, fpdf_bmp_stride);
    LOGD("FPDFBitmap_CreateEx w=%d, h=%d, format=%d, stride=%d, format=%d", width, height, fpdf_bmp_format, fpdf_bmp_stride, fpdf_bmp_format);

    if(fpdf_bmp_format == FPDFBitmap_BGR) {
        //in rgb565 format, erase bg color to white, it can be change to a custom color.
        FPDFBitmap_FillRect(pdfBitmap, 0, 0, width, height,
                            0xFFFFFFFF);
    }

    FS_MATRIX pdf_matrix;
    FS_RECTF pdf_clip;

    memcpy(&pdf_matrix, transform_data, 6 * sizeof(float));
    memcpy(&pdf_clip, clip_data, 4 * sizeof(float));

    int render_flags = FPDF_REVERSE_BYTE_ORDER;
    if (render_mode == RENDER_MODE_FOR_DISPLAY) {
        render_flags |= FPDF_LCD_TEXT;
    } else if (render_mode == RENDER_MODE_FOR_PRINT) {
        render_flags |= FPDF_PRINTING;
    }

    LOGD("call FPDF_RenderPageBitmapWithMatrix %lx, %lx", (long)pdfBitmap, (long)page);
    FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &pdf_matrix, &pdf_clip, render_flags);
    //FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, 0, 0, render_flags);

    LOGD("pdfBitmap rendered");

    if(bmp_format == ANDROID_BITMAP_FORMAT_RGB_565) {
        int pixel_count = width * height;
        RGB* data_pdf = (RGB*)fpdf_bmp_data;
        uint16_t* data_bmp = (uint16_t*)addr;

        RGB* rgb;
        for(int i = 0; i < pixel_count; i++) {
            rgb = &data_pdf[i];
            data_bmp[i] = ((rgb->r >> 3) << 11) | ((rgb->g >> 2) << 5) | (rgb->b >> 3);
        }

        free(fpdf_bmp_data);
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

FPDF_PAGE
PdfRendererCompat::openPageAndGetSize(JNIEnv *env,FPDF_DOCUMENT document, jint page_index,
                                      float *out_size) {

    FPDF_PAGE page = FPDF_LoadPage(document, page_index);

    if (!page) {
        jniThrowException(env, "java/lang/IllegalStateException",
                          "cannot load page");
        return 0;
    }

    FS_SIZEF fs_size;
    FPDF_BOOL r = FPDF_GetPageSizeByIndexF(document, page_index, &fs_size);
    if(!r) {
        jniThrowException(env, "java/lang/IllegalStateException",
                          "cannot get page size");
    }
    LOGD("page opened %lx width=%f height=%f", (long)page, fs_size.width, fs_size.height);
    if(out_size != 0) {
        out_size[0] = fs_size.width;
        out_size[1] = fs_size.height;
    }

    return page;
}

void PdfRendererCompat::closePage(JNIEnv *env, FPDF_PAGE page) {
    FPDF_ClosePage(page);
}
