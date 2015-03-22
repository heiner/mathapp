package org.kuettler.mathapp;

import java.util.TreeMap;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


class StatsView extends View {
    private final int green;
    private final int red;
    private final int gray;
    private final int black;

    private final Stats stats = Stats.getInstance();

    private Exercise.Level level = Exercise.Level.getDefault();
    private MathActivity.Mode mode = MathActivity.Mode.getDefault();

    private float axisOffset = 6;
    private float blockOffset = 4;
    private float barWidth = 40;
    private float blockHeight = 40;
    private float barOffset = 30;
    private int pointsPerBlock = 5;
    private float borderDistance;
    private int textSize = 40;

    private float bestGameLineY = 0;

    private TreeMap<Float, Stats.Game> xToBar;
    private Stats.GameList currentGameList = stats.getGameList(level, mode);

    private Stats.Game taggedGame;

    private final Path zigzag;
    {
        zigzag = new Path();
        //float height = 2*blockOffset + blockHeight;
        float height = blockHeight;
        //height *= 2;
        zigzag.rLineTo(0, height/8);
        zigzag.rLineTo(- barWidth/2 - axisOffset, height/8);
        zigzag.rLineTo(barWidth + 2*axisOffset, height/2);
        zigzag.rLineTo(- barWidth/2 - axisOffset, height/8);
        zigzag.rLineTo(0, height/8);
    }

    public StatsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        green = getResources().getColor(R.color.good_green);
        red = getResources().getColor(R.color.bad_red);
        gray = getResources().getColor(R.color.neutral_gray);
        black = getResources().getColor(android.R.color.black);

        borderDistance = getResources().getDimension(R.dimen.statsview_margin);

        xToBar = new TreeMap<Float, Stats.Game>();
    }

    public void setLevel(Exercise.Level level) {
        this.level = level;
        invalidate();
    }
    public void setMode(MathActivity.Mode mode) {
        this.mode = mode;
        invalidate();
    }

    protected float atAxis() {
        return 2.0f*getHeight()/3;
    }

    protected float aboveAxis() {
        return atAxis() - axisOffset;
    }
    protected float belowAxis() {
        return atAxis() + axisOffset;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint p = new Paint();
        p.setTextSize(textSize);
        p.setColor(gray);
        p.setAntiAlias(true);

        currentGameList = stats.getGameList(level, mode);

        if (currentGameList.isEmpty()) {
            drawTextMiddle("(Never played before)", canvas,
                           getWidth()/2, (getHeight() - 20)/2, p);
            return;
        }

        Stats.Game best = currentGameList.getBestGame();

        p.setStrokeWidth(4);
        canvas.drawLine(borderDistance, atAxis(), getWidth() - borderDistance, atAxis(), p);
        p.setStrokeWidth(3);

        float x = getWidth() - borderDistance;

        xToBar.clear();
        for (Stats.Game g : currentGameList.asDescendingIterable()) {
            x -= barWidth + barOffset;
            if (x < 0)
                break;

            xToBar.put(x, g);

            int points;
            float y;
            float scale = 1f;

            p.setColor(g.wrongAnswers() == 0 ? green : gray);
            for (points = g.points(), y = aboveAxis();
                 points > 0;
                 y -= blockOffset + scale*blockHeight, points -= pointsPerBlock)
            {
                scale = Math.min((float) points/pointsPerBlock, 1.0f);
                canvas.drawRect(x, y - scale*blockHeight, x + barWidth, y, p);
            }
            p.setColor(g.wrongAnswers() == 0 && g.rightAnswers() > 0 ? green : gray);
            drawTextMiddle(Integer.toString(g.rightAnswers()),
                           canvas, x + barWidth/2, y - axisOffset, p);

            p.setColor(red);
            for (points = g.wrongAnswers(), y = belowAxis();
                 points > 0;
                 y += blockOffset + scale*blockHeight, points -= pointsPerBlock)
            {
                scale = Math.min((float) points/pointsPerBlock, 1.0f);
                canvas.drawRect(x, y, x + barWidth, y + scale*blockHeight, p);

                if (y + (float) points/pointsPerBlock * (blockHeight + blockOffset)
                    > getHeight() - 3*p.getFontSpacing()) {
                    // someone was ridiculous
                    y += blockHeight;

                    int missing = (pointsPerBlock - points) % pointsPerBlock;
                    float skipped = blockHeight*missing/pointsPerBlock + blockHeight;

                    p.setStyle(Paint.Style.STROKE);

                    Path z = new Path(zigzag);
                    Matrix M = new Matrix();
                    if (missing == 0) {
                        M.setScale(1, skipped/blockHeight, 0, 0);
                    } else {
                        M.setScale(1, 1 + skipped/blockHeight, 0, 0);
                    }
                    z.transform(M);
                    z.offset(x + barWidth/2, y);
                    canvas.drawPath(z, p);
                    p.setStyle(Paint.Style.FILL);

                    y += skipped - blockOffset;

                    points = points % pointsPerBlock + pointsPerBlock;
                    if (points == pointsPerBlock) {
                        y -= blockHeight;
                        points *= 2;
                    }
                }
            }
            if (g.wrongAnswers() > 0) {
                y += textSize;
                drawTextMiddle(Integer.toString(g.wrongAnswers()),
                               canvas, x + barWidth/2, y, p);
                y += p.descent();
            }

            if (g == taggedGame) {
                p.setColor(gray);
                p.setStrokeWidth(0);
                canvas.drawLine(x + barWidth/2, y + axisOffset,
                                x + barWidth/2, getHeight() - 80, p);
                String info = String.format("Points: %d", g.points());

                if (g == best && best.points() > 0) {
                    info += ", current record!";
                    //p.setTypeface(Typeface.DEFAULT_BOLD);
                    p.setColor(green);
                }
                drawTextMiddle(info, canvas, x + barWidth/2, getHeight() - textSize, p);
                info = String.format("%.1f sec/answer", g.secondsPerAnswer());
                drawTextMiddle(info, canvas, x + barWidth/2, getHeight() , p);
            }
        }

        if (best.points() == 0)
            return;

        p.setColor(black);
        p.setStyle(Paint.Style.STROKE);
        p.setPathEffect(new DashPathEffect(new float[] {20,10}, 0));
        p.setStrokeWidth(1);

        bestGameLineY = aboveAxis()
            - (blockOffset + blockHeight) * (float) best.points() / pointsPerBlock;

        Path path = new Path();
        path.moveTo(borderDistance/2, bestGameLineY);
        path.rLineTo(getWidth() - borderDistance, 0);

        canvas.drawPath(path, p);
    }

    protected void drawTextMiddle(String text, Canvas canvas,
                                  float x, float y, Paint p) {
        float length = p.measureText(text);
        if (x + length/2 <= getWidth()) {
            canvas.drawText(text, x - length/2, y, p);
        } else {
            canvas.drawText(text, getWidth() - length, y, p);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Float x = xToBar.lowerKey(event.getX());
        if (x == null) {
            taggedGame = null;
            invalidate();
            return super.onTouchEvent(event);
        } else if (event.getY() < bestGameLineY + blockHeight) {
            if (event.getY() > bestGameLineY - blockHeight) {
                taggedGame = currentGameList.getBestGame();
            } else {
                taggedGame = null;
            }
            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            taggedGame = xToBar.get(x);
            Log.d(MathActivity.TAG, taggedGame.toString());
            invalidate();
        }
        return true;
    }
}
