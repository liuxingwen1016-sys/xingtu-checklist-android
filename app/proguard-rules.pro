# Xingtu Checklist demo keep rules.
# Release build ships with minification disabled, these rules document the
# reflection-sensitive entry points in case minification is enabled later.

# Room entities are accessed via generated implementation classes.
-keep class com.xinghan.xingtu.data.local.entity.** { *; }

# AppWidgetProvider is instantiated by the system.
-keep class com.xinghan.xingtu.platform.widget.TripAppWidgetProvider { *; }
