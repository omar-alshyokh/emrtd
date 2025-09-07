# emrtd-core/consumer-rules.pro
-keep class com.omartech.emrtd_core.Models.** { *; }     # AccessKey, ReadOptions, PassportData, EmrtdPortrait
-keep class com.omartech.emrtd_core.Interfaces.** { *; } # EmrtdReader, events, etc.
