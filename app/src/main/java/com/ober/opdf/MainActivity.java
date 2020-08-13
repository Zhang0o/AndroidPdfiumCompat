package com.ober.opdf;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ober.opdf.logic.AssetUtil;
import com.ober.opdf.logic.PdfRendererLogic;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;

    private PdfRendererLogic pdfRendererLogic;

    private File dstPdfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = MainActivityBinding.bind(this);

        pdfRendererLogic = new PdfRendererLogic();

        dstPdfFile = AssetUtil.extractAssetPdf(this);

        setupViews();
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT < 21) {
            binding.rbPdfRender.setEnabled(false); //API < 21 not support PdfRenderer
        }
        binding.radioGroupRenderer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                pdfRendererLogic.destroy();
                binding.radioGroupPage.clearCheck();

                try {
                    if (checkedId == R.id.rbPdfRender) {
                        pdfRendererLogic.initialize(dstPdfFile, true);
                        binding.checkBoxUse565.setChecked(false); //PdfRenderer not support RGB565
                        binding.checkBoxUse565.setEnabled(false);
                    } else {
                        pdfRendererLogic.initialize(dstPdfFile, false);
                        binding.checkBoxUse565.setEnabled(true);
                    }
                    toastMsg("render initialized");
                } catch (IOException e) {
                    e.printStackTrace();
                    toastMsg("render initialize ERROR");
                }
            }
        });

        binding.checkBoxUse565.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pdfRendererLogic.setUseRGB565(isChecked);
            }
        });

        binding.checkBoxApplyTransform.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pdfRendererLogic.setApplyTransform(isChecked);
            }
        });

        binding.radioGroupPage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int pageIndex;
                switch (checkedId) {
                    case R.id.rb0:
                        pageIndex = 0;
                        break;
                    case R.id.rb1:
                        pageIndex = 1;
                        break;
                    case R.id.rb2:
                        pageIndex = 2;
                        break;
                    default:
                        return;
                }

                pdfRendererLogic.openPage(pageIndex);

                toastMsg("page opened index = " + pageIndex);
            }
        });

        binding.btnRender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderBmpToImageView();
            }
        });

    }

    private void renderBmpToImageView() {
        if (!pdfRendererLogic.isRenderInitialized()) {
            toastMsg("please select render implementation");
            return;
        }

        if (!pdfRendererLogic.isPageOpened()) {
            toastMsg("please open page");
            return;
        }

        Bitmap bmp = pdfRendererLogic.render(binding.imageView.getWidth(), binding.imageView.getHeight());
        binding.imageView.setImageBitmap(bmp);
    }

    private void toastMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
