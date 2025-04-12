package chu.yi.dev.nocomment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import kotlin.reflect.KMutableProperty0

/**
 * Edit It To Add Var
 */
object GlobalData {
    var bilibiliLongVideo: Boolean = false
    var bilibiliShortVideo: Boolean = false
    var bilibiliFeed: Boolean = false
    var douyin: Boolean = false

    val globalDataMap = mapOf(
        "bilibiliLongVideo" to ::bilibiliLongVideo,
        "bilibiliShortVideo" to ::bilibiliShortVideo,
        "bilibiliFeed" to ::bilibiliFeed,
        "douyin" to ::douyin
    )

    val viewMap = mapOf(
        "Bilibili功能区" to mapOf<String, KMutableProperty0<Boolean>>(
            "Bilibili长视频评论区屏蔽" to ::bilibiliLongVideo,
            "Bilibili短视频评论区屏蔽" to ::bilibiliShortVideo,
            "Bilibili动态评论区屏蔽" to ::bilibiliFeed
        ),
        "Douyin功能区" to mapOf<String, KMutableProperty0<Boolean>>(
            "抖音评论区屏蔽" to ::douyin
        )
    )

    fun saveForCache(context: Context){
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("ServiceData", MODE_PRIVATE)
        with(sharedPref.edit()) {
            globalDataMap.forEach {
                k,v->
                putBoolean(k,v.invoke())
            }
            apply()
        }
    }

    fun readFromCache(context: Context){
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("ServiceData", MODE_PRIVATE)
        globalDataMap.forEach {
            k,v->
            v.set(sharedPref.getBoolean(k,false))
        }
    }


}
