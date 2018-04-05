package teamidus.com.drawing;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import teamidus.com.drawing.util.MathUtil;

/**
 * Created by nyh0111 on 2018-04-05.
 * <p>
 * RoundProgressBar with indicator.
 *
 * TODO: add section
 */

public class RoundPeakProgressBar extends View {
    
    public static final float DEFAULT_HEIGHT_SET = 0;
    
    public static final float DEFAULT_INDICATOR_HEIGHT_RATIO = 0.2f;
    public static final float MAX_INDICATOR_HEIGHT_RATIO = 0.5f;
    
    private Rect baseRect;
    private Rect indicatorRect;
    private Rect progressBackgroundRect;
    private Rect progressForegroundRect;
    
    private Paint indicatorPaint;
    private Path indicatorPath;
    
    private Paint sectionDividerPaint;
    private Path sectionDividerPath;
    
    // set from users
    
    private float progress;
    private float max;
    
    private float radius;
    
    private float indicatorHeight;
    private int indicatorColor;
    
    private int sectionDividerColor;
    private int progressForegroundColor;
    private int progressBackgroundColor;
    
    public RoundPeakProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs, R.styleable.RoundPeakProgressBar, 0, 0);
        
        try {
            progress = a.getFloat(R.styleable.RoundPeakProgressBar_progress, 0);
            max = a.getFloat(R.styleable.RoundPeakProgressBar_max, 100);
            radius = a.getDimension(R.styleable.RoundPeakProgressBar_radius, 0);
            indicatorHeight = a.getDimension(R.styleable.RoundPeakProgressBar_indicatorHeight, DEFAULT_HEIGHT_SET);
            indicatorColor = a.getColor(R.styleable.RoundPeakProgressBar_indicatorColor, Color.BLACK);
            sectionDividerColor = a.getColor(R.styleable.RoundPeakProgressBar_sectionDividerColor, Color.WHITE);
            progressForegroundColor = a.getColor(R.styleable.RoundPeakProgressBar_progressForegroundColor, Color.BLACK);
            progressBackgroundColor = a.getColor(R.styleable.RoundPeakProgressBar_progressBackgroundColor, Color.GRAY);
        } finally {
            a.recycle();
        }
        
        init();
    }
    
    private void init() {
        // rect
        baseRect = new Rect();
        indicatorRect = new Rect();
        progressBackgroundRect = new Rect();
        progressForegroundRect = new Rect();
    
        // indicator paint
        indicatorPaint = new Paint();
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPath = new Path();
    
        // divider paint
        sectionDividerPaint = new Paint();
        sectionDividerPaint.setColor(sectionDividerColor);
        sectionDividerPaint.setStyle(Paint.Style.FILL);
        sectionDividerPath = new Path();
    }
    
    // setters
    
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
        requestLayout();
    }
    
    public void setMax(float max) {
        this.max = max;
        invalidate();
        requestLayout();
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
        requestLayout();
    }
    
    public void setSectionDividerColor(int sectionDividerColor) {
        this.sectionDividerColor = sectionDividerColor;
        sectionDividerPaint.setColor(sectionDividerColor);
        invalidate();
        requestLayout();
    }
    
    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        indicatorPaint.setColor(indicatorColor);
        invalidate();
        requestLayout();
    }
    
    public void setProgressForegroundColor(int progressForegroundColor) {
        this.progressForegroundColor = progressForegroundColor;
        invalidate();
        requestLayout();
    }
    
    public void setProgressBackgroundColor(int progressBackgroundColor) {
        this.progressBackgroundColor = progressBackgroundColor;
        invalidate();
        requestLayout();
    }
    
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        Log.e("asdf", "padding left: " + getPaddingLeft());
        Log.e("asdf", "padding right: " + getPaddingRight());
        Log.e("asdf", "padding bottom: " + getPaddingBottom());
        Log.e("asdf", "padding top: " + getPaddingTop());
        
        // 1. Measured width and height don't affect by padding or margin.
        // 2. MeasuredSpec.getSize(###) == getMeasured###() in this method.
        
        Log.e("asdf", "Measured width: " + MeasureSpec.getSize(widthMeasureSpec));
        Log.e("asdf", "Measured height: " + MeasureSpec.getSize(heightMeasureSpec));
        
        Log.e("asdf", "Measured width by function: " + getMeasuredWidth());
        Log.e("asdf", "Measured height by function: " + getMeasuredHeight());
        
        float height = MeasureSpec.getSize(widthMeasureSpec);
        
        if (indicatorHeight == DEFAULT_HEIGHT_SET)
            indicatorHeight = height * DEFAULT_INDICATOR_HEIGHT_RATIO;
        
        indicatorHeight = MathUtil.clamp(indicatorHeight, 0, height * MAX_INDICATOR_HEIGHT_RATIO);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        
        getDrawingRect(baseRect);
        Log.e("asdf", "DrawingRect -> " + baseRect.toString());
        
        // Ready rect for draw.
        readyIndicatorRect(baseRect);
        readyProgressRect(baseRect);
        Log.e("asdf", "IndicatorRect -> " + indicatorRect.toString());
        Log.e("asdf", "ProgressRect -> " + progressBackgroundRect.toString());
        
        drawProgressBackground(canvas);
        drawProgressForeground(canvas);
        drawIndicator(canvas);
        
        canvas.restore();
    }
    
    // draw a indicator - right triangle on progress.
    private void drawIndicator(Canvas canvas) {
        float x = getWidth() * ratio();
        float dx = (float) (indicatorHeight / Math.tan(Math.toRadians(60)));
        
        indicatorPath.reset();
        indicatorPath.moveTo(x, indicatorHeight);
        indicatorPath.lineTo(x - dx, 0);
        indicatorPath.lineTo(x + dx, 0);
        indicatorPath.close();
        canvas.drawPath(indicatorPath, indicatorPaint);
    }
    
    private void readyIndicatorRect(Rect rect) {
        progressBackgroundRect.set(rect.left, rect.top, rect.right, (int) indicatorHeight);
    }
    
    private void readyProgressRect(Rect rect) {
        progressBackgroundRect.set(rect.left, (int) indicatorHeight, rect.right, rect.bottom);
        progressForegroundRect.set(rect.left, (int) indicatorHeight, (int) (rect.right * ratio()), rect.bottom);
    }
    
    private float ratio() {
        return progress / max;
    }
    
    private void drawProgressBackground(Canvas canvas) {
        GradientDrawable drawable = createGradientDrawable(progressBackgroundColor);
        drawable.setCornerRadius(radius);
        drawable.setBounds(progressBackgroundRect);
        drawable.draw(canvas);
    }
    
    private void drawProgressForeground(Canvas canvas) {
        GradientDrawable drawable = createGradientDrawable(progressForegroundColor);
        drawable.setCornerRadius(radius);
        drawable.setBounds(progressForegroundRect);
        drawable.draw(canvas);
    }
    
    private GradientDrawable createGradientDrawable(int color) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(color);
        return gradientDrawable;
    }
}
