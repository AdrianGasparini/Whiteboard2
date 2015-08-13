/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package com.microsoft.o365_android_onenote_rest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.microsoft.AuthenticationManager;
import com.microsoft.AuthenticationManagers;
import com.microsoft.AzureADModule;
import com.microsoft.AzureAppCompatActivity;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;
import com.microsoft.o365_android_onenote_rest.application.SnippetApp;
import com.microsoft.o365_android_onenote_rest.conf.ServiceConstants;
import com.microsoft.o365_android_onenote_rest.snippet.AbstractSnippet;
import com.microsoft.o365_android_onenote_rest.snippet.Callback;
import com.microsoft.o365_android_onenote_rest.snippet.Input;
import com.microsoft.o365_android_onenote_rest.snippet.SectionSnippet;
import com.microsoft.o365_android_onenote_rest.snippet.SnippetContent;
import com.microsoft.o365_android_onenote_rest.util.SharedPrefsUtil;
import com.microsoft.o365_android_onenote_rest.util.User;
import com.microsoft.onenoteapi.service.OneNotePartsMap;
import com.microsoft.onenoteapi.service.SiteMetadataService;
import com.microsoft.onenotevos.BaseVO;
import com.microsoft.onenotevos.Envelope;
import com.microsoft.onenotevos.Links;
import com.microsoft.onenotevos.Notebook;
import com.microsoft.onenotevos.Page;
import com.microsoft.onenotevos.Section;
import com.microsoft.onenotevos.SiteMetadata;
import com.microsoft.sharepoint.service.SitesService;
import com.microsoft.sharepointvos.FollowedSites;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import timber.log.Timber;

import static android.R.layout.simple_spinner_dropdown_item;
import static android.R.layout.simple_spinner_item;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_launch_browser;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_open_onenote;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_new_section;
import static com.microsoft.o365_android_onenote_rest.R.id.btn_run;
import static com.microsoft.o365_android_onenote_rest.R.id.progressbar;
import static com.microsoft.o365_android_onenote_rest.R.id.spinner0;
import static com.microsoft.o365_android_onenote_rest.R.id.spinner;
import static com.microsoft.o365_android_onenote_rest.R.id.spinner2;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_desc;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_hyperlink;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_input;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_request_url;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_response_body;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_response_headers;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_status_code;
import static com.microsoft.o365_android_onenote_rest.R.id.txt_status_color;
import static com.microsoft.o365_android_onenote_rest.R.string.clippy;
import static com.microsoft.o365_android_onenote_rest.R.string.req_url;
import static com.microsoft.o365_android_onenote_rest.R.string.response_body;
import static com.microsoft.o365_android_onenote_rest.R.string.response_headers;

