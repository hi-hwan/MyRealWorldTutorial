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

    private var drawCheck = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)
        buttonText = typedArray.getString(R.styleable.ProgressButton_progressButton_text) ?: ""
        typedArray.recycle()
    }

    // onDraw는 자주 호출되므로 성능 향상을 위해 onDraw 안에서 객체 생성하는 것을 피하자
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

        if (drawCheck) {
            // Canvas의 상태를 저장
            canvas.save()
            // Canvas를 45도 회전시키고, 뷰의 중심을 기준점으로 설정
            canvas.rotate(45f, measuredWidth / 2f, measuredHeight / 2f)

            // L의 좌우 반전 모양을 그린다
            val x1 = measuredWidth / 2f - buttonRect.width() / 8
            val y1 = measuredHeight / 2f + buttonRect.width() / 4
            val x2 = measuredWidth / 2f + buttonRect.width() / 8
            val y2 = measuredHeight / 2f + buttonRect.width() / 4
            val x3 = measuredWidth / 2f + buttonRect.width() / 8
            val y3 = measuredHeight / 2f - buttonRect.width() / 4
            canvas.drawLine(x1, y1, x2, y2, progressPaint) // 수평선
            canvas.drawLine(x2, y2, x3, y3, progressPaint) // 수직선
            canvas.restore() // 원래 방향으로 되돌린다.
        }
    }

    fun startLoading() {
        widthAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                offset = (measuredWidth - measuredHeight) / 2f * it.animatedValue as Float
                invalidate() // Canvas에 뷰를 다시 그린다.
            }
            addListener(object : AnimatorListenerAdapter() {
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
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    loading = false
                    invalidate()
                }
            })
        }
        rotationAnimator?.start()
    }

    fun done() {
        loading = false
        drawCheck = true
        rotationAnimator?.cancel()
        invalidate()
    }

    // 사용자가 애니메이션이 완료되기 전에 종료하면 애니메이션도 종료
    // 그렇지 않으면 뷰가 파괴되었음에도 불구하고 애니메이션이 계속 실행되면서 메모리 누수가 발생
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        widthAnimator?.cancel()
        rotationAnimator?.cancel()
    }
}