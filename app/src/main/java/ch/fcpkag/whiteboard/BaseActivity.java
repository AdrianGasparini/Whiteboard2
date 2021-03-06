/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/

package ch.fcpkag.whiteboard;

import android.content.Context;
import android.content.SharedPreferences;

import ch.fcpkag.whiteboard.application.WhiteboardApp;
import ch.fcpkag.whiteboard.conf.ServiceConstants;
import ch.fcpkag.whiteboard.inject.AppModule;
import ch.fcpkag.whiteboard.inject.AzureModule;
import ch.fcpkag.whiteboard.util.SharedPrefsUtil;
import com.microsoft.AzureADModule;
import com.microsoft.AzureAppCompatActivity;
import ch.fcpkag.whiteboard.inject.ObjectGraphInjector;

import dagger.ObjectGraph;

/**
 * Initialization of activities
 */
public abstract class BaseActivity
        extends AzureAppCompatActivity
        implements ObjectGraphInjector {

    @Override
    protected AzureADModule getAzureADModule() {
        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        String sharePointUrl = preferences.getString(SharedPrefsUtil.PREF_SHAREPOINT_URL, null);
        System.out.println("*** BaseActivity.getAzureADModule: " + sharePointUrl);
        AzureADModule.Builder builder = new AzureADModule.Builder(this);
        builder.validateAuthority(true)
                .skipBroker(true)
                .authenticationResourceId(ServiceConstants.AUTHENTICATION_RESOURCE_ID1, sharePointUrl)
                .authorityUrl(ServiceConstants.AUTHORITY_URL)
                .redirectUri(ServiceConstants.REDIRECT_URI)
                .clientId(ServiceConstants.CLIENT_ID);
        return builder.build();
    }

    @Override
    protected Object[] getModules() {
        return new Object[]{new AzureModule()};
    }

    @Override
    protected ObjectGraph getRootGraph() {
        return WhiteboardApp.getApp().mObjectGraph;
    }

    @Override
    public void inject(Object target) {
        mObjectGraph.inject(target);
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