public class SnippetDetailFragment<T, Result>
        extends BaseFragment
        implements Callback<Result>,
        AuthenticationCallback<AuthenticationResult>, LiveAuthListener {

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_TEXT_INPUT = "TextInput";
    public static final String ARG_SPINNER_SELECTION = "SpinnerSelection";
    //public static final String ARG_SPINNER2_SELECTION = "Spinner2Selection";
    public static final int UNSET = -1;
    public static final String APP_STORE_URI = "https://play.google.com/store/apps/details?id=com.microsoft.office.onenote";

    static final int REQUEST_TAKE_PHOTO = 1;
    String mNotebookId = null;
    String mSectionId = null;
    String mCurrentPhotoPath = null;
    File mPhotoFile = null;
    Activity mActivity;
    //Callback mCallback = this;
    String mSectionName = null;
    String mOneNoteClientUrl = null;
    //String mSiteCollectionId = null;
    //String mSiteId = null;

    @InjectView(txt_status_code)
    protected TextView mStatusCode;

    @InjectView(txt_status_color)
    protected View mStatusColor;

    @InjectView(txt_desc)
    protected TextView mSnippetDescription;

    @InjectView(txt_request_url)
    protected TextView mRequestUrl;

    @InjectView(txt_response_headers)
    protected TextView mResponseHeaders;

    @InjectView(txt_response_body)
    protected TextView mResponseBody;

    @InjectView(spinner0)
    protected Spinner mSpinner0;

    @InjectView(spinner)
    protected Spinner mSpinner;

    @InjectView(spinner2)
    protected Spinner mSpinner2;

    @InjectView(txt_input)
    protected EditText mEditText;

    @InjectView(progressbar)
    protected ProgressBar mProgressbar;

    @InjectView(btn_run)
    protected Button mRunButton;

    @InjectView(btn_new_section)
    protected Button mNewSectionButton;

    @InjectView(btn_open_onenote)
    protected Button mOpenOneNoteButton;

    @Inject
    public AuthenticationManagers mAuthenticationManagers;

    //@Inject
    //protected AuthenticationManager mAuthenticationManager;

    @Inject
    protected LiveAuthClient mLiveAuthClient;

    boolean setupDidRun = false;
    private AbstractSnippet<T, Result> mItem;

    //private AbstractSnippet<T, Result> mItem2;

    public SnippetDetailFragment() {
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

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

    @OnClick(btn_run)
    public void onRunClicked(Button btn) {
        System.out.println("*** onRunClicked");
        mOpenOneNoteButton.setEnabled(false);

        System.out.println("*** Notebook id: " + mNotebookId);
        // TODO: uncomment if section id should not be read on spinner selection
        /*
        System.out.println("*** Section: " + mSpinner2.getSelectedItem().toString());
        SectionSnippet item = (SectionSnippet)mItem;
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
        SectionSnippet item = (SectionSnippet)mItem;
        Section section = (Section) item.sectionMap.get(mSpinner2.getSelectedItem().toString());
        System.out.println("*** Section id: " + section.id);

        DateTime date = DateTime.now();
        String imagePartName = "image1";
        String simpleHtml = getSimplePageContentBody(SnippetApp
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
                mItem.getVersion(),
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
        mItem.request(mItem.mService, this);
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
            SectionSnippet item = (SectionSnippet)mItem;
            Section section = (Section) item.sectionMap.get(mSpinner2.getSelectedItem().toString());
            System.out.println("*** Section id: " + section.id);
*/
            DateTime date = DateTime.now();
            String imagePartName = "image1";
            String simpleHtml = getSimplePageContentBody(SnippetApp
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

            SectionSnippet item = (SectionSnippet)mItem;
            AbstractSnippet.sServices.mPagesService.postMultiPartPagesSP(
                    mItem.getVersion(),
                    item.mSiteCollectionId,
                    item.mSiteId,
                    mSectionId,
                    oneNotePartsMap,
                    //(Callback<Envelope<Page>>)this
                    new retrofit.Callback<Envelope<Page>>() {
                        @Override
                        public void success(Envelope<Page> env, Response response) {
                            //mProgressbar.setVisibility(View.GONE);
                            //if (isAdded() && (null == response /*|| strings.length > 0*/)) {
                            System.out.println("*** Getting OneNote Client URL");
                            mOneNoteClientUrl = env.links.oneNoteClientUrl.href;
                            mOpenOneNoteButton.setEnabled(true);
                            //}
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (isAdded()) {
                                displayThrowable(error.getCause());
                                mProgressbar.setVisibility(View.GONE);
                            }
                        }
                    }
            );
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode != Activity.RESULT_OK) {
            System.out.println("*** Photo cancelled");
            mOpenOneNoteButton.setEnabled(mOneNoteClientUrl != null);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Section Name");

// Set up the input
        final EditText input = new EditText(mActivity);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT/* | InputType.TYPE_TEXT_VARIATION_PASSWORD*/);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*final String*/
                mSectionName = input.getText().toString();
                System.out.println("*** New section name: " + mSectionName);

                SectionSnippet item = (SectionSnippet)mItem;
                AbstractSnippet.sServices.mSectionsService.postSectionSP(
                        mItem.getVersion(),
                        item.mSiteCollectionId,
                        item.mSiteId,
                        "application/json",
                        mNotebookId,
                        createNewSection(mSectionName),
                        //mCallback
                        new retrofit.Callback<Envelope>() {
                            @Override
                            public void success(Envelope env, Response response) {
                                //mProgressbar.setVisibility(View.GONE);
                                //if (isAdded() && (null == response /*|| strings.length > 0*/)) {
                                System.out.println("*** postSection success");
                                mSectionId = env.id;
                                System.out.println("*** Section ID: " + mSectionId);
                                System.out.println("*** Fetching sections");
                                SectionSnippet item = (SectionSnippet) mItem;
                                item.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback3(), item.sectionMap, mNotebookId);
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
                                    displayThrowable(error.getCause());
                                    mProgressbar.setVisibility(View.GONE);
                                }
                            }
                        }
                );
/*
                System.out.println("Fetching sections");
                SectionSnippet item = (SectionSnippet)mItem;
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

    @OnClick(btn_launch_browser)
    public void onLaunchBrowserClicked(Button btn) {
        System.out.println("*** onLaunchBrowserClicked");
        mItem.setUp(AbstractSnippet.sServices, getSetUpCallback0());
/*
        AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadata(
                mItem.getVersion(),
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
                        SectionSnippet item = (SectionSnippet) mItem;
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

    @OnClick(txt_hyperlink)
    public void onDocsLinkClicked(TextView textView) {
        launchUri(Uri.parse(mItem.getUrl()));
    }

    @OnItemSelected(spinner0)
    public void onSpinner0ItemSelected(Spinner theSpinner) {
        System.out.println("*** Spinner0 selected: " + theSpinner.getSelectedItem().toString());

        SectionSnippet item = (SectionSnippet)mItem;
        //com.microsoft.sharepointvos.Result result = (com.microsoft.sharepointvos.Result) item.siteMap.get(this
        //        .getParams()
        //        .get(SnippetDetailFragment.ARG_SPINNER_SELECTION).toString());
        com.microsoft.sharepointvos.Result result = (com.microsoft.sharepointvos.Result) item.siteMap.get(
                theSpinner.getSelectedItem().toString());
        System.out.println("*** Site URI: " + result.getUri());
/*
        System.out.println("*** Sync invocation of SiteMetadataService");
        SiteMetadata data = AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadataSync(
                mItem.getVersion(),
                result.getUri());
        System.out.println("*** Received site metadata");
        System.out.println("*** Site Collection ID and Site ID: " + data.siteCollectionId + " " + data.siteId);
        item.mSiteCollectionId = data.siteCollectionId;
        item.mSiteId = data.siteId;

        item.fillNotebookSpinner(AbstractSnippet.sServices.mNotebooksService, getSetUpCallback(), item.notebookMap);
*/
        System.out.println("*** Async invocation of SiteMetadataService");
        AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadata(
                mItem.getVersion(),
                result.getUri(),
                new retrofit.Callback<SiteMetadata>() {
                    @Override
                    public void success(SiteMetadata siteMetadata, Response response) {
                        System.out.println("*** Received site metadata");
                        System.out.println("*** Site Collection ID and Site ID: " + siteMetadata.siteCollectionId + " " + siteMetadata.siteId);
                        SectionSnippet item = (SectionSnippet)mItem;
                        item.mSiteCollectionId = siteMetadata.siteCollectionId;
                        item.mSiteId = siteMetadata.siteId;
                        item.fillNotebookSpinner(AbstractSnippet.sServices.mNotebooksService, getSetUpCallback(), item.notebookMap);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("*** Failure receiving site metadata");
                    }
                }
        );
    }

    @OnItemSelected(spinner)
    public void onSpinnerItemSelected(Spinner theSpinner) {
        System.out.println("*** Spinner selected: " + theSpinner.getSelectedItem().toString());
        SectionSnippet item = (SectionSnippet)mItem;
        //Notebook notebook = (Notebook) item.notebookMap.get(this
        //        .getParams()
        //        .get(SnippetDetailFragment.ARG_SPINNER_SELECTION).toString());
        Notebook notebook = (Notebook) item.notebookMap.get(theSpinner.getSelectedItem().toString());
        System.out.println("*** Notebook id: " + notebook.id);
        mNotebookId = notebook.id;

        item.fillSectionSpinner(AbstractSnippet.sServices.mSectionsService, getSetUpCallback2(), item.sectionMap, notebook.id);
    }

    // TODO: remove if section id should not be read on spinner selection
    @OnItemSelected(spinner2)
    public void onSpinner2ItemSelected(Spinner theSpinner) {
        System.out.println("*** Section: " + mSpinner2.getSelectedItem().toString());
        SectionSnippet item = (SectionSnippet)mItem;
        Section section = (Section) item.sectionMap.get(mSpinner2.getSelectedItem().toString());
        System.out.println("*** Section id: " + section.id);
        mSectionId = section.id;
    }

    private void launchUri(Uri uri) {
        Intent launchOneNoteExtern = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(launchOneNoteExtern);
        } catch (ActivityNotFoundException e) {
            launchOneNoteExtern = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_STORE_URI));
            startActivity(launchOneNoteExtern);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = (AbstractSnippet<T, Result>)
                    SnippetContent.ITEMS.get(getArguments().getInt(ARG_ITEM_ID));
            //mItem2 = (AbstractSnippet<T, Result>)
            //        SnippetContent.ITEMS.get(getArguments().getInt(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_snippet_detail, container, false);
        ButterKnife.inject(this, rootView);
        mSnippetDescription.setText(mItem.getDescription());
        if (Input.Spinner == mItem.mInputArgs) {
            mSpinner0.setVisibility(VISIBLE);
            mSpinner.setVisibility(VISIBLE);
            mSpinner2.setVisibility(VISIBLE);
        } else if (Input.Text == mItem.mInputArgs) {
            mEditText.setVisibility(VISIBLE);
        } else if (Input.Both == mItem.mInputArgs) {
            mSpinner.setVisibility(VISIBLE);
            mEditText.setVisibility(VISIBLE);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != getActivity() && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (null != activity.getSupportActionBar()) {
                activity.getSupportActionBar().setTitle(mItem.getName());
            }
        }

        System.out.println("*** onActivityCreated");
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
                mItem.setUp(AbstractSnippet.sServices, getSetUpCallback0());    // instead of in ready method
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
                .setLogLevel(SnippetApp.getApp().logLevel)
                //.setConverter(SnippetApp.getApp().converter)
                .setRequestInterceptor(SnippetApp.getApp().requestInterceptor)
                .build();
        SitesService sitesService = restAdapter.create(SitesService.class);
        Envelope env = sitesService.getFollowedSitesSync();
        System.out.println("*** Envelope: " + env.toString());
