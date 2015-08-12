/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/

package com.microsoft.o365_android_onenote_rest.snippet;

import com.google.gson.JsonObject;
import com.microsoft.o365_android_onenote_rest.SnippetDetailFragment;
import com.microsoft.o365_android_onenote_rest.application.SnippetApp;
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
import retrofit.mime.TypedString;

import static com.microsoft.o365_android_onenote_rest.R.array.create_section;
import static com.microsoft.o365_android_onenote_rest.R.array.get_all_sections;
import static com.microsoft.o365_android_onenote_rest.R.array.get_metadata_of_section;
import static com.microsoft.o365_android_onenote_rest.R.array.sections_specific_name;
import static com.microsoft.o365_android_onenote_rest.R.array.sections_specific_notebook;

public abstract class SectionSnippet<Result>
        extends AbstractSnippet<SectionsService, Result> {

    public Map<String, com.microsoft.sharepointvos.Result> siteMap = new HashMap<>();
    public Map<String, Notebook> notebookMap = new HashMap<>();
    public Map<String, Section> sectionMap = new HashMap<>();
    public String mSiteCollectionId = null;
    public String mSiteId = null;

    public SectionSnippet(Integer descriptionArray) {
        super(SnippetCategory.sectionsSnippetCategory, descriptionArray);
    }

    public SectionSnippet(Integer descriptionArray, Input input) {
        super(SnippetCategory.sectionsSnippetCategory, descriptionArray, input);
    }

    static SectionSnippet[] getSectionsServiceSnippets() {
        return new SectionSnippet[]{
                // Marker element
                new SectionSnippet(null) {
                    @Override
                    public void request(SectionsService service, Callback callback) {
                        // not implemented
                    }
                },
                /*
                 * Gets all sections in the selected notebook specified by notebook id
                 */
/*
                new SectionSnippet<String[]>(sections_specific_notebook, Input.Spinner) {

                    Map<String, Notebook> notebookMap = new HashMap<>();

                    @Override
                    public void setUp(Services services, final retrofit.Callback<String[]> callback) {
                        fillNotebookSpinner(services.mNotebooksService, callback, notebookMap);
                    }

                    @Override
                    public void request(SectionsService service, Callback callback) {

                        Notebook notebook = notebookMap.get(callback
                                .getParams()
                                .get(SnippetDetailFragment.ARG_SPINNER_SELECTION).toString());

                        service.getNotebookSections(
                                getVersion(),
                                notebook.id,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                callback
                        );
                    }
                },
*/
                new SectionSnippet<String[]>(sections_specific_notebook, Input.Spinner) {

                    //Map<String, Notebook> notebookMap = new HashMap<>();
                    //Map<String, Section> sectionMap = new HashMap<>();

                    @Override
                    public void setUp(Services services, final retrofit.Callback<String[]> callback) {
                        RestAdapter restAdapter = SnippetApp.getApp().getRestAdapter2();
                        SitesService sitesService = restAdapter.create(SitesService.class);
                        fillSiteSpinner(sitesService, callback, siteMap);
                        //fillNotebookSpinner(services.mNotebooksService, callback, notebookMap);
                    }

/*
                    @Override
                    public void setUp2(Services services, final retrofit.Callback<String[]> callback, final retrofit.Callback<String[]> callback2) {
                        fillNotebookSpinner(services.mNotebooksService, callback, notebookMap);
                        //fillSectionSpinner(services.mSectionsService, callback2, sectionMap);
                    }
*/
                    @Override
                    public void request(SectionsService service, Callback callback) {
                        System.out.println("*** request");

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
                    }
                },

                /*
                 * Gets all of the sections in the user's default notebook
                 * HTTP GET https://www.onenote.com/api/beta/me/notes/sections
                 */
                new SectionSnippet<Envelope<Section>>(get_all_sections) {
                    @Override
                    public void request(SectionsService service, Callback<Envelope<Section>> callback) {
                        service.getSections(
                                getVersion(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                callback);
                    }
                },

                /*
                 * Gets any section whose title matches the given title
                 * HTTP GET https://www.onenote.com/api/beta/me/notes/sections?filter=name+eq+%27{1}%27
                 */
                new SectionSnippet<Envelope<Section>>(sections_specific_name, Input.Text) {

                    @Override
                    public void request(SectionsService service, Callback<Envelope<Section>> callback) {
                        service.getSections(
                                getVersion(),
                                "name eq '" + callback
                                        .getParams()
                                        .get(SnippetDetailFragment.ARG_TEXT_INPUT)
                                        .toString() + "'",
                                null,
                                null,
                                null,
                                null,
                                null,
                                callback);
                    }
                },

                /*
                 * Gets the metadata for a section by section id
                 */
                new SectionSnippet<Envelope<Section>>(get_metadata_of_section, Input.Spinner) {
                    Map<String, Section> sectionMap = new HashMap<>();

                    @Override
                    public void setUp(Services services, final retrofit.Callback<String[]> callback) {
                        // fillSectionSpinner(services.mSectionsService, callback, sectionMap);
                    }

                    @Override
                    public void request(SectionsService service, Callback callback) {

                        Section section = sectionMap.get(callback
                                .getParams()
                                .get(SnippetDetailFragment.ARG_SPINNER_SELECTION));

                        service.getSectionById(
                                getVersion(),
                                section.id,
                                callback);
                    }
                },

                /*
                 * Creates a new section with a title in a notebook specified by id
                 */
                new SectionSnippet<Envelope<Section>>(create_section, Input.Both) {

                    Map<String, Notebook> notebookMap = new HashMap<>();

                    @Override
                    public void setUp(
                            Services services,
                            final retrofit.Callback<String[]> callback) {
                        fillNotebookSpinner(services.mNotebooksService, callback, notebookMap);
                    }

                    //Create the JSON body of a new section request.
                    //The body sets the section name
                    TypedString createNewSection(String sectionName) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("name", sectionName);
                        return new TypedString(jsonObject.toString()) {
                            @Override
                            public String mimeType() {
                                return "application/json";
                            }
                        };
                    }

                    @Override
                    public void request(SectionsService service, Callback callback) {

                        Notebook notebook = notebookMap.get(callback
                                .getParams()
                                .get(SnippetDetailFragment.ARG_SPINNER_SELECTION).toString());

                        service.postSection(
                                getVersion(),
                                "application/json",
                                notebook.id,
                                createNewSection(callback
                                        .getParams()
                                        .get(SnippetDetailFragment.ARG_TEXT_INPUT)
                                        .toString()),
                                callback
                        );
                    }
                }
        };
    }

    @Override
    public abstract void request(SectionsService service, Callback<Result> callback);

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
                        callback.success(siteNames, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** fillSiteSpinner failure");
                    }

                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }
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
                        callback.success(bookNames, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }

                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }
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
                null,
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
                        callback.success(sectionNames, response);

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** fillSectionSpinner failure");
                        sectionMap.clear();
                        //callback.failure(error);
                    }

                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }
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