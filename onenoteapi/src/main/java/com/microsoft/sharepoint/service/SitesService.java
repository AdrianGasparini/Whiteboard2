package com.microsoft.sharepoint.service;

import com.microsoft.onenotevos.Envelope;
import com.microsoft.sharepointvos.FollowedSites;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by adrian.gasparini on 06.08.2015.
 */
public interface SitesService {
    @Headers("accept: application/json;odata=verbose")
    @GET("/_api/social.following/my/followed(types=4)")
    void getFollowedSites(
            Callback<Envelope> callback
    );

    @Headers("accept: application/json;odata=verbose")
    @GET("/_api/social.following/my/followed(types=4)")
    //@POST("/_api/sp.userprofiles.profileloader.getprofileloader/getuserprofile/FollowedContent")
    //@POST("/_api/sp.userprofiles.profileloader.getprofileloader/getuserprofile/FollowedSitesUrl")
    FollowedSites getFollowedSitesSync(
    );
}
