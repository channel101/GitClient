package com.rk.xed_editor_plugin_demo

import android.content.Context
import android.graphics.drawable.PaintDrawable
import android.util.Patterns
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.controlpanel.ControlItem
import com.rk.extension.ExtensionAPI
import com.rk.extension.Hooks
import com.rk.extension.SettingsScreen
import com.rk.file_wrapper.FileWrapper
import com.rk.libcommons.LoadingPopup
import com.rk.libcommons.alpineHomeDir
import com.rk.libcommons.askInput
import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import com.rk.libcommons.toast
import com.rk.settings.Settings
import com.rk.xededitor.MainActivity.MainActivity
import com.rk.xededitor.MainActivity.tabs.editor.EditorFragment
import com.rk.xededitor.ui.components.SettingsToggle
import com.rk.xededitor.ui.screens.settings.feature_toggles.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.util.FS
import java.io.File

val commit:(Boolean)-> Unit = { askForMessage ->
    val context = MainActivity.activityRef.get()!!
    val currentFile = context?.adapter?.getCurrentFragment()?.fragment!!

    if (currentFile is EditorFragment && currentFile.file is FileWrapper){
        context.lifecycleScope.launch(Dispatchers.Main){


            val root = findGitRoot((currentFile.file as FileWrapper).file)

            if (root != null){

                val commitInternal:(String)-> Unit = { message ->
                    context.lifecycleScope.launch(Dispatchers.IO) {
                        val loading = LoadingPopup(ctx = context,hideAfterMillis = null)
                        loading.show()
                        runCatching {
                            Git.open(root).use { git ->
                                val config = loadGitConfig(context)
                                val username = config.first
                                val email = config.second
                                val passwd = getToken(context)

                                git.add().addFilepattern(".").call()
                                git.commit().setMessage(message).apply {
                                    setAuthor(username,email)
                                    setCommitter(username, email)
                                    if (username != "root" && passwd.isNotEmpty()) {
                                        setCredentialsProvider(
                                            UsernamePasswordCredentialsProvider(
                                                username, passwd
                                            )
                                        )

                                    }
                                }.call()
                            }
                        }.onFailure {
                            withContext(Dispatchers.Main){
                                MaterialAlertDialogBuilder(context).apply {
                                    setTitle("Error")
                                    setMessage(it.message)
                                    setPositiveButton("OK",null)
                                    show()
                                }
                            }
                        }
                        loading.hide()
                    }
                }

                if (askForMessage){
                    withContext(Dispatchers.Main){
                        context.askInput(title = "Message", hint = "Commit Message", onResult = { message ->
                            if (message.isBlank()){
                                toast("Commit message can not be empty")
                            }else{
                                commitInternal.invoke(message)
                            }
                        })
                    }
                }else{
                    commitInternal.invoke("Auto Commit")
                }

            }else{
                toast("Unable to find a git repository for this file")
            }
        }
    }else{
        toast("Unsupported file type only native files are supported")

    }
}

val push = {
    val context = MainActivity.activityRef.get()!!
    val currentFile = context?.adapter?.getCurrentFragment()?.fragment!!

    if (currentFile is EditorFragment && currentFile.file is FileWrapper){
        context.lifecycleScope.launch(Dispatchers.Main){
            val loading = LoadingPopup(ctx = context,hideAfterMillis = null)
            loading.show()

            val root = findGitRoot((currentFile.file as FileWrapper).file)

            if (root != null){
                withContext(Dispatchers.IO){
                    runCatching {
                        Git.open(root).use { git ->
                            git.push().apply {
                                val config = loadGitConfig(context)
                                val username = config.first
                                val passwd = getToken(context)

                                if (username.isNotEmpty() && passwd.isNotEmpty()) {
                                    setCredentialsProvider(
                                        UsernamePasswordCredentialsProvider(
                                            username, passwd
                                        )
                                    )
                                }
                                call()
                            }
                        }
                    }.onFailure {
                        withContext(Dispatchers.Main){
                            MaterialAlertDialogBuilder(context).apply {
                                setTitle("Error")
                                setMessage(it.message)
                                setPositiveButton("OK",null)
                                show()
                            }
                        }
                    }

                }

            }else{
                toast("Unable to find a git repository for this file")
            }

            loading.hide()
        }


    }else{
        toast("Unsupported file type only native files are supported")

    }
}

val pull = {
    val context = MainActivity.activityRef.get()!!
    val currentFile = context?.adapter?.getCurrentFragment()?.fragment!!

    if (currentFile is EditorFragment && currentFile.file is FileWrapper){
        context.lifecycleScope.launch(Dispatchers.Main){
            val loading = LoadingPopup(ctx = context,hideAfterMillis = null)
            loading.show()

            val root = findGitRoot((currentFile.file as FileWrapper).file)

            if (root != null){
                withContext(Dispatchers.IO){
                    runCatching {
                        Git.open(root).use { git ->
                            git.pull().apply {
                                val config = loadGitConfig(context)
                                val username = config.first
                                val passwd = getToken(context)

                                if (username.isNotEmpty() && passwd.isNotEmpty()) {
                                    setCredentialsProvider(
                                        UsernamePasswordCredentialsProvider(
                                            username, passwd
                                        )
                                    )
                                }
                                call()
                            }
                        }
                    }.onFailure {
                        withContext(Dispatchers.Main){
                            MaterialAlertDialogBuilder(context).apply {
                                setTitle("Error")
                                setMessage(it.message)
                                setPositiveButton("OK",null)
                                show()
                            }
                        }
                    }

                }

            }else{
                toast("Unable to find a git repository for this file")
            }

            loading.hide()
        }


    }else{
        toast("Unsupported file type only native files are supported")

    }

}


class Main : ExtensionAPI() {
    override fun onPluginLoaded() {
        //add git setting screen
        Hooks.Settings.screens["git"] = SettingsScreen(
            label = "Git",
            description = "Git client settings",
            route = "git",
            icon = {
                Icon(imageVector = Icons.Default.Build,contentDescription = null)
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

    }

    override fun onAppCreated() {
       
    }

    override fun onAppLaunched() {
       
    }

    override fun onAppPaused() {
       
    }

    override fun onAppResumed() {
       
    }

    override fun onAppDestroyed() {
       
    }

    override fun onLowMemory() {
       
    }
}

private val gits = mutableSetOf<String>()
suspend fun findGitRoot(file: File): File? {
    return withContext(Dispatchers.IO){
        gits.forEach { root ->
            if (file.absolutePath.contains(root)) {
                return@withContext File(root)
            }
        }
        var currentFile = file
        while (currentFile.parentFile != null) {
            if (File(currentFile.parentFile, ".git").exists()) {
                currentFile.parentFile?.let { gits.add(it.absolutePath) }
                return@withContext currentFile.parentFile
            }
            currentFile = currentFile.parentFile!!
        }
        return@withContext null
    }

}


