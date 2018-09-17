package red.packet

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*

/**
 * 数字随机滚动显示.
 */
class RollNumView : LinearLayout {
    private var mNumTextColor = Color.WHITE
    private var mNumTextSize = 16
    private var mNumberValue: Double = 0.toDouble()
    private var mRollDuration = 1000
    private var mRollRepeatCount = 1
    private var mNumberFormat = "%,.2f"
    private val mNumberRandom = Random()
    private val mTextWidthArray = SparseIntArray()
    private var mNumberCount: Int = 0

    /**
     * 文本颜色
     */
    var numTextColor: Int
        get() = mNumTextColor
        set(exampleColor) {
            mNumTextColor = exampleColor
            invalidateText()
        }

    /**
     * 文字大小
     */
    var numTextSize: Int
        get() = mNumTextSize
        set(size) {
            mNumTextSize = size
            invalidateText()
        }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RollNumView, defStyle, 0)

        mNumTextColor = a.getColor(
                R.styleable.RollNumView_numTextColor,
                mNumTextColor)
        mNumTextSize = a.getDimensionPixelSize(
                R.styleable.RollNumView_numTextSize,
                mNumTextSize)
        mRollDuration = a.getInteger(
                R.styleable.RollNumView_rollDuration,
                mRollDuration)
        mRollRepeatCount = a.getInteger(
                R.styleable.RollNumView_rollRepeatCount,
                mRollRepeatCount)
        a.recycle()

        orientation = LinearLayout.HORIZONTAL
        invalidateText()
    }

    private fun invalidateText() {
        initRollNum()
    }

    /**
     * 添加表示数值的文字
     */
    private fun initRollNum() {
        removeAllViews()
        mTextWidthArray.clear()
        mNumberCount = 0
        val numStr = String.format(Locale.getDefault(), mNumberFormat, mNumberValue)
        val chars = numStr.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            addText(c)
            if (isNumber(c)) {
                mNumberCount++
            }
        }
    }

    /**
     * 浮点数格式化字符串
     *
     * @param format
     */
    fun setNumberFormat(format: String) {
        mNumberFormat = format
    }

    /**
     * 是否数字字符
     *
     * @param c 字符
     * @return
     */
    private fun isNumber(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun addText(c: Char): TextView {
        val textView = TextView(context)
        textView.setTextColor(mNumTextColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNumTextSize.toFloat())
        textView.text = c.toString()
        textView.tag = c
        var measuredWidth = mTextWidthArray.get(c.toInt())
        if (measuredWidth == 0) {
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 shl 30) - 1, View.MeasureSpec.AT_MOST)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 shl 30) - 1, View.MeasureSpec.AT_MOST)
            textView.measure(widthMeasureSpec, heightMeasureSpec)
            measuredWidth = textView.measuredWidth
            mTextWidthArray.put(c.toInt(), measuredWidth)
        }
        val params = LinearLayout.LayoutParams(measuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(textView, params)
        return textView
    }

    /**
     * 开始滚动
     */
    fun startRoll() {
        post { rullToNum() }
    }

    /**
     * 滚动数字
     */
    private fun rullToNum() {
        val childCount = childCount
        var numPos = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i) as TextView
            val tag = childView.tag as Char
            if (!isNumber(tag)) {
                continue
            }
            numPos++
            setRandomNumber(childView)
            val height = childView.height
            val isRollUp = isRollUpDirec(numPos)
            val rollFrom = (if (isRollUp) -height else height).toFloat()
            val rollTo = (if (isRollUp) height else -height).toFloat()
            val values = getAnimationValues(rollFrom, rollTo)
            val animator = ValueAnimator.ofFloat(*values)
            animator.duration = mRollDuration.toLong()
            animator.interpolator = DecelerateInterpolator()//减速变化
            animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                internal var animatedValueLast = 0f
                internal var time = 0

                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val animatedValue = animation.animatedValue as Float
                    if (isRollUp && animatedValueLast < 0 && animatedValue > 0 || !isRollUp && animatedValueLast > 0 && animatedValue < 0) {
                        time++
                        val isLastTime = time + 1 >= mRollRepeatCount
                        if (isLastTime) {
                            val tag = childView.tag as Char
                            childView.text = tag.toString()
                        } else {
                            setRandomNumber(childView)
                        }
                    }
                    animatedValueLast = animatedValue
                    childView.translationY = animatedValue
                }
            })
            animator.start()
        }
    }

    /**
     * 滚动方向向上
     * @param numPositon 数字位置(从左向右)
     * @return 向上
     */
    private fun isRollUpDirec(numPositon: Int): Boolean {
        return Math.random() >= 0.5
    }

    /**
     * 动画的变化值
     *
     * @param rollFrom 滚动起始点位移
     * @param rollTo   滚动结束点位移
     * @return
     */
    private fun getAnimationValues(rollFrom: Float, rollTo: Float): FloatArray {
        val values = FloatArray(2 * mRollRepeatCount)
        for (i in values.indices) {
            val singular = i % 2 != 0
            values[i] = if (singular) rollFrom else rollTo
        }
        values[values.size - 1] = 0f
        return values
    }

    /**
     * 设置0-9随机数字
     *
     * @param textView 文本框
     */
    private fun setRandomNumber(textView: TextView) {
        val number = mNumberRandom.nextInt(10)
        textView.text = number.toString()
    }

    /**
     * 设置最终显示数值
     *
     * @param numberValue 数值
     */
    fun setNumberValue(numberValue: Double) {
        mNumberValue = numberValue
        initRollNum()
    }

    /**
     * 滚动次数，至少一次
     *
     * @param count
     */
    fun setRollRepeatCount(count: Int) {
        mRollRepeatCount = Math.max(count, 1)
    }

    /**
     * 滚动总时长
     *
     * @param duration
     */
    fun setRollDuration(duration: Int) {
        mRollDuration = duration
    }

}
