/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/

package ch.fcpkag.whiteboard.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import ch.fcpkag.whiteboard.inject.RequestInterceptors;
import ch.fcpkag.whiteboard.util.SharedPrefsUtil;
import ch.fcpkag.whiteboard.BuildConfig;
import ch.fcpkag.whiteboard.inject.AppModule;

import javax.inject.Inject;

import dagger.ObjectGraph;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import timber.log.Timber;

/*
* The Whiteboard app
*/
public class WhiteboardApp extends Application {
    /**
     * The {@link dagger.ObjectGraph} used by Dagger to fulfill <code>@inject</code> annotations
     *
     * @see javax.inject.Inject
     * @see dagger.Provides
     * @see javax.inject.Singleton
     */
    public ObjectGraph mObjectGraph;

    private static WhiteboardApp sWhiteboardApp;

    @Inject
    protected String endpoint;

    @Inject
    public Converter converter;

    @Inject
    public RestAdapter.LogLevel logLevel;

    @Inject
    public RequestInterceptors mRequestInterceptors;

    @Override
    public void onCreate() {
        super.onCreate();
        sWhiteboardApp = this;
        mObjectGraph = ObjectGraph.create(new AppModule());
        mObjectGraph.inject(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public static WhiteboardApp getApp() {
        return sWhiteboardApp;
    }

    // get adapter for OneNote REST API
    public RestAdapter getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setConverter(converter)
                .setRequestInterceptor(mRequestInterceptors.requestInterceptor1)
                .build();
    }

    // get adapter for SharePoint REST API
    public RestAdapter getRestAdapter2() {
        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        String sharePointUrl = preferences.getString(SharedPrefsUtil.PREF_SHAREPOINT_URL, null);
        System.out.println("*** WhiteboardApp.getRestAdapter2: " + sharePointUrl);
        return new RestAdapter.Builder()
                .setEndpoint(sharePointUrl)
                .setLogLevel(logLevel)
                .setConverter(converter)
                .setRequestInterceptor(mRequestInterceptors.requestInterceptor2)
                .build();
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