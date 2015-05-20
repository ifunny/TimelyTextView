package com.github.adnansm.timelytextview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import com.github.adnansm.timelytextview.animation.TimelyEvaluator;
import com.github.adnansm.timelytextview.model.NumberUtils;

public class TimelyView extends View {
	private static final float RATIO = 1f;
	private static final Property<TimelyView, float[][]> CONTROL_POINTS_PROPERTY =
			new Property<TimelyView, float[][]>(float[][].class, "controlPoints") {
				@Override
				public float[][] get(TimelyView object) {
					return object.getControlPoints();
				}

				@Override
				public void set(TimelyView object, float[][] value) {
					object.setControlPoints(value);
				}
			};

	private Paint mPaint = null;
	private Path mPath = null;
	private float[][] controlPoints = null;

	public TimelyView(Context context) {
		this(context, null);
	}

	public TimelyView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TimelyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		final TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.TimelyView, defStyleAttr, 0
		);
		int textColor;
		try {
			textColor = a.getColor(R.styleable.TimelyView_timely_text_color, Color.BLACK);
		} finally {
			a.recycle();
		}

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(textColor);
		mPaint.setStrokeWidth(2.0f);
		mPaint.setStyle(Paint.Style.STROKE);
		mPath = new Path();
	}

	public float[][] getControlPoints() {
		return controlPoints;
	}

	public void setControlPoints(float[][] controlPoints) {
		this.controlPoints = controlPoints;
		invalidate();
	}

	private int lastEnd = -1;

	public ObjectAnimator animate(int start, int end) {
		if (start == end) {
			return null;
		}
		final float[][] startPoints = NumberUtils.getControlPointsFor(start);
		final float[][] endPoints = NumberUtils.getControlPointsFor(end);
		lastEnd = end;
		return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, new TimelyEvaluator(),
				startPoints, endPoints);
	}

	public ObjectAnimator animate(int end) {
		return animate(lastEnd, end);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (controlPoints == null) return;

		final float minDimen = 0.95f * Math.min(getWidth(), getHeight());

		mPath.reset();
		mPath.moveTo(minDimen * controlPoints[0][0], minDimen * controlPoints[0][1]);
		final int length = controlPoints.length;
		for (int i = 1; i < length; i += 3) {
			mPath.cubicTo(minDimen * controlPoints[i][0], minDimen * controlPoints[i][1],
					minDimen * controlPoints[i + 1][0], minDimen * controlPoints[i + 1][1],
					minDimen * controlPoints[i + 2][0], minDimen * controlPoints[i + 2][1]);
		}
		canvas.drawPath(mPath, mPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
		int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();

		int maxWidth = (int) (heigthWithoutPadding * RATIO);
		int maxHeight = (int) (widthWithoutPadding / RATIO);

		if (widthWithoutPadding > maxWidth) {
			width = maxWidth + getPaddingLeft() + getPaddingRight();
		} else {
			height = maxHeight + getPaddingTop() + getPaddingBottom();
		}

		setMeasuredDimension(width, height);
	}
}
