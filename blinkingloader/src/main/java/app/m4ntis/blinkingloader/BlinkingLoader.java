package app.m4ntis.blinkingloader;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.annotation.AnimatorRes;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import java.util.List;


public class BlinkingLoader extends LinearLayout {

  private final static int DEFAULT_INDICATOR_WIDTH = 10;
  private final static int DEFAULT_NUM_DOTS = 3;
  private static final long DURATION_DIFF = 500L;
  private static final int DEFAULT_NEUTRAL_COLOR = Color.parseColor("#3300a9ce");
  private static final int DEFAULT_BLINKING_COLOR = Color.parseColor("#00a9ce");
  private static final int DEFAULT_DOT_RADIUS = 20;
  protected int blinkingColor = DEFAULT_BLINKING_COLOR;
  protected int neutralColor = DEFAULT_NEUTRAL_COLOR;
  protected int dotRadius = DEFAULT_DOT_RADIUS;
  int percentHeight = DEFAULT_INDICATOR_WIDTH;
  int oldPos = -1;
  int pos;
  boolean stop;
  ValueAnimator blink = new ValueAnimator();
  ValueAnimator normal = new ValueAnimator();
  BlinkingView[] blinkingViews;
  private int mIndicatorMargin = -1;
  private int mIndicatorWidth = -1;
  private int mIndicatorHeight = -1;
  private int mNumDots = DEFAULT_NUM_DOTS;
  private int mAnimatorResId = R.animator.du_dot_animator;
  private int mAnimatorReverseResId = 0;
  private Animator mAnimatorScaleUp;
  private Animator mAnimatorIn;
  private Animator mImmediateAnimatorOut;
  private Animator mImmediateAnimatorIn;
  private AnimatorSet animatorSetscaleUp;
  private AnimatorSet animatorSetscaleDown;
  private int mLastPosition = -1;
  private boolean auto_start = false;

  public BlinkingLoader(Context context) {
    super(context);
    init(context, null);
  }

  public BlinkingLoader(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public BlinkingLoader(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  public BlinkingLoader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }

  public int getNeutralColor() {
    return neutralColor;
  }

  public void setNeutralColor(int neutralColor) {
    this.neutralColor = neutralColor;
    reinitAnimators();
    changeCircleColor(neutralColor);
    invalidate();
  }


  public int getBlinkingColor() {
    return blinkingColor;
  }

  public void setBlinkingColor(int blinkingColor) {
    this.blinkingColor = blinkingColor;
    reinitAnimators();
    invalidate();
  }

  private void reinitAnimators() {
    animatorSetscaleDown = createAnimatorSetScaleDown(getContext());
    animatorSetscaleUp = createAnimatorSetScaleUp(getContext());
  }

  private void changeCircleColor(int color) {
    if (blinkingViews == null && blinkingViews.length == 0) {
      return;
    }
    for (int i = 0; i < mNumDots; i++) {
      blinkingViews[i].setColor(color);
    }
  }

  private void init(Context context, AttributeSet attrs) {
    handleTypedArray(context, attrs);
    checkIndicatorConfig(context);
    createIndicators();
  }

  private void handleTypedArray(Context context, AttributeSet attrs) {
    if (attrs == null) {
      return;
    }

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BlinkingLoader);
    mIndicatorWidth =
        typedArray.getDimensionPixelSize(R.styleable.BlinkingLoader_dot_width, percentHeight);
    mIndicatorHeight =
        typedArray.getDimensionPixelSize(R.styleable.BlinkingLoader_dot_height, percentHeight);
    mIndicatorMargin =
        typedArray.getDimensionPixelSize(R.styleable.BlinkingLoader_dot_margin, percentHeight);
    mNumDots = typedArray.getInt(R.styleable.BlinkingLoader_dot_num_dots, DEFAULT_NUM_DOTS);
    mAnimatorResId = typedArray.getResourceId(R.styleable.BlinkingLoader_dot_animator,
        R.animator.du_dot_animator);
    mAnimatorReverseResId =
        typedArray.getResourceId(R.styleable.BlinkingLoader_dot_animator_reverse, 0);
    neutralColor =
        typedArray.getColor(R.styleable.BlinkingLoader_dot_neutralColor,
            DEFAULT_NEUTRAL_COLOR);
    blinkingColor =
        typedArray.getColor(R.styleable.BlinkingLoader_dot_blinkingColor,
            DEFAULT_BLINKING_COLOR);
    auto_start =
        typedArray.getBoolean(R.styleable.BlinkingLoader_auto_start,
            false);

    int orientation = typedArray.getInt(R.styleable.BlinkingLoader_dot_orientation, -1);
    setOrientation(orientation == VERTICAL ? VERTICAL : HORIZONTAL);

    int gravity = typedArray.getInt(R.styleable.BlinkingLoader_dot_gravity, -1);
    setGravity(gravity >= 0 ? gravity : Gravity.CENTER);

    typedArray.recycle();
  }

