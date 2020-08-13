package com.ober.opdf;

import android.app.Activity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by ober on 20-8-12.
 */
class MainActivityBinding {
    ImageView imageView;
    RadioGroup radioGroupRenderer;
    RadioButton rbPdfRender;
    RadioButton rbPdfRenderCompat;
    CheckBox checkBoxApplyTransform;
    CheckBox checkBoxUse565;
    RadioGroup radioGroupPage;
    RadioButton rb0;
    RadioButton rb1;
    RadioButton rb2;
    Button btnRender;

    static MainActivityBinding bind(MainActivity a) {
        MainActivityBinding binding = new MainActivityBinding();
        binding.findViews(a);
        return binding;
    }

    private void findViews(Activity a) {
        imageView = a.findViewById(R.id.imageView);
        radioGroupRenderer = a.findViewById(R.id.radioGroupRender);
        rbPdfRender = a.findViewById(R.id.rbPdfRender);
        rbPdfRenderCompat = a.findViewById(R.id.rbPdfRenderCompat);
        checkBoxApplyTransform = a.findViewById(R.id.checkBoxApplyTransform);
        checkBoxUse565 = a.findViewById(R.id.checkBoxUse565);
        radioGroupPage = a.findViewById(R.id.radioGroupPage);
        rb0 = a.findViewById(R.id.rb0);
        rb1 = a.findViewById(R.id.rb1);
        rb2 = a.findViewById(R.id.rb2);
        btnRender = a.findViewById(R.id.btnRender);
    }
}
