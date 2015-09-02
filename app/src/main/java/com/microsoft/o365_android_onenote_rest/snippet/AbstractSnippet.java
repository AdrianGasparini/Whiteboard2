/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/

package com.microsoft.o365_android_onenote_rest.snippet;

import com.microsoft.o365_android_onenote_rest.application.WhiteboardApp;
import com.microsoft.o365_android_onenote_rest.util.User;
import com.microsoft.onenoteapi.service.NotebooksService;
import com.microsoft.onenoteapi.service.PagesService;
import com.microsoft.onenoteapi.service.SectionGroupsService;
import com.microsoft.onenoteapi.service.SectionsService;
import com.microsoft.onenoteapi.service.SiteMetadataService;

/**
 * The base class for snippets
 *
 */
public abstract class AbstractSnippet {

    public static final Services sServices = new Services();

    private String mO365Version = "beta", mMSAVersion = "v1.0";

    /**
     * Snippet constructor
     *
     */
    public AbstractSnippet() {
    }

    public static class Services {

        public final NotebooksService mNotebooksService;
        public final PagesService mPagesService;
        public final SectionGroupsService mSectionGroupsService;
        public final SectionsService mSectionsService;
        public final SiteMetadataService mSiteMetadataService;

        Services() {
            mNotebooksService = WhiteboardApp.getApp().getRestAdapter().create(NotebooksService.class);
            mPagesService = WhiteboardApp.getApp().getRestAdapter().create(PagesService.class);
            mSectionGroupsService = WhiteboardApp.getApp().getRestAdapter().create(SectionGroupsService.class);
            mSectionsService = WhiteboardApp.getApp().getRestAdapter().create(SectionsService.class);
            mSiteMetadataService = WhiteboardApp.getApp().getRestAdapter().create(SiteMetadataService.class);
        }
    }

    @SuppressWarnings("unused")
    public void setUp(Services services, retrofit.Callback<String[]> callback) {
        // Optional method....
        callback.success(new String[]{}, null);
    }

    /**
     * Returns the version segment of the endpoint url with input from
     * XML snippet description and authentication method (Office 365, MSA)
     *
     * @return the version of the endpoint to use
     */
    public String getVersion() {
        return User.isMsa() ? mMSAVersion : mO365Version;
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
