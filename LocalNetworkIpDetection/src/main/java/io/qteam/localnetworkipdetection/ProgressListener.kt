package io.qteam.localnetworkipdetection

interface ProgressListener {
    fun onUpdate(message: String)
    fun onComplete(ipList:MutableList<String>)
}