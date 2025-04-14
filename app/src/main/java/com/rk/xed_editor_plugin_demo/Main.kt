package com.rk.xed_editor_plugin_demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.rk.controlpanel.ControlItem
import com.rk.extension.*
import com.rk.libcommons.toast

class Main : ExtensionAPI() {
    var init = false

    override fun onPluginLoaded(extension: Extension) {

    }

    override fun onMainActivityCreated() {
        if (init){
            return
        }

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
            commit.invoke(true)
        })

        Hooks.ControlPanel.controlItems["commit + push"] = ControlItem(label = "Commit + Push", description = "GitClient",  hideControlPanelOnClick = true, sideEffect = {
            commit.invoke(false)
        })

        Hooks.ControlPanel.controlItems["log"] = ControlItem(label = "Logs", description = "GitClient",  hideControlPanelOnClick = true, sideEffect = {
            toast("Not Implemented")
        })

        init = true
    }

    override fun onMainActivityPaused() {
        
    }

    override fun onMainActivityResumed() {
        
    }

    override fun onMainActivityDestroyed() {
        
    }

    override fun onLowMemory() {

    }
}
