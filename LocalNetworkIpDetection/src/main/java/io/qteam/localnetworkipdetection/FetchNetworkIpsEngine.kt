package io.qteam.localnetworkipdetection

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executors


class NetworkIpDetection private constructor(
    private var startRange: Int = 2,
    private var endRange: Int = 255,
    private var subnet: String = "192.168.1",
    private var observeResultInMainThread: Boolean = false
) {


    fun fetchNetworkIps(listener: ProgressListener) {
        fetchNetworkIps(startRange, endRange, subnet, observeResultInMainThread, listener)
    }

    class Builder : IBuilder<NetworkIpDetection> {
        private var startRange: Int = 2
        private var endRange: Int = 255
        private var subnet: String = "192.168.1"
        private var observeResultInMainThread: Boolean = false
        fun startRange(startRange: Int): Builder {
            this.startRange = startRange
            return this
        }

        fun endRange(endRange: Int): Builder {
            this.endRange = endRange
            return this
        }

        fun subnet(subnet: String): Builder {
            this.subnet = subnet
            return this
        }

        fun observeResultInMainThread(observeResultInMainThread: Boolean): Builder {
            this.observeResultInMainThread = observeResultInMainThread
            return this
        }

        override fun build(): NetworkIpDetection =
            NetworkIpDetection(startRange, endRange, subnet, observeResultInMainThread)
    }
}


private fun fetchNetworkIps(
    startRange: Int,
    endRange: Int,
    subnet: String,
    observeResultInMainThread: Boolean,
    listener: ProgressListener
) {
    Observable.fromCallable {
        val resultList = mutableListOf<String>()
        val commandList = mutableListOf<Pair<String, String>>()
        for (i in startRange..endRange) {
            val currentHost = "$subnet.$i"
            commandList.add(Pair(currentHost, "ping -c 1 $currentHost"))
        }

        val executor = Executors.newFixedThreadPool(4)
        var lastCommittedProgress = 0
        printInitInformations(
            startRange,
            endRange,
            subnet,
            observeResultInMainThread,
            listener
        )
        for (command in commandList) {
            val progress = (commandList.indexOf(command) * 100) / commandList.size
            executor.execute {
                val processBuilder = ProcessBuilder(command.second.split(" "))
                var process: Process? = null
                try {
                    process = processBuilder.start()
                    BufferedReader(InputStreamReader(process?.inputStream)).use { reader ->
                        var line: String?
                        while ((reader.readLine().also { line = it }) != null) {
                            // Process the output
                        }
                    }
                    // Wait for the process to finish
                    val returnVal = process?.waitFor()
                    val reachable = (returnVal == 0)
                    if (reachable) {
                        resultList.add(command.first)
                    }

                    // reporting network detection progress section
                    if (progress in 0..19 && lastCommittedProgress == 0) {
                        listener.onUpdate("Fetching Network Ips in progress $lastCommittedProgress% and found ${resultList.size} devices")

                        lastCommittedProgress = 10
                    } else if (progress in 20..39 && lastCommittedProgress < 20) {
                        lastCommittedProgress = 20
                        listener.onUpdate("Fetching Network Ips in progress $lastCommittedProgress% and found ${resultList.size} devices")
                    } else if (progress in 40..59 && lastCommittedProgress < 40) {
                        lastCommittedProgress = 40
                        listener.onUpdate("Fetching Network Ips in progress $lastCommittedProgress% and found ${resultList.size} devices")
                    } else if (progress in 60..79 && lastCommittedProgress < 60) {
                        lastCommittedProgress = 60
                        listener.onUpdate("Fetching Network Ips in progress $lastCommittedProgress% and found ${resultList.size} devices")
                    } else if (progress in 80..89 && lastCommittedProgress < 80) {
                        lastCommittedProgress = 80
                        listener.onUpdate("Fetching Network Ips in progress $lastCommittedProgress% and found ${resultList.size} devices")
                    } else if (progress in 90..97 && lastCommittedProgress < 90) {
                        lastCommittedProgress = 90
                        listener.onUpdate("Fetching Network Ips in progress $lastCommittedProgress% and found ${resultList.size} devices")
                    } else if (progress in 98..100 && lastCommittedProgress < 100) {
                        lastCommittedProgress = 100
                        listener.onUpdate("Fetching Network Ips in progress 100% and found ${resultList.size} devices")
                    }
                } catch (e: IOException) {
                    Log.e("ALLAH", "getNetworkIps: ${e.message}")
                } catch (e: InterruptedException) {
                    Log.e("ALLAH", "getNetworkIps: ${e.message}")
                } finally {
                    if (process != null) {
                        process?.inputStream?.close()
                        process?.outputStream?.close()
                        process?.errorStream?.close()
                        process?.destroy()
                    }
                }
            }
        }
        executor.shutdown()
        while (!executor.isTerminated) {
        }
        listener.onComplete(resultList)
    }.subscribeOn(Schedulers.io())
        .observeOn(observeClientDemand(observeResultInMainThread))
        .subscribe()


}

private fun observeClientDemand(observeResultInMainThread: Boolean): Scheduler {
    if (observeResultInMainThread) {
        return AndroidSchedulers.mainThread()
    } else {
        return Schedulers.io()
    }
}

private fun printInitInformations(
    startRange: Int,
    endRange: Int,
    subnet: String,
    observeResultInMainThread: Boolean,
    listener: ProgressListener
) {
    val stringBuilder = StringBuilder()
    stringBuilder.append("**************************************************")
    stringBuilder.appendLine()
    stringBuilder.append("Starting to discover your local network IPs ...")
    stringBuilder.appendLine()
    stringBuilder.append("Subnet configured to: $subnet")
    stringBuilder.appendLine()
    stringBuilder.append("Starting discovery from ip: $subnet.$startRange")
    stringBuilder.appendLine()
    stringBuilder.append("Ending discovery to ip: $subnet.$endRange")
    stringBuilder.appendLine()
    stringBuilder.append("Observation will be done in ")
    if (observeResultInMainThread) {
        stringBuilder.append("Main Thread")
    } else {
        stringBuilder.append("IO Thread")
    }
    stringBuilder.appendLine()
    stringBuilder.append("**************************************************")
    listener.onUpdate(stringBuilder.toString())
}
