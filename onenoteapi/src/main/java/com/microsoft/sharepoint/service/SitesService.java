package com.microsoft.sharepoint.service;

import com.microsoft.sharepointvos.FollowedSites;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;

/**
 * Created by adrian.gasparini on 06.08.2015.
 */
public interface SitesService {
    @Headers("accept: application/json;odata=verbose")
    @GET("/_api/social.following/my/followed(types=4)")
    void getFollowedSites(
            Callback<FollowedSites> callback
    );
}
