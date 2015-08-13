/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/

package com.microsoft.o365_android_onenote_rest.application;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import com.microsoft.o365_android_onenote_rest.BuildConfig;
import com.microsoft.o365_android_onenote_rest.inject.AppModule;
import com.microsoft.o365_android_onenote_rest.inject.RequestInterceptors;
import com.microsoft.o365_android_onenote_rest.snippet.AbstractSnippet;

import javax.inject.Inject;

import dagger.ObjectGraph;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import timber.log.Timber;

public class SnippetApp extends Application {
    /**
     * The {@link dagger.ObjectGraph} used by Dagger to fulfill <code>@inject</code> annotations
     *
     * @see javax.inject.Inject
     * @see dagger.Provides
     * @see javax.inject.Singleton
     */
    public ObjectGraph mObjectGraph;

    private static SnippetApp sSnippetApp;

    @Inject
    protected String endpoint;

    @Inject
    public Converter converter;

    @Inject
    public RestAdapter.LogLevel logLevel;

    //@Inject
    //public RequestInterceptor requestInterceptor;

    @Inject
    public RequestInterceptors mRequestInterceptors;

    @Override
    public void onCreate() {
        super.onCreate();
        sSnippetApp = this;
        mObjectGraph = ObjectGraph.create(new AppModule());
        mObjectGraph.inject(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SharePoint URL");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sharePointUrl = input.getText().toString();
                System.out.println("*** SharePoint URL: " + sharePointUrl);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
*/
    }

    public static SnippetApp getApp() {
        return sSnippetApp;
    }
/*
    public RestAdapter getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setConverter(converter)
                .setRequestInterceptor(requestInterceptor)
                .build();
    }
*/
    public RestAdapter getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setConverter(converter)
                .setRequestInterceptor(mRequestInterceptors.requestInterceptor1)
                .build();
    }

    public RestAdapter getRestAdapter2(String endpoint) {
        System.out.println("*** SnippetApp.getRestAdapter2: " + endpoint);
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)   // TODO: change
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