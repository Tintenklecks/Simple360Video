package com.eje_c.simple360video;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.eje_c.meganekko.MeganekkoActivity;
import com.eje_c.meganekko.VrContext;
import com.eje_c.meganekko.scene_objects.VideoSceneObject;

import java.io.IOException;

public class MainActivity extends MeganekkoActivity {

    private VideoPlayerScene scene;
    private boolean controlEnabled;

    @Override
    protected void oneTimeInit(VrContext context) {

        // シーンの読み込み
        scene = new VideoPlayerScene(context);
        setScene(scene);

        // 設定アプリから設定を取得
        try {
            Context settingsContext = createPackageContext("com.eje_c.simple360video.settings", CONTEXT_IGNORE_SECURITY);
            SharedPreferences prefs = settingsContext.getSharedPreferences("VideoSettings", MODE_WORLD_READABLE);
            String videoUri = prefs.getString("videoUri", null);

            if (videoUri != null) {
                scene.setDataSource(videoUri);
                scene.prepare();
                scene.start();
            }

            String videoTypeName = prefs.getString("videoType", "MONO");
            VideoSceneObject.VideoType videoType = VideoSceneObject.VideoType.valueOf(videoTypeName);
            scene.setVideoType(videoType);

            // ユーザー操作を受け付けるかどうか
            controlEnabled = prefs.getBoolean("controlEnabled", true);
            scene.setUserControlEnabled(controlEnabled);

        } catch (PackageManager.NameNotFoundException | IOException e) {
            e.printStackTrace();
            createVrToastOnUiThread("Make sure to be installed Simple 360 Video settings app.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (scene != null) {
            scene.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (scene != null) {
            scene.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scene != null) {
            scene.pause();
        }
    }

    /*
     * 戻るボタン等
     */

    @Override
    public boolean onKeyDown(int keyCode, int repeatCount) {
        return !controlEnabled || super.onKeyDown(keyCode, repeatCount);
    }

    @Override
    public boolean onKeyShortPress(int keyCode, int repeatCount) {
        return !controlEnabled || super.onKeyShortPress(keyCode, repeatCount);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, int repeatCount) {
        return !controlEnabled || super.onKeyLongPress(keyCode, repeatCount);
    }
}
