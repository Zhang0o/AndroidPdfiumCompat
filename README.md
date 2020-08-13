# PdfiumCompat
Compatible implementation for [ android.graphics.pdf.PdfRenderer ]( https://developer.android.com/reference/android/graphics/pdf/PdfRenderer ) that support API level under 19. And some extension functions is supplied.

## Usage
 * Use PdfRenderer in API level under 19
 * Pdf Page render on Bitmap with format RBG565
 * Use Pdfium library in native develop quickly instead of compile it complexly 

## Get Started
  #### Simple Usage For PdfRenderer Compat
    package current available in jcenter:
    implementation "com.ober.opdf:pdfium_compat:0.0.1"
    
    use in java:
    PdfRendererCompat pdfRenderer = new PdfRendererCompat(parcelFileDescriptor);
    //then pdfRenderer perform every function similar to android.graphics.pdf.PdfRenderer
  
  Demo app show the simple usage of PdfRendererCompat and PdfRenderer
  
  <img src="https://github.com/Zhang0o/AndroidPdfiumCompat/blob/master/demo_screenshot.png" width="250"/>
  
  
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
 
## ChangeLog
  #### v0.0.1
   * Initialize library
   * Provide pdfium library 
   * Write PdfRendererCompat java class wrapper that similar to android builtin PdfRenderer
    
