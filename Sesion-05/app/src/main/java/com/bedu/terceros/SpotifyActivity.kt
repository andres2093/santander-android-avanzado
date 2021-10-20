package com.bedu.terceros

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bedu.terceros.SpotifyActivity.AuthParams.CLIENT_ID
import com.bedu.terceros.SpotifyActivity.AuthParams.REDIRECT_URI
import com.bedu.terceros.SpotifyActivity.SpotifySampleContexts.ALBUM_URI
import com.bedu.terceros.SpotifyActivity.SpotifySampleContexts.ARTIST_URI
import com.bedu.terceros.SpotifyActivity.SpotifySampleContexts.PLAYLIST_URI
import com.bedu.terceros.SpotifyActivity.SpotifySampleContexts.PODCAST_URI
import com.bedu.terceros.SpotifyActivity.SpotifySampleContexts.TRACK_URI
import com.bedu.terceros.databinding.ActivitySpotifyBinding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SpotifyActivity : AppCompatActivity() {

    object AuthParams {
        const val CLIENT_ID = "bd8d948bd60e430ab85ed4b2c6438975"
        const val REDIRECT_URI = "https://developer.spotify.com/dashboard"
    }

//    https://open.spotify.com/track/2Foc5Q5nqNiosCNqttzHof?si=8292b1e9b6174ce9

    object SpotifySampleContexts {
        const val TRACK_URI = "spotify:track:2Foc5Q5nqNiosCNqttzHof"
        const val ALBUM_URI = "spotify:album:50oWFJ0mDEeMa74ElsdAp3"
        const val ARTIST_URI = "spotify:artist:3WrFJ7ztbogyGnTHbHJFl2"
        const val PLAYLIST_URI = "spotify:playlist:37i9dQZF1E8OchtdOZK7KR"
        const val PODCAST_URI = "spotify:show:5pMNIoy0SoxMBiWZ1qtk4h"
    }

    companion object {
        const val TAG = "SpotifyActivity"
    }

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null

    private lateinit var views: List<View>
    private lateinit var trackProgressBar: TrackProgressBar
    private lateinit var binding: ActivitySpotifyBinding

    private val errorCallback = { throwable: Throwable -> logError(throwable) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySpotifyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.seekTo.apply {
            isEnabled = false
            progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }

        trackProgressBar =
            TrackProgressBar(binding.seekTo) { seekToPosition: Long -> seekTo(seekToPosition) }

        views = listOf(
            binding.disconnectButton,
            binding.subscribeToPlayerContextButton,
            binding.subscribeToPlayerStateButton,
            binding.playPauseButton,
            binding.skipPrevButton,
            binding.skipNextButton,
            binding.playPodcastButton,
            binding.playTrackButton,
            binding.playAlbumButton,
            binding.playArtistButton,
            binding.playPlaylistButton,
            binding.seekTo
        )

        SpotifyAppRemote.setDebugMode(false)

        onDisconnected()
        onConnectAndAuthorizedClicked(binding.connectAuthorizeButton)

    }

    private fun seekTo(seekToPosition: Long) {
        assertAppRemoteConnected()
            .playerApi
            .seekTo(seekToPosition)
            .setErrorCallback(errorCallback)
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        onDisconnected()
    }

    private fun onConnected() {
        for (input in views) {
            input.isEnabled = true
        }
        binding.connectButton.apply {
            isEnabled = false
            text = getString(R.string.connected)
        }
        binding.connectAuthorizeButton.apply {
            isEnabled = false
            text = getString(R.string.connected)
        }

        onSubscribedToPlayerStateButtonClicked(binding.subscribeToPlayerStateButton)
        onSubscribedToPlayerContextButtonClicked(binding.subscribeToPlayerContextButton)
    }

    private fun onConnecting() {
        binding.connectButton.apply {
            isEnabled = false
            text = getString(R.string.connecting)
        }
        binding.connectAuthorizeButton.apply {
            isEnabled = false
            text = getString(R.string.connecting)
        }
    }

    private fun onDisconnected() {
        for (view in views) {
            view.isEnabled = false
        }
        binding.connectButton.apply {
            isEnabled = true
            text = getString(R.string.connect)
        }
        binding.connectAuthorizeButton.apply {
            isEnabled = true
            text = getString(R.string.authorize)
        }
        binding.image.setImageResource(R.drawable.widget_placeholder)
        binding.subscribeToPlayerContextButton.apply {
            visibility = View.VISIBLE
            setText(R.string.title_player_context)
        }
        binding.subscribeToPlayerStateButton.apply {
            visibility = View.VISIBLE
            setText(R.string.title_current_track)
        }

        binding.currentContextLabel.visibility = View.INVISIBLE
        binding.currentTrackLabel.visibility = View.INVISIBLE
    }

    fun onConnectClicked(notUsed: View) {
        onConnecting()
        connect(false)
    }

    fun onConnectAndAuthorizedClicked(notUsed: View) {
        onConnecting()
        connect(true)
    }

    private fun connect(showAuthView: Boolean) {

        SpotifyAppRemote.disconnect(spotifyAppRemote)
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(showAuthView)
                onConnected()
            } catch (error: Throwable) {
                onDisconnected()
                logError(error)
            }
        }
    }

    private suspend fun connectToAppRemote(showAuthView: Boolean): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                application,
                ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(showAuthView)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        cont.resumeWithException(error)
                    }
                })
        }

    fun onDisconnectClicked(notUsed: View) {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        onDisconnected()
    }

    fun onSubscribedToPlayerContextButtonClicked(notUsed: View) {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)

        binding.currentContextLabel.visibility = View.VISIBLE
        binding.subscribeToPlayerContextButton.visibility = View.INVISIBLE
        playerContextSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerContext()
            .setEventCallback(playerContextEventCallback)
            .setErrorCallback { throwable ->
                binding.currentContextLabel.visibility = View.INVISIBLE
                binding.subscribeToPlayerContextButton.visibility = View.VISIBLE
                logError(throwable)
            } as Subscription<PlayerContext>
    }

    fun onSubscribedToPlayerStateButtonClicked(notUsed: View) {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)

        binding.currentTrackLabel.visibility = View.VISIBLE
        binding.subscribeToPlayerStateButton.visibility = View.INVISIBLE

        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
            .setEventCallback(playerStateEventCallback)
            .setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        logMessage("Event: start")
                    }

                    override fun onStop() {
                        logMessage("Event: end")
                    }
                })
            .setErrorCallback {
                binding.currentTrackLabel.visibility = View.INVISIBLE
                binding.subscribeToPlayerStateButton.visibility = View.VISIBLE
            } as Subscription<PlayerState>
    }

    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?): Subscription<T>? {
        return subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
            null
        }
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        Log.e(TAG, getString(R.string.err_spotify_disconnected))
        throw SpotifyDisconnectedException()
    }

    private fun logError(throwable: Throwable) {
        Toast.makeText(this, R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "", throwable)
    }

    private fun logMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
