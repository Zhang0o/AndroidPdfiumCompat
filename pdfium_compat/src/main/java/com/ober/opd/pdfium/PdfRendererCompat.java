package com.ober.opd.pdfium;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * Compat class for {@link android.graphics.pdf.PdfRenderer} <br/>
 * <p>
 * Some Differences width built-in PdfRenderer are:<br/>
 * <p>
 * It support API>=14 <br/>
 * {@link PdfRendererCompat.Page#render(Bitmap, Rect, Matrix, int)} support bitmap format ARGB8888 and RGB565 <br/>
 * <p>
 * Created by ober on 20-8-11.
 */
public class PdfRendererCompat {

    //pdfium library build with source code
    static {
        System.loadLibrary("c++");
        System.loadLibrary("icuuc");
        System.loadLibrary("icui18n");
        System.loadLibrary("chrome_zlib");
        System.loadLibrary("pdfium");
    }

    //native renderer library depend on pdfium
    static {
        System.loadLibrary("opdfium");
    }

    final static Object sPdfiumLock = new Object();

    private long mNativeDocument;

    private final int mPageCount;

    private ParcelFileDescriptor mInput;

    private Page mCurrentPage;

    public PdfRendererCompat(ParcelFileDescriptor input) throws IOException {
        if (input == null) {
            throw new NullPointerException("input cannot be null");
        }

        mInput = input;

        synchronized (sPdfiumLock) {
            mNativeDocument = nCreate(mInput.getFd());

            if (mNativeDocument == 0) {
                throw new IOException("Open Pdf Document Error");
            }

            try {
                mPageCount = nGetPageCount(mNativeDocument);
            } catch (Throwable t) {
                nClose(mNativeDocument);
                mNativeDocument = 0;
                throw t;
            }
        }

    }

    public void close() {
        throwIfClosed();
        throwIfPageOpened();
        doClose();
    }

    public int getPageCount() {
        throwIfClosed();
        return mPageCount;
    }

    public boolean shouldScaleForPrinting() {
        throwIfClosed();

        synchronized (sPdfiumLock) {
            return nScaleForPrinting(mNativeDocument);
        }
    }

    public Page openPage(int index) {
        throwIfClosed();
        throwIfPageOpened();
        throwIfPageNotInDocument(index);
        mCurrentPage = new Page(index);
        return mCurrentPage;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            doClose();
        } finally {
            super.finalize();
        }
    }

    private void doClose() {
        if (mCurrentPage != null) {
            mCurrentPage.close();
            mCurrentPage = null;
        }

        if (mNativeDocument != 0) {
            synchronized (sPdfiumLock) {
                nClose(mNativeDocument);
            }
            mNativeDocument = 0;
        }

        if (mInput != null) {
            try {
                mInput.close();
            } catch (Exception ignore) {
            }
            mInput = null;
        }
    }

    private void throwIfClosed() {
        if (mInput == null) {
            throw new IllegalStateException("Already closed");
        }
    }

    private void throwIfPageOpened() {
        if (mCurrentPage != null) {
            throw new IllegalStateException("Current page not closed");
        }
    }

    private void throwIfPageNotInDocument(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= mPageCount) {
            throw new IllegalArgumentException("Invalid page index");
        }
    }

    public final class Page {

        public static final int RENDER_MODE_FOR_DISPLAY = 1;

        public static final int RENDER_MODE_FOR_PRINT = 2;

        private final int mIndex;
        private final int mWidth;
        private final int mHeight;

        private long mNativePage;

        private Page(int index) {
            float[] vec2f = new float[2];
            synchronized (sPdfiumLock) {
                mNativePage = nOpenPageAndGetSize(mNativeDocument, index, vec2f);
            }
            mIndex = index;
            mWidth = (int) vec2f[0];
            mHeight = (int) vec2f[1];
        }

        public int getIndex() {
            return mIndex;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public void render(Bitmap destination, Rect destClip,
                           Matrix transform, int renderMode) {
            if (mNativePage == 0) {
                throw new NullPointerException();
            }

            if (destination == null) {
                throw new NullPointerException("bitmap null");
            }

            if (!(destination.getConfig() == Bitmap.Config.ARGB_8888 || destination.getConfig() == Bitmap.Config.RGB_565)) {
                throw new IllegalArgumentException("Unsupported pixel format");
            }

            if (destClip != null) {
                if (destClip.left < 0 || destClip.top < 0
                        || destClip.right > destination.getWidth()
                        || destClip.bottom > destination.getHeight()) {
                    throw new IllegalArgumentException("destBounds not in destination");
                }
            }

            if (renderMode != RENDER_MODE_FOR_PRINT && renderMode != RENDER_MODE_FOR_DISPLAY) {
                throw new IllegalArgumentException("Unsupported render mode");
            }

            final int contentLeft = (destClip != null) ? destClip.left : 0;
            final int contentTop = (destClip != null) ? destClip.top : 0;
            final int contentRight = (destClip != null) ? destClip.right
                    : destination.getWidth();
            final int contentBottom = (destClip != null) ? destClip.bottom
                    : destination.getHeight();

            // If transform is not set, stretch page to whole clipped area
            if (transform == null) {
                int clipWidth = contentRight - contentLeft;
                int clipHeight = contentBottom - contentTop;

                transform = new Matrix();
                transform.postTranslate(contentLeft, contentTop);
                transform.postScale((float) clipWidth / getWidth(),
                        (float) clipHeight / getHeight());
            }

            float[] transformData = new float[9];
            transform.getValues(transformData);

            float[] nativeTransform = new float[6];
            nativeTransform[0] = transformData[Matrix.MSCALE_X];
            nativeTransform[1] = transformData[Matrix.MSKEW_X];
            nativeTransform[2] = transformData[Matrix.MSKEW_Y];
            nativeTransform[3] = transformData[Matrix.MSCALE_Y];
            nativeTransform[4] = transformData[Matrix.MTRANS_X];
            nativeTransform[5] = transformData[Matrix.MTRANS_Y];

            float[] nativeClip = new float[4];
            nativeClip[0] = contentLeft;
            nativeClip[1] = contentTop;
            nativeClip[2] = contentRight;
            nativeClip[3] = contentBottom;

            synchronized (sPdfiumLock) {
                nRenderPage(mNativeDocument, mNativePage, destination, nativeClip, nativeTransform, renderMode);
            }
        }

        public void close() {
            throwIfClosed();
            doClose();
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                doClose();
            } finally {
                super.finalize();
            }
        }

        private void doClose() {
            if (mNativePage != 0) {
                synchronized (sPdfiumLock) {
                    nativeClosePage(mNativePage);
                }
                mNativePage = 0;
            }

            mCurrentPage = null;
        }

        private void throwIfClosed() {
            if (mNativePage == 0) {
                throw new IllegalStateException("Already closed");
            }
        }
    }

    //implementation in main.cpp
    private static native long nCreate(int fd);
    private static native void nClose(long documentPtr);
    private static native int nGetPageCount(long documentPtr);
    private static native boolean nScaleForPrinting(long documentPtr);
    private static native void nRenderPage(long documentPtr, long pagePtr, Bitmap dest,
                                           float[] clipData, float[] transformData,
                                           int renderMode);
    private static native long nOpenPageAndGetSize(long documentPtr, int pageIndex,
                                                   float[] outSize);
    private static native void nativeClosePage(long pagePtr);

}
