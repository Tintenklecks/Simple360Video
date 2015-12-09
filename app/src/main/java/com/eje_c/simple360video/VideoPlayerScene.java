package com.eje_c.simple360video;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.XmlRes;

import com.eje_c.meganekko.Camera;
import com.eje_c.meganekko.Picker;
import com.eje_c.meganekko.Scene;
import com.eje_c.meganekko.VrContext;
import com.eje_c.meganekko.VrFrame;
import com.eje_c.meganekko.event.SwipeBackEvent;
import com.eje_c.meganekko.event.SwipeForwardEvent;
import com.eje_c.meganekko.event.TouchSingleEvent;
import com.eje_c.meganekko.scene_objects.CanvasSceneObject;
import com.eje_c.meganekko.scene_objects.VideoSceneObject;
import com.eje_c.meganekko.xml.XmlSceneParser;
import com.eje_c.meganekko.xml.XmlSceneParserFactory;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class VideoPlayerScene extends Scene implements MediaPlayer.OnSeekCompleteListener {

    public static final float CONTROL_DISTANCE = 5.0f;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private VideoSceneObject player;
    private CanvasSceneObject controls;
    private ControlsRenderer controlsRenderer;
    private boolean userControlEnabled;

    public VideoPlayerScene(VrContext vrContext) {
        super(vrContext);

        // シーンの読み込み
        try {
            load(R.xml.scene);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // コントロールUIの設定
        controls = (CanvasSceneObject) findObjectById(R.id.controls);
        controlsRenderer = new ControlsRenderer(getVrContext().getContext());
        controls.setOnDrawListener(controlsRenderer);
        controls.attachEyePointeeHolder();

        // 動画プレイヤーの設定
        player = (VideoSceneObject) findObjectById(R.id.player);
        player.setMediaPlayer(mediaPlayer);
        player.setVideoType(VideoSceneObject.VideoType.MONO);

        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setLooping(true);
    }

    public void load(@XmlRes int xmlRes) throws IOException, XmlPullParserException {
        XmlSceneParser parser = XmlSceneParserFactory.getInstance(getVrContext()).getSceneParser();
        parser.parse(xmlRes, this);
    }

    public void setVideoType(VideoSceneObject.VideoType videoType) {
        player.setVideoType(videoType);
    }

    /* ----------
     * MediaPlayerのラッパーメソッド
     * ----------
     */

    public void setDataSource(String uri) throws IOException {
        mediaPlayer.setDataSource(getVrContext().getContext(), Uri.parse(uri));
    }

    public void prepare() throws IOException {
        mediaPlayer.prepare();
        updateControls();
    }

    public void release() {

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void start() {

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateControls();
        }
    }

    public void pause() {

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateControls();
        }
    }

    public void ff() {

        if (mediaPlayer != null) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
        }
    }

    public void rew() {

        if (mediaPlayer != null) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    private void updateControls() {
        controlsRenderer.setTotalTime(mediaPlayer.getDuration() * 0.001f);
        controlsRenderer.setCurrentTime(mediaPlayer.getCurrentPosition() * 0.001f);
        controlsRenderer.setPlaying(mediaPlayer.isPlaying());
    }

    /**
     * サムネイル一覧がカメラの正面に来るようにする。
     */
    private void centerControls() {
        final Camera camera = getMainCamera();
        float[] lookAt = camera.getLookAt();
        float yaw = camera.getTransform().getRotationYaw();
        float pitch = camera.getTransform().getRotationPitch();
        float angle = pitch < 90 && pitch > -90 ? yaw : 180.0f - yaw;
        controls.getTransform().setRotationByAxis(angle, 0, 1, 0);
        controls.getTransform().setPosition(lookAt[0] * CONTROL_DISTANCE, 0, lookAt[2] * CONTROL_DISTANCE);
    }

    /**
     * フレームごとの処理
     *
     * @param vrFrame
     */
    @Override
    public void onEvent(VrFrame vrFrame) {

        // 再生位置を反映
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            controlsRenderer.setCurrentTime(mediaPlayer.getCurrentPosition() * 0.001f);

        super.onEvent(vrFrame);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

        // 再生中でない場合はUIを更新
        if (!mp.isPlaying())
            controlsRenderer.setCurrentTime(mediaPlayer.getCurrentPosition() * 0.001f);
    }

    public void showControls() {
        controls.setVisible(true);
        centerControls();
        getVrContext().getActivity().showGazeCursor();
    }

    public void hideControls() {
        controls.setVisible(false);
        getVrContext().getActivity().hideGazeCursor();
    }

    /* ----------
     * ユーザー操作
     * ----------
     */

    public void setUserControlEnabled(boolean userControlEnabled) {
        this.userControlEnabled = userControlEnabled;
    }

    @Override
    public void onEvent(SwipeForwardEvent event) {
        super.onEvent(event);

        if (userControlEnabled) {
            ff();
        }
    }

    @Override
    public void onEvent(SwipeBackEvent event) {
        super.onEvent(event);

        if (userControlEnabled) {
            rew();
        }
    }

    @Override
    public void onEvent(TouchSingleEvent event) {
        super.onEvent(event);

        if (userControlEnabled) {

            if (controls.isShown()) {

                // コントロールを見ている間
                if (getVrContext().getActivity().isLookingAt(controls)) {

                    // シークバーを見ているか調べる
                    float[] lookedPosition = Picker.pickSceneObjectv(controls, getMainCamera());
                    float x = lookedPosition[0];
                    float y = lookedPosition[1];
                    if (x > -2.0f && x < 2.0f && y > -0.75f && y < -0.5f) {

                        // シーク
                        int seekTime = (int) ((x + 2.0f) / 4.0f * mediaPlayer.getDuration());
                        seekTo(seekTime);

                    } else {

                        // 再生・一時停止
                        if (isPlaying()) {
                            pause();
                        } else {
                            start();
                        }
                    }
                } else {
                    // コントロールを非表示
                    hideControls();
                }

            } else {
                // コントロールを表示
                showControls();
            }
        }
    }
}
