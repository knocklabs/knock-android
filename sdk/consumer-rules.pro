# Knock's API models are (de)serialized reflectively by Jackson, which relies on Kotlin metadata for
# constructor parameters and on the original property names. Keep them intact in minified apps.
-keep class app.knock.client.models.** { *; }
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod

# The Phoenix socket client decodes messages with Gson TypeTokens, which need their generic signatures.
-keep class org.phoenixframework.** { *; }
