package com.rk.xed_editor_plugin_demo

import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.file_wrapper.FileWrapper
import com.rk.libcommons.LoadingPopup
import com.rk.libcommons.askInput
import com.rk.libcommons.toast
import com.rk.xededitor.MainActivity.MainActivity
import com.rk.xededitor.MainActivity.tabs.editor.EditorFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
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


