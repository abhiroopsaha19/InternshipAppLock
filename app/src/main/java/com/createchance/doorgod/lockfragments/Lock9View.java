package com.createchance.doorgod.lockfragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.createchance.doorgod.R;

import java.util.ArrayList;
import java.util.List;

public class Lock9View extends ViewGroup {

    private List<NodeView> nodeList = new ArrayList<>();
    private float x;
    private float y;


    private Drawable nodeSrc;
    private Drawable nodeOnSrc;
    private float nodeSize;
    private float nodeAreaExpand;
    private int nodeOnAnim;
    private int lineColor;
    private float lineWidth;
    private float padding;
    private float spacing;


    private boolean autoLink;



    private Vibrator vibrator;
    private boolean enableVibrate;
    private int vibrateTime;


    private Paint paint;


    private StringBuilder passwordBuilder = new StringBuilder();


    private CallBack callBack;

    public interface CallBack {

        void onFinish(String password);

    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }



    public Lock9View(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public Lock9View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public Lock9View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Lock9View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }


    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Lock9View, defStyleAttr, defStyleRes);

        nodeSrc = a.getDrawable(R.styleable.Lock9View_lock9_nodeSrc);
        nodeOnSrc = a.getDrawable(R.styleable.Lock9View_lock9_nodeOnSrc);
        nodeSize = a.getDimension(R.styleable.Lock9View_lock9_nodeSize, 0);
        nodeAreaExpand = a.getDimension(R.styleable.Lock9View_lock9_nodeAreaExpand, 0);
        nodeOnAnim = a.getResourceId(R.styleable.Lock9View_lock9_nodeOnAnim, 0);
        lineColor = a.getColor(R.styleable.Lock9View_lock9_lineColor, Color.argb(0, 0, 0, 0));
        lineWidth = a.getDimension(R.styleable.Lock9View_lock9_lineWidth, 0);
        padding = a.getDimension(R.styleable.Lock9View_lock9_padding, 0);
        spacing = a.getDimension(R.styleable.Lock9View_lock9_spacing, 0);

        autoLink = a.getBoolean(R.styleable.Lock9View_lock9_autoLink, false);

        enableVibrate = a.getBoolean(R.styleable.Lock9View_lock9_enableVibrate, false);
        vibrateTime = a.getInt(R.styleable.Lock9View_lock9_vibrateTime, 20);

        a.recycle();


        if (enableVibrate && !isInEditMode()) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        paint = new Paint(Paint.DITHER_FLAG);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(lineColor);
        paint.setAntiAlias(true);


        for (int n = 0; n < 9; n++) {
            NodeView node = new NodeView(getContext(), n + 1);
            addView(node);
        }


        setWillNotDraw(false);
    }

    // gets and setters


    public void setNodeOnAnim(int nodeOnAnim) {
        this.nodeOnAnim = nodeOnAnim;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = measureSize(widthMeasureSpec);
        setMeasuredDimension(size, size);
    }


    private int measureSize(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                return specSize;
            default:
                return 0;
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            if (nodeSize > 0) {
                float areaWidth = (right - left) / 3;
                for (int n = 0; n < 9; n++) {
                    NodeView node = (NodeView) getChildAt(n);

                    int row = n / 3;
                    int col = n % 3;

                    int l = (int) (col * areaWidth + (areaWidth - nodeSize) / 2);
                    int t = (int) (row * areaWidth + (areaWidth - nodeSize) / 2);
                    int r = (int) (l + nodeSize);
                    int b = (int) (t + nodeSize);
                    node.layout(l, t, r, b);
                }
            } else {
                float nodeSize = (right - left - padding * 2 - spacing * 2) / 3;
                for (int n = 0; n < 9; n++) {
                    NodeView node = (NodeView) getChildAt(n);

                    int row = n / 3;
                    int col = n % 3;

                    int l = (int) (padding + col * (nodeSize + spacing));
                    int t = (int) (padding + row * (nodeSize + spacing));
                    int r = (int) (l + nodeSize);
                    int b = (int) (t + nodeSize);
                    node.layout(l, t, r, b);
                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                NodeView currentNode = getNodeAt(x, y);
                if (currentNode != null && !currentNode.isHighLighted()) {
                    if (nodeList.size() > 0) {
                        if (autoLink) {
                            NodeView lastNode = nodeList.get(nodeList.size() - 1);
                            NodeView middleNode = getNodeBetween(lastNode, currentNode);
                            if (middleNode != null && !middleNode.isHighLighted()) {
                                middleNode.setHighLighted(true, true);
                                nodeList.add(middleNode);
                            }
                        }
                    }

                    currentNode.setHighLighted(true, false);
                    nodeList.add(currentNode);
                }

                if (nodeList.size() > 0) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (nodeList.size() > 0) {

                    if (callBack != null) {

                        passwordBuilder.setLength(0);
                        for (NodeView nodeView : nodeList) {
                            passwordBuilder.append(nodeView.getNum());
                        }
                        // callback
                        callBack.onFinish(passwordBuilder.toString());
                    }

                    nodeList.clear();
                    for (int n = 0; n < getChildCount(); n++) {
                        NodeView node = (NodeView) getChildAt(n);
                        node.setHighLighted(false, false);
                    }

                    invalidate();
                }
                break;
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        for (int n = 1; n < nodeList.size(); n++) {
            NodeView firstNode = nodeList.get(n - 1);
            NodeView secondNode = nodeList.get(n);
            canvas.drawLine(firstNode.getCenterX(), firstNode.getCenterY(), secondNode.getCenterX(), secondNode.getCenterY(), paint);
        }

        if (nodeList.size() > 0) {
            NodeView lastNode = nodeList.get(nodeList.size() - 1);
            canvas.drawLine(lastNode.getCenterX(), lastNode.getCenterY(), x, y, paint);
        }
    }



    private NodeView getNodeAt(float x, float y) {
        for (int n = 0; n < getChildCount(); n++) {
            NodeView node = (NodeView) getChildAt(n);
            if (!(x >= node.getLeft() - nodeAreaExpand && x < node.getRight() + nodeAreaExpand)) {
                continue;
            }
            if (!(y >= node.getTop() - nodeAreaExpand && y < node.getBottom() + nodeAreaExpand)) {
                continue;
            }
            return node;
        }
        return null;
    }


    private NodeView getNodeBetween(NodeView na, NodeView nb) {
        if (na.getNum() > nb.getNum()) {
            NodeView nc = na;
            na = nb;
            nb = nc;
        }
        if (na.getNum() % 3 == 1 && nb.getNum() - na.getNum() == 2) {
            return (NodeView) getChildAt(na.getNum());
        } else if (na.getNum() <= 3 && nb.getNum() - na.getNum() == 6) {
            return (NodeView) getChildAt(na.getNum() + 2);
        } else if ((na.getNum() == 1 && nb.getNum() == 9) || (na.getNum() == 3 && nb.getNum() == 7)) {
            return (NodeView) getChildAt(4);
        } else {
            return null;
        }
    }


    private class NodeView extends View {

        private int num;
        private boolean highLighted = false;

        @SuppressWarnings("deprecation")
        public NodeView(Context context, int num) {
            super(context);
            this.num = num;
            setBackgroundDrawable(nodeSrc);
        }

        public boolean isHighLighted() {
            return highLighted;
        }

        @SuppressWarnings("deprecation")
        public void setHighLighted(boolean highLighted, boolean isMid) {
            if (this.highLighted != highLighted) {
                this.highLighted = highLighted;
                if (nodeOnSrc != null) {
                    setBackgroundDrawable(highLighted ? nodeOnSrc : nodeSrc);
                }
                if (nodeOnAnim != 0) {
                    if (highLighted) {
                        startAnimation(AnimationUtils.loadAnimation(getContext(), nodeOnAnim));
                    } else {
                        clearAnimation();
                    }
                }
                if (enableVibrate && !isMid) {
                    if (highLighted) {
                        vibrator.vibrate(vibrateTime);
                    }
                }
            }
        }

        public int getCenterX() {
            return (getLeft() + getRight()) / 2;
        }

        public int getCenterY() {
            return (getTop() + getBottom()) / 2;
        }

        public int getNum() {
            return num;
        }

    }

}
