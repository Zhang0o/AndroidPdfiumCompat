//
// Created by ober on 20-8-11.
//

#ifndef ANDROIDPDFIUMCOMPAT_PDFRENDERERCOMPAT_H
#define ANDROIDPDFIUMCOMPAT_PDFRENDERERCOMPAT_H

#include <jni.h>
#include "fpdfview.h"

class PdfRendererCompat {

public:

    static FPDF_DOCUMENT create(JNIEnv *env, int fd);

    static void close(JNIEnv *env, FPDF_DOCUMENT document);

    static int getPageCount(JNIEnv *env, FPDF_DOCUMENT document);

    static bool scaleForPrinting(JNIEnv *env, FPDF_DOCUMENT document);

    static void renderPage(JNIEnv *env,
                    FPDF_DOCUMENT document, FPDF_PAGE page,
                    jobject dest_bmp,
                    const float* clip_data,
                    const float* transform_data,
                    jint render_mode);

    static FPDF_PAGE openPageAndGetSize(JNIEnv *env, FPDF_DOCUMENT document, jint page_index, float* out_size);

    static void closePage(JNIEnv *env, FPDF_PAGE page);

};


#endif //ANDROIDPDFIUMCOMPAT_PDFRENDERERCOMPAT_H