*/
        /*
        System.out.println("*** Calling SitesService synchronously");
        RestAdapter restAdapter = SnippetApp.getApp().getRestAdapter2();
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
                mItem.getVersion(),
                "https://fcpkag.sharepoint.com/Site2");
        System.out.println("*** Received site metadata");
        System.out.println("*** Site Collection ID and Site ID: " + data.siteCollectionId + " " + data.siteId);
        SectionSnippet item = (SectionSnippet)mItem;
        item.mSiteCollectionId = data.siteCollectionId;
        item.mSiteId = data.siteId;
        */
/*
        AbstractSnippet.sServices.mSiteMetadataService.getSiteMetadata(
                mItem.getVersion(),
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
                        SectionSnippet item = (SectionSnippet)mItem;
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
        super.onResume();
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

    private retrofit.Callback<String[]> getSetUpCallback0() {
        return new retrofit.Callback<String[]>() {
            @Override
            public void success(String[] strings, Response response) {
                System.out.println("*** Callback0 success");
                mProgressbar.setVisibility(View.GONE);
                if (isAdded() && (null == response || strings.length > 0)) {
                    //mRunButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner0(strings);
                    }
                } else if (isAdded() && strings.length <= 0 && null != response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.err_setup)
                            .setMessage(R.string.err_setup_msg)
                            .setPositiveButton(R.string.dismiss, null)
                            .show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    displayThrowable(error.getCause());
                    mProgressbar.setVisibility(View.GONE);
                }
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
                    mRunButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner(strings);
                    }
                } else if (isAdded() && strings.length <= 0 && null != response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.err_setup)
                            .setMessage(R.string.err_setup_msg)
                            .setPositiveButton(R.string.dismiss, null)
                            .show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    displayThrowable(error.getCause());
                    mProgressbar.setVisibility(View.GONE);
                }
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
                    if (strings.length > 0) {
                        populateSpinner2(strings);
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
                if (isAdded()) {
                    displayThrowable(error.getCause());
                    mProgressbar.setVisibility(View.GONE);
                }
            }
        };
    }

    // select new section
    private retrofit.Callback<String[]> getSetUpCallback3() {
        return new retrofit.Callback<String[]>() {
            @Override
            public void success(String[] strings, Response response) {
                System.out.println("*** Callback3 success");
                mProgressbar.setVisibility(View.GONE);
                if (isAdded() && (null == response || strings.length > 0)) {
                    mRunButton.setEnabled(true);
                    if (strings.length > 0) {
                        populateSpinner2(strings);
                        //mSpinner2.setSelection(((ArrayAdapter)mSpinner2.getAdapter()).getPosition(sectionName), true);
                        mSpinner2.post(new Runnable() {
                            @Override
                            public void run() {
                                mSpinner2.setSelection(((ArrayAdapter) mSpinner2.getAdapter()).getPosition(mSectionName), true);
                            }
                        });
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
                    displayThrowable(error.getCause());
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

    private void displayThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String trace = sw.toString();
        mResponseBody.setText(trace);
    }

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

    @Override
    public void failure(RetrofitError error) {
        Timber.d(error, "");
        mProgressbar.setVisibility(GONE);
        if (null != error.getResponse()) {
            displayResponse(error.getResponse());
        }
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> args = new HashMap<>();
        if (Input.Spinner == mItem.mInputArgs) {
            args.put(ARG_SPINNER_SELECTION, mSpinner.getSelectedItem().toString());
            //args.put(ARG_SPINNER2_SELECTION, mSpinner2.getSelectedItem().toString());
        } else if (Input.Text == mItem.mInputArgs) {
            args.put(ARG_TEXT_INPUT, mEditText.getText().toString());
        } else if (Input.Both == mItem.mInputArgs) {
            args.put(ARG_SPINNER_SELECTION, mSpinner.getSelectedItem().toString());
            args.put(ARG_TEXT_INPUT, mEditText.getText().toString());
        } else {
            throw new IllegalStateException("No input modifier to match type");
        }
        return args;
    }

    @Override
    public void onSuccess(AuthenticationResult authenticationResult) {
        SharedPrefsUtil.persistAuthToken(authenticationResult);
        ready();
    }

    private void ready() {
        if (Input.None == mItem.mInputArgs) {
            mRunButton.setEnabled(true);
        } else if (!setupDidRun) {
            setupDidRun = true;
            mProgressbar.setVisibility(View.VISIBLE);
            mItem.setUp(AbstractSnippet.sServices, getSetUpCallback0());     // TODO: uncomment if immediate setup required

            //mItem.setUp(AbstractSnippet.sServices, getSetUpCallback());
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
