# kotlinx.serialization: keep generated serializers for stored models.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.okdaithi.daycounter.** {
    *** Companion;
}
-keepclasseswithmembers class com.okdaithi.daycounter.** {
    kotlinx.serialization.KSerializer serializer(...);
}
