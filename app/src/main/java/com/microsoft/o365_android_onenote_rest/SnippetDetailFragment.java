/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package com.microsoft.o365_android_onenote_rest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.AuthenticationManagers;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;
import com.microsoft.o365_android_onenote_rest.application.WhiteboardApp;
import com.microsoft.o365_android_onenote_rest.inject.AppModule;
import com.microsoft.o365_android_onenote_rest.snippet.AbstractSnippet;
import com.microsoft.o365_android_onenote_rest.snippet.Snippet;
import com.microsoft.o365_android_onenote_rest.util.SharedPrefsUtil;
import com.microsoft.o365_android_onenote_rest.util.User;
import com.microsoft.onenoteapi.service.OneNotePartsMap;
import com.microsoft.onenoteapi.service.PatchCommand;
import com.microsoft.onenotevos.Envelope;
import com.microsoft.onenotevos.Notebook;
import com.microsoft.onenotevos.Page;
import com.microsoft.onenotevos.Section;
import com.microsoft.onenotevos.SiteMetadata;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import timber.log.Timber;

import static android.R.layout.simple_spinner_dropdown_item;
import static android.R.layout.simple_spinner_item;
import static android.view.View.VISIBLE;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_set_default;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_goto_default;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_pick_photos;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_refresh;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_open_onenote;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_new_section;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_run;
import static com.microsoft.o365_android_onenote_rest.R.id.progressbar;
import static com.microsoft.o365_android_onenote_rest.R.id.spinner0;
import static com.microsoft.o365_android_onenote_rest.R.id.spinner;
import static com.microsoft.o365_android_onenote_rest.R.id.spinner2;

