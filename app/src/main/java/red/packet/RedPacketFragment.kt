package red.packet

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.fragment.app.Fragment
import red.packet.bean.RedPacketResp


/**
 * Description:
 */

class RedPacketFragment : Fragment(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mAcceleration: Sensor? = null
    private var lastUpateTime: Long = 0
    private var lastX: Float = 0.toFloat()
    private var lastY: Float = 0.toFloat()
    private var lastZ: Float = 0.toFloat()
    private var tvRedPacket: TextView? = null
    private var ivShake: View? = null
    private var rlBanner: View? = null
    private val isRedPacketReceived: Boolean = false//已领取
    private var isOpening: Boolean = false//领取中，防止多次点击摇晃重复领取
    private var mRedPacketDialogFragment: RedPacketDialogFragment? = null
    private val mRedPacketResult = 94110.00

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_red_packet, container, false)
        initView(view)
        return view
    }

    protected fun initView(view: View) {
        rlBanner = view.findViewById(R.id.rl_red_banner)
        rlBanner!!.setOnClickListener { onClickRedPacketBanner() }
        tvRedPacket = view.findViewById<View>(R.id.tv_receive_hb) as TextView
        ivShake = view.findViewById(R.id.iv_hb_shake)
        initAccelerometerSensor()
    }


    /**
     * 点击领红包
     */
    private fun onClickRedPacketBanner() {
        if (isRedPacketReceived) {
            //已经领取，直接弹窗
            showRedPacketDialog(mRedPacketResult, false)
        } else {
            receiveRedPacket()
        }
    }

    /**
     * 领红包
     */
    private fun receiveRedPacket() {
        if (!isOpening) {
            isOpening = true
            tvRedPacket!!.text = "红包要来啦..."
            //动画领取
            startShakePhoneAnim()
            tvRedPacket!!.postDelayed({
                onReceiveRedPacketSuccess()
                isOpening = false
            }, 1000)
        }
    }

    private fun onReceiveRedPacketSuccess() {
        stopShakePhoneAnim()
        tvRedPacket!!.text = "谢谢参与"
        //停止传感器监听
        //        unregisterShakeListener();
        showRedPacketDialog(mRedPacketResult, !isRedPacketReceived)
        //        isRedPacketReceived = true;
    }

    /**
     * 展示红包弹窗
     */
    private fun showRedPacketDialog(result: Double, withAnimation: Boolean) {
        val bean = RedPacketResp()
        bean.isWithAnimation = withAnimation
        bean.redPocketAmount = result
        bean.account = "6666"
        mRedPacketDialogFragment = RedPacketDialogFragment.newInstance(bean)
        mRedPacketDialogFragment!!.show(childFragmentManager)
    }

    /**
     * 执行摇晃手机动画
     */
    private fun startShakePhoneAnim() {
        val rotateAnimation = RotateAnimation(SHAKE_DEGREES.toFloat(), (-SHAKE_DEGREES).toFloat(), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.repeatMode = Animation.REVERSE
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.duration = 300
        ivShake!!.startAnimation(rotateAnimation)
    }

    private fun stopShakePhoneAnim() {
        ivShake!!.clearAnimation()
    }

    override fun onResume() {
        super.onResume()
        registerShakeListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterShakeListener()
    }

    /**
     * 初始化加速度传感器
     */
    private fun initAccelerometerSensor() {
        val context = context
        mSensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (mSensorManager != null) {
            if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mAcceleration = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            } else {
                //设备上没有加速度传感器
                Log.w(TAG, "设备上没有加速度传感器")
            }
        }
    }

    /**
     * 开启传感器监听
     */
    private fun registerShakeListener() {
        if (mSensorManager != null && mAcceleration != null) {
            mSensorManager!!.registerListener(this, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * 停止传感器监听，界面暂停或使用传感器后
     */
    private fun unregisterShakeListener() {
        if (mSensorManager != null && mAcceleration != null) {
            mSensorManager!!.unregisterListener(this, mAcceleration)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        //传感器值发生改变，加速度传感器返回3个值
        val type = event.sensor.type
        if (type == Sensor.TYPE_ACCELEROMETER) {
            //摇一摇算法加强
            val currentTimeMillis = System.currentTimeMillis()
            val timeInterval = currentTimeMillis - lastUpateTime
            if (timeInterval < UPDATE_INTERVAL_TIME) {
                return
            }
            lastUpateTime = currentTimeMillis
            val aux = event.values[0]
            val auy = event.values[1]
            val auz = event.values[2]
            val deltaX = aux - lastX
            val deltaY = auy - lastY
            val deltaZ = auz - lastZ
            //            Log.d(TAG, "aux=" + aux + " auy=" + auy + " auz=" + auz);
            lastX = aux
            lastY = auy
            lastZ = auz
            val speed = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble())
            if (speed >= SPEED_SHAESHOLD) {
                onShake()
            }
        }
        /*//获取传感器类型
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            float aux = event.values[0];
            float auy = event.values[1];
            float auz = event.values[2];
            LogUtil.d("aux=" + aux + " auy=" + auy + " auz=" + auz);
            if (Math.abs(aux) > SHAKE_ACCELEROMETER_VALUE || Math.abs(auy) > SHAKE_ACCELEROMETER_VALUE || Math.abs(auz) > SHAKE_ACCELEROMETER_VALUE) {
                onShake();
            }
        }*/
    }

    /**
     * 摇一摇
     */
    private fun onShake() {
        if (isOpening || mRedPacketDialogFragment != null && mRedPacketDialogFragment!!.dialog != null && mRedPacketDialogFragment!!.dialog.isShowing) {
            return
        }
        //        Log.d(TAG, "摇一摇");
        //播放摇一摇音乐
        RedpacketHelper.playShakeSound(context!!)
        //震动
        RedpacketHelper.vibrate(context!!, 300)
        onClickRedPacketBanner()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //传感器的精度发生改变
    }

    companion object {

        val SHAKE_ACCELEROMETER_VALUE = 17f//摇晃幅度
        const val UPDATE_INTERVAL_TIME: Long = 50
        const val SPEED_SHAESHOLD = 20//调节灵敏度
        const val SHAKE_DEGREES = 20
        val TAG: String = RedPacketFragment::class.java.simpleName
    }


}
