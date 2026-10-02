package com.example.ui

import com.example.data.model.AppLanguage

object Strings {
  private val translations = mapOf(
    // App Branding
    "app_name" to mapOf(
      AppLanguage.BANGLA to "ওয়াফা পিডিএফ",
      AppLanguage.ENGLISH to "Wafa PDF",
      AppLanguage.ARABIC to "وفا بي دي إف"
    ),
    "app_subtitle" to mapOf(
      AppLanguage.BANGLA to "পিডিএফ এবং ডকুমেন্ট টুলকিট",
      AppLanguage.ENGLISH to "PDF & Document Toolkit",
      AppLanguage.ARABIC to "مجموعة أدوات PDF والمستندات"
    ),
    "dev_credit" to mapOf(
      AppLanguage.BANGLA to "তৈরি করেছেন ❤️ দিয়ে Mehedi364",
      AppLanguage.ENGLISH to "Made with ❤️ by Mehedi364",
      AppLanguage.ARABIC to "صنع بكل ❤️ بواسطة Mehedi364"
    ),
    // Tabs
    "tab_home" to mapOf(
      AppLanguage.BANGLA to "হোম",
      AppLanguage.ENGLISH to "Home",
      AppLanguage.ARABIC to "الرئيسية"
    ),
    "tab_files" to mapOf(
      AppLanguage.BANGLA to "ফাইলস",
      AppLanguage.ENGLISH to "Files",
      AppLanguage.ARABIC to "الملفات"
    ),
    "tab_scan" to mapOf(
      AppLanguage.BANGLA to "স্ক্যান",
      AppLanguage.ENGLISH to "Scan",
      AppLanguage.ARABIC to "مسح ضوئي"
    ),
    "tab_ai" to mapOf(
      AppLanguage.BANGLA to "এআই",
      AppLanguage.ENGLISH to "AI",
      AppLanguage.ARABIC to "الذكاء الاصطناعي"
    ),
    "tab_settings" to mapOf(
      AppLanguage.BANGLA to "সেটিংস",
      AppLanguage.ENGLISH to "Settings",
      AppLanguage.ARABIC to "الإعدادات"
    ),
    // Search
    "search_hint" to mapOf(
      AppLanguage.BANGLA to "পিডিএফ, ডকুমেন্ট ও ফোল্ডার খুঁজুন...",
      AppLanguage.ENGLISH to "Search your PDFs, documents and folders...",
      AppLanguage.ARABIC to "ابحث في ملفات PDF والمستندات والمجلدات..."
    ),
    // Quick Actions
    "action_camera" to mapOf(AppLanguage.BANGLA to "ক্যামেরা", AppLanguage.ENGLISH to "Camera", AppLanguage.ARABIC to "الكاميرا"),
    "action_scan" to mapOf(AppLanguage.BANGLA to "স্ক্যান", AppLanguage.ENGLISH to "Scan", AppLanguage.ARABIC to "مسح"),
    "action_capture" to mapOf(AppLanguage.BANGLA to "ক্যাপচার", AppLanguage.ENGLISH to "Capture", AppLanguage.ARABIC to "التقاط"),
    "action_create_pdf" to mapOf(AppLanguage.BANGLA to "পিডিএফ তৈরি", AppLanguage.ENGLISH to "Create PDF", AppLanguage.ARABIC to "إنشاء PDF"),
    "action_compress" to mapOf(AppLanguage.BANGLA to "কম্প্রেস", AppLanguage.ENGLISH to "Compress", AppLanguage.ARABIC to "ضغط"),
    "action_edit" to mapOf(AppLanguage.BANGLA to "সম্পাদনা", AppLanguage.ENGLISH to "Edit", AppLanguage.ARABIC to "تعديل"),
    "action_convert" to mapOf(AppLanguage.BANGLA to "কনভার্ট", AppLanguage.ENGLISH to "Convert", AppLanguage.ARABIC to "تحويل"),
    "action_watermark" to mapOf(AppLanguage.BANGLA to "জলছাপ", AppLanguage.ENGLISH to "Watermark", AppLanguage.ARABIC to "علامة مائية"),
    "action_merge" to mapOf(AppLanguage.BANGLA to "মার্জ", AppLanguage.ENGLISH to "Merge", AppLanguage.ARABIC to "دمج"),
    "action_split" to mapOf(AppLanguage.BANGLA to "স্প্লিট", AppLanguage.ENGLISH to "Split", AppLanguage.ARABIC to "تقسيم"),
    "action_ocr" to mapOf(AppLanguage.BANGLA to "ওসিআর টেক্সট", AppLanguage.ENGLISH to "OCR Text", AppLanguage.ARABIC to "التعرف الضوئي"),
    "action_print" to mapOf(AppLanguage.BANGLA to "প্রিন্ট প্রস্তুতি", AppLanguage.ENGLISH to "Ready Print", AppLanguage.ARABIC to "جاهز للطباعة"),
    "action_vault" to mapOf(AppLanguage.BANGLA to "প্রাইভেট ভল্ট", AppLanguage.ENGLISH to "Private Vault", AppLanguage.ARABIC to "الخزنة الخاصة"),

    // Cards
    "continue_reading" to mapOf(
      AppLanguage.BANGLA to "পড়া চালিয়ে যান",
      AppLanguage.ENGLISH to "Continue Reading",
      AppLanguage.ARABIC to "متابعة القراءة"
    ),
    "continue_btn" to mapOf(
      AppLanguage.BANGLA to "চালিয়ে যান",
      AppLanguage.ENGLISH to "Continue",
      AppLanguage.ARABIC to "متابعة"
    ),
    "recent_documents" to mapOf(
      AppLanguage.BANGLA to "সাম্প্রতিক ডকুমেন্টস",
      AppLanguage.ENGLISH to "Recent Documents",
      AppLanguage.ARABIC to "المستندات الأخيرة"
    ),
    "folders_title" to mapOf(
      AppLanguage.BANGLA to "ফোল্ডারসমূহ",
      AppLanguage.ENGLISH to "Folders",
      AppLanguage.ARABIC to "المجلدات"
    ),
    "ask_pdf_title" to mapOf(
      AppLanguage.BANGLA to "আপনার পিডিএফ-কে প্রশ্ন করুন",
      AppLanguage.ENGLISH to "Ask your PDF",
      AppLanguage.ARABIC to "اسأل ملف PDF الخاص بك"
    ),
    "ask_pdf_subtitle" to mapOf(
      AppLanguage.BANGLA to "এআই দিয়ে সারসংক্ষেপ, ব্যাখ্যা ও অনুবাদ করুন",
      AppLanguage.ENGLISH to "Summarize, explain and translate instantly with AI",
      AppLanguage.ARABIC to "تلخيص وشرح وترجمة فورية باستخدام الذكاء الاصطناعي"
    ),

    // AI Actions
    "ai_ask" to mapOf(AppLanguage.BANGLA to "প্রশ্ন", AppLanguage.ENGLISH to "Ask", AppLanguage.ARABIC to "سؤال"),
    "ai_summarize" to mapOf(AppLanguage.BANGLA to "সারসংক্ষেপ", AppLanguage.ENGLISH to "Summarize", AppLanguage.ARABIC to "تلخيص"),
    "ai_explain" to mapOf(AppLanguage.BANGLA to "ব্যাখ্যা", AppLanguage.ENGLISH to "Explain", AppLanguage.ARABIC to "شرح"),
    "ai_translate" to mapOf(AppLanguage.BANGLA to "অনুবাদ", AppLanguage.ENGLISH to "Translate", AppLanguage.ARABIC to "ترجمة"),
    "ai_notes" to mapOf(AppLanguage.BANGLA to "নোটস", AppLanguage.ENGLISH to "Notes", AppLanguage.ARABIC to "ملاحظات"),
    "ai_mcq" to mapOf(AppLanguage.BANGLA to "কুইজ / MCQ", AppLanguage.ENGLISH to "Quiz / MCQ", AppLanguage.ARABIC to "اختبار / MCQ"),
    "ai_flashcards" to mapOf(AppLanguage.BANGLA to "ফ্ল্যাশকার্ড", AppLanguage.ENGLISH to "Flashcards", AppLanguage.ARABIC to "بطاقات تعليمية"),
    "ai_keypoints" to mapOf(AppLanguage.BANGLA to "মূল পয়েন্ট", AppLanguage.ENGLISH to "Key Points", AppLanguage.ARABIC to "النقاط الرئيسية"),

    // FAB menu
    "fab_create" to mapOf(AppLanguage.BANGLA to "+ তৈরি করুন", AppLanguage.ENGLISH to "+ Create", AppLanguage.ARABIC to "+ إنشاء"),
    "fab_blank_pdf" to mapOf(AppLanguage.BANGLA to "নতুন পিডিএফ", AppLanguage.ENGLISH to "Blank PDF", AppLanguage.ARABIC to "ملف PDF فارغ"),
    "fab_scan_doc" to mapOf(AppLanguage.BANGLA to "ডকুমেন্ট স্ক্যান", AppLanguage.ENGLISH to "Scan Document", AppLanguage.ARABIC to "مسح مستند"),
    "fab_images_to_pdf" to mapOf(AppLanguage.BANGLA to "ছবি → পিডিএফ", AppLanguage.ENGLISH to "Images → PDF", AppLanguage.ARABIC to "صور إلى PDF"),
    "fab_text_to_pdf" to mapOf(AppLanguage.BANGLA to "লেখা → পিডিএফ", AppLanguage.ENGLISH to "Text → PDF", AppLanguage.ARABIC to "نص إلى PDF"),
    "fab_import_pdf" to mapOf(AppLanguage.BANGLA to "পিডিএফ ইমপোর্ট", AppLanguage.ENGLISH to "Import PDF", AppLanguage.ARABIC to "استيراد PDF"),

    // Reader
    "reader_continuous" to mapOf(AppLanguage.BANGLA to "ধারাবাহিক", AppLanguage.ENGLISH to "Continuous", AppLanguage.ARABIC to "مستمر"),
    "reader_single" to mapOf(AppLanguage.BANGLA to "একক পৃষ্ঠা", AppLanguage.ENGLISH to "Single Page", AppLanguage.ARABIC to "صفحة واحدة"),
    "reader_book" to mapOf(AppLanguage.BANGLA to "বই মোড", AppLanguage.ENGLISH to "Book View", AppLanguage.ARABIC to "وضع الكتاب"),
    "reader_fit_width" to mapOf(AppLanguage.BANGLA to "প্রস্থ সমান", AppLanguage.ENGLISH to "Fit Width", AppLanguage.ARABIC to "ملائمة العرض"),
    "reader_bookmark" to mapOf(AppLanguage.BANGLA to "বুকমার্ক", AppLanguage.ENGLISH to "Bookmark", AppLanguage.ARABIC to "إشارة مرجعية"),
    "reader_search" to mapOf(AppLanguage.BANGLA to "খুঁজুন", AppLanguage.ENGLISH to "Search", AppLanguage.ARABIC to "بحث"),
    "reader_annotate" to mapOf(AppLanguage.BANGLA to "চিহ্নিত করুন", AppLanguage.ENGLISH to "Annotate", AppLanguage.ARABIC to "تعليق"),
    "reader_mode_light" to mapOf(AppLanguage.BANGLA to "লাইট", AppLanguage.ENGLISH to "Light", AppLanguage.ARABIC to "فاتح"),
    "reader_mode_sepia" to mapOf(AppLanguage.BANGLA to "সেপিয়া", AppLanguage.ENGLISH to "Sepia", AppLanguage.ARABIC to "بني داكن"),
    "reader_mode_dark" to mapOf(AppLanguage.BANGLA to "ডার্ক", AppLanguage.ENGLISH to "Dark", AppLanguage.ARABIC to "داكن"),

    // Tools
    "tool_compress_title" to mapOf(AppLanguage.BANGLA to "পিডিএফ সাইজ কমান", AppLanguage.ENGLISH to "PDF Compressor", AppLanguage.ARABIC to "ضغط ملف PDF"),
    "tool_watermark_title" to mapOf(AppLanguage.BANGLA to "জলছাপ যুক্ত করুন", AppLanguage.ENGLISH to "Watermark Studio", AppLanguage.ARABIC to "إضافة علامة مائية"),
    "tool_print_title" to mapOf(AppLanguage.BANGLA to "প্রিন্ট লেআউট প্রস্তুতি", AppLanguage.ENGLISH to "Ready Print Setup", AppLanguage.ARABIC to "إعداد الطباعة الجاهزة"),

    // Status & Common
    "status_success" to mapOf(AppLanguage.BANGLA to "সফল হয়েছে!", AppLanguage.ENGLISH to "Success!", AppLanguage.ARABIC to "تم بنجاح!"),
    "status_error" to mapOf(AppLanguage.BANGLA to "সমস্যা হয়েছে", AppLanguage.ENGLISH to "Error occurred", AppLanguage.ARABIC to "حدث خطأ"),
    "cancel" to mapOf(AppLanguage.BANGLA to "বাতিল", AppLanguage.ENGLISH to "Cancel", AppLanguage.ARABIC to "إلغاء"),
    "save" to mapOf(AppLanguage.BANGLA to "সংরক্ষণ", AppLanguage.ENGLISH to "Save", AppLanguage.ARABIC to "حفظ"),
    "share" to mapOf(AppLanguage.BANGLA to "শেয়ার", AppLanguage.ENGLISH to "Share", AppLanguage.ARABIC to "مشاركة"),
    "delete" to mapOf(AppLanguage.BANGLA to "মুছে ফেলুন", AppLanguage.ENGLISH to "Delete", AppLanguage.ARABIC to "حذف"),
    "empty_documents" to mapOf(
      AppLanguage.BANGLA to "কোনো পিডিএফ নথি পাওয়া যায়নি। নতুন পিডিএফ তৈরি করতে '+' চাপুন।",
      AppLanguage.ENGLISH to "No documents found. Tap '+' to create or import a PDF.",
      AppLanguage.ARABIC to "لم يتم العثور على مستندات. اضغط على '+' لإنشاء أو استيراد ملف PDF."
    )
  )

  fun get(key: String, language: AppLanguage): String {
    return translations[key]?.get(language) ?: translations[key]?.get(AppLanguage.BANGLA) ?: key
  }
}
