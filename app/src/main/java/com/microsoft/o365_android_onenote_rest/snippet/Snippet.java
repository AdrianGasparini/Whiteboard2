/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/

package com.microsoft.o365_android_onenote_rest.snippet;

import com.microsoft.o365_android_onenote_rest.application.WhiteboardApp;
import com.microsoft.onenoteapi.service.NotebooksService;
import com.microsoft.onenoteapi.service.SectionsService;
import com.microsoft.onenotevos.Envelope;
import com.microsoft.onenotevos.Notebook;
import com.microsoft.onenotevos.Section;
import com.microsoft.sharepoint.service.SitesService;
import com.microsoft.sharepointvos.FollowedSites;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.Callback;

/*
import static com.microsoft.o365_android_onenote_rest.R.array.create_section;
import static com.microsoft.o365_android_onenote_rest.R.array.get_all_sections;
import static com.microsoft.o365_android_onenote_rest.R.array.get_metadata_of_section;
import static com.microsoft.o365_android_onenote_rest.R.array.sections_specific_name;
import static com.microsoft.o365_android_onenote_rest.R.array.sections_specific_notebook;
*/

public class Snippet
        extends AbstractSnippet/*<SectionsService, String[]>*/ {

    public Map<String, com.microsoft.sharepointvos.Result> siteMap = new HashMap<>();
    public Map<String, Notebook> notebookMap = new HashMap<>();
    public Map<String, Section> sectionMap = new HashMap<>();
    public String mSiteCollectionId = null;
    public String mSiteId = null;

    public Snippet() {
        //super(SnippetCategory.sectionsSnippetCategory, sections_specific_notebook, Input.Spinner);
        super();
    }

    /*
    public Snippet(Integer descriptionArray) {
        super(SnippetCategory.sectionsSnippetCategory, descriptionArray);
    }

    public Snippet(Integer descriptionArray, Input input) {
        super(SnippetCategory.sectionsSnippetCategory, descriptionArray, input);
    }
    */

    @Override
    public void setUp(Services services, final retrofit.Callback<String[]> callback) {
        RestAdapter restAdapter = WhiteboardApp.getApp().getRestAdapter2();
        SitesService sitesService = restAdapter.create(SitesService.class);
        fillSiteSpinner(sitesService, callback, siteMap);

        //fillNotebookSpinner(services.mNotebooksService, callback, notebookMap);
    }

//    @Override
//    public void request(SectionsService service, Callback callback) {
//        System.out.println("*** request");
/*
        Notebook notebook = notebookMap.get(callback
                .getParams()
                .get(SnippetDetailFragment.ARG_SPINNER_SELECTION).toString());

        service.getNotebookSectionsSP(
                getVersion(),
                mSiteCollectionId,
                mSiteId,
                notebook.id,
                null,
                null,
                null,
                null,
                null,
                null,
                callback
        );
*/
//    }

    protected void fillSiteSpinner(
            SitesService sitesService,
            final retrofit.Callback<String[]> callback,
            final Map<String, com.microsoft.sharepointvos.Result> sitesMap) {
        System.out.println("*** fillSiteSpinner");
        sitesService.getFollowedSites(
                new Callback<FollowedSites>() {

                    @Override
                    public void success(FollowedSites followedSites, Response response) {
                        System.out.println("*** fillSiteSpinner success");
                        List<com.microsoft.sharepointvos.Result> resultList = followedSites.getD().getFollowed().getResults();
                        String[] siteNames = new String[resultList.size()];
                        for (int i = 0; i < resultList.size(); i++) {
                            siteNames[i] = resultList.get(i).getName();
                            sitesMap.put(resultList.get(i).getName(), resultList.get(i));
                            System.out.println("*** Site: " + siteNames[i]);
                        }
                        //mFragment.mProgressbar.setVisibility(View.GONE);
                        callback.success(siteNames, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** fillSiteSpinner failure");
                        /*
                        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
                        builder.setTitle(R.string.err_setup)
                                .setMessage(R.string.err_setup_msg)
                                .setPositiveButton(R.string.dismiss, null)
                                .show();
                        */
                        //mFragment.mProgressbar.setVisibility(View.GONE);
                        callback.failure(error);
                    }

                    /*
                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }
                    */
                });
    }

    public void fillNotebookSpinner(
            NotebooksService notebooksService,
            final retrofit.Callback<String[]> callback,
            final Map<String, Notebook> notebookMap) {
        System.out.println("*** fillNotebookSpinner");
        notebooksService.getNotebooksSP(getVersion(),
                //notebooksService.getSharedNotebooks(getVersion(),
                mSiteCollectionId,
                mSiteId,
                null,
                null,
                null,
                null,
                null,
                null,
                new Callback<Envelope<Notebook>>() {

                    @Override
                    public void success(Envelope<Notebook> notebookEnvelope, Response response) {
                        Notebook[] notebooks = notebookEnvelope.value;
                        String[] bookNames = new String[notebooks.length];
                        for (int i = 0; i < notebooks.length; i++) {
                            bookNames[i] = notebooks[i].name;
                            notebookMap.put(notebooks[i].name, notebooks[i]);
                        }
                        //mFragment.mProgressbar.setVisibility(View.GONE);
                        callback.success(bookNames, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** fillNotebookSpinner failure");
                        /*
                        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
                        builder.setTitle(R.string.err_setup)
                                .setMessage(R.string.err_setup_msg)
                                .setPositiveButton(R.string.dismiss, null)
                                .show();
                        */
                        //mFragment.mProgressbar.setVisibility(View.GONE);
                        callback.failure(error);
                    }

                    /*
                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }
                    */
                });
    }

    public void fillSectionSpinner(
            SectionsService sectionsService,
            final retrofit.Callback<String[]> callback,
            final Map<String, Section> sectionMap,
            String notebookId) {
        System.out.println("*** fillSectionSpinner");
/*
        sectionsService.getSections(
                getVersion(),
                null,
                null,
                null,
                null,
                null,
                null,
*/
        sectionsService.getNotebookSectionsSP(
                getVersion(),
                mSiteCollectionId,
                mSiteId,
                notebookId,
                null,
                "createdTime desc",
                null,
                null,
                null,
                null,
                new Callback<Envelope<Section>>() {

                    @Override
                    public void success(Envelope<Section> envelope, Response response) {
                        System.out.println("*** fillSectionSpinner success");
                        Section[] sections = envelope.value;
                        String[] sectionNames = new String[sections.length];
                        for (int i = 0; i < sections.length; i++) {
                            sectionNames[i] = sections[i].name;
                            sectionMap.put(sections[i].name, sections[i]);
                            System.out.println("*** Section: " + sectionNames[i]);
                        }
                        //mFragment.mProgressbar.setVisibility(View.GONE);
                        callback.success(sectionNames, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** fillSectionSpinner failure");
                        sectionMap.clear();
                        /*
                        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
                        builder.setTitle(R.string.err_setup)
                                .setMessage(R.string.err_setup_msg)
                                .setPositiveButton(R.string.dismiss, null)
                                .show();
                        */
                        //mFragment.mProgressbar.setVisibility(View.GONE);
                        callback.failure(error);
                    }

                    /*
                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }
                    */
                });
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