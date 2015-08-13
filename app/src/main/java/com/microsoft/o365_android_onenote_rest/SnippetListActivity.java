/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package com.microsoft.o365_android_onenote_rest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.microsoft.o365_android_onenote_rest.inject.AppModule;
import com.microsoft.o365_android_onenote_rest.util.User;


public class SnippetListActivity extends BaseActivity
        implements SnippetListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("*** SnippetListActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_list);

        if (findViewById(R.id.snippet_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((SnippetListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.snippet_list))
                    .setActivateOnItemClick(true);
        }
/*
        System.out.println("*** onCreate");
        Bundle arguments = new Bundle();
        arguments.putInt(SnippetDetailFragment.ARG_ITEM_ID, 34);
        SnippetDetailFragment fragment = new SnippetDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.snippet_detail_container, fragment)
                .commit();
*/
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
                onItemSelected(34);
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
        onItemSelected(34);
    }

    @Override
    public void onItemSelected(int position) {
        System.out.println("*** onItemSelected position: " + position);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(SnippetDetailFragment.ARG_ITEM_ID, position);
            SnippetDetailFragment fragment = new SnippetDetailFragment();
            fragment.setActivity(this);
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.snippet_detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, SnippetDetailActivity.class);
            detailIntent.putExtra(SnippetDetailFragment.ARG_ITEM_ID, position);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onDisconnectClicked() {
        finish();

        if (User.isOrg()) {
            //mAuthenticationManager.disconnect();
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