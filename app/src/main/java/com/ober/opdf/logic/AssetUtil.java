package com.ober.opdf.logic;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ober on 20-8-13.
 */
public class AssetUtil {

    public static File extractAssetPdf(Context context) {
        File dst = new File(context.getCacheDir(), "sample.pdf");
        if (dst.exists()) {
            dst.delete();
        }

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = context.getAssets().open("sample.pdf");
            fos = new FileOutputStream(dst);
            byte[] buf = new byte[10240];
            int r;
            while ((r = is.read(buf)) != -1) {
                fos.write(buf, 0, r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return dst;
    }
}
