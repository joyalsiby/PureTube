# Keep NewPipeExtractor and Rhino / Jackson / NanoJson classes if minification is enabled
-keep class org.schabi.newpipe.** { *; }
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**
-dontwarn javax.naming.**
