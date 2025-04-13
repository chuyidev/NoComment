package chu.yi.dev.nocomment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import chu.yi.dev.nocomment.GlobalData.viewMap
import chu.yi.dev.nocomment.ui.theme.AppTheme
import chu.yi.dev.nocomment.ui.theme.backgroundDark
import chu.yi.dev.nocomment.ui.theme.backgroundLight
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Don't Edit For It
 */
class MainActivity : ComponentActivity() {
    //private val TAG = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalData.readFromCache(this.applicationContext)
        val serviceIntent = Intent(this, PersistentNotificationService::class.java)
        startForegroundService(serviceIntent)
        setContent {
            AppTheme{
                MainScreen()
            }
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(
                color = if (isSystemInDarkTheme()){
                    backgroundDark
                }else{
                    backgroundLight
                },
                darkIcons = !isSystemInDarkTheme()
            )
        }
    }

    // 主界面
    @Composable
    fun MainScreen() {

        val context = LocalContext.current
        val packageName = stringResource(R.string.hide_comment_name)

        //State
        var isAccEnabled by remember { mutableStateOf(false) }
        var isNotification by remember {  mutableStateOf(false)}
        var groupStateMap by remember { mutableStateOf(viewMap.mapValues { it.value.map { entry -> entry.key to entry.value.invoke() } }) }

        //辅助函数与Map、List
        fun updateState() {
            isAccEnabled = isAccessibilityEnabled(context, packageName)
            isNotification = NotificationManagerCompat.from(this).areNotificationsEnabled()
            groupStateMap = viewMap.mapValues { it.value.map { entry -> entry.key to entry.value.invoke() } }
        }

        val groupSwitchFunctionMap = viewMap.mapValues { group ->
            group.value.map { entry ->
                entry.key to { isChecked: Boolean ->
                    entry.value.set(isChecked)
                    GlobalData.saveForCache(context)
                    updateState()
                }
            }.toMap()
        }

        OnLifecycleResumeEvent {
            GlobalData.readFromCache(context)
            updateState()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 无障碍功能权限状态卡片
            //AccessibilityStatusCard(isAccEnabled, context)
            PermissionCheckScreen(isAccEnabled, isNotification)
            // 循环添加不同功能区的设置卡片
            groupStateMap.forEach { (groupName, switchMap) ->
                SettingsCard(
                    groupName = groupName,
                    isAccEnabled = isAccEnabled,
                    switchMap = switchMap
                ) { name, isChecked ->
                    groupSwitchFunctionMap[groupName]?.get(name)?.invoke(isChecked)
                }
            }
        }
    }


    @Composable
    fun PermissionCheckScreen(isAccEnabled: Boolean,isNotification: Boolean) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "应用保活设置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(text = "应用后台锁定：")
                    Text(text = "进入后台管理，找到‘NoComment’，设置锁定", fontSize = 14.sp)
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()){
                        Button(onClick = {
                            openLockHelp()
                        }) {
                            Text(text = "设置帮助")
                        }
                    }
                }
                Text(text = "通知权限: ${if (isNotification) "已开启" else "未开启"}",
                    color = if(isNotification) Color.Black  else Color.Red)
                if (!isNotification) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()){
                        Button(onClick = {
                            openNotificationSettings()
                        }) {
                            Text(text = "开启通知权限")
                        }
                    }
                }
                Text(text = "无障碍权限: ${if (isAccEnabled) "已开启" else "未开启"}",
                        color = if(isAccEnabled) Color.Black  else Color.Red)
                if (!isAccEnabled) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()){
                        Button(onClick = {
                            openAccessibilitySettings()
                        }) {
                            Text(text = "开启无障碍权限")
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun openLockHelp(){
        val imageView = ImageView(this)
        // 这里使用系统自带的图片资源作为示例，你可以替换为自己的图片资源
        val drawable: Drawable? = getDrawable(R.drawable.help)
        imageView.setImageDrawable(drawable)
        val builder = AlertDialog.Builder(this)
        builder.setView(imageView)
        builder.setCancelable(false) // 设置对话框不可通过点击外部关闭
        val dialog = builder.create()
        dialog.show()
        imageView.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent()
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(intent)
        } else {
            val intent = Intent()
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo.uid)
            startActivity(intent)
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    // 设置卡片
    @Composable
    fun SettingsCard(
        groupName: String,
        isAccEnabled: Boolean,
        switchMap: List<Pair<String, Boolean>>,
        onSwitchChange: (String, Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = groupName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                switchMap.forEach { (name, isCheckedProvider) ->
                    SwitchSetting(
                        label = name,
                        isChecked = isCheckedProvider,
                        //isAccessibilityEnabled = isAccEnabled,
                        onCheckedChange = { isChecked ->
                            if (isAccEnabled) {
                                onSwitchChange(name, isChecked)
                            }
                        }
                    )
                }
            }
        }
    }

    // 开关设置组件
    @Composable
    fun SwitchSetting(
        label: String,
        isChecked: Boolean,
        //isAccessibilityEnabled: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = true
            )
        }
    }

    // 监听生命周期事件
    @Composable
    fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
        val eventHandler = rememberUpdatedState(onEvent)
        val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
        DisposableEffect(lifecycleOwner.value) {
            val lifecycle = lifecycleOwner.value.lifecycle
            val observer = LifecycleEventObserver { owner, event ->
                eventHandler.value(owner, event)
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }
    }

    @Composable
    fun OnLifecycleResumeEvent(onEvent: () -> Unit) {
        OnLifecycleEvent { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onEvent()
            }
        }
    }
}