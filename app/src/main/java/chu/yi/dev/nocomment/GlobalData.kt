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
    var zhihuquestion: Boolean = false
    //var wxVideo: Boolean = false

    val globalDataMap = mapOf(
        "bilibiliLongVideo" to ::bilibiliLongVideo,
        "bilibiliShortVideo" to ::bilibiliShortVideo,
        "bilibiliFeed" to ::bilibiliFeed,
        "douyin" to ::douyin,
        "zhihuQuestion" to ::zhihuquestion,
        //"wxVideo" to ::wxVideo
    )

    val viewMap = mapOf(
        "B站功能区" to mapOf<String, KMutableProperty0<Boolean>>(
            "B站长视频评论区屏蔽" to ::bilibiliLongVideo,
            "B站短视频评论区屏蔽" to ::bilibiliShortVideo,
            "B站动态评论区屏蔽" to ::bilibiliFeed
        ),
        "抖音功能区" to mapOf<String, KMutableProperty0<Boolean>>(
            "抖音评论区屏蔽" to ::douyin
        ),
        "知乎功能区" to mapOf<String, KMutableProperty0<Boolean>>(
            "知乎回答评论区屏蔽" to ::zhihuquestion
        ),
//        "微信功能区" to mapOf<String, KMutableProperty0<Boolean>>(
//            "微信视频号评论区屏蔽" to ::wxVideo
//        )

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
