package com.eje_c.simple360video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.eje_c.meganekko.VrFrame;
import com.eje_c.meganekko.scene_objects.CanvasSceneObject;

public class ControlsRenderer implements CanvasSceneObject.OnDrawListener {

    private Context context;
    private Paint bg = new Paint();
    private Paint progressBackground = new Paint();
    private Paint progress = new Paint();
    private Paint timeText = new Paint();
    private Drawable playIcon, pauseIcon;
    private boolean playing;
    private float totalTime;
    private String durationText;
    private float currentTime;
    private String currentTimeText;
    private boolean dirty = true;

    ControlsRenderer(Context context) {
        this.context = context.getApplicationContext();

        bg.setColor(Color.BLACK);
        bg.setAlpha(128);

        progressBackground.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        progressBackground.setStrokeWidth(10f);

        progress.set(progressBackground);
        progress.setColor(ContextCompat.getColor(context, R.color.colorAccent));

        timeText.setColor(ContextCompat.getColor(context, R.color.textColor));
        timeText.setTextSize(60f);
        timeText.setTextAlign(Paint.Align.CENTER);

        playIcon = ContextCompat.getDrawable(context, R.drawable.ic_play_circle_outline_white_48dp);
        playIcon.setBounds(-playIcon.getMinimumWidth() / 2, -playIcon.getMinimumHeight() / 2, playIcon.getMinimumWidth() / 2, playIcon.getMinimumHeight() / 2);

        pauseIcon = ContextCompat.getDrawable(context, R.drawable.ic_pause_circle_outline_white_48dp);
        pauseIcon.setBounds(-pauseIcon.getMinimumWidth() / 2, -pauseIcon.getMinimumHeight() / 2, pauseIcon.getMinimumWidth() / 2, pauseIcon.getMinimumHeight() / 2);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void onDraw(CanvasSceneObject canvasSceneObject, Canvas canvas, VrFrame vrFrame) {

        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawPaint(bg);

        final float x = canvas.getWidth() * 0.5f;
        final float y = canvas.getHeight() * 0.5f;
        canvas.translate(x, y);

        // 再生・一時停止表示
        canvas.save();
        canvas.translate(0.0f * x, -0.4f * y);

        if (playing) {
            pauseIcon.draw(canvas);
        } else {
            playIcon.draw(canvas);
        }

        canvas.restore();

        // プログレスバー
        float progressLeftX = -0.8f * x;
        float progressRightX = 0.8f * x;
        float progressY = 0.5f * y;
        canvas.drawLine(progressLeftX, progressY, progressRightX, progressY, progressBackground);

        if (totalTime > 0.0f) {

            // 進捗表示
            float progressLength = (progressRightX - progressLeftX) * currentTime / totalTime;
            float progressEndX = progressLeftX + progressLength;
            canvas.drawLine(progressLeftX, progressY, progressEndX, progressY, progress);

            // 経過時間表示
            if (currentTimeText != null)
                canvas.drawText(currentTimeText, progressEndX, 0.3f * y, timeText);

            // 全体時間表示
            if (durationText != null)
                canvas.drawText(durationText, progressRightX, 0.8f * y, timeText);
        }

        dirty = false;
    }

    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
        durationText = context.getString(R.string.time, (int) totalTime / 60, (int) totalTime % 60);
        dirty = true;
    }

    public void setCurrentTime(float currentTime) {
        this.currentTime = currentTime;
        currentTimeText = context.getString(R.string.time, (int) currentTime / 60, (int) currentTime % 60);
        dirty = true;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        dirty = true;
    }
}
