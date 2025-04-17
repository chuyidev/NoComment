package chu.yi.dev.nocomment

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Edit It In 'onAccessibilityEvent'
 */
class HideCommentService : AccessibilityService() {

    private val TAG = "HideCommentService"
    private fun queryNodeTree(rootNode: AccessibilityNodeInfo) {
        Log.d(TAG, "!!start queryNodeTree: ")
        val layoutStructure = StringBuilder()
        buildLayoutStructure(rootNode, 0, layoutStructure)
        Log.d(TAG, layoutStructure.toString())
        Log.d(TAG, "!!end findCommentArea: ")
    }

    private fun buildLayoutStructure(node: AccessibilityNodeInfo, depth: Int, sb: StringBuilder) {
        val indent = StringBuilder()
        for (i in 0..<depth) {
            indent.append("--")
        }

        sb.append(indent).append("Node: ").append(node.className)
            .append(", Text: ").append(if (node.text != null) node.text.toString() else "null")
            .append(", ")
            .append(", ViewId: ")
            .append(if (node.viewIdResourceName != null) node.viewIdResourceName else "null")
            .append("\n")

        val childCount = node.childCount
        for (i in 0..<childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                buildLayoutStructure(childNode, depth + 1, sb)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
        mService = this
        GlobalData.readFromCache(this.applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if(event.eventType !in listOf(
                AccessibilityEvent.TYPE_VIEW_CLICKED,
                AccessibilityEvent.TYPE_TOUCH_INTERACTION_START,
                AccessibilityEvent.TYPE_TOUCH_INTERACTION_END,
                AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END,
                AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START,
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED,
                AccessibilityEvent.TYPE_VIEW_SCROLLED
        ))return
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val packageName = rootNode.packageName.toString()

            Log.d(TAG, "onAccessibilityEvent:packageName $packageName")
            Log.d(TAG, "onAccessibilityEvent:TYPE ${event.eventType}")
            if (packageName == "tv.danmaku.bili") {
                if(GlobalData.bilibiliLongVideo) hideBilibiliLongVideoComment(rootNode)
                if(GlobalData.bilibiliShortVideo) hideBilibiliShortVideoComment(rootNode)
                if(GlobalData.bilibiliFeed) hideBilibiliFeedComment(rootNode)
            }else if(packageName == "com.ss.android.ugc.aweme"){
                //douyin
                if(GlobalData.douyin) hideDouyinComment(rootNode)
                //queryNodeTree(rootNode)  // 看一下View结构
            }else if(packageName == "com.zhihu.android"){
                //queryNodeTree(rootNode)
                if (GlobalData.zhihuquestion) hideZhihuQuestionComment(rootNode)
            }else if(packageName == "com.tencent.mm"){
                var parent = event.source?.parent
                while (parent != null){
                    if (parent.parent!=null){
                        parent = parent.parent
                    }else{
                        break
                    }
                }
//                //Log.d(TAG, "com.tencent.m: ${info}")
//                parent?.let { queryNodeTree(it) }
            }

        }
    }

    private fun hideZhihuQuestionComment(root: AccessibilityNodeInfo){
        val clos = root.findAccessibilityNodeInfosByViewId("com.zhihu.android:id/iv_close")
        if (clos.isEmpty()){
            return
        }
        clos[0]?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun hideBilibiliLongVideoComment(root: AccessibilityNodeInfo) {
        val tabs = root.findAccessibilityNodeInfosByViewId("tv.danmaku.bili:id/tab_title")
        val summaryNode = tabs.firstOrNull { it.text.toString() == "简介" }
        val commentNode = tabs.firstOrNull { it.text.toString() == "评论" }

        val summaryParentParentNode = summaryNode?.parent?.parent ?: return
        if (commentNode?.isSelected != true) return

        Log.d(TAG, "hideBilibiliLongVideoComment:action_click ")
        summaryParentParentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun hideBilibiliShortVideoComment(root: AccessibilityNodeInfo) {
        val tab = root.findAccessibilityNodeInfosByViewId("tv.danmaku.bili:id/story_bottom_tab_layout").firstOrNull()

        val summaryNode = tab?.getChild(0)?.getChild(0)?.getChild(0)
        val commentNode = tab?.getChild(0)?.getChild(1)?.getChild(0)

        val summaryParentParentNode = summaryNode?.parent?: return
        if (commentNode?.isSelected != true) return

        Log.d(TAG, "hideBilibiliShortVideoComment:action_click ")
        summaryParentParentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun hideBilibiliFeedComment(root: AccessibilityNodeInfo) {
        val commentNode = root.findAccessibilityNodeInfosByViewId("tv.danmaku.bili:id/comment").firstOrNull()
        val summaryNode = root.findAccessibilityNodeInfosByViewId("tv.danmaku.bili:id/interaction_title").firstOrNull()

        val summaryParentParentNode = summaryNode?.parent?: return
        if (commentNode?.isSelected != true) return

        Log.d(TAG, "hideBilibiliFeedComment:action_click ")
        summaryParentParentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun hideDouyinComment(root: AccessibilityNodeInfo) {
        val commentNode = root.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/title").firstOrNull()
        val videoViewNode = root.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/mask_view").firstOrNull()
        Log.d(TAG, "hideDouyinComment:action_click ${commentNode==null} ${videoViewNode==null}")
        if (commentNode != null){
            videoViewNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }


    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt: ")
        GlobalData.readFromCache(this.applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        mService = null
    }

    companion object {
        var mService: HideCommentService? = null
    }
}