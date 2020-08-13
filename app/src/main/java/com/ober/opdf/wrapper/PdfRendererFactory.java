package com.ober.opdf.wrapper;

import android.os.Build;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * Created by ober on 20-8-12.
 */
public class PdfRendererFactory {

    public static PdfRendererDelegate create(ParcelFileDescriptor parcelFileDescriptor, boolean useBuiltIn) throws IOException {

        if (Build.VERSION.SDK_INT < 21) {
            useBuiltIn = false;
        }

        if (useBuiltIn) {
            return new AndroidPdfRendererImpl(parcelFileDescriptor);
        } else {
            return new CompatPdfRendererImpl(parcelFileDescriptor);
        }
    }
}