//        Toast.makeText(this, msg, duration).show()
        Log.d(TAG, msg)
    }

    private val playerContextEventCallback =
        Subscription.EventCallback<PlayerContext> { playerContext ->
            binding.currentContextLabel.apply {
                text = String.format(
                    Locale.getDefault(),
                    "%s\n%s",
                    playerContext.title,
                    playerContext.subtitle
                )
                tag = playerContext
            }
        }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->

        updateTrackStateButton(playerState)

        updatePlayPauseButton(playerState)

        updateTrackCoverArt(playerState)

        updateSeekbar(playerState)
    }

    private fun updatePlayPauseButton(playerState: PlayerState) {
        // Invalidate play / pause
        if (playerState.isPaused) {
            binding.playPauseButton.setImageResource(R.drawable.btn_play)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.btn_pause)
        }
    }

    private fun updateTrackStateButton(playerState: PlayerState) {
        binding.currentTrackLabel.apply {
            text = String.format(
                Locale.US,
                "%s\n%s",
                playerState.track.name,
                playerState.track.artist.name
            )
            tag = playerState
        }
    }

    private fun updateSeekbar(playerState: PlayerState) {
        // Update progressbar
        trackProgressBar.apply {
            if (playerState.playbackSpeed > 0) {
                unpause()
            } else {
                pause()
            }
            // Invalidate seekbar length and position
            binding.seekTo.max = playerState.track.duration.toInt()
            binding.seekTo.isEnabled = true
            setDuration(playerState.track.duration)
            update(playerState.playbackPosition)
        }
    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        // Get image from track
        assertAppRemoteConnected()
            .imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
            .setResultCallback { bitmap ->
                binding.image.setImageBitmap(bitmap)
            }
    }

    fun onSkipPreviousButtonClicked(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .skipPrevious()
            .setResultCallback { logMessage(getString(R.string.command_feedback, "skip previous")) }
            .setErrorCallback(errorCallback)
    }

    fun onPlayPauseButtonClicked(notUsed: View) {
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        it.playerApi
                            .resume()
                            .setResultCallback {
                                logMessage(
                                    getString(
                                        R.string.command_feedback,
                                        "play"
                                    )
                                )
                            }
                            .setErrorCallback(errorCallback)
                    } else {
                        it.playerApi
                            .pause()
                            .setResultCallback {
                                logMessage(
                                    getString(
                                        R.string.command_feedback,
                                        "pause"
                                    )
                                )
                            }
                            .setErrorCallback(errorCallback)
                    }
                }
        }
    }

    fun onSkipNextButtonClicked(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .skipNext()
            .setResultCallback { logMessage(getString(R.string.command_feedback, "skip next")) }
            .setErrorCallback(errorCallback)
    }

    fun onPlayPodcastButtonClicked(notUsed: View) {
        playUri(PODCAST_URI)
    }

    fun onPlayTrackButtonClicked(notUsed: View) {
        playUri(TRACK_URI)
    }

    fun onPlayAlbumButtonClicked(notUsed: View) {
        playUri(ALBUM_URI)
    }

    fun onPlayArtistButtonClicked(notUsed: View) {
        playUri(ARTIST_URI)
    }

    fun onPlayPlaylistButtonClicked(notUsed: View) {
        playUri(PLAYLIST_URI)
    }

    private fun playUri(uri: String) {
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
            .setErrorCallback(errorCallback)
    }

}
