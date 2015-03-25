package org.kuettler.mathapp;

import java.util.TreeMap;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


class StatsView extends View {
    private final int green = getResources().getColor(R.color.good_green);
    private final int red = getResources().getColor(R.color.bad_red);
    private final int gray = getResources().getColor(R.color.neutral_gray);
    private final int black = getResources().getColor(android.R.color.black);
    private final int teal = getResources().getColor(R.color.active_teal);

    private final Stats stats = Stats.getInstance();

    private Exercise.Level level = Exercise.Level.getDefault();
    private MathActivity.Mode mode = MathActivity.Mode.getDefault();

    private int unitsPerBlock
        = getResources().getInteger(R.integer.statsview_unitsPerBlock);

    private float axisOffset
        = getResources().getDimension(R.dimen.statsview_axisOffset);
    private float blockOffset
        = getResources().getDimension(R.dimen.statsview_blockOffset);
    private float barWidth
        = getResources().getDimension(R.dimen.statsview_barWidth);
    private float blockHeight
        = getResources().getDimension(R.dimen.statsview_blockHeight);
    private float barDistance
        = getResources().getDimension(R.dimen.statsview_barDistance);
    private final float borderDistance
        = getResources().getDimension(R.dimen.statsview_margin);
    private int textSize
         = getResources().getDimensionPixelSize(R.dimen.statsview_textSize);
    private float axisWidth
        = getResources().getDimension(R.dimen.statsview_axisWidth);

    private float bestGameLineY = 0;

    private TreeMap<Float, Stats.Game> xToBar;
    private Stats.GameList currentGameList = stats.getGameList(level, mode);

    private Stats.Game taggedGame;

    private final Path zigzag;
    private final float zigzagHeight
        = getResources().getDimension(R.dimen.statsview_zigzagHeight);;
    {
        zigzag = new Path();
        zigzag.rLineTo(0, zigzagHeight/16);
        zigzag.rLineTo(- barWidth/2 - axisOffset, zigzagHeight/16);
        zigzag.rLineTo(barWidth + 2*axisOffset, zigzagHeight/4);
        zigzag.rLineTo(- barWidth - 2*axisOffset, zigzagHeight/4);
        zigzag.rLineTo(barWidth + 2*axisOffset, zigzagHeight/4);
        zigzag.rLineTo(- barWidth/2 - axisOffset, zigzagHeight/16);
        zigzag.rLineTo(0, zigzagHeight/16);
    }

    public StatsView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        Log.d(MathActivity.TAG, String.format("1dp = %f", axisWidth));

        Paint p = new Paint();
        p.setTextSize(textSize);
        p.setColor(gray);
        p.setAntiAlias(true);

        currentGameList = stats.getGameList(level, mode);

        if (currentGameList.isEmpty()) {
            drawTextMiddle("(Never played before)", canvas,
                           getWidth()/2, (getHeight() - textSize/2)/2, p);
            return;
        }

        Stats.Game best = currentGameList.getBestGame();

        p.setStrokeWidth(axisWidth);
        canvas.drawLine(borderDistance, atAxis(), getWidth() - borderDistance, atAxis(), p);

        float x = getWidth() - borderDistance;

        xToBar.clear();
        for (Stats.Game game : currentGameList.asDescendingIterable()) {
            x -= barWidth + barDistance;
            if (x < 0)
                break;

            xToBar.put(x, game);

            float y;

            p.setColor(game.wrongAnswers() == 0 && game.rightAnswers() > 0 ? green : gray);
            y = drawBar(game.rightAnswers(), x, canvas, p);
            drawTextMiddle(Integer.toString(game.rightAnswers()),
                           canvas, x + barWidth/2, y - axisOffset, p);

            p.setColor(red);
            y = drawBar(-game.wrongAnswers(), x, canvas, p);
            if (game.wrongAnswers() > 0) {
                y += textSize - axisOffset; // hand-fiddeling of distances
                drawTextMiddle(Integer.toString(game.wrongAnswers()),
                               canvas, x + barWidth/2, y, p);
                y += p.descent();
            } else {
                y = belowAxis();
            }

            if (game == taggedGame) {
                p.setColor(teal);
                p.setStrokeWidth(0);
                canvas.drawLine(x + barWidth/2, y + axisOffset,
                                x + barWidth/2, getHeight() - 2*textSize, p);
                String info = String.format("%d point", game.points());
                if (game.points() != 1)
                    info += "s";

                p.setColor(gray);

                if (game == best && best.points() > 0) {
                    info += ", current record!";
                    //p.setTypeface(Typeface.DEFAULT_BOLD);
                    p.setColor(green);
                }
                drawTextMiddle(info, canvas, x + barWidth/2, getHeight() - textSize, p);
                info = String.format("%.1f sec⁄answer", game.secondsPerAnswer());
                drawTextMiddle(info, canvas, x + barWidth/2, getHeight() , p);
            }
        }

