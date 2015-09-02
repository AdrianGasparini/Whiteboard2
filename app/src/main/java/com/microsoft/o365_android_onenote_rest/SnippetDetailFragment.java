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
//import com.microsoft.o365_android_onenote_rest.snippet.Callback;
//import com.microsoft.o365_android_onenote_rest.snippet.Input;
//import com.microsoft.o365_android_onenote_rest.snippet.SectionSnippet;
import com.microsoft.o365_android_onenote_rest.snippet.Snippet;
//import com.microsoft.o365_android_onenote_rest.snippet.SnippetContent;
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
//import static com.microsoft.o365_android_onenote_rest.R.id.btn_launch_browser;
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
//import static com.microsoft.o365_android_onenote_rest.R.id.txt_desc;
//import static com.microsoft.o365_android_onenote_rest.R.id.txt_hyperlink;
//import static com.microsoft.o365_android_onenote_rest.R.id.txt_input;
/*
import static com.microsoft.o365_android_onenote_rest.R.id.txt_request_url;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_response_body;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_response_headers;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_status_code;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_status_color;
*/
/*
import static com.microsoft.o365_android_onenote_rest.R.string.clippy;
import static com.microsoft.o365_android_onenote_rest.R.string.req_url;
import static com.microsoft.o365_android_onenote_rest.R.string.response_body;
import static com.microsoft.o365_android_onenote_rest.R.string.response_headers;
*/

