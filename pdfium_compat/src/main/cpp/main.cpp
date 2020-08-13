#include <jni.h>

#include "PdfRendererCompat.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_ober_opd_pdfium_PdfRendererCompat_nCreate(JNIEnv *env, jclass clazz, jint fd) {
    FPDF_DOCUMENT doc = PdfRendererCompat::create(env, fd);
    return (long) doc;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ober_opd_pdfium_PdfRendererCompat_nClose(JNIEnv *env, jclass clazz, jlong document_ptr) {
    FPDF_DOCUMENT doc = (FPDF_DOCUMENT) document_ptr;
    PdfRendererCompat::close(env, doc);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ober_opd_pdfium_PdfRendererCompat_nGetPageCount(JNIEnv *env, jclass clazz,
                                                         jlong document_ptr) {
    FPDF_DOCUMENT doc = (FPDF_DOCUMENT) document_ptr;
    return PdfRendererCompat::getPageCount(env, doc);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ober_opd_pdfium_PdfRendererCompat_nScaleForPrinting(JNIEnv *env, jclass clazz,
                                                             jlong document_ptr) {
    FPDF_DOCUMENT doc = (FPDF_DOCUMENT) document_ptr;
    return PdfRendererCompat::scaleForPrinting(env, doc);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ober_opd_pdfium_PdfRendererCompat_nRenderPage(JNIEnv *env, jclass clazz,
                                                       jlong document_ptr, jlong page_ptr,
                                                       jobject dest,
                                                       jfloatArray clip_data,
                                                       jfloatArray transform_data,
                                                       jint render_mode) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT) document_ptr;
    FPDF_PAGE page = (FPDF_PAGE) page_ptr;

    float* clip = env -> GetFloatArrayElements(clip_data, 0);
    float* transform = env -> GetFloatArrayElements(transform_data, 0);

    PdfRendererCompat::renderPage(env, doc, page, dest, clip, transform, render_mode);

    env -> ReleaseFloatArrayElements(clip_data, clip, 0);
    env -> ReleaseFloatArrayElements(transform_data, transform, 0);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_ober_opd_pdfium_PdfRendererCompat_nOpenPageAndGetSize(JNIEnv *env, jclass clazz,
                                                               jlong document_ptr, jint page_index,
                                                               jfloatArray out_size) {

    FPDF_DOCUMENT doc = (FPDF_DOCUMENT)document_ptr;
    float* out_size_arr = env -> GetFloatArrayElements(out_size, 0);

    FPDF_PAGE page = PdfRendererCompat::openPageAndGetSize(env, doc, page_index, out_size_arr);

    env -> ReleaseFloatArrayElements(out_size, out_size_arr, 0);

    return (long) page;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ober_opd_pdfium_PdfRendererCompat_nativeClosePage(JNIEnv *env, jclass clazz,
                                                           jlong page_ptr) {
    FPDF_PAGE page = (FPDF_PAGE) page_ptr;
    PdfRendererCompat::closePage(env, page);
}
