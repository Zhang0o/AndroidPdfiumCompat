package com.ober.opdf.logic;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.ober.opdf.wrapper.PdfRendererDelegate;
import com.ober.opdf.wrapper.PdfRendererFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by ober on 20-8-12.
 */
public class PdfRendererLogic {

    private PdfRendererDelegate mRenderer;
    private PdfRendererDelegate.PdfPageDelegate mPage;
    private int mPageWidth;
    private int mPageHeight;

    private boolean bApplyTransform;
    private boolean bUseRgb565;

    public PdfRendererLogic() {

    }

    public void initialize(File pdfFile, boolean useBuiltIn) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
        mRenderer = PdfRendererFactory.create(parcelFileDescriptor, useBuiltIn);
    }

    public void destroy() {
        if (mPage != null) {
            mPage.close();
            mPage = null;
        }
        if (mRenderer != null) {
            mRenderer.close();
            mRenderer = null;
        }
    }

    public void setApplyTransform(boolean bApplyTransform) {
        this.bApplyTransform = bApplyTransform;
    }

    public void setUseRGB565(boolean useRGB565) {
        this.bUseRgb565 = useRGB565;
    }

    public void openPage(int index) {
        if (mRenderer == null) {
            return;
        }
        if (mPage != null) {
            mPage.close();
        }
        mPage = mRenderer.openPage(index);
        mPageWidth = mPage.getWidth();
        mPageHeight = mPage.getHeight();
        Log.d("PdfRenderLogic", "page opened index="
                + index + " " + mPageWidth + "," + mPageHeight);
    }

    public Bitmap render(int targetWidth, int targetHeight) {

        Bitmap bmp = Bitmap.createBitmap(targetWidth, targetHeight,
                bUseRgb565 ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888);

        if (bUseRgb565) {
            bmp.eraseColor(Color.WHITE);
        }

        Rect clip = null;
        Matrix transform = null;

        if (bApplyTransform) {
            clip = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
            transform = new Matrix();

            if (mPage.getIndex() == 0) {
                transform.postTranslate(0, 0);
                transform.postScale(2.0f, 2.0f);
            } else if (mPage.getIndex() == 1) {
                transform.postTranslate(-120, -60);
                transform.postScale(5.0f, 5.0f);
            } else {
                transform.postTranslate(-50, -50);
                transform.postScale(0.5f, 0.5f);
            }
        }

        long startTimestamp = System.currentTimeMillis();
        mPage.render(bmp, clip, transform, 1);
        long cost = System.currentTimeMillis() - startTimestamp;
        Log.d("PdfRendererLogic", "page render cost " + cost + "ms");

        return bmp;
    }

    public boolean isPageOpened() {
        return mPage != null;
    }

    public boolean isRenderInitialized() {
        return mRenderer != null;
    }

}
