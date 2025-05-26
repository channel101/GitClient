package com.rk.git


import com.rk.libcommons.LoadingPopup
import com.rk.libcommons.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.lib.ProgressMonitor

class ProgressIndicator(private val loadingPopup: LoadingPopup) : ProgressMonitor{
    var totalWork: Int = 0
    var completed: Int = 0
    var cancelled = false
    var title: String = ""

    override fun start(totalTasks: Int) {
        totalWork = totalTasks
    }

    override fun beginTask(title: String?, totalWork: Int) {
        this.totalWork = totalWork
        this.title = title.toString()

        runOnUiThread{
            loadingPopup.setMessage("$title $completed/$totalWork")
        }
    }

    override fun update(completed: Int) {
        this.completed = completed
        runBlocking(Dispatchers.Main){
            loadingPopup.setMessage("$title $completed/$totalWork")
            delay(80)
        }
    }

    override fun endTask() {
        if (completed >= totalWork){
            loadingPopup.hide()
        }
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

}