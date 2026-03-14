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
import com.helper.MainActivity
import com.helper.databinding.FragmentVideoBinding
import kotlin.getValue
@UnstableApi
@OptIn(androidx.media3.common.util.UnstableApi::class)
class VideoFragment : Fragment() {
    private val dataLC: DataLC by activityViewModels()

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null

    // Для теста: начало и конец воспроизведения в миллисекундах
    private val startMs: Long = 5000L   // проигрывать с 5-й секунды
    private val endMs: Long = 15000L    // до 15-й секунды

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

        // --- Тестовое видео ---
        val videoUri = "android.resource://${requireContext().packageName}/raw/test".toUri()

        // --- Тестовые start и end прямо в методе ---
        val startMs: Long = 0L   // с 5-й секунды
        val endMs: Long = C.TIME_END_OF_SOURCE    // до 15-й секунды

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

    companion object {
        @JvmStatic
        fun newInstance()=VideoFragment()
    }
}