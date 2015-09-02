/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package com.microsoft.o365_android_onenote_rest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;
import com.microsoft.o365_android_onenote_rest.application.WhiteboardApp;
import com.microsoft.o365_android_onenote_rest.conf.ServiceConstants;
import com.microsoft.o365_android_onenote_rest.inject.AppModule;
import com.microsoft.o365_android_onenote_rest.util.SharedPrefsUtil;
import com.microsoft.o365_android_onenote_rest.util.User;

import java.net.URI;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.microsoft.o365_android_onenote_rest.R.id.o365_signin;

public class SignInActivity
        extends BaseActivity
        implements AuthenticationCallback<AuthenticationResult>, LiveAuthListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        System.out.println("*** SignInActivity.onCreate");

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        String sharePointUrl = preferences.getString(SharedPrefsUtil.PREF_SHAREPOINT_URL, null);
        doIt = (sharePointUrl != null);
        super.onCreate(savedInstanceState);
        doIt = true;
        setContentView(R.layout.activity_signin);

        if (User.isOrg()) {
            mAuthenticationManagers.mAuthenticationManager1.connect(this);
            mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
        }
        ButterKnife.inject(this);

    }

    @OnClick(o365_signin)
    public void onSignInO365Clicked() {
        System.out.println("*** onSignInO365Clicked");

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        String sharePointUrl = preferences.getString(SharedPrefsUtil.PREF_SHAREPOINT_URL, null);
        if(sharePointUrl == null) sharePointUrl = ServiceConstants.AUTHENTICATION_RESOURCE_ID2;

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
                        = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
                preferences.edit().putString(SharedPrefsUtil.PREF_SHAREPOINT_URL, sharePointUrl).commit();
                try {
                    doIt();
                    authenticateOrganization();
                } catch (IllegalArgumentException e) {
                    warnBadClient();
                }
            }
        });
        builder.show();
    }

    private void warnBadClient() {
        Toast.makeText(this,
                R.string.warning_clientid_redirecturi_incorrect,
                Toast.LENGTH_LONG)
                .show();
    }

    AuthenticationCallback<AuthenticationResult> getAuthenticationCallback() {
        return new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                finish();
                SharedPrefsUtil.persistAuthToken2(authenticationResult);
                start();
            }

            @Override
            public void onError(Exception e) {
                System.out.println("*** SignInActivity onError: " + e);
            }
        };
    }

    private void authenticateOrganization() throws IllegalArgumentException {
        validateOrganizationArgs();
        if (!User.isOrg()) {
            mLiveAuthClient.logout(new LiveAuthListener() {
                @Override
                public void onAuthComplete(LiveStatus status,
                                           LiveConnectSession session,
                                           Object userState) {
                    mAuthenticationManagers.mAuthenticationManager1.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
                }

                @Override
                public void onAuthError(LiveAuthException exception, Object userState) {
                    mAuthenticationManagers.mAuthenticationManager1.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
                }
            });
        } else {
            System.out.println("*** SignInActivity.authenticateOrganization");
            mAuthenticationManagers.mAuthenticationManager1.connect(this);
            mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
        }
    }

    private void validateOrganizationArgs() throws IllegalArgumentException {
        UUID.fromString(ServiceConstants.CLIENT_ID);
        URI.create(ServiceConstants.REDIRECT_URI);
    }

    @Override
    public void onSuccess(AuthenticationResult authenticationResult) {
        finish();
        SharedPrefsUtil.persistAuthToken(authenticationResult);
        start();
    }

    private void start() {
        Intent appLaunch = new Intent(this, SnippetDetailActivity.class);
        startActivity(appLaunch);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onAuthComplete(LiveStatus status,
                               LiveConnectSession session,
                               Object userState) {
        Timber.d("MSA: Auth Complete...");
        if (null != status) {
            Timber.d(status.toString());
        }
        if (null != session) {
            Timber.d(session.toString());
            SharedPrefsUtil.persistAuthToken(session);
        }
        if (null != userState) {
            Timber.d(userState.toString());
        }
        start();
    }

    @Override
    public void onAuthError(LiveAuthException exception, Object userState) {
        exception.printStackTrace();
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