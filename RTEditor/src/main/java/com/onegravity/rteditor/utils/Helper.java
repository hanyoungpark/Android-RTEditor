/*
 * Copyright (C) 2015 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.rteditor.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.utils.io.IOUtils;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Miscellaneous helper methods
 */
public abstract class Helper {

    private static float sDensity = Float.MAX_VALUE;
    private static float sDensity4Fonts = Float.MAX_VALUE;

    public static void closeQuietly(Closeable closeable) {
        IOUtils.closeQuietly(closeable);
    }

    public static float getDisplayDensity() {
        synchronized (Helper.class) {
            if (sDensity == Float.MAX_VALUE) {
                sDensity = getDisplayMetrics().density;
            }
            return sDensity;
        }
    }

    /**
     * Convert absolute pixels to scale dependent pixels.
     * This scales the size by scale dependent screen density (accessibility setting) and
     * the global display setting for message composition fields
     */
    public static int convertPxToSp(int pxSize) {
        return Math.round((float) pxSize * getDisplayDensity4Fonts());
    }

    /**
     * Convert scale dependent pixels to absolute pixels.
     * This scales the size by scale dependent screen density (accessibility setting) and
     * the global display setting for message composition fields
     */
    public static int convertSpToPx(int spSize) {
        return Math.round((float) spSize / getDisplayDensity4Fonts());
    }

    private static DisplayMetrics getDisplayMetrics() {
        Display display = ((WindowManager) RTApi.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    private static float getDisplayDensity4Fonts() {
        synchronized (Helper.class) {
            if (sDensity4Fonts == Float.MAX_VALUE) {
                sDensity4Fonts = getDisplayMetrics().density * getFontScale();
            }
            return sDensity4Fonts;
        }
    }

    private static float getFontScale() {
        Configuration config = RTApi.getApplicationContext().getResources().getConfiguration();
        return config.fontScale;
    }

    /**
     * This method encodes the query part of an url
     * @param url an url (e.g. http://www.1gravity.com?query=üö)
     * @return The url with an encoded query, e.g. http://www.1gravity.com?query%3D%C3%BC%C3%B6
     */
    public static String encodeQuery(String url) {
        Uri uri = Uri.parse(url);

        try {
            String query = uri.getQuery();
            String encodedQuery = query != null ? URLEncoder.encode(query, "UTF-8") : null;
            URI tmp = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
            return tmp + (encodedQuery != null && encodedQuery.length() > 0 ? "?" + encodedQuery : "");
        }
        catch (UnsupportedEncodingException ignore) {}
        catch (URISyntaxException ignore) {}

        return uri.toString();
    }

    /**
     * This method decodes an url with encoded query string
     * @param url an url with encoded query string (e.g. http://www.1gravity.com?query%3D%C3%BC%C3%B6)
     * @return The decoded url, e.g. http://www.1gravity.com?query=üö
     */
    public static String decodeQuery(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        }
        catch (UnsupportedEncodingException ignore) {}

        return url;
    }
}
