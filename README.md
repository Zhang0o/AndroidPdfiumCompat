# PdfiumCompat
Compatible [ android.graphics.pdf.PdfRenderer ]( https://developer.android.com/reference/android/graphics/pdf/PdfRenderer ) for android API level under 19 and more extension.

## Usage
 * Use PdfRenderer in API level under 19
 * Render on Bitmap with format RBG565
 * Use Pdfium library in native develop quickly instead of compile it complexly 

## Get Started
  #### Simple Usage For PdfRenderer Compat
    implementation "com.ober.opdf:pdfium_compat:0.0.1"
    
    PdfRendererCompat pdfRenderer = new PdfRendererCompat(parcelFileDescriptor);
    //then pdfRenderer perform every function similar to android.graphics.pdf.PdfRenderer
  
  #### Advance Usage
    * clone source code
    * get so libraries in cpp/jniLibs
    * get pdfium header files in cpp/public/
    * integrate them into user own project

## native libraries source

  * source code repo: https://pdfium.googlesource.com/pdfium
  
  * version 84.0.4147.125	(marked as stable in android according to https://omahaproxy.appspot.com/)
  
  * some build params are:
  -----
    is_debug = false
    target_os = "android"
    target_cpu = "arm","arm64"
    arm_version = 7
    pdf_is_standalone = true
    is_component_build = true
    pdf_enable_xfa = false
    pdf_enable_v8 = false
    pdf_use_skia_paths = false
    pdf_use_skia = false
    symbol_level = 1
 
