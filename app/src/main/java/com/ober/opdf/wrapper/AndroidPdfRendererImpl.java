package com.ober.opdf.wrapper;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * Created by ober on 20-8-12.
 */
@TargetApi(21)
class AndroidPdfRendererImpl extends PdfRendererDelegate {

    private final PdfRenderer pdfRenderer;

    AndroidPdfRendererImpl(ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
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
        PdfRenderer.Page page = pdfRenderer.openPage(index);
        return new PageImp(page);
    }

    class PageImp extends PdfPageDelegate {

        private final PdfRenderer.Page page;

        PageImp(PdfRenderer.Page page) {
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