public class SnippetDetailFragment/*<T, Result>*/
        extends BaseFragment
        implements //Callback<Result>,
        AuthenticationCallback<AuthenticationResult>, LiveAuthListener, LocationListener {

    //public static final String ARG_ITEM_ID = "item_id";
    //public static final String ARG_TEXT_INPUT = "TextInput";
    public static final String ARG_SPINNER_SELECTION = "SpinnerSelection";
    //public static final String ARG_SPINNER2_SELECTION = "Spinner2Selection";
    public static final int UNSET = -1;
    public static final String APP_STORE_URI = "https://play.google.com/store/apps/details?id=com.microsoft.office.onenote";

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PICK_IMAGES = 2;
    String mNotebookId = null;
    String mSectionId = null;
    String mPageId = null;
    String mCurrentPhotoPath = null;
    File mPhotoFile = null;
    //Activity mActivity;
    //Callback mCallback = this;
    //String mSectionName = null;
    String mOneNoteClientUrl = null;
    //String mSiteCollectionId = null;
    //String mSiteId = null;
    public static String sSiteName = null;
    public static String sNotebookName = null;
    public static String sSectionName = null;
    public boolean mGotoDefault = false;
    LocationManager mLocationManager = null;
    String mLocationProvider = null;
    String mFinalAddress = null;
    //boolean mInitializingSpinner0 = true;
    //boolean mInitializingSpinner1 = true;
    //boolean mInitializingSpinner2 = true;

/*
    @InjectView(txt_status_code)
    protected TextView mStatusCode;

    @InjectView(txt_status_color)
    protected View mStatusColor;
*/
/*
    @InjectView(txt_desc)
    protected TextView mSnippetDescription;
*/
/*
    @InjectView(txt_request_url)
    protected TextView mRequestUrl;

    @InjectView(txt_response_headers)
    protected TextView mResponseHeaders;

    @InjectView(txt_response_body)
    protected TextView mResponseBody;
*/

    @InjectView(spinner0)
    protected Spinner mSpinner0;

    @InjectView(spinner)
    protected Spinner mSpinner;

    @InjectView(spinner2)
    protected Spinner mSpinner2;
/*
    @InjectView(txt_input)
    protected EditText mEditText;
*/

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

    //@Inject
    //protected AuthenticationManager mAuthenticationManager;

    @Inject
    protected LiveAuthClient mLiveAuthClient;

    boolean setupDidRun = false;
    private Snippet/*<T, Result>*/ mSnippet;

    //private AbstractSnippet<T, Result> mItem2;

    public SnippetDetailFragment() {
    }
/*
    public void setActivity(Activity activity) {
        mActivity = activity;
        //SectionSnippet.sActivity = activity;
        //SectionSnippet.mFragment = this;
    }
*/
/*
    @OnClick(txt_request_url)
    public void onRequestUrlClicked(TextView tv) {
        clipboard(tv);
    }

    @OnClick(txt_response_headers)
    public void onResponseHeadersClicked(TextView tv) {
        clipboard(tv);
    }

    @OnClick(txt_response_body)
    public void onResponseBodyClicked(TextView tv) {
        clipboard(tv);
    }

    @InjectView(btn_launch_browser)
    protected Button mLaunchBrowser;
*/
/*
    private void clipboard(TextView tv) {
        int which;
        switch (tv.getId()) {
            case txt_request_url:
                which = req_url;
                break;
            case txt_response_headers:
                which = response_headers;
                break;
            case txt_response_body:
                which = response_body;
                break;
            default:
                which = UNSET;
        }
        String what = which == UNSET ? "" : getString(which) + " ";
        what += getString(clippy);
        Toast.makeText(getActivity(), what, Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // old way
            ClipboardManager clipboardManager = (ClipboardManager)
                    getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(tv.getText());
        } else {
            clipboard11(tv);
        }
    }

    @TargetApi(11)
    private void clipboard11(TextView tv) {
        android.content.ClipboardManager clipboardManager =
                (android.content.ClipboardManager) getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("OneNote", tv.getText());
        clipboardManager.setPrimaryClip(clipData);
    }
*/

    @OnClick(btn_run)
    public void onRunClicked(Button btn) {
        System.out.println("*** onRunClicked");
        mProgressbar.setVisibility(View.VISIBLE);
        mRunButton.setEnabled(false);
        mPickPhotosButton.setEnabled(false);
        //mOpenOneNoteButton.setEnabled(false);

        System.out.println("*** Notebook id: " + mNotebookId);
        // TODO: uncomment if section id should not be read on spinner selection
        /*
        System.out.println("*** Section: " + mSpinner2.getSelectedItem().toString());
        SectionSnippet item = (SectionSnippet)mSnippet;
        Section section = (Section) item.sectionMap.get(mSpinner2.getSelectedItem().toString());
        System.out.println("*** Section id: " + section.id);
        mSectionId = section.id;
        */
        System.out.println("*** Section id: " + mSectionId);

        //File photoFile = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        //if (takePictureIntent.resolveActivity(new Activity().getPackageManager()) != null) {
            // Create the File where the photo should go
            //File photoFile = null;
            try {
                mPhotoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                String msg = ex.getMessage();
                System.out.println("*** " + msg);
            }
            // Continue only if the File was successfully created
            if (mPhotoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mPhotoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        //}
/*
        System.out.println("*** CurrentPhotoPath: " + mCurrentPhotoPath);

        System.out.println("*** Notebook id: " + mNotebookId);
        System.out.println("*** Section: " + mSpinner2.getSelectedItem().toString());
        SectionSnippet item = (SectionSnippet)mSnippet;
        Section section = (Section) item.sectionMap.get(mSpinner2.getSelectedItem().toString());
        System.out.println("*** Section id: " + section.id);

        DateTime date = DateTime.now();
        String imagePartName = "image1";
        String simpleHtml = getSimplePageContentBody(WhiteboardApp
                        .getApp()
                        .getResources()
                        .openRawResource(R.raw.create_page_with_image),
                date.toString(),
                imagePartName);

        TypedString presentationString = new TypedString(simpleHtml) {
            @Override
            public String mimeType() {
                return "text/html";
            }
        };
        OneNotePartsMap oneNotePartsMap = new OneNotePartsMap(presentationString);
        //File imageFile = getImageFile("/res/drawable/logo.jpg");

        //TypedFile typedFile = new TypedFile("image/jpg", imageFile);
        TypedFile typedFile = new TypedFile("image/jpg", photoFile);
        oneNotePartsMap.put(imagePartName, typedFile);

        AbstractSnippet.sServices.mPagesService.postMultiPartPages(
                mSnippet.getVersion(),
                section.id,
                oneNotePartsMap,
                (Callback<Envelope<Page>>)this);
*/
/*
        mRequestUrl.setText("");
        mResponseHeaders.setText("");
        mResponseBody.setText("");
        displayStatusCode("", getResources().getColor(R.color.transparent));
        mProgressbar.setVisibility(VISIBLE);
        mSnippet.request(mSnippet.mService, this);
*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult ******************************");
/*
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView mImageView = new ImageView(getApplicationContext());
            mImageView.setImageBitmap(imageBitmap);
        }
        else
            System.out.println("******************************");
*/
/*
        File f = new File("file:" + mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        System.out.println("*** contentUri: " + contentUri);

        StartRequest();
        //SendPageCreateAsyncTask task = new SendPageCreateAsyncTask(contentUri.toString());
        SendPageCreateAsyncTask task = new SendPageCreateAsyncTask(mCurrentPhotoPath);
        task.delegate = this;
        task.execute(mAccessToken);
*/
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            System.out.println("*** CurrentPhotoPath: " + mCurrentPhotoPath);
/*
            System.out.println("*** Notebook id: " + mNotebookId);
            System.out.println("*** Section: " + mSpinner2.getSelectedItem().toString());
            SectionSnippet item = (SectionSnippet)mSnippet;
            Section section = (Section) item.sectionMap.get(mSpinner2.getSelectedItem().toString());
            System.out.println("*** Section id: " + section.id);
*/
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
            command.mContent = simpleHtml;//"<p>New trailing content</p>";
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
            //String pageId = env.id;
            System.out.println("*** pageId: " + mPageId);
            //Snippet item = (Snippet) mSnippet;
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
                            //mOpenOneNoteButton.setEnabled(true);
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

            /*
            DateTime date = DateTime.now();
            String imagePartName = "image1";
            String simpleHtml = getSimplePageContentBody(WhiteboardApp
                            .getApp()
                            .getResources()
                            .openRawResource(R.raw.create_page_with_image),
                    date.toString(),
                    imagePartName);

            TypedString presentationString = new TypedString(simpleHtml) {
                @Override
                public String mimeType() {
                    return "text/html";
                }
            };
            OneNotePartsMap oneNotePartsMap = new OneNotePartsMap(presentationString);
            //File imageFile = getImageFile("/res/drawable/logo.jpg");

            //TypedFile typedFile = new TypedFile("image/jpg", imageFile);
            TypedFile typedFile = new TypedFile("image/jpg", mPhotoFile);
            oneNotePartsMap.put(imagePartName, typedFile);

            Snippet item = (Snippet)mSnippet;
            AbstractSnippet.sServices.mPagesService.postMultiPartPagesSP(
                    mSnippet.getVersion(),
                    item.mSiteCollectionId,
                    item.mSiteId,
                    mSectionId,
                    oneNotePartsMap,
                    //(Callback<Envelope<Page>>)this
                    new retrofit.Callback<Envelope<Page>>() {
                        @Override
                        public void success(Envelope<Page> env, Response response) {
                            mProgressbar.setVisibility(View.GONE);
                            //if (isAdded() && (null == response)) {
                            System.out.println("*** Getting OneNote Client URL");
                            mOneNoteClientUrl = env.links.oneNoteClientUrl.href;
                            mRunButton.setEnabled(true);
                            mOpenOneNoteButton.setEnabled(true);
                            Toast toast = Toast.makeText(mActivity, R.string.photo_saved, Toast.LENGTH_SHORT);
                            toast.show();

                            // ------------
if(true) {

    DateTime date = DateTime.now();
    String imagePartName = "image1";
    String simpleHtml = getSimplePageContentBody(WhiteboardApp
                    .getApp()
                    .getResources()
                    .openRawResource(R.raw.create_page_with_image),
            date.toString(),
            imagePartName);

    PatchCommand command = new PatchCommand();
    command.mAction = "append";
    command.mTarget = "body";
    command.mPosition = "after";
    command.mContent = simpleHtml;//"<p>New trailing content</p>";
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
    String pageId = env.id;
    System.out.println("*** pageId: " + pageId);
    SectionSnippet item = (SectionSnippet) mSnippet;
    AbstractSnippet.sServices.mPagesService.patchMultiPartPageSP(
            "",
            mSnippet.getVersion(),
            item.mSiteCollectionId,
            item.mSiteId,
            pageId,
            oneNotePartsMap,
            new retrofit.Callback<Envelope<Page>>() {
                @Override
                public void success(Envelope<Page> env, Response response) {
                    System.out.println("*** patchMultiPartPage success");
                }

                @Override
                public void failure(RetrofitError error) {
                    System.out.println("*** patchMultiPartPage failure: " + error);
                }
            }
    );
}
                            // ------------

                            //}
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (isAdded()) {
                                //displayThrowable(error.getCause());
                                displayThrowable(error);
                                mRunButton.setEnabled(true);
                                mProgressbar.setVisibility(View.GONE);
                            }
                        }
                    }
            );
    */
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode != Activity.RESULT_OK) {
            System.out.println("*** Photo cancelled");
            mProgressbar.setVisibility(View.GONE);
            mRunButton.setEnabled(true);
            mPickPhotosButton.setEnabled(true);
            //mOpenOneNoteButton.setEnabled(mOneNoteClientUrl != null);
        } else if(requestCode == PICK_IMAGES){
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Uri> uriArray = new ArrayList<Uri>();
                //data.getParcelableArrayExtra(name);
                //If Single image selected then it will fetch from Gallery
                if(data.getData() != null){
                    Uri uri = data.getData();
                    uriArray.add(uri);
                    System.out.println("*** Single Uri: " + uri);
                } else {
                    if(data.getClipData() != null){
                        ClipData mClipData=data.getClipData();
                        //ArrayList<Uri> mArrayUri=new ArrayList<Uri>();
                        for(int i = 0; i < mClipData.getItemCount(); i++){
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            uriArray.add(uri);
                            System.out.println("*** Uri: " + uri);
                        }
                        //System.out.println("*** Selected Images: " + uriArray.size());
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
                    /*
                    InputStream is0 = null;
                    int available0 = 0;
                    try {
                        is0 = mActivity.getContentResolver().openInputStream(uri);
                        available0 = is0.available();
                        System.out.println("*** available: " + is0.available());
                    } catch (IOException ex) {
                        System.out.println("*** Error: " + ex);
                    }
                    final InputStream is = is0;
                    final int available = available0;
                    */
                    //String path = getRealPathFromURI(uri);
                    //System.out.println("*** path:" + path);
                    //File photoFile = new File(path);
                    //InputStream is = mActivity.getContentResolver().openInputStream(uri);
                    //File photoFile = new File();
                    //TypedFile typedFile = new TypedFile("image/jpg", photoFile);
                    //oneNotePartsMap.put(imagePartName, typedFile)
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
                    /*
                    TypedInput typedInput = new TypedInput() {
                        @Override
                        public String mimeType() {
                            return "image/jpg";
                        }

                        @Override
                        public long length() {
                            return -1;
                        }

                        @Override
                        public InputStream in() throws IOException {
                            return mActivity.getContentResolver().openInputStream(uri);
                        }
                    };
                    oneNotePartsMap.put(imagePartName, typedInput);
                    */
                    i++;
                }

                System.out.println("*** Invoking patchMultiPartPageSP");
                System.out.println("*** pageId: " + mPageId);
                //Snippet item = (Snippet) mSnippet;
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
                                //mOpenOneNoteButton.setEnabled(true);
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

    /*
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    */
    /*
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mActivity, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
    */
    /*
    private String getPath(Uri uri) {
        String[]  data = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mActivity, uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    */

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

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
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
/*
    @Override
    public abstract void request(PagesService service, Callback<Result> callback);
*/
    /*
     * @param imagePath The path to the image
     * @return File. the image to attach to a OneNote page
     */
/*
    protected File getImageFile(String imagePath) {
        URL imageResource = getClass().getResource(imagePath);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(
                    FilenameUtils.getBaseName(imageResource.getFile()),
                    FilenameUtils.getExtension(imageResource.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            IOUtils.copy(imageResource.openStream(),
                    FileUtils.openOutputStream(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }
*/

    @OnClick(btn_new_section)
    public void onNewSectionClicked(Button btn) {
        //String finalAddress = null;
        /*
        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        */
        //Location location = mLocationManager.getLastKnownLocation(mLocationProvider);
        /*
        if (mLocation != null) {
            System.out.println("*** Provider " + mLocationProvider + " has been selected.");
            double lat = mLocation.getLatitude();
            double lng = mLocation.getLongitude();
            Geocoder geoCoder = new Geocoder(mActivity, Locale.getDefault());
            //StringBuilder stringBuilder = new StringBuilder();
            try {
                List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
                int maxLines = address.get(0).getMaxAddressLineIndex();
                finalAddress = address.get(0).getAddressLine(maxLines-1);
                System.out.println("*** Latitude: " + String.valueOf(lat));
                System.out.println("*** Longitude: " + String.valueOf(lng));
                System.out.println("*** Address: " + finalAddress);
            } catch (IOException e) {}
            catch (NullPointerException e) {}
        } else {
            System.out.println("*** Location not available");
        }
        */

        /*
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, new Date().getTime());
        ContentUris.appendId(eventsUriBuilder, new Date().getTime());
        //ContentUris.appendId(eventsUriBuilder, endOfToday);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = null;
        cursor = mActivity.getContentResolver().query(eventsUri, new String[] { "calendar_id", "title", "description",
                "dtstart", "dtend", "eventLocation" }, null, null, CalendarContract.Instances.DTSTART + " ASC");
        */
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
                    /*
                    String name = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    // This is actually a better pattern:
                    String color = cursor.getString(cursor.getColumnIndex(
                            CalendarContract.Calendars.CALENDAR_COLOR));
                    Boolean selected = !cursor.getString(3).equals("0");
                    */
                }
            }
        } catch (AssertionError ex) {
            // TODO: log exception and bail
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
        input.setInputType(InputType.TYPE_CLASS_TEXT/* | InputType.TYPE_TEXT_VARIATION_PASSWORD*/);
        //input.setText(getResources().getString(R.string.meeting_on) + new SimpleDateFormat(" yyyy-MM-dd HH.mm").format(new Date()) + finalAddress);
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

                /*final String*/
                final String sectionName = input.getText().toString();
                System.out.println("*** New section name: " + sectionName);

                //Snippet item = (Snippet) mSnippet;
                AbstractSnippet.sServices.mSectionsService.postSectionSP(
                        mSnippet.getVersion(),
                        mSnippet.mSiteCollectionId,
                        mSnippet.mSiteId,
                        "application/json",
                        mNotebookId,
                        createNewSection(sectionName),
                        //mCallback
                        new retrofit.Callback<Envelope>() {
                            @Override
                            public void success(Envelope env, Response response) {
                                System.out.println("*** postSection success");
                                // /**/mProgressbar.setVisibility(View.GONE);
                                //if (isAdded() && (null == response /*|| strings.length > 0*/)) {
                                mSectionId = env.id;
                                System.out.println("*** Section ID: " + mSectionId);
/*
                                System.out.println("*** Fetching sections");
                                SectionSnippet item = (SectionSnippet) mSnippet;
                                item.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback3(), item.sectionMap, mNotebookId);
*/
                                //item.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback2(), item.sectionMap, mNotebookId);
                                //mSpinner2.setVisibility(VISIBLE);

                                // ********************
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

                                Snippet item = (Snippet) mSnippet;
                                AbstractSnippet.sServices.mPagesService.postPagesSP(
                                        "text/html; encoding=utf8",
                                        mSnippet.getVersion(),
                                        item.mSiteCollectionId,
                                        item.mSiteId,
                                        mSectionId,
                                        presentationString,
                                        //(Callback<Envelope<Page>>)this
                                        new retrofit.Callback<Page>() {
                                            @Override
                                            public void success(Page page, Response response) {
                                                //mProgressbar.setVisibility(View.GONE);
                                                mPageId = page.id;
                                                //if (isAdded() && (null == response /*|| strings.length > 0*/)) {
                                                System.out.println("*** Getting OneNote Client URL");
                                                mOneNoteClientUrl = page.links.oneNoteClientUrl.href;

                                                mRunButton.setEnabled(true);
                                                mPickPhotosButton.setEnabled(true);
                                                mOpenOneNoteButton.setEnabled(true);

                                                System.out.println("*** Fetching sections");
                                                //Snippet item = (Snippet) mSnippet;
                                                mSnippet.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback3(sectionName), mSnippet.sectionMap, mNotebookId);

                                                //Toast toast = Toast.makeText(mActivity, R.string.section_created_msg, Toast.LENGTH_SHORT);
                                                //toast.show();

                                                //}
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                if (isAdded()) {
                                                    //displayThrowable(error.getCause());
                                                    displayThrowable(error);
                                                    mRunButton.setEnabled(true);
                                                    mPickPhotosButton.setEnabled(true);
                                                    mProgressbar.setVisibility(View.GONE);
                                                }
                                            }
                                        }
                                );

                                // ********************
/*
                                //mSpinner2.setSelection(((ArrayAdapter)mSpinner2.getAdapter()).getPosition(sectionName), true);
                                mSpinner2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSpinner2.setSelection(((ArrayAdapter) mSpinner2.getAdapter()).getPosition(mSectionName), true);
                                    }
                                });
*/
                                //}
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
/*
                System.out.println("Fetching sections");
                Snippet item = (SectionSnippet)mSnippet;
                item.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback2(), item.sectionMap, mNotebookId);
*/
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

        /*
        sSiteName = sNotebookName = sSectionName = null;
        Object selectedSite = mSpinner0.getSelectedItem();
        if(selectedSite != null) {
            sSiteName = selectedSite.toString();
            Object selectedNotebook = mSpinner.getSelectedItem();
            if(selectedNotebook != null) {
                sNotebookName = selectedNotebook.toString();
                Object selectedSection = mSpinner2.getSelectedItem();
                if(selectedSection != null)
                    sSectionName = selectedSection.toString();
            }
        }
        */
        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        sSiteName = preferences.getString(SharedPrefsUtil.PREF_SITE, null);
        sNotebookName = preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null);
        //sSectionName = preferences.getString(SharedPrefsUtil.PREF_SECTION, null);
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
        //mProgressbar.setVisibility(View.VISIBLE);

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

/*
    @OnClick(btn_launch_browser)
    public void onLaunchBrowserClicked(Button btn) {
        System.out.println("*** onLaunchBrowserClicked");
    }
*/
/*
    @OnClick(txt_hyperlink)
    public void onDocsLinkClicked(TextView textView) {
        launchUri(Uri.parse(mSnippet.getUrl()));
    }
*/

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
        if((/*!mInitializingSpinner0 &&*/ sSiteName == null) || (preferences.getString(SharedPrefsUtil.PREF_SITE, null) == null)) {
            //preferences.edit().putString(SharedPrefsUtil.PREF_SITE, theSpinner.getSelectedItem().toString()).commit();
            preferences.edit().putString(SharedPrefsUtil.PREF_SITE, theSpinner.getSelectedItem().toString())
                    .putString(SharedPrefsUtil.PREF_NOTEBOOK, null)
                    .putString(SharedPrefsUtil.PREF_SECTION, null).commit();
        }
        //mInitializingSpinner0 = false;

        //Snippet item = (Snippet)mSnippet;
        //com.microsoft.sharepointvos.Result result = (com.microsoft.sharepointvos.Result) item.siteMap.get(this
        //        .getParams()
        //        .get(SnippetDetailFragment.ARG_SPINNER_SELECTION).toString());
        com.microsoft.sharepointvos.Result result = (com.microsoft.sharepointvos.Result) mSnippet.siteMap.get(
                theSpinner.getSelectedItem().toString());
        System.out.println("*** Site URI: " + result.getUri());
        String siteUri = result.getUri().toString();//.replace("%20", " ");
        //System.out.println("*** Site URI: " + siteUri);
/*
        System.out.println("*** Sync invocation of SiteMetadataService");
        SiteMetadata data = AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadataSync(
                mSnippet.getVersion(),
                result.getUri());
        System.out.println("*** Received site metadata");
        System.out.println("*** Site Collection ID and Site ID: " + data.siteCollectionId + " " + data.siteId);
        item.mSiteCollectionId = data.siteCollectionId;
        item.mSiteId = data.siteId;

        item.fillNotebookSpinner(AbstractSnippet.sServices.mNotebooksService, getSetUpCallback(), item.notebookMap);
*/
        System.out.println("*** Async invocation of SiteMetadataService");
        AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadata(
                mSnippet.getVersion(),
                siteUri,
                new retrofit.Callback<SiteMetadata>() {
                    @Override
                    public void success(SiteMetadata siteMetadata, Response response) {
                        System.out.println("*** Received site metadata");
                        System.out.println("*** Site Collection ID and Site ID: " + siteMetadata.siteCollectionId + " " + siteMetadata.siteId);
                        //Snippet item = (Snippet) mSnippet;
                        mSnippet.mSiteCollectionId = siteMetadata.siteCollectionId;
                        mSnippet.mSiteId = siteMetadata.siteId;
                        //mProgressbar.setVisibility(View.GONE);
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
        //mNewSectionButton.setEnabled(false);
        mRunButton.setEnabled(false);
        mPickPhotosButton.setEnabled(false);
        mOpenOneNoteButton.setEnabled(false);

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        if((/*!mInitializingSpinner1 &&*/ sNotebookName == null) || (preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null) == null)) {
            //preferences.edit().putString(SharedPrefsUtil.PREF_NOTEBOOK, theSpinner.getSelectedItem().toString()).commit();
            preferences.edit().putString(SharedPrefsUtil.PREF_NOTEBOOK, theSpinner.getSelectedItem().toString())
                    .putString(SharedPrefsUtil.PREF_SECTION, null).commit();
        }
        //mInitializingSpinner1 = false;

        //Snippet item = (Snippet)mSnippet;
        //Notebook notebook = (Notebook) item.notebookMap.get(this
        //        .getParams()
        //        .get(SnippetDetailFragment.ARG_SPINNER_SELECTION).toString());
        Notebook notebook = (Notebook) mSnippet.notebookMap.get(theSpinner.getSelectedItem().toString());
        System.out.println("*** Notebook id: " + notebook.id);
        mNotebookId = notebook.id;

        mSnippet.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback2(), mSnippet.sectionMap, notebook.id);

        mSetDefaultButton.setEnabled(true);
        mNewSectionButton.setEnabled(true);
        //mProgressbar.setVisibility(View.GONE);
    }

    // TODO: remove if section id should not be read on spinner selection
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
        if((/*!mInitializingSpinner2 &&*/ sSectionName == null) || (preferences.getString(SharedPrefsUtil.PREF_SECTION, null) == null)) {
            //preferences.edit().putString(SharedPrefsUtil.PREF_SECTION, theSpinner.getSelectedItem().toString()).commit();
            preferences.edit().putString(SharedPrefsUtil.PREF_SECTION, theSpinner.getSelectedItem().toString()).commit();
        }
        //mInitializingSpinner2 = false;

        //Snippet item = (Snippet)mSnippet;
        Section section = (Section) mSnippet.sectionMap.get(mSpinner2.getSelectedItem().toString());
        System.out.println("*** Section id: " + section.id);
        mSectionId = section.id;

        //SectionSnippet item = (SectionSnippet)mSnippet;
        AbstractSnippet.sServices.mPagesService.getSectionPagesSP(
                mSnippet.getVersion(),
                mSnippet.mSiteCollectionId,
                mSnippet.mSiteId,
                mSectionId,
                null,
                null,
                null,
                null,
                null,
                new retrofit.Callback<Envelope<Page>>() {
                    @Override
                    public void success(Envelope<Page> env, Response response) {
                        if (env.value.length > 0) {
                            int i;
                            for (i = 0; i < env.value.length; i++) {
                                if (env.value[0].title.equals(R.string.page_title))
                                    break;
                            }
                            if (i >= env.value.length)
                                i = 0;
                            mPageId = env.value[i].id;
                            mOneNoteClientUrl = env.value[i].links.oneNoteClientUrl.href;
                            mRunButton.setEnabled(true);
                            mPickPhotosButton.setEnabled(true);
                            mOpenOneNoteButton.setEnabled(true);
                            mProgressbar.setVisibility(View.GONE);
                        } else {
                            //mRunButton.setEnabled(false);
                            //mOpenOneNoteButton.setEnabled(false);
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

/*
    private void launchUri(Uri uri) {
        Intent launchOneNoteExtern = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(launchOneNoteExtern);
        } catch (ActivityNotFoundException e) {
            launchOneNoteExtern = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_STORE_URI));
            startActivity(launchOneNoteExtern);
        }
    }
*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("*** onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        //mActivity = getActivity();

        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        sSiteName = preferences.getString(SharedPrefsUtil.PREF_SITE, null);
        sNotebookName = preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null);
        sSectionName = preferences.getString(SharedPrefsUtil.PREF_SECTION, null);
        System.out.println("*** Site: " + sSiteName);
        System.out.println("*** Notebook: " + sNotebookName);
        System.out.println("*** Section: " + sSectionName);

        /*
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mSnippet = (AbstractSnippet<T, Result>)
                    SnippetContent.ITEMS.get(getArguments().getInt(ARG_ITEM_ID));
            //mItem2 = (AbstractSnippet<T, Result>)
            //        SnippetContent.ITEMS.get(getArguments().getInt(ARG_ITEM_ID));
        }
        */
        mSnippet = /*(AbstractSnippet<T, Result>)*/new Snippet();

        //mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

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
        //mSnippetDescription.setText(mSnippet.getDescription());
        /*
        if (Input.Spinner == mSnippet.mInputArgs) {
            //mSpinner0.setVisibility(VISIBLE);
            //mSpinner.setVisibility(VISIBLE);
            //mSpinner2.setVisibility(VISIBLE);
        } else if (Input.Text == mSnippet.mInputArgs) {
            //mEditText.setVisibility(VISIBLE);
        } else if (Input.Both == mSnippet.mInputArgs) {
            mSpinner.setVisibility(VISIBLE);
            //mEditText.setVisibility(VISIBLE);
        }
        */
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        System.out.println("*** onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        /*
        if (null != getActivity() && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (null != activity.getSupportActionBar()) {
                activity.getSupportActionBar().setTitle(mSnippet.getName());
            }
        }
        */

        //System.out.println("*** onActivityCreated");

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("SharePoint URL");
        final EditText input = new EditText(mActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sharePointUrl = input.getText().toString();
                System.out.println("*** SharePoint URL: " + sharePointUrl);
                mSnippet.setUp(AbstractSnippet.sServices, getSetUpCallback0());    // instead of in ready method
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
/*
        AzureADModule.Builder builder = new AzureADModule.Builder(mActivity);
        builder.validateAuthority(true)
                .skipBroker(true)
                .authenticationResourceId("https://fcpkag.sharepoint.com")
                .authorityUrl(ServiceConstants.AUTHORITY_URL)
                .redirectUri(ServiceConstants.REDIRECT_URI)
                .clientId(ServiceConstants.CLIENT_ID);
        AzureADModule sharePointADModule = builder.build();
*/

//        ((AzureAppCompatActivity)mActivity).forSharePoint = true;
/*
        System.out.println("*** Calling SitesService synchronously");
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://fcpkag.sharepoint.com")
                .setLogLevel(WhiteboardApp.getApp().logLevel)
                //.setConverter(WhiteboardApp.getApp().converter)
                .setRequestInterceptor(WhiteboardApp.getApp().requestInterceptor)
                .build();
        SitesService sitesService = restAdapter.create(SitesService.class);
        Envelope env = sitesService.getFollowedSitesSync();
        System.out.println("*** Envelope: " + env.toString());
*/
        /*
        System.out.println("*** Calling SitesService synchronously");
        RestAdapter restAdapter = WhiteboardApp.getApp().getRestAdapter2();
        SitesService sitesService = restAdapter.create(SitesService.class);
        FollowedSites followedSites = sitesService.getFollowedSitesSync();
        for(int j = 0; j < followedSites.getD().getFollowed().getResults().size(); j++)
            System.out.println("*** Followed site: " + followedSites.getD().getFollowed().getResults().get(j).getName());
        */
/*
        ((AzureAppCompatActivity)mActivity).forSharePoint = false;
        mAuthenticationManager.disconnect();
        ((AzureAppCompatActivity)mActivity).doAgain();
        onResume();
*/
        /*
        System.out.println("*** Sync invocation of SiteMetadataService");
        SiteMetadata data = AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadataSync(
                mSnippet.getVersion(),
                "https://fcpkag.sharepoint.com/Site2");
        System.out.println("*** Received site metadata");
        System.out.println("*** Site Collection ID and Site ID: " + data.siteCollectionId + " " + data.siteId);
        SectionSnippet item = (SectionSnippet)mSnippet;
        item.mSiteCollectionId = data.siteCollectionId;
        item.mSiteId = data.siteId;
        */
/*
        AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadata(
                mSnippet.getVersion(),
                "https://fcpkag.sharepoint.com/Site2",
                //(Callback<Envelope<Page>>)this
                //new retrofit.Callback<Envelope<SiteMetadata>>() {
                new retrofit.Callback<SiteMetadata>() {
                    @Override
                    //public void success(Envelope<SiteMetadata> env, Response response) {
                    public void success(SiteMetadata data, Response response) {
                        //mProgressbar.setVisibility(View.GONE);
                        //if (isAdded() && (null == response)) {
                        System.out.println("*** Received site metadata");
                        //System.out.println("Site Collection ID and Site ID: " + env.value[0].siteCollectionId + " " + env.value[0].siteId);
                        System.out.println("Site Collection ID and Site ID: " + data.siteCollectionId + " " + data.siteId);
                        SectionSnippet item = (SectionSnippet)mSnippet;
                        item.mSiteCollectionId = data.siteCollectionId;
                        item.mSiteId = data.siteId;
                        //}
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** Error receiving site metadata");
                        if (isAdded()) {
                            displayThrowable(error.getCause());
                            mProgressbar.setVisibility(View.GONE);
                        }
                    }
                }
        );
*/
    }

    @Override
    public void onResume() {
        System.out.println("*** onResume");
        super.onResume();
        //mLocationManager.requestLocationUpdates(mLocationProvider, 400, 1.0f, this);
        mLocationManager.requestLocationUpdates(mLocationProvider, 60000, 100.0f, this);

        /*
        SharedPreferences preferences
                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
        sSiteName = preferences.getString(SharedPrefsUtil.PREF_SITE, null);
        sNotebookName = preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null);
        sSectionName = preferences.getString(SharedPrefsUtil.PREF_SECTION, null);
        System.out.println("*** Site: " + sSiteName);
        System.out.println("*** Notebook: " + sNotebookName);
        System.out.println("*** Section: " + sSectionName);
        */

        if (User.isOrg()) {
            //mAuthenticationManager.connect(this);
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
        } else if (User.isMsa()) {
            mLiveAuthClient.loginSilent(BaseActivity.sSCOPES, this);
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
        //StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
            //int maxLines = address.get(0).getMaxAddressLineIndex();
            /*
            for (int i=0; i<maxLines; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }
            String finalAddress = builder.toString();
            */
            //mFinalAddress = address.get(0).getAddressLine(maxLines - 1);
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
                    //mRunButton.setEnabled(true);
                    mNewSectionButton.setEnabled(false);
                    if (strings.length > 0) {
                        populateSpinner0(strings);
                        mSpinner0.setVisibility(VISIBLE);
                        /*
                        SharedPreferences preferences
                                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
                        String sSiteName = preferences.getString(SharedPrefsUtil.PREF_SITE, null);
                        */
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
                    //displayThrowable(error.getCause());
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
                    //mRunButton.setEnabled(true);
                    mNewSectionButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner(strings);
                        mSpinner.setVisibility(VISIBLE);
                        /*
                        SharedPreferences preferences
                                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
                        String sNotebookName = preferences.getString(SharedPrefsUtil.PREF_NOTEBOOK, null);
                        */
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
                    //displayThrowable(error.getCause());
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
                        /*
                        SharedPreferences preferences
                                = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
                        String sSectionName = preferences.getString(SharedPrefsUtil.PREF_SECTION, null);
                        */
                        if(sSectionName != null) {
                            int pos = ((ArrayAdapter) mSpinner2.getAdapter()).getPosition(sSectionName);
                            if(pos != -1) mSpinner2.setSelection(pos, true);
                            sSectionName = null;
                        } else if(mGotoDefault) {
                            onNewSectionClicked(mNewSectionButton);
                            mGotoDefault = false;
                        }
                    }
                }/* else if (isAdded() && strings.length <= 0 && null != response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.err_setup)
                            .setMessage(R.string.err_setup_msg)
                            .setPositiveButton(R.string.dismiss, null)
                            .show();
                }*/
                sSectionName = null;
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    //displayThrowable(error.getCause());
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
                //mProgressbar.setVisibility(View.GONE);
                if (isAdded() && (null == response || strings.length > 0)) {
                    mRunButton.setEnabled(true);
                    mPickPhotosButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner2(strings);
                        //mSpinner2.setSelection(((ArrayAdapter)mSpinner2.getAdapter()).getPosition(sectionName), true);
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
                }/* else if (isAdded() && strings.length <= 0 && null != response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.err_setup)
                            .setMessage(R.string.err_setup_msg)
                            .setPositiveButton(R.string.dismiss, null)
                            .show();
                }*/
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("*** Callback3 failure");
                if (isAdded()) {
                    //displayThrowable(error.getCause());
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

/*
    @Override
    public void success(Result result, Response response) {
        System.out.println("*** SnippetDetailFragment.success");
        if (!isAdded()) {
            // the user has left...
            return;
        }
        mProgressbar.setVisibility(GONE);
        displayResponse(response);
        maybeShowQuickLink(result);
    }

    private void maybeShowQuickLink(Result result) {
        if (result instanceof BaseVO) {
            final BaseVO vo = (BaseVO) result;
            if (hasWebClientLink(vo)) {
                showBrowserLaunchBtn(vo);
            }
        }
    }

    private boolean hasWebClientLink(BaseVO vo) {
        return null != vo.links && null != vo.links.oneNoteWebUrl;
    }

    private boolean hasOneNoteClientLink(BaseVO vo) {
        return null != vo.links && null != vo.links.oneNoteClientUrl;
    }

    private void showBrowserLaunchBtn(final BaseVO vo) {
        mLaunchBrowser.setEnabled(true);
        mLaunchBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchUri(Uri.parse(vo.links.oneNoteWebUrl.href));
            }
        });
    }

    private void displayResponse(Response response) {
        int color = getColor(response);
        displayStatusCode(Integer.valueOf(response.getStatus())
                .toString(), getResources().getColor(color));
        displayRequestUrl(response);
        maybeDisplayResponseHeaders(response);
        maybeDisplayResponseBody(response);
    }

    private void maybeDisplayResponseBody(Response response) {
        if (null != response.getBody()) {
            String body = null;
            InputStream is = null;
            try {
                is = response.getBody().in();
                body = IOUtils.toString(is);
                String formattedJson = new JSONObject(body).toString(2);
                formattedJson = StringEscapeUtils.unescapeJson(formattedJson);
                mResponseBody.setText(formattedJson);
            } catch (JSONException e) {
                if (null != body) {
                    // body wasn't JSON
                    mResponseBody.setText(body);
                } else {
                    // set the stack trace as the response body
                    displayThrowable(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayThrowable(e);
            } finally {
                if (null != is) {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    private void maybeDisplayResponseHeaders(Response response) {
        if (null != response.getHeaders()) {
            List<Header> headers = response.getHeaders();
            String headerText = "";
            for (Header header : headers) {
                headerText += header.getName() + " : " + header.getValue() + "\n";
            }
            mResponseHeaders.setText(headerText);
        }
    }

    private void displayRequestUrl(Response response) {
        String requestUrl = response.getUrl();
        mRequestUrl.setText(requestUrl);
    }

    private void displayStatusCode(String text, int color) {
        mStatusCode.setText(text);
        mStatusColor.setBackgroundColor(color);
    }
*/
    private void displayThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String trace = sw.toString();
        //mResponseBody.setText(trace);
        //Throwable cause = t.getCause();
        //String causeMsg = (cause == null) ? "" : (": " + cause.getMessage());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.err_setup)
                //.setMessage(R.string.err_setup_msg)
                .setMessage(t.getMessage())
                .setPositiveButton(R.string.dismiss, null)
                .show();
    }

/*
    private int getColor(Response response) {
        int color;
        switch (response.getStatus() / 100) {
            case 1:
            case 2:
                color = R.color.code_1xx;
                break;
            case 3:
                color = R.color.code_3xx;
                break;
            case 4:
            case 5:
                color = R.color.code_4xx;
                break;
            default:
                color = R.color.transparent;
        }
        return color;
    }
*/

/*
    @Override
    public void failure(RetrofitError error) {
        Timber.d(error, "");
        mProgressbar.setVisibility(GONE);
        if (null != error.getResponse()) {
            displayResponse(error.getResponse());
        }
    }
*/

/*
    @Override
    public Map<String, String> getParams() {
        Map<String, String> args = new HashMap<>();
        if (Input.Spinner == mSnippet.mInputArgs) {
            args.put(ARG_SPINNER_SELECTION, mSpinner.getSelectedItem().toString());
            //args.put(ARG_SPINNER2_SELECTION, mSpinner2.getSelectedItem().toString());
        } else if (Input.Text == mSnippet.mInputArgs) {
            //args.put(ARG_TEXT_INPUT, mEditText.getText().toString());
        } else if (Input.Both == mSnippet.mInputArgs) {
            args.put(ARG_SPINNER_SELECTION, mSpinner.getSelectedItem().toString());
            //args.put(ARG_TEXT_INPUT, mEditText.getText().toString());
        } else {
            throw new IllegalStateException("No input modifier to match type");
        }
        return args;
    }
*/

    @Override
    public void onSuccess(AuthenticationResult authenticationResult) {
        SharedPrefsUtil.persistAuthToken(authenticationResult);
        ready();
    }

    private void ready() {
        /*if (Input.None == mSnippet.mInputArgs) {
            mRunButton.setEnabled(true);
            mPickPhotosButton.setEnabled(true);
        } else*/ if (!setupDidRun) {
            setupDidRun = true;
            mProgressbar.setVisibility(View.VISIBLE);
            mSpinner0.setVisibility(View.INVISIBLE);
            mSpinner.setVisibility(View.INVISIBLE);
            mSpinner2.setVisibility(View.INVISIBLE);
            mSnippet.setUp(AbstractSnippet.sServices, getSetUpCallback0());     // TODO: uncomment if immediate setup required

            SharedPreferences preferences
                    = WhiteboardApp.getApp().getSharedPreferences(AppModule.PREFS, Context.MODE_PRIVATE);
            mGotoDefaultButton.setEnabled(preferences.getString(SharedPrefsUtil.PREF_DEFAULT_SITE, null) != null &&
                    preferences.getString(SharedPrefsUtil.PREF_DEFAULT_NOTEBOOK, null) != null);

            //mSnippet.setUp(AbstractSnippet.sServices, getSetUpCallback());
            //mItem2.setUp2(AbstractSnippet.sServices, getSetUpCallback(), getSetUpCallback2());
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
                        //mAuthenticationManager.disconnect();
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