public class SnippetDetailFragment
        extends BaseFragment
        implements
        AuthenticationCallback<AuthenticationResult>, LiveAuthListener, LocationListener {

    public static final String ARG_SPINNER_SELECTION = "SpinnerSelection";
    public static final int UNSET = -1;
    public static final String APP_STORE_URI = "https://play.google.com/store/apps/details?id=com.microsoft.office.onenote";

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PICK_IMAGES = 2;
    String mNotebookId = null;
    String mSectionId = null;
    String mPageId = null;
    String mCurrentPhotoPath = null;
    File mPhotoFile = null;
    String mOneNoteClientUrl = null;
    public static String sSiteName = null;
    public static String sNotebookName = null;
    public static String sSectionName = null;
    public boolean mGotoDefault = false;
    LocationManager mLocationManager = null;
    String mLocationProvider = null;
    String mFinalAddress = null;

    @InjectView(spinner0)
    protected Spinner mSpinner0;

    @InjectView(spinner)
    protected Spinner mSpinner;

    @InjectView(spinner2)
    protected Spinner mSpinner2;

    @InjectView(progressbar)
    protected ProgressBar mProgressbar;

    @InjectView(btn_run)
    protected Button mRunButton;

    @InjectView(btn_new_section)
    protected Button mNewSectionButton;

    @InjectView(btn_open_onenote)
    protected Button mOpenOneNoteButton;

    @InjectView(btn_refresh)
    protected Button mRefreshButton;

    @InjectView(btn_pick_photos)
    protected Button mPickPhotosButton;

    @InjectView(btn_set_default)
    protected Button mSetDefaultButton;

    @InjectView(btn_goto_default)
    protected Button mGotoDefaultButton;

    @Inject
    public AuthenticationManagers mAuthenticationManagers;

    @Inject
    protected LiveAuthClient mLiveAuthClient;

    boolean setupDidRun = false;
    private Snippet mSnippet;

    public SnippetDetailFragment() {
    }

    @OnClick(btn_run)
    public void onRunClicked(Button btn) {
        System.out.println("*** onRunClicked");
        mProgressbar.setVisibility(View.VISIBLE);
        mRunButton.setEnabled(false);
        mPickPhotosButton.setEnabled(false);

        System.out.println("*** Notebook id: " + mNotebookId);
        System.out.println("*** Section id: " + mSectionId);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mPhotoFile = createImageFile();
        } catch (IOException ex) {
            String msg = ex.getMessage();
            System.out.println("*** " + msg);
        }
        if (mPhotoFile != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(mPhotoFile));
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult ******************************");
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            System.out.println("*** CurrentPhotoPath: " + mCurrentPhotoPath);

            DateTime date = DateTime.now();
            String imagePartName = "image1";
            String simpleHtml = getSimplePageContentBody(WhiteboardApp
                            .getApp()
                            .getResources()
                            .openRawResource(R.raw.patch_page_with_image),
                    date.toString(),
                    imagePartName);

            PatchCommand command = new PatchCommand();
            command.mAction = "append";
            command.mTarget = "body";
            command.mPosition = "after";
            command.mContent = simpleHtml;
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(command.serialize(command, null, null));
            Timber.d(jsonArray.toString());
            TypedString typedString = new TypedString(jsonArray.toString()) {
                @Override
                public String mimeType() {
                    return "application/json";
                }
            };

            System.out.println("*** actionString: " + typedString);
            OneNotePartsMap oneNotePartsMap = new OneNotePartsMap("commands", typedString);

            TypedFile typedFile = new TypedFile("image/jpg", mPhotoFile);
            oneNotePartsMap.put(imagePartName, typedFile);

            System.out.println("*** Invoking patchMultiPartPageSP");
            System.out.println("*** pageId: " + mPageId);
            AbstractSnippet.sServices.mPagesService.patchMultiPartPageSP(
                    "",
                    mSnippet.getVersion(),
                    mSnippet.mSiteCollectionId,
                    mSnippet.mSiteId,
                    mPageId,
                    oneNotePartsMap,
                    new retrofit.Callback<Envelope<Page>>() {
                        @Override
                        public void success(Envelope<Page> env, Response response) {
                            System.out.println("*** patchMultiPartPage success");
                            mProgressbar.setVisibility(View.GONE);
                            mRunButton.setEnabled(true);
                            mPickPhotosButton.setEnabled(true);
                            Toast toast = Toast.makeText(getActivity(), R.string.photo_saved, Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            System.out.println("*** patchMultiPartPage failure: " + error);
                            displayThrowable(error);
                            mProgressbar.setVisibility(View.GONE);
                            mRunButton.setEnabled(true);
                            mPickPhotosButton.setEnabled(true);
                        }
                    }
            );
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode != Activity.RESULT_OK) {
            System.out.println("*** Photo cancelled");
            mProgressbar.setVisibility(View.GONE);
            mRunButton.setEnabled(true);
            mPickPhotosButton.setEnabled(true);
        } else if(requestCode == PICK_IMAGES){
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Uri> uriArray = new ArrayList<Uri>();
                if(data.getData() != null){
                    Uri uri = data.getData();
                    uriArray.add(uri);
                    System.out.println("*** Single Uri: " + uri);
                } else {
                    if(data.getClipData() != null){
                        ClipData mClipData=data.getClipData();
                        for(int i = 0; i < mClipData.getItemCount(); i++){
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            uriArray.add(uri);
                            System.out.println("*** Uri: " + uri);
                        }
                    }
                }
                System.out.println("*** Selected Images: " + uriArray.size());

                StringBuffer html = new StringBuffer("<html><body>");
                for(int i = 0; i < uriArray.size(); i++) {
                    String imagePartName = "partName" + i;
                    System.out.println("*** partName: " + imagePartName);
                    html.append("<br><img src=\"name:").append(imagePartName).append("\" alt=\"An image\"/><br>");
                }
                html.append("</body></html>");
                System.out.println("*** html: " + html);

                PatchCommand command = new PatchCommand();
                command.mAction = "append";
                command.mTarget = "body";
                command.mPosition = "after";
                command.mContent = html.toString();
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(command.serialize(command, null, null));
                Timber.d(jsonArray.toString());
                TypedString typedString = new TypedString(jsonArray.toString()) {
                    @Override
                    public String mimeType() {
                        return "application/json";
                    }
                };
                System.out.println("*** actionString: " + typedString);
                OneNotePartsMap oneNotePartsMap = new OneNotePartsMap("commands", typedString);

                int i = 0;
                for(final Uri uri : uriArray) {
                    String imagePartName = "partName" + i;
                    System.out.println("*** partName: " + imagePartName);
                    System.out.println("*** Uri:" + uri.getPath());
                    InputStream is = null;
                    File photoFile = null;
                    try {
                        is = getActivity().getContentResolver().openInputStream(uri);
                        photoFile = createImageFile();
                        FileOutputStream os = new FileOutputStream(photoFile);
                        int read = 0;
                        byte[] bytes = new byte[1024];
                        while ((read = is.read(bytes)) != -1) {
                            os.write(bytes, 0, read);
                        }
                        os.flush();
                    } catch (IOException ex) {
                        System.out.println("*** Error: " + ex);
                    } finally {
                        try {
                            if (is != null)
                                is.close();
                        } catch (IOException ex) {
                            System.out.println("*** Error: " + ex);
                        }
                    }
                    TypedFile typedFile = new TypedFile("image/jpg", photoFile);
                    oneNotePartsMap.put(imagePartName, typedFile);
                    i++;
                }

                System.out.println("*** Invoking patchMultiPartPageSP");
                System.out.println("*** pageId: " + mPageId);
                AbstractSnippet.sServices.mPagesService.patchMultiPartPageSP(
                        "",
                        mSnippet.getVersion(),
                        mSnippet.mSiteCollectionId,
                        mSnippet.mSiteId,
                        mPageId,
                        oneNotePartsMap,
                        new retrofit.Callback<Envelope<Page>>() {
                            @Override
                            public void success(Envelope<Page> env, Response response) {
                                System.out.println("*** patchMultiPartPage success");
                                mProgressbar.setVisibility(View.GONE);
                                mRunButton.setEnabled(true);
                                mPickPhotosButton.setEnabled(true);
                                Toast toast = Toast.makeText(getActivity(), R.string.photo_saved, Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                System.out.println("*** patchMultiPartPage failure: " + error);
                                displayThrowable(error);
                                mProgressbar.setVisibility(View.GONE);
                                mRunButton.setEnabled(true);
                                mPickPhotosButton.setEnabled(true);
                            }
                        }
                );
            } else {
                mProgressbar.setVisibility(View.GONE);
                mRunButton.setEnabled(true);
                mPickPhotosButton.setEnabled(true);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        System.out.println("*** Current photo path: " + mCurrentPhotoPath);
        return image;
    }

    static String getSimplePageContentBody(
            InputStream input, String replacement, String imagePartName) {
        String simpleHtml = "";
        try {
            simpleHtml = IOUtils.toString(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (replacement != null) {
            simpleHtml = simpleHtml.replace("{contentDate}", replacement);
        }
        if (imagePartName != null) {
            simpleHtml = simpleHtml.replace("{partName}", imagePartName);
        }

        return simpleHtml;
    }

    @OnClick(btn_new_section)
    public void onNewSectionClicked(Button btn) {
        String eventTitle = null;
        String eventLocation = null;
        String eventDate = null;
        Uri eventUri = Uri.parse("content://com.android.calendar/events");
        long now = new Date().getTime();
        Cursor cursor = getActivity().getContentResolver().query(eventUri, new String[] { "title",
                        "dtstart", "dtend", "eventLocation" }, "(" + "dtstart" + "<=" + now + " and "
                        + "dtend" + ">=" + now + ")", null, "dtstart DESC");
        try {
            if(cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    eventTitle = cursor.getString(0);
                    eventDate = new SimpleDateFormat("yyyy-MM-dd HH.mm").format(new Date(Long.parseLong(cursor.getString(1))));
                    eventLocation = cursor.getString(3);
                    System.out.println("*** Event: " + eventTitle + " " + eventLocation + " " + eventDate);
                }
            }
        } catch (AssertionError ex) {
            System.out.println("*** Error reading calendar event cursor: " + ex);
        }

        String newSectionName = null;
        if(eventTitle != null && !eventTitle.equals(""))
            newSectionName = eventTitle + " on ";
        else
            newSectionName = getResources().getString(R.string.meeting_on) + " ";
        if(eventDate != null && !eventDate.equals(""))
            newSectionName = newSectionName + eventDate;
        else
            newSectionName = newSectionName + new SimpleDateFormat("yyyy-MM-dd HH.mm").format(new Date());
        if(eventLocation == null || eventLocation.equals(""))
            eventLocation = mFinalAddress;
        if(eventLocation != null) {
            String completeSectionName = newSectionName + " in " + eventLocation;
            if(completeSectionName.length() <= 50)
                newSectionName = completeSectionName;
        }
        if(newSectionName.length() > 50)
            newSectionName = newSectionName.substring(0, 49);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.section_name);

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(newSectionName);
        input.selectAll();
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProgressbar.setVisibility(View.VISIBLE);
                mRunButton.setEnabled(false);
                mPickPhotosButton.setEnabled(false);
                mOpenOneNoteButton.setEnabled(false);

                final String sectionName = input.getText().toString();
                System.out.println("*** New section name: " + sectionName);

                AbstractSnippet.sServices.mSectionsService.postSectionSP(
                        mSnippet.getVersion(),
                        mSnippet.mSiteCollectionId,
                        mSnippet.mSiteId,
                        "application/json",
                        mNotebookId,
                        createNewSection(sectionName),
                        new retrofit.Callback<Envelope>() {
                            @Override
                            public void success(Envelope env, Response response) {
                                System.out.println("*** postSection success");
                                mSectionId = env.id;
                                System.out.println("*** Section ID: " + mSectionId);

                                DateTime date = DateTime.now();
                                String imagePartName = "image1";
                                String simpleHtml = getSimplePageContentBody(WhiteboardApp
                                                .getApp()
                                                .getResources()
                                                .openRawResource(R.raw.simple_page),
                                        date.toString(),
                                        imagePartName);

                                TypedString presentationString = new TypedString(simpleHtml) {
                                    @Override
                                    public String mimeType() {
                                        return "text/html";
                                    }
                                };

                                AbstractSnippet.sServices.mPagesService.postPagesSP(
                                        "text/html; encoding=utf8",
                                        mSnippet.getVersion(),
                                        mSnippet.mSiteCollectionId,
                                        mSnippet.mSiteId,
                                        mSectionId,
                                        presentationString,
                                        new retrofit.Callback<Page>() {
                                            @Override
                                            public void success(Page page, Response response) {
                                                mPageId = page.id;
                                                System.out.println("*** Getting OneNote Client URL");
                                                mOneNoteClientUrl = page.links.oneNoteClientUrl.href;

                                                mRunButton.setEnabled(true);
                                                mPickPhotosButton.setEnabled(true);
                                                mOpenOneNoteButton.setEnabled(true);

                                                System.out.println("*** Fetching sections");
                                                mSnippet.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback3(sectionName), mSnippet.sectionMap, mNotebookId);
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                if (isAdded()) {
                                                    displayThrowable(error);
                                                    mRunButton.setEnabled(true);
                                                    mPickPhotosButton.setEnabled(true);
                                                    mProgressbar.setVisibility(View.GONE);
                                                }
                                            }
                                        }
                                );
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                System.out.println("*** postSection failure");
                                if (isAdded()) {
                                    //displayThrowable(error.getCause());
                                    displayThrowable(error);
                                    mProgressbar.setVisibility(View.GONE);
                                    mRunButton.setEnabled(true);
                                    mPickPhotosButton.setEnabled(true);
                                    mProgressbar.setVisibility(View.GONE);
                                }
                            }
                        }
                );
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

    @OnClick(btn_open_onenote)
    public void onOpenOneNoteClicked(Button btn) {
        System.out.println("*** onOpenOneNoteClicked");
        String androidClientUrl = mOneNoteClientUrl.replaceAll(
                "=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})&",
                "={$1}&");
        System.out.println("*** androidClientUrl: " + androidClientUrl);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(androidClientUrl));
        startActivity(browserIntent);
    }

    @OnClick(btn_refresh)
    public void onRefreshClicked(Button btn) {
        System.out.println("*** onRefreshClicked");
        mProgressbar.setVisibility(View.VISIBLE);

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        sSiteName = preferences.getString(SharedPrefsUtil.PREF_SITE, null);
        sNotebookName = preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null);
        sSectionName = null;
        System.out.println("*** Selected spinners: " + sSiteName + " " + sNotebookName + " " + sSectionName);

        mSpinner0.setVisibility(View.INVISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);
        mSpinner2.setVisibility(View.INVISIBLE);
        mGotoDefault = false;
        mSnippet.setUp(AbstractSnippet.sServices, getSetUpCallback0());
    }

    @OnClick(btn_pick_photos)
    public void onPickPhotosClicked(Button btn) {
        System.out.println("*** onPickPhotosClicked");
        mProgressbar.setVisibility(View.VISIBLE);
        mRunButton.setEnabled(false);
        mPickPhotosButton.setEnabled(false);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES);
    }

    @OnClick(btn_set_default)
    public void onSetDefaultClicked(Button btn) {
        System.out.println("*** onSetDefaultClicked");

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        preferences.edit().putString(SharedPrefsUtil.PREF_DEFAULT_SITE, mSpinner0.getSelectedItem().toString())
                .putString(SharedPrefsUtil.PREF_DEFAULT_NOTEBOOK, mSpinner.getSelectedItem().toString()).apply();

        mGotoDefaultButton.setEnabled(true);

        Toast toast = Toast.makeText(getActivity(), R.string.set_default_msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    @OnClick(btn_goto_default)
    public void onGotoDefaultClicked(Button btn) {
        System.out.println("*** onGotoDefaultClicked");
        mProgressbar.setVisibility(View.VISIBLE);

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        sSiteName = preferences.getString(SharedPrefsUtil.PREF_DEFAULT_SITE, null);
        sNotebookName = preferences.getString(SharedPrefsUtil.PREF_DEFAULT_NOTEBOOK, null);
        sSectionName = null;
        System.out.println("*** Selected spinners: " + sSiteName + " " + sNotebookName + " " + sSectionName);

        mSpinner0.setVisibility(View.INVISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);
        mSpinner2.setVisibility(View.INVISIBLE);
        mGotoDefault = true;
        mSnippet.setUp(AbstractSnippet.sServices, getSetUpCallback0());
    }

    @OnItemSelected(spinner0)
    public void onSpinner0ItemSelected(Spinner theSpinner) {
        System.out.println("*** Spinner0 selected: " + theSpinner.getSelectedItem().toString());
        mProgressbar.setVisibility(View.VISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);
        mSpinner2.setVisibility(View.INVISIBLE);
        mPageId = null;
        mNewSectionButton.setEnabled(false);
        mRunButton.setEnabled(false);
        mPickPhotosButton.setEnabled(false);
        mOpenOneNoteButton.setEnabled(false);

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        if((sSiteName == null) || (preferences.getString(SharedPrefsUtil.PREF_SITE, null) == null)) {
            preferences.edit().putString(SharedPrefsUtil.PREF_SITE, theSpinner.getSelectedItem().toString())
                    .putString(SharedPrefsUtil.PREF_NOTEBOOK, null)
                    .putString(SharedPrefsUtil.PREF_SECTION, null).commit();
        }

        com.microsoft.sharepointvos.Result result = (com.microsoft.sharepointvos.Result) mSnippet.siteMap.get(
                theSpinner.getSelectedItem().toString());
        System.out.println("*** Site URI: " + result.getUri());
        String siteUri = result.getUri().toString();

        System.out.println("*** Async invocation of SiteMetadataService");
        AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadata(
                mSnippet.getVersion(),
                siteUri,
                new retrofit.Callback<SiteMetadata>() {
                    @Override
                    public void success(SiteMetadata siteMetadata, Response response) {
                        System.out.println("*** Received site metadata");
                        System.out.println("*** Site Collection ID and Site ID: " + siteMetadata.siteCollectionId + " " + siteMetadata.siteId);
                        mSnippet.mSiteCollectionId = siteMetadata.siteCollectionId;
                        mSnippet.mSiteId = siteMetadata.siteId;
                        mSnippet.fillNotebookSpinner(AbstractSnippet.sServices.mNotebooksService, getSetUpCallback(), mSnippet.notebookMap);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** Failure receiving site metadata: " + error);
                        mProgressbar.setVisibility(View.GONE);
                        displayThrowable(error);
                    }
                }
        );
    }

    @OnItemSelected(spinner)
    public void onSpinnerItemSelected(Spinner theSpinner) {
        System.out.println("*** Spinner selected: " + theSpinner.getSelectedItem().toString());
        mProgressbar.setVisibility(View.VISIBLE);
        mSpinner2.setVisibility(View.INVISIBLE);
        mPageId = null;
        mRunButton.setEnabled(false);
        mPickPhotosButton.setEnabled(false);
        mOpenOneNoteButton.setEnabled(false);

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        if((sNotebookName == null) || (preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null) == null)) {
            preferences.edit().putString(SharedPrefsUtil.PREF_NOTEBOOK, theSpinner.getSelectedItem().toString())
                    .putString(SharedPrefsUtil.PREF_SECTION, null).commit();
        }

        Notebook notebook = (Notebook) mSnippet.notebookMap.get(theSpinner.getSelectedItem().toString());
        System.out.println("*** Notebook id: " + notebook.id);
        mNotebookId = notebook.id;

        mSnippet.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback2(), mSnippet.sectionMap, notebook.id);

        mSetDefaultButton.setEnabled(true);
        mNewSectionButton.setEnabled(true);
    }

    @OnItemSelected(spinner2)
    public void onSpinner2ItemSelected(Spinner theSpinner) {
        System.out.println("*** Spinner2 selected: " + mSpinner2.getSelectedItem().toString());
        mPageId = null;
        mProgressbar.setVisibility(View.VISIBLE);
        mRunButton.setEnabled(false);
        mPickPhotosButton.setEnabled(false);
        mOpenOneNoteButton.setEnabled(false);

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        if((sSectionName == null) || (preferences.getString(SharedPrefsUtil.PREF_SECTION, null) == null)) {
            preferences.edit().putString(SharedPrefsUtil.PREF_SECTION, theSpinner.getSelectedItem().toString()).commit();
        }

        Section section = (Section) mSnippet.sectionMap.get(mSpinner2.getSelectedItem().toString());
        System.out.println("*** Section id: " + section.id);
        mSectionId = section.id;

        AbstractSnippet.sServices.mPagesService.getSectionPagesSP(
                mSnippet.getVersion(),
                mSnippet.mSiteCollectionId,
                mSnippet.mSiteId,
                mSectionId,
                "createdTime asc",
                null,
                null,
                null,
                null,
                new retrofit.Callback<Envelope<Page>>() {
                    @Override
                    public void success(Envelope<Page> env, Response response) {
                        if (env.value.length > 0) {
                            mPageId = env.value[0].id;
                            mOneNoteClientUrl = env.value[0].links.oneNoteClientUrl.href;
                            mRunButton.setEnabled(true);
                            mPickPhotosButton.setEnabled(true);
                            mOpenOneNoteButton.setEnabled(true);
                            mProgressbar.setVisibility(View.GONE);
                        } else {
                            mProgressbar.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(getActivity(), R.string.section_without_page_msg, Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isAdded()) {
                            mProgressbar.setVisibility(View.GONE);
                            displayThrowable(error);
                        }
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("*** onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        sSiteName = preferences.getString(SharedPrefsUtil.PREF_SITE, null);
        sNotebookName = preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null);
        sSectionName = preferences.getString(SharedPrefsUtil.PREF_SECTION, null);
        System.out.println("*** Site: " + sSiteName);
        System.out.println("*** Notebook: " + sNotebookName);
        System.out.println("*** Section: " + sSectionName);

        mSnippet = new Snippet();

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        mLocationProvider = mLocationManager.getBestProvider(criteria, true);
        System.out.println("*** Location provider: " + mLocationProvider);
        Location location = null;
        if(mLocationProvider != null) {
            location = mLocationManager.getLastKnownLocation(mLocationProvider);
        }
        if (location != null) {
            onLocationChanged(location);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_snippet_detail, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        System.out.println("*** onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        System.out.println("*** onResume");
        super.onResume();
        mLocationManager.requestLocationUpdates(mLocationProvider, 60000, 100.0f, this);

        if (User.isOrg()) {
            mAuthenticationManagers.mAuthenticationManager1.connect(this);
            mAuthenticationManagers.mAuthenticationManager2.connect(new AuthenticationCallback<AuthenticationResult>() {
                @Override
                public void onSuccess(AuthenticationResult authenticationResult) {
                    SharedPrefsUtil.persistAuthToken2(authenticationResult);
                    ready();
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("*** onError: " + e);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);

            String city = address.get(0).getLocality();
            if (city != null && !city.equals("")) {
                System.out.println("*** City: " + city);
                mFinalAddress = city;
            }
        } catch (IOException e) {
            mFinalAddress = null;
        }
        catch (NullPointerException e) {
            mFinalAddress = null;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.snippet_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.disconnect == item.getItemId()) {
            ((SnippetDetailActivity)getActivity()).onDisconnectClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private retrofit.Callback<String[]> getSetUpCallback0() {
        return new retrofit.Callback<String[]>() {
            @Override
            public void success(String[] strings, Response response) {
                System.out.println("*** Callback0 success");
                mProgressbar.setVisibility(View.GONE);
                if (isAdded() && (null == response || strings.length > 0)) {
                    mNewSectionButton.setEnabled(false);
                    if (strings.length > 0) {
                        populateSpinner0(strings);
                        mSpinner0.setVisibility(VISIBLE);

                        if(sSiteName != null) {
                            int pos = ((ArrayAdapter) mSpinner0.getAdapter()).getPosition(sSiteName);
                            if(pos != -1) mSpinner0.setSelection(pos, true);
                            sSiteName = null;
                        }
                    }
                } else if (isAdded() && strings.length <= 0 && null != response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.err_setup)
                            .setMessage(R.string.err_setup_msg)
                            .setPositiveButton(R.string.dismiss, null)
                            .show();
                }
                sSiteName = null;
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    displayThrowable(error);
                    mProgressbar.setVisibility(View.GONE);
                }
                sSiteName = null;
            }
        };
    }

    private retrofit.Callback<String[]> getSetUpCallback() {
        return new retrofit.Callback<String[]>() {
            @Override
            public void success(String[] strings, Response response) {
                System.out.println("*** Callback1 success");
                mProgressbar.setVisibility(View.GONE);
                if (isAdded() && (null == response || strings.length > 0)) {
                    mNewSectionButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner(strings);
                        mSpinner.setVisibility(VISIBLE);

                        if(sNotebookName != null) {
                            int pos = ((ArrayAdapter) mSpinner.getAdapter()).getPosition(sNotebookName);
                            if(pos != -1) mSpinner.setSelection(pos, true);
                            sNotebookName = null;
                        }
                    }
                } else if (isAdded() && strings.length <= 0 && null != response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.err_setup)
                            .setMessage(R.string.err_setup_msg)
                            .setPositiveButton(R.string.dismiss, null)
                            .show();
                }
                sNotebookName = null;
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    displayThrowable(error);
                    mProgressbar.setVisibility(View.GONE);
                }
                sNotebookName = null;
            }
        };
    }

    private retrofit.Callback<String[]> getSetUpCallback2() {
        return new retrofit.Callback<String[]>() {
            @Override
            public void success(String[] strings, Response response) {
                System.out.println("*** Callback2 success");
                mProgressbar.setVisibility(View.GONE);
                if (isAdded() && (null == response || strings.length > 0)) {
                    mRunButton.setEnabled(true);
                    mPickPhotosButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner2(strings);
                        mSpinner2.setVisibility(VISIBLE);

                        if(sSectionName != null) {
                            int pos = ((ArrayAdapter) mSpinner2.getAdapter()).getPosition(sSectionName);
                            if(pos != -1) mSpinner2.setSelection(pos, true);
                            sSectionName = null;
                        } else if(mGotoDefault) {
                            onNewSectionClicked(mNewSectionButton);
                            mGotoDefault = false;
                        }
                    }
                }
                sSectionName = null;
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    displayThrowable(error);
                    mProgressbar.setVisibility(View.GONE);
                    mGotoDefault = false;
                }
                sSectionName = null;
            }
        };
    }

    // select new section
    private retrofit.Callback<String[]> getSetUpCallback3(final String sectionName) {
        return new retrofit.Callback<String[]>() {
            @Override
            public void success(String[] strings, Response response) {
                System.out.println("*** Callback3 success");
                if (isAdded() && (null == response || strings.length > 0)) {
                    mRunButton.setEnabled(true);
                    mPickPhotosButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner2(strings);
                        mSpinner2.post(new Runnable() {
                            @Override
                            public void run() {
                                mSpinner2.setSelection(((ArrayAdapter) mSpinner2.getAdapter()).getPosition(sectionName), true);
                            }
                        });
                        mProgressbar.setVisibility(View.GONE);
                        mSpinner2.setVisibility(VISIBLE);
                        Toast toast = Toast.makeText(getActivity(), R.string.section_created_msg, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("*** Callback3 failure");
                if (isAdded()) {
                    displayThrowable(error);
                    mProgressbar.setVisibility(View.GONE);
                }
            }
        };
    }


    private void populateSpinner0(String[] strings) {
        ArrayAdapter<String> spinnerArrayAdapter
                = new ArrayAdapter<>(
                getActivity(),
                simple_spinner_item,
                strings);
        spinnerArrayAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
        mSpinner0.setAdapter(spinnerArrayAdapter);
    }

    private void populateSpinner(String[] strings) {
        ArrayAdapter<String> spinnerArrayAdapter
                = new ArrayAdapter<>(
                getActivity(),
                simple_spinner_item,
                strings);
        spinnerArrayAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void populateSpinner2(String[] strings) {
        ArrayAdapter<String> spinnerArrayAdapter
                = new ArrayAdapter<>(
                getActivity(),
                simple_spinner_item,
                strings);
        spinnerArrayAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
        mSpinner2.setAdapter(spinnerArrayAdapter);
    }

    private void displayThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.err_setup)
                .setMessage(t.getMessage())
                .setPositiveButton(R.string.dismiss, null)
                .show();
    }

    @Override
    public void onSuccess(AuthenticationResult authenticationResult) {
        SharedPrefsUtil.persistAuthToken(authenticationResult);
        ready();
    }

    private void ready() {
        if (!setupDidRun) {
            setupDidRun = true;
            mProgressbar.setVisibility(View.VISIBLE);
            mSpinner0.setVisibility(View.INVISIBLE);
            mSpinner.setVisibility(View.INVISIBLE);
            mSpinner2.setVisibility(View.INVISIBLE);
            mSnippet.setUp(AbstractSnippet.sServices, getSetUpCallback0());

            SharedPreferences preferences
                    = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
            mGotoDefaultButton.setEnabled(preferences.getString(SharedPrefsUtil.PREF_DEFAULT_SITE, null) != null &&
                    preferences.getString(SharedPrefsUtil.PREF_DEFAULT_NOTEBOOK, null) != null);
        }
    }

    @Override
    public void onError(Exception e) {
        if (!isAdded()) {
            return;
        }
        displayThrowable(e);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.token_err_title)
                .setMessage(R.string.token_err_msg)
                .setPositiveButton(R.string.dismiss, null)
                .setNegativeButton(R.string.disconnect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuthenticationManagers.mAuthenticationManager1.disconnect();
                        mAuthenticationManagers.mAuthenticationManager2.disconnect();
                    }
                }).show();
    }

    @Override
    public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
        if (null != session) {
            SharedPrefsUtil.persistAuthToken(session);
        }
        ready();
    }

    @Override
    public void onAuthError(LiveAuthException exception, Object userState) {
        onError(exception);
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
