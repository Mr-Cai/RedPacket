package red.packet

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Vibrator

/**
 * Description:
 */

object RedpacketHelper {

    /**
     * 播放摇一摇声音
     * @param context
     */
    fun playShakeSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.shake)
        mediaPlayer.start()
    }

    /**
     * 播放摇一摇声音
     * @param context
     */
    fun playSound(context: Context) {
        val soundPool = SoundPool(1, AudioManager.STREAM_SYSTEM, 0)
        val soundID = soundPool.load(context, R.raw.shake, 1)
        soundPool.play(soundID, 1f, 1f, 0, 0, 1f)
    }

    /**
     * 震动
     * @param context
     * @param milliseconds
     */
    fun vibrate(context: Context, milliseconds: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(milliseconds)
    }
}
