package com.ober.opdf.wrapper;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;

import com.ober.opdf.pdfium.PdfRendererCompat;

import java.io.IOException;

/**
 * Created by ober on 20-8-12.
 */
class CompatPdfRendererImpl extends PdfRendererDelegate {

    private final PdfRendererCompat pdfRenderer;

    CompatPdfRendererImpl(ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        pdfRenderer = new PdfRendererCompat(parcelFileDescriptor);
    }

    @Override
    public void close() {
        pdfRenderer.close();
    }

    @Override
    public int getPageCount() {
        return pdfRenderer.getPageCount();
    }

    @Override
    public boolean shouldScaleForPrinting() {
        return pdfRenderer.shouldScaleForPrinting();
    }

    @Override
    public PdfPageDelegate openPage(int index) {
        PdfRendererCompat.Page page = pdfRenderer.openPage(index);
        return new PageImp(page);
    }

    class PageImp extends PdfPageDelegate {

        private final PdfRendererCompat.Page page;

        PageImp(PdfRendererCompat.Page page) {
            this.page = page;
        }

        @Override
        public int getIndex() {
            return page.getIndex();
        }

        @Override
        public int getWidth() {
            return page.getWidth();
        }

        @Override
        public int getHeight() {
            return page.getHeight();
        }

        @Override
        public void render(Bitmap destination, Rect destClip, Matrix transform, int renderMode) {
            page.render(destination, destClip, transform, renderMode);
        }

        @Override
        public void close() {
            page.close();
        }
    }
}
