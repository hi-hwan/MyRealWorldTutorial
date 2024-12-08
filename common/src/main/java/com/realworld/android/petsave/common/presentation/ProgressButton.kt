package com.realworld.android.petsave.common.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.realworld.android.petsave.common.R
import com.realworld.android.petsave.common.utils.dpToPx
import com.realworld.android.petsave.common.utils.getTextWidth

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var buttonText = ""

    private val textPaint = Paint().apply {
        isAntiAlias = true // 화면에 그린 도형의 가장자리를 부드럽게 만드는 기법
        style = Paint.Style.FILL // 채우기
        color = Color.WHITE
        textSize = context.dpToPx(16f)
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE // 윤곽선
        color = Color.WHITE
        strokeWidth = context.dpToPx(2f)
    }

    private val buttonRect = RectF() // 꼭짓점의 위치를 설정
    private val progressRect = RectF()

    private var buttonRadius = context.dpToPx(16f)

    private var offset: Float = 0f

    private var widthAnimator: ValueAnimator? = null
    private var loading = false
    private var startAngle = 0f

    private var rotationAnimator: ValueAnimator? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)
        buttonText = typedArray.getString(R.styleable.ProgressButton_progressButton_text) ?: ""
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) { // 그리는 작업이 이루어지는 곳
        super.onDraw(canvas)

        buttonRadius = measuredHeight / 2f // 절반으로 설정하여 버튼이 축소될 때 타원이 아닌 원형으로 변형
        buttonRect.apply {
            top = 0f
            left = 0f + offset
            right = measuredWidth.toFloat() - offset
            bottom = measuredHeight.toFloat()
        }

        // 둥근 모서리가 있는 사각현을 그린다
        canvas.drawRoundRect(buttonRect, buttonRadius, buttonRadius, backgroundPaint)

        // 텍스트를 버튼안에 그린다
        if (offset < (measuredWidth - measuredHeight) / 2f) {
            val textX = measuredWidth / 2.0f - textPaint.getTextWidth(buttonText) / 2.0f
            val textY = measuredHeight / 2.0f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(buttonText, textX, textY, textPaint)
        }

        // 버튼이 이제 원형이 되었는지 확인
        if (loading && offset == (measuredWidth - measuredHeight) / 2f) {
            // Progress bar가 원형 모양 버튼 안에 표시
            progressRect.left = measuredWidth / 2.0f - buttonRect.width() / 4
            progressRect.top = measuredHeight / 2.0f - buttonRect.width() / 4
            progressRect.right = measuredWidth / 2.0f + buttonRect.width() / 4
            progressRect.bottom = measuredHeight / 2.0f + buttonRect.width() / 4
            canvas.drawArc(progressRect, startAngle, 140f, false, progressPaint)
        }
    }

    fun startLoading() {
        widthAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                offset = (measuredWidth - measuredHeight) / 2f * it.animatedValue as Float
                invalidate() // Canvas에 뷰를 다시 그린다.
            }
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    startProgressAnimation()
                }
            })
            duration = 200
        }
        loading = true
        isClickable = false
        widthAnimator?.start()
    }

    fun startProgressAnimation() {
        rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            addUpdateListener {
                startAngle = it.animatedValue as Float
                invalidate()
            }
            duration = 600
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    loading = false
                    invalidate()
                }
            })
        }
        rotationAnimator?.start()
    }
}