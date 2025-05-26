package com.rk.git

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import com.rk.controlpanel.ControlItem
import com.rk.extension.*
import com.rk.file_wrapper.FileWrapper
import com.rk.libcommons.ActionPopup
import com.rk.libcommons.askInput
import com.rk.libcommons.toast
import com.rk.xededitor.MainActivity.MainActivity

class Main : ExtensionAPI() {
    override fun onPluginLoaded(extension: Extension) {}

    override fun onMainActivityCreated() {
        //add git setting screen
        Hooks.Settings.screens["git"] = SettingsScreen(
            label = "Git",
            description = "Git client settings",
            route = "git",
            icon = {
                // Icon(painter = painterResource())
                Icon(imageVector = Icons.Default.Build,contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            content = { SettingsGitScreen() }
        )

        //add items in control panel
        Hooks.ControlPanel.controlItems["pull"] = ControlItem(label = "Pull", description = "GitClient", hideControlPanelOnClick = true, sideEffect = {
            pull.invoke()
        })

        Hooks.ControlPanel.controlItems["push"] = ControlItem(label = "Push", description = "GitClient", hideControlPanelOnClick = true, sideEffect = {
            push.invoke()
        })

        Hooks.ControlPanel.controlItems["commit"] = ControlItem(label = "Commit", description = "GitClient",  hideControlPanelOnClick = true, sideEffect = {
            commit.invoke(true){}
        })

        Hooks.ControlPanel.controlItems["commit + push"] = ControlItem(label = "Commit + Push", description = "GitClient",  hideControlPanelOnClick = true, sideEffect = {
            commit.invoke(false){
                push.invoke()
            }
        })

//        Hooks.ControlPanel.controlItems["log"] = ControlItem(label = "Logs", description = "GitClient",  hideControlPanelOnClick = true, sideEffect = {
//            toast("Not Implemented")
//        })

        Hooks.FileActions.actionPopupHook["git-clone"] = { fileAction ->
            if (fileAction.file.isDirectory() && fileAction.file is FileWrapper){
                MainActivity.withContext {
                    addItem(title = "Clone Repo", description = "Clone a git repository", icon = getDrawableByName(this,"github"), listener = {
                        askInput(title = "CLone", hint = "https://github.com/Xed-Editor/Xed-Editor", onResult = { input ->
                            if (input.isBlank()){
                                toast("Invalid Input")
                                return@askInput
                            }

                            clone.invoke(input,fileAction.file as FileWrapper)
                        })
                    })
                }
            }
        }

    }

    override fun onMainActivityPaused() {
        
    }

    override fun onMainActivityResumed() {
        
    }

    override fun onMainActivityDestroyed() {
        
    }

    override fun onLowMemory() {

    }

    fun getDrawableByName(context: Context, name: String): Drawable? {
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return ContextCompat.getDrawable(context, resId)
    }

}
