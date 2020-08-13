package com.ober.opdf.wrapper;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;

/**
 * Created by ober on 20-8-12.
 */
public abstract class PdfRendererDelegate {

    public abstract void close();

    public abstract int getPageCount();

    public abstract boolean shouldScaleForPrinting();

    public abstract PdfPageDelegate openPage(int index);

    @SuppressWarnings({"InnerClassMayBeStatic"})
    public abstract class PdfPageDelegate {

        public abstract int getIndex();

        public abstract int getWidth();

        public abstract int getHeight();

        public abstract void render(Bitmap destination, Rect destClip,
                                    Matrix transform, int renderMode);

        public abstract void close();

    }


}
