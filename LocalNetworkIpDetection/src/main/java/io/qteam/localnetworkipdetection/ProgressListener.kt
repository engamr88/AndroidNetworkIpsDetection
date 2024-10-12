package io.qteam.localnetworkipdetection

interface ProgressListener {
    fun onStart(message: String)
    fun onUpdate(percentage: Int)
    fun onComplete(ipList:MutableList<String>)
}