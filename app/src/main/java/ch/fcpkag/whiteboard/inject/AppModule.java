/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/

package ch.fcpkag.whiteboard.inject;

import android.content.Context;
import android.content.SharedPreferences;

import ch.fcpkag.whiteboard.application.WhiteboardApp;
import ch.fcpkag.whiteboard.conf.ServiceConstants;
import ch.fcpkag.whiteboard.util.SharedPrefsUtil;

import com.microsoft.onenoteapi.service.GsonDateTime;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

@Module(library = true,
        injects = {
                WhiteboardApp.class
        }
)
public class AppModule {

    public static final String PREFS = "ch.fcpkag.whiteboard";

    @Provides
    public String providesRestEndpoint() {
        return ServiceConstants.ONENOTE_API;
    }

    @Provides
    public RestAdapter.LogLevel providesLogLevel() {
        return RestAdapter.LogLevel.FULL;
    }

    @Provides
    public Converter providesConverter() {
        return new GsonConverter(GsonDateTime.getOneNoteBuilder()
                .create());
    }

    @Provides()
    public RequestInterceptors providesRequestInterceptor() {
        RequestInterceptors is = new RequestInterceptors();
        is.requestInterceptor1 = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                // apply the Authorization header if we had a token...
                final SharedPreferences preferences
                        = WhiteboardApp.getApp().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                final String token =
                        preferences.getString(SharedPrefsUtil.PREF_AUTH_TOKEN1, null);
                System.out.println("*** token1: " + token);
                if (null != token) {
                    request.addHeader("Authorization", "Bearer " + token);
                }
            }
        };
        is.requestInterceptor2 = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                // apply the Authorization header if we had a token...
                System.out.println("*** RequestInterceptor2.intercept");
                final SharedPreferences preferences
                        = WhiteboardApp.getApp().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                final String token =
                        preferences.getString(SharedPrefsUtil.PREF_AUTH_TOKEN2, null);
                System.out.println("*** token2: " + token);
                if (null != token) {
                    request.addHeader("Authorization", "Bearer " + token);
                }
            }
        };
        return is;
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