  /**
   * Create and configure Indicator in Java code.
   */
  public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin) {
    configureIndicator(indicatorWidth, indicatorHeight, indicatorMargin,
        R.animator.du_dot_animator, 0);
  }

  public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin,
      @AnimatorRes int animatorId, @AnimatorRes int animatorReverseId) {

    mIndicatorWidth = indicatorWidth;
    mIndicatorHeight = indicatorHeight;
    mIndicatorMargin = indicatorMargin;

    mAnimatorResId = animatorId;
    mAnimatorReverseResId = animatorReverseId;

    checkIndicatorConfig(getContext());
  }

  private void checkIndicatorConfig(Context context) {
    mIndicatorWidth = (mIndicatorWidth < 0) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorWidth;
    mIndicatorHeight =
        (mIndicatorHeight < 0) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorHeight;
    mIndicatorMargin =
        (mIndicatorMargin < 0) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorMargin;

    mAnimatorResId = (mAnimatorResId == 0) ? R.animator.du_dot_animator : mAnimatorResId;

    mAnimatorScaleUp = createAnimatorScaleUp(context);
    mImmediateAnimatorOut = createAnimatorScaleUp(context);
    mImmediateAnimatorOut.setDuration(0);

    mAnimatorIn = createAnimatorScaleDown(context);
    mImmediateAnimatorIn = createAnimatorScaleDown(context);
    mImmediateAnimatorIn.setDuration(0);

    animatorSetscaleDown = createAnimatorSetScaleDown(context);
    animatorSetscaleUp = createAnimatorSetScaleUp(context);

  }

  private Animator createAnimatorScaleUp(Context context) {
    return AnimatorInflater.loadAnimator(context, mAnimatorResId);
  }

  private Animator createAnimatorScaleDown(Context context) {
    Animator animatorIn;
    if (mAnimatorReverseResId == 0) {
      animatorIn = AnimatorInflater.loadAnimator(context, mAnimatorResId);
      animatorIn.setInterpolator(new ReverseInterpolator());
    } else {
      animatorIn = AnimatorInflater.loadAnimator(context, mAnimatorReverseResId);
    }
    return animatorIn;
  }

  private AnimatorSet createAnimatorSetScaleUp(Context context) {
    AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(context, mAnimatorResId);
    List<Animator> animatorList = set.getChildAnimations();
    for (Animator animator : animatorList) {
      if (animator instanceof ObjectAnimator) {
        ObjectAnimator anim = (ObjectAnimator) animator;
        if (anim.getPropertyName().equals("color")) {
          anim.setIntValues(neutralColor, blinkingColor);
          anim.setEvaluator(new ArgbEvaluator());
        }
      }
    }
    return set;
  }

  private AnimatorSet createAnimatorSetScaleDown(Context context) {
    AnimatorSet set = createAnimatorSetScaleUp(context);
    set.setInterpolator(new ReverseInterpolator());
    return set;
  }

  private void scaleUp(View view) {
    animatorSetscaleUp.setTarget(view);
    animatorSetscaleUp.start();
  }

  private void scaleDown(View view) {
    animatorSetscaleDown.setTarget(view);
    animatorSetscaleDown.start();
  }

  private void createIndicators() {
    removeAllViews();
    int count = mNumDots;
    blinkingViews = new BlinkingView[mNumDots];
    if (count <= 0) {
      return;
    }
    int orientation = getOrientation();

    for (int i = 0; i < count; i++) {
      addIndicator(orientation, neutralColor, i);
    }
    if (auto_start) {
      startBlinking();
    }
  }

  private void addIndicator(int orientation, @ColorInt int backgroundDrawableId, int pos) {

    BlinkingView Indicator = new BlinkingView(getContext());
    Indicator.setColor(backgroundDrawableId);
    Indicator.setRadius(dotRadius);
    blinkingViews[pos] = Indicator;
    addView(Indicator, WRAP_CONTENT, WRAP_CONTENT);
    LayoutParams lp = (LayoutParams) Indicator.getLayoutParams();

    lp.leftMargin = mIndicatorMargin;
    lp.rightMargin = mIndicatorMargin;
    lp.topMargin = mIndicatorMargin;
    lp.bottomMargin = mIndicatorMargin;
    Indicator.setLayoutParams(lp);
  }

  public void startBlinking() {
    final Handler handler = new Handler();
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (oldPos > -1) {
          scaleDown(getChildAt(oldPos));
        }
        scaleUp(getChildAt(pos));
        oldPos = pos;
        pos++;
        if (pos >= mNumDots) {
          pos = 0;
        }
        if (!stop) {
          handler.postDelayed(this, DURATION_DIFF);
        }
      }
    });
  }

  public int dip2px(float dpValue) {
    final float scale = getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  private class ReverseInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float value) {
      return Math.abs(1.0f - value);
    }
  }
}