/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package com.microsoft.o365_android_onenote_rest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.microsoft.o365_android_onenote_rest.conf.ServiceConstants;
import com.microsoft.o365_android_onenote_rest.snippet.SectionSnippet;
import com.microsoft.o365_android_onenote_rest.util.SharedPrefsUtil;
import com.microsoft.o365_android_onenote_rest.util.User;

import java.net.URI;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.microsoft.o365_android_onenote_rest.R.id.msa_signin;
import static com.microsoft.o365_android_onenote_rest.R.id.o365_signin;

public class SignInActivity
        extends BaseActivity
        implements AuthenticationCallback<AuthenticationResult>, LiveAuthListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        System.out.println("*** SignInActivity.onCreate");
/*
        SignInActivity.super.onCreate(savedInstanceState);

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
                BaseActivity.mResourceId2 = "https://fcpkag.sharepoint.com";
                //SignInActivity.super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_signin);
                if (User.isOrg()) {
                    //mAuthenticationManager.connect(this);
                    mAuthenticationManagers.mAuthenticationManager1.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
                }
                ButterKnife.inject(SignInActivity.this);
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
        doIt = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if (User.isOrg()) {
            //mAuthenticationManager.connect(this);
            mAuthenticationManagers.mAuthenticationManager1.connect(this);
            mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
        }
        ButterKnife.inject(this);

    }

    @OnClick(o365_signin)
    public void onSignInO365Clicked() {
        System.out.println("*** onSignInO365Clicked");
/*
        try {
            authenticateOrganization();
        } catch (IllegalArgumentException e) {
            warnBadClient();
        }
*/
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
                if(sharePointUrl.equals("")) sharePointUrl = "https://fcpkag.sharepoint.com";
                System.out.println("*** SharePoint URL: " + sharePointUrl);
                BaseActivity.mResourceId2 = sharePointUrl;
                SectionSnippet.mSharePointEndpoint = sharePointUrl;
                try {
                    doIt();
                    authenticateOrganization();
                } catch (IllegalArgumentException e) {
                    warnBadClient();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
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
                    //mAuthenticationManager.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager1.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
                }

                @Override
                public void onAuthError(LiveAuthException exception, Object userState) {
                    //mAuthenticationManager.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager1.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
                }
            });
        } else {
            System.out.println("*** SignInActivity.authenticateOrganization");
            //mAuthenticationManager.connect(this);
            mAuthenticationManagers.mAuthenticationManager1.connect(this);
            mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
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
                    BaseActivity.mResourceId2 = "https://fcpkag.sharepoint.com";
                    mAuthenticationManagers.mAuthenticationManager1.connect(SignInActivity.this);
                    mAuthenticationManagers.mAuthenticationManager2.connect(getAuthenticationCallback());
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
    }

    private void validateOrganizationArgs() throws IllegalArgumentException {
        UUID.fromString(ServiceConstants.CLIENT_ID);
        URI.create(ServiceConstants.REDIRECT_URI);
    }

    @OnClick(msa_signin)
    public void onSignInMsaClicked() {
        authenticateMsa();
    }

    private void authenticateMsa() {
        try {
            validateMsaArgs();
            mLiveAuthClient.login(this, sSCOPES, this);
        } catch (IllegalArgumentException e) {
            warnBadClient();
        }
    }

    private void validateMsaArgs() throws IllegalArgumentException {
        if (ServiceConstants.MSA_CLIENT_ID.equals("<Your MSA CLIENT ID>")) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void onSuccess(AuthenticationResult authenticationResult) {
        finish();
        SharedPrefsUtil.persistAuthToken(authenticationResult);
        start();
    }

    private void start() {
        Intent appLaunch = new Intent(this, SnippetListActivity.class);
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