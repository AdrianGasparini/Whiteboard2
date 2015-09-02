package com.microsoft.onenoteapi.service;

import com.microsoft.onenotevos.SiteMetadata;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface SiteMetadataService {

    @GET("/{version}/myorganization/siteCollections/FromUrl(url='{sitepath}')")
    void getSiteMetadata(
            @Path("version") String version,
            @Path("sitepath") String sitePath,
            Callback<SiteMetadata> callback
    );

}
