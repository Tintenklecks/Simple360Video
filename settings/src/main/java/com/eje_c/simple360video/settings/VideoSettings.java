package com.eje_c.simple360video.settings;

import android.content.Context;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE, mode = Context.MODE_WORLD_READABLE)
public interface VideoSettings {

    String videoUri();

    @DefaultString("MONO")
    String videoType();

    @DefaultBoolean(true)
    boolean controlEnabled();
}
