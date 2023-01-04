package tk.mallumo.io

import api.rc.RCMessage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tk.mallumo.GlobalParams
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun File.timeStampFromName(): Int = name.split("_").firstOrNull()?.toIntOrNull() ?: 0

private val dirFormat = SimpleDateFormat("yyyyMMdd")
private val fileFormat = SimpleDateFormat("HHmmssSSS")

val Calendar.dirDtName: String
    get() = dirFormat.format(time)

val Calendar.fileDtName: String
    get() = fileFormat.format(time)


class RepoDiskManager : ImplRepo() {

    override val scope: CoroutineScope = CoroutineScope(CoroutineName("DiskManager") + Dispatchers.IO)

    fun storeImage(id: Int, data: ByteArray): String {

        val cal = Calendar.getInstance()
        val timeStamp = cal.fileDtName

        if (GlobalParams.backupDays < 1) return timeStamp
        if (cal[Calendar.HOUR_OF_DAY] in 8..18) return timeStamp

        scope.launch {
            GlobalParams.getCamDirectory(id)?.runCatching {
                File(this, cal.dirDtName).apply {
                    if (!exists()) mkdirs()
                    File(this, "${timeStamp}.webp").writeBytes(data)
                }
                deleteOldDir(id)
            }

        }
        return timeStamp
    }

    private fun deleteOldDir(id: Int) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -GlobalParams.backupDays)
        }
        val dirDtNameMin = dirFormat.format(cal.time).toInt()
        GlobalParams.getCamDirectory(id)
            ?.listFiles()
            ?.filter { it.isFile }
            ?.filter { (it.name.toIntOrNull() ?: dirDtNameMin) < dirDtNameMin }
            ?.onEach { dir ->
                dir.runCatching {
                    deleteRecursively()
                }.onFailure { it.printStackTrace() }
            }
    }

    fun getHistoryStructure(id: Int): List<RCMessage.Content.StreamHistoryAnswer.Item> {
        return GlobalParams.getCamDirectory(id)
            ?.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { day ->
                day.listFiles()
                    ?.filter { it.isFile }
                    ?.let { imagesInDay ->
                        val images = imagesInDay.mapNotNull { it.timeStampFromName() }
                        images.minOrNull()?.let { min ->
                            images.maxOrNull()?.let { max ->
                                RCMessage.Content.StreamHistoryAnswer.Item(
                                    day = day.name,
                                    first = min.toString(),
                                    last = max.toString(),
                                )
                            }
                        }
                    }
            } ?: listOf()
    }

    fun getParts(id: Int, timeStart: String, durationMs: Long): List<File> {
        val start = Calendar.getInstance().apply {
            time = fileFormat.parse(timeStart)
        }
        val end = Calendar.getInstance().apply {
            time = start.time
            add(Calendar.MILLISECOND, durationMs.toInt())
        }

        fun getFiles(startCal: Calendar, endCal: Calendar): List<File> {
            val timeRange = startCal.fileDtName.toInt()..endCal.fileDtName.toInt()

            return GlobalParams.getCamDirectory(id)?.let {
                File(it, startCal.dirDtName)
                    .listFiles()
                    ?.filter { it.timeStampFromName() in timeRange }
                    ?.sortedBy { it.name.toInt() }
                    ?: listOf()
            } ?: listOf()
        }

        return buildList {
            var items: List<File>
            do {
                items = getFiles(start, end)
                addAll(items)
                start.add(Calendar.DAY_OF_MONTH, 1)
            } while (items.isNotEmpty())
        }
    }


}
