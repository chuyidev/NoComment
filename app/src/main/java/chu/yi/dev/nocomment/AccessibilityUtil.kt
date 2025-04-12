package chu.yi.dev.nocomment

import android.content.Context
import android.provider.Settings
import android.util.Log

/**
 * Don't Edit For It
 */
fun isAccessibilityEnabled(context:Context, servicePackageName:String):Boolean {
    val TAG = "AccessibilityUtil"
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val componentNames = enabledServicesSetting?.split(":") ?: emptyList()
    Log.d(TAG, "componentNames: ${componentNames}\n servicePackageName: $servicePackageName")
    return componentNames.any { it.contains(servicePackageName) }
}