        if (best.points() == 0)
            return;

        p.setColor(black);
        p.setStyle(Paint.Style.STROKE);
        p.setPathEffect(new DashPathEffect(new float[] {20,10}, 0));
        p.setStrokeWidth(axisWidth/2);

        bestGameLineY = aboveAxis()
            - (blockOffset + blockHeight) * (float) best.points() / unitsPerBlock;

        Path path = new Path();
        path.moveTo(borderDistance/2, bestGameLineY);
        path.rLineTo(getWidth() - borderDistance, 0);

        canvas.drawPath(path, p);
    }

    protected void drawTextMiddle(String text, Canvas canvas,
                                  float x, float y, Paint p) {
        float length = p.measureText(text);
        if (x + length/2 > getWidth()) {
            canvas.drawText(text, getWidth() - length, y, p);
        } else if (x - length/2 < 0) {
            canvas.drawText(text, 0, y, p);
        } else {
            canvas.drawText(text, x - length/2, y, p);
        }
    }

    protected float drawBar(int units, float x, Canvas canvas, Paint p) {
        // If units >= 0, draw bar upwards; otherwise, downwards.
        // Return topmost or bottommost y-value plus blockOffset, respectively
        if (units == 0) {
            return aboveAxis();
        }

        float offset = blockHeight + blockOffset;
        if (units > 0)
            offset = -offset;

        // see if we have enough space for the full bar
        float necessarySpace =
            units > 0 ? aboveAxis() : getHeight() - belowAxis() - 3f*p.getFontSpacing();
        boolean fractionalBlock = units % unitsPerBlock != 0;
        float availableSpace = necessarySpace + (units / unitsPerBlock) * offset;
        if (fractionalBlock)
            availableSpace -=
                Math.abs(units % unitsPerBlock) * blockHeight / unitsPerBlock + blockOffset;

        RectF rect =
            new RectF(x,
                      units > 0 ? aboveAxis() - blockHeight : belowAxis(),
                      x + barWidth,
                      units > 0 ? aboveAxis() : belowAxis() + blockHeight);

        //Log.d(MathActivity.TAG, String.format("StatsView.drawBar: units=%d, availableSpace=%f",
        //                                      units, availableSpace));

        if (availableSpace < 0 && units < 0) {
            // let great numbers of right answers go through the roof for now.
            // our players deserve it.
            canvas.drawRect(rect, p);

            Path z = new Path(zigzag);
            z.offset(x + barWidth/2, rect.bottom);
            if (units > 0)
                z.offset(0, -zigzagHeight - blockHeight);
            p.setStrokeWidth(axisWidth);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawPath(z, p);
            p.setStyle(Paint.Style.FILL);

            if (units < 0) { // many wrong answers: more "usual" case
                rect.offset(0, zigzagHeight + blockHeight);
                canvas.drawRect(rect, p);
                return rect.bottom + blockOffset;
            } else {
                rect.offset(0, -zigzagHeight - blockHeight);
                canvas.drawRect(rect, p);
                return rect.top - blockOffset;
            }
        }

        for (int i = Math.abs(units)/unitsPerBlock; i > 0; --i) {
            canvas.drawRect(rect, p);
            rect.offset(0, offset);
        }

        float y = units > 0 ? rect.bottom : rect.top;

        if (fractionalBlock) {
            y -= (units % unitsPerBlock) * blockHeight / unitsPerBlock;

            canvas.drawRect(x,
                            units > 0 ? y : rect.top,
                            x + barWidth,
                            units > 0 ? rect.bottom : y,
                            p);
            y -= units > 0 ? blockOffset : -blockOffset;
        }

        return y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
         if (event.getY() < bestGameLineY + blockHeight) {
            if (event.getY() > bestGameLineY - blockHeight) {
                taggedGame = currentGameList.getBestGame();
            } else {
                taggedGame = null;
            }
            invalidate();
            return true;
        }

        Float x = xToBar.lowerKey(event.getX());
        if (x == null) {
            taggedGame = null;
            invalidate();
            return super.onTouchEvent(event);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            taggedGame = xToBar.get(x);
            Log.d(MathActivity.TAG, taggedGame.toString());
            invalidate();
        }
        return true;
    }
}
