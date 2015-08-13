/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 *  See full license at the bottom of this file.
 */
package com.microsoft;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;

import com.microsoft.aad.adal.AuthenticationContext;

import javax.inject.Inject;

import dagger.ObjectGraph;
import microsoft.o365_android_easyauth.R;

public abstract class AzureAppCompatActivity extends AppCompatActivity {

    protected ObjectGraph mObjectGraph;

    //@Inject
    //protected AuthenticationManager mAuthenticationManager;

    @Inject
    protected AuthenticationManagers mAuthenticationManagers;

    //@Inject
    //protected AuthenticationManager mAuthenticationManager2;

    //@Inject
    //protected AuthenticationContext mAuthenticationContext;

    @Inject
    protected AuthenticationContexts mAuthenticationContexts;

    //public boolean forSharePoint = true;

    //public static String mResourceId2 = null; //"https://fcpkag.sharepoint.com";

    public boolean doIt = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("*** AzureAppCompatActivity.onCreate: " + this.getClass().getName());
        super.onCreate(savedInstanceState);
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
                mResourceId2 = "https://fcpkag.sharepoint.com";

                Object[] modules = new Object[getModules().length + 1];
                int ii = 0;
                modules[ii++] = getAzureADModule();
                for (Object module : getModules()) {
                    modules[ii++] = module;
                }

                mObjectGraph = getRootGraph();
                if (null == mObjectGraph) {
                    // create a new one
                    mObjectGraph = ObjectGraph.create(modules);
                } else {
                    // extend the existing one
                    mObjectGraph = mObjectGraph.plus(modules);
                }

                mObjectGraph.inject(AzureAppCompatActivity.this);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

        //while(mResourceId2 == null);
*/
        if(doIt) {
            Object[] modules = new Object[getModules().length + 1];
            int ii = 0;
            modules[ii++] = getAzureADModule();
            for (Object module : getModules()) {
                modules[ii++] = module;
            }

            mObjectGraph = getRootGraph();
            if (null == mObjectGraph) {
                // create a new one
                mObjectGraph = ObjectGraph.create(modules);
            } else {
                // extend the existing one
                mObjectGraph = mObjectGraph.plus(modules);
            }

            mObjectGraph.inject(this);
        }
    }

    public void doIt() {
        Object[] modules = new Object[getModules().length + 1];
        int ii = 0;
        modules[ii++] = getAzureADModule();
        for (Object module : getModules()) {
            modules[ii++] = module;
        }

        mObjectGraph = getRootGraph();
        if (null == mObjectGraph) {
            // create a new one
            mObjectGraph = ObjectGraph.create(modules);
        } else {
            // extend the existing one
            mObjectGraph = mObjectGraph.plus(modules);
        }

        mObjectGraph.inject(this);
    }

/*
    public void doAgain() {
        Object[] modules = new Object[getModules().length + 1];
        //Object[] modules = new Object[getModules().length + 2];
        int ii = 0;
        if(!forSharePoint)
            modules[ii++] = getAzureADModule();
        else {
            AzureADModule.Builder builder = new AzureADModule.Builder(this);
            builder.validateAuthority(true)
                    .skipBroker(true)
                    .authenticationResourceId("https://fcpkag.sharepoint.com")
                    .authorityUrl("https://login.microsoftonline.com/common")
                    .redirectUri("http://localhost/OneNoteRESTExplorer")
                    .clientId("7b94795c-ccdb-4ca0-96a1-b40c1fa323b1");
            AzureADModule sharePointADModule = builder.build();
            modules[ii++] = sharePointADModule;
        }

        for (Object module : getModules()) {
            modules[ii++] = module;
        }

        mObjectGraph = getRootGraph();
        if (null == mObjectGraph) {
            // create a new one
            mObjectGraph = ObjectGraph.create(modules);
        } else {
            // extend the existing one
            mObjectGraph = mObjectGraph.plus(modules);
        }

        mObjectGraph.inject(this);
    }
*/

    protected abstract AzureADModule getAzureADModule();

    protected abstract Object[] getModules();

    protected ObjectGraph getRootGraph() {
        return null;
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