/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package com.microsoft.o365_android_onenote_rest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.microsoft.o365_android_onenote_rest.inject.AppModule;
import com.microsoft.o365_android_onenote_rest.util.User;


public class SnippetDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("*** SnippetDetailActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_detail);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        if (savedInstanceState == null) {
            SnippetDetailFragment fragment = new SnippetDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.snippet_detail_container, fragment)
                    .commit();
        }
    }

    public void onDisconnectClicked() {
        finish();

        if (User.isOrg()) {
            mAuthenticationManagers.mAuthenticationManager1.disconnect();
            mAuthenticationManagers.mAuthenticationManager2.disconnect();
        } else if (User.isMsa()) {
            mLiveAuthClient.logout(null);
        }
        // drop the application shared preferences to clear any old auth tokens
        getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE)
                .edit() // get the editor
                .clear() // clear it
                .apply(); // asynchronously apply
        Intent login = new Intent(this, SignInActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
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