package com.reptimate.iot_teamnova.customAlbum;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;

public class Utils {
    public static String formatDuration(final Long durationInSeconds) {
        long hours = (durationInSeconds / (1000 * 60 * 60)) % 24;
        long minutes = (durationInSeconds / (1000 * 60)) % 60;
        long seconds = (durationInSeconds / 1000) % 60;

        if (hours > 0) {
            return "▶ " + String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "▶ " + String.format("%02d:%02d", minutes, seconds);
        }
    }
}
