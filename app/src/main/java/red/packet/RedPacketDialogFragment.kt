package red.packet

import android.animation.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import red.packet.bean.RedPacketResp

class RedPacketDialogFragment : DialogFragment() {
    private var mView: View? = null
    private var ivCoins: View? = null
    private var ivOpen: View? = null
    private var flClosed: View? = null
    private var ivlogo: View? = null
    private var llPacket: View? = null
    private var ivCancel: View? = null
    private var ivCover: View? = null
    private var tvCongratulateAcquire: View? = null
    private var tvReceiveAccount: TextView? = null
    private var mRedPacketResult: Double = 0.toDouble()
    private var mRedPacket: RedPacketResp? = null
    private var rollNumView: RollNumView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (dialog != null) {
            //无标题
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            //触摸外面不消失
            dialog.setCanceledOnTouchOutside(false)
            if (dialog.window != null) {
                //设置背景色透明
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        mView = inflater.inflate(R.layout.dialog_red_packet, container, false)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupWindow()
        initView(mView)
    }

    private fun setupWindow() {
        val screenWidth = context!!.resources.displayMetrics.widthPixels
        val screenHeight = context!!.resources.displayMetrics.heightPixels
        val lp = dialog.window!!.attributes
        lp.width = screenWidth
        lp.height = screenHeight
        dialog.window!!.attributes = lp
    }

    protected fun initView(view: View?) {
        ivCoins = view!!.findViewById(R.id.iv_hb_coins)
        ivOpen = view.findViewById(R.id.iv_hb_open)
        flClosed = view.findViewById(R.id.fl_hb_closed)
        ivlogo = view.findViewById(R.id.iv_hb_logo)
        ivCover = view.findViewById(R.id.iv_hb_cover)
        llPacket = view.findViewById(R.id.ll_packet)
        tvCongratulateAcquire = view.findViewById(R.id.tv_congratulate_acquire)
        tvReceiveAccount = view.findViewById<View>(R.id.tv_receive_hb_account) as TextView
        rollNumView = view.findViewById<View>(R.id.roll_num_view) as RollNumView
        ivCancel = view.findViewById(R.id.iv_cancel)
        ivCancel!!.setOnClickListener { dismiss() }
        mRedPacket = arguments!!.getSerializable(EXTRA_KEY) as RedPacketResp
        var withAnimation = true
        if (mRedPacket != null) {
            mRedPacketResult = mRedPacket!!.redPocketAmount
            tvReceiveAccount!!.text = getString(R.string.f_receive_hb_account, mRedPacket!!.account)
            withAnimation = mRedPacket!!.isWithAnimation
        }
        if (withAnimation) {
            setCameraDistance()
            //View没有测量完毕，无法获取宽高，投递Runnable到消息队列尾部，动画中需要宽高
            ivCancel!!.post { openRedPacket() }
        } else {
            showRedPacketResult()
        }
    }

    /**
     * 设置视角距离，贴近屏幕（布局Y轴旋转，需改变视角，否则会超出影响视觉体验）
     */
    private fun setCameraDistance() {
        val distance = 16000
        val scale = context!!.resources.displayMetrics.density * distance
        flClosed!!.cameraDistance = scale
    }

    /**
     * 直接展示红包数额
     */
    private fun showRedPacketResult() {
        ivCoins!!.visibility = View.GONE
        ivOpen!!.visibility = View.GONE
        flClosed!!.visibility = View.VISIBLE
        ivlogo!!.visibility = View.VISIBLE
        rollNumView!!.setNumberValue(mRedPacketResult)
    }

    /**
     * 动画展示红包数额，打开红包
     * 执行红包动画
     * 1.金币下落，金额数字滚动
     * 2.关闭红包
     */
    private fun openRedPacket() {
        redPacketRun()
        goldCoinDown()
    }

    /**
     * 金额数字滚动
     */
    private fun redPacketRun() {
        rollNumView!!.setNumberValue(mRedPacketResult)
        rollNumView!!.startRoll()
    }

    /**
     * 金币下落
     */
    private fun goldCoinDown() {
        val context = context
        //初始状态
        ivOpen!!.visibility = View.VISIBLE
        flClosed!!.visibility = View.GONE
        ivlogo!!.visibility = View.GONE
        //金币下落
        ivCoins!!.visibility = View.VISIBLE
        val openDuration = context!!.resources.getInteger(R.integer.red_packet_open_anim_duration)
        val valueToTranslationY = ivCover!!.measuredHeight.toFloat()
        val coinAnim = ObjectAnimator.ofFloat(ivCoins, null, -valueToTranslationY, 0F)
        coinAnim.duration = openDuration.toLong()
        //        coinAnim.start();
        //关闭红包
        val packetCloseAnim = AnimatorInflater.loadAnimator(context, R.animator.red_packet_close)
        flClosed!!.pivotX = 0.5f
        flClosed!!.pivotY = 0f
        packetCloseAnim.setTarget(flClosed)
        packetCloseAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                ivOpen!!.visibility = View.GONE
                flClosed!!.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                ivlogo!!.visibility = View.VISIBLE
            }
        })
        //        packetCloseAnim.start();
        val animatorSet = AnimatorSet()
        animatorSet.play(coinAnim).before(packetCloseAnim)
        animatorSet.start()
    }

    fun show(manager: FragmentManager) {
        showDialog(manager)
    }

    internal fun showDialog(manager: FragmentManager) {
        try {
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            val ft = manager.beginTransaction()
            val prev = manager.findFragmentByTag(TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            super.show(ft, TAG)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    companion object {

        private val EXTRA_KEY = "extra_key"
        private val TAG = RedPacketDialogFragment::class.java.name

        fun newInstance(bean: RedPacketResp): RedPacketDialogFragment {
            val fragment = RedPacketDialogFragment()
            val args = Bundle()
            args.putSerializable(EXTRA_KEY, bean)
            fragment.arguments = args
            return fragment
        }
    }

}
