/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package ch.fcpkag.whiteboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import ch.fcpkag.whiteboard.application.WhiteboardApp;
import ch.fcpkag.whiteboard.conf.ServiceConstants;
import ch.fcpkag.whiteboard.inject.AppModule;
import ch.fcpkag.whiteboard.util.SharedPrefsUtil;

import butterknife.ButterKnife;

public class SignInActivity
        extends BaseActivity
        implements AuthenticationCallback<AuthenticationResult> {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        System.out.println("*** SignInActivity.onCreate");

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, MODE_PRIVATE);
        String sharePointUrl = preferences.getString(SharedPrefsUtil.PREF_SHAREPOINT_URL, null);
        doInject = (sharePointUrl != null);
        super.onCreate(savedInstanceState);
        doInject = true;
        setContentView(R.layout.activity_signin);

        if(sharePointUrl == null) {
            sharePointUrl = ServiceConstants.AUTHENTICATION_RESOURCE_ID2;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.sharepoint_url);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(sharePointUrl);
            input.selectAll();
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String sharePointUrl = input.getText().toString();
                    System.out.println("*** SharePoint URL: " + sharePointUrl);
                    if (sharePointUrl.equals(""))
                        sharePointUrl = ServiceConstants.AUTHENTICATION_RESOURCE_ID2;  // default URL
                    System.out.println("*** SharePoint URL: " + sharePointUrl);
                    SharedPreferences preferences
                            = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, MODE_PRIVATE);
                    preferences.edit().putString(SharedPrefsUtil.PREF_SHAREPOINT_URL, sharePointUrl).commit();
                    doInject();
                    mAuthenticationManagers.mAuthenticationManager1.connect(SignInActivity.this);
                    ButterKnife.inject(SignInActivity.this);
                }
            });
            builder.show();
        }
        else {
            mAuthenticationManagers.mAuthenticationManager1.connect(this);
            ButterKnife.inject(this);
        }
    }

    @Override
    public void onSuccess(final AuthenticationResult authenticationResult) {
        mAuthenticationManagers.mAuthenticationManager2.connect(new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult2) {
                finish();
                SharedPrefsUtil.persistAuthToken(authenticationResult);
                SharedPrefsUtil.persistAuthToken2(authenticationResult2);
                start();
            }

            @Override
            public void onError(Exception e) {
                System.out.println("*** SignInActivity onError: " + e);
                onError(e);
            }
        });
    }

    private void start() {
        Intent appLaunch = new Intent(this, DetailActivity.class);
        startActivity(appLaunch);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
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