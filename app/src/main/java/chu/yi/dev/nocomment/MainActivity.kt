package chu.yi.dev.nocomment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalData.readFromCache(this.applicationContext)
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
        var groupStateMap by remember { mutableStateOf(viewMap.mapValues { it.value.map { entry -> entry.key to entry.value.invoke() } }) }

        //辅助函数与Map、List
        fun updateState() {
            isAccEnabled = isAccessibilityEnabled(context, packageName)
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
            AccessibilityStatusCard(isAccEnabled, context)

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

    // 无障碍功能权限状态卡片
    @Composable
    fun AccessibilityStatusCard(
        isAccessibilityEnabled: Boolean,
        context: Context
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
                    text = "无障碍功能权限状态：${if (isAccessibilityEnabled) "已经启用" else "未启用"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "前往无障碍设置", fontSize = 18.sp)
                }
            }
        }
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
                        isAccessibilityEnabled = isAccEnabled,
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
        isAccessibilityEnabled: Boolean,
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
                enabled = isAccessibilityEnabled
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