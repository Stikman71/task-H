package com.helper.TaskFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.helper.DataManager.DataLC
import com.helper.Logic.JSON.PathBuilder
import com.helper.Logic.JSON.sanitizeTopicName
import com.helper.MainActivity
import com.helper.databinding.FragmentVideoBinding
import org.json.JSONObject
import java.io.FileNotFoundException
import kotlin.getValue
@UnstableApi
@OptIn(UnstableApi::class)
class VideoFragment : Fragment() {
    private val dataLC: DataLC by activityViewModels()

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null

    // Для теста: начало и конец воспроизведения в миллисекундах
    private var startMs: Long = 0L   // проигрывать с 5-й секунды
    private var endMs: Long = C.TIME_END_OF_SOURCE   // до 15-й секунды

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playerView.controllerShowTimeoutMs = 1000
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        (requireActivity() as? MainActivity)?.saveCurrentFragment(outState)
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        val pathParts = arrayOf(
            "Tasks",
            dataLC.currentSession.value?.language!!,
            "Class_${dataLC.currentSession.value?.classID!!}",
            sanitizeTopicName(dataLC.currentSession.value?.topic!!),
            "video.json"
        )
        val path = PathBuilder.buildPathForTask(pathParts)
        val jsonString = try {
            context?.assets?.open(path)?.bufferedReader().use { it?.readText() }
        } catch (e: FileNotFoundException) {
            android.util.Log.d("PathBuilder", "Файл не найден: $path")
            throw RuntimeException("Файл не найден: $path")
        }
        val jsonObject = JSONObject(jsonString!!)
        // --- Тестовое видео ---
        //val videoUri = "android.resource://${requireContext().packageName}/raw/test".toUri()
        val rawUrl = jsonObject.get("url").toString()
        val videoUri = getDriveDirectLink(rawUrl)?.toUri() ?: return
        endMs=jsonObject.get("endTime").toString().toLong()
        startMs=jsonObject.get("startTime").toString().toLong()

        // Создаём MediaItem с Clipping
        val mediaItem = MediaItem.Builder()
            .setUri(videoUri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMs)
                    .setEndPositionMs(endMs)
                    .build()
            )
            .build()

        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }


    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDriveDirectLink(rawUrl: String): String? {
        // Регулярка для извлечения FILE_ID
        val regex = "https://drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)/view.*".toRegex()
        val match = regex.find(rawUrl)
        val fileId = match?.groups?.get(1)?.value
        return fileId?.let { "https://drive.google.com/uc?export=download&id=$it" }
    }

    companion object {
        @JvmStatic
        fun newInstance()=VideoFragment()
    }
}