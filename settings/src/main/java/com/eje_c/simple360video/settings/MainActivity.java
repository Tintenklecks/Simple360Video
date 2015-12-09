package com.eje_c.simple360video.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 1;
    private SharedPreferences prefs;

    @ViewById
    TextView filepath;
    @ViewById
    RadioButton radioMono;
    @ViewById
    RadioButton radioHorizontalStereo;
    @ViewById
    RadioButton radioVerticalStereo;
    @ViewById
    Switch switchUserControlEnabled;

    @Pref
    VideoSettings_ videoSettings;

    @AfterViews
    void init() {

        // 設定の値をUIに反映
        filepath.setText(videoSettings.videoUri().getOr(getString(R.string.please_choose_a_file)));
        switchUserControlEnabled.setChecked(videoSettings.controlEnabled().get());

        switch (videoSettings.videoType().get()) {
            case "MONO":
                radioMono.setChecked(true);
                break;
            case "HORIZONTAL_STEREO":
                radioHorizontalStereo.setChecked(true);
                break;
            case "VERTICAL_STEREO":
                radioVerticalStereo.setChecked(true);
                break;
        }
    }

    @Click
    void radioMono() {
        videoSettings.videoType().put("MONO");
    }

    @Click
    void radioHorizontalStereo() {
        videoSettings.videoType().put("HORIZONTAL_STEREO");
    }

    @Click
    void radioVerticalStereo() {
        videoSettings.videoType().put("VERTICAL_STEREO");
    }

    /**
     * ユーザーコントロールの有効化スイッチ
     *
     * @param checked
     */
    @CheckedChange
    void switchUserControlEnabled(boolean checked) {
        videoSettings.controlEnabled().put(checked);
    }

    /**
     * ファイルを選択ボタンをクリックした時
     */
    @Click
    void btnChooseFile() {
        Intent intent = new Intent(Intent.ACTION_PICK)
                .setType("video/mp4");
        startActivityForResult(intent, FILE_SELECT_CODE);
    }

    /**
     * ファイル選択結果
     *
     * @param resultCode
     * @param data
     */
    @OnActivityResult(FILE_SELECT_CODE)
    void onChooseFileResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        Uri uri = data.getData();
        MediaInfo mediaInfo = getMediaInfo(this, uri);
        if (mediaInfo == null) {
            Toast.makeText(MainActivity.this, R.string.cannot_retrieve_media_info, Toast.LENGTH_SHORT).show();
            return;
        }

        filepath.setText(mediaInfo.path);
        videoSettings.videoUri().put(mediaInfo.path);
    }

    public static MediaInfo getMediaInfo(Context context, Uri contentUri) {

        String[] proj = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
        };

        try (Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null)) {
            if (cursor == null) return null;

            if (cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
                return new MediaInfo(path, width, height);
            }
        }

        return null;
    }
}
