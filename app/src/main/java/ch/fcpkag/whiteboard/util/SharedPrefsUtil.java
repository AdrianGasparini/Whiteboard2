/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package ch.fcpkag.whiteboard.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.aad.adal.AuthenticationResult;
import ch.fcpkag.whiteboard.application.WhiteboardApp;
import ch.fcpkag.whiteboard.inject.AppModule;

/**
 * Access to shared preferences
 */
public class SharedPrefsUtil {

    public static final String PREF_AUTH_TOKEN1 = "PREF_AUTH_TOKEN";    // for OneNote
    public static final String PREF_AUTH_TOKEN2 = "PREF_AUTH_TOKEN2";   // for SharePoint
    public static final String PREF_SHAREPOINT_URL = "PREF_SHAREPOINT_URL";
    public static final String PREF_SITE = "PREF_SITE";
    public static final String PREF_NOTEBOOK = "PREF_NOTEBOOK";
    public static final String PREF_SECTION = "PREF_SECTION";
    public static final String PREF_DEFAULT_SITE = "PREF_DEFAULT_SITE";
    public static final String PREF_DEFAULT_NOTEBOOK = "PREF_DEFAULT_NOTEBOOK";

    public static SharedPreferences getSharedPreferences() {
        return WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
    }

    public static void persistAuthToken1(AuthenticationResult result) {
        setAccessToken1(result.getAccessToken());
    }

    private static void setAccessToken1(String accessToken) {
        getSharedPreferences().edit().putString(PREF_AUTH_TOKEN1, accessToken).commit();
    }

    public static void persistAuthToken2(AuthenticationResult result) {
        setAccessToken2(result.getAccessToken());
    }

    private static void setAccessToken2(String accessToken) {
        getSharedPreferences().edit().putString(PREF_AUTH_TOKEN2, accessToken).commit();
    }
}
// *********************************************************
//
// Android-REST-API-Explorer, https://github.com/OneNoteDev/Android-REST-API-Explorer
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************