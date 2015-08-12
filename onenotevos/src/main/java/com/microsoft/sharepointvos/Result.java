
package com.microsoft.sharepointvos;

import java.util.HashMap;
import java.util.Map;

public class Result {

    private Object AccountName;
    private Integer ActorType;
    private Boolean CanFollow;
    private String ContentUri;
    private Object EmailAddress;
    private Object FollowedContentUri;
    private String Id;
    private Object ImageUri;
    private Boolean IsFollowed;
    private Object LibraryUri;
    private String Name;
    private Object PersonalSiteUri;
    private Integer Status;
    private Object StatusText;
    private String TagGuid;
    private Object Title;
    private String Uri;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The AccountName
     */
    public Object getAccountName() {
        return AccountName;
    }

    /**
     * 
     * @param AccountName
     *     The AccountName
     */
    public void setAccountName(Object AccountName) {
        this.AccountName = AccountName;
    }

    /**
     * 
     * @return
     *     The ActorType
     */
    public Integer getActorType() {
        return ActorType;
    }

    /**
     * 
     * @param ActorType
     *     The ActorType
     */
    public void setActorType(Integer ActorType) {
        this.ActorType = ActorType;
    }

    /**
     * 
     * @return
     *     The CanFollow
     */
    public Boolean getCanFollow() {
        return CanFollow;
    }

    /**
     * 
     * @param CanFollow
     *     The CanFollow
     */
    public void setCanFollow(Boolean CanFollow) {
        this.CanFollow = CanFollow;
    }

    /**
     * 
     * @return
     *     The ContentUri
     */
    public String getContentUri() {
        return ContentUri;
    }

    /**
     * 
     * @param ContentUri
     *     The ContentUri
     */
    public void setContentUri(String ContentUri) {
        this.ContentUri = ContentUri;
    }

    /**
     * 
     * @return
     *     The EmailAddress
     */
    public Object getEmailAddress() {
        return EmailAddress;
    }

    /**
     * 
     * @param EmailAddress
     *     The EmailAddress
     */
    public void setEmailAddress(Object EmailAddress) {
        this.EmailAddress = EmailAddress;
    }

    /**
     * 
     * @return
     *     The FollowedContentUri
     */
    public Object getFollowedContentUri() {
        return FollowedContentUri;
    }

    /**
     * 
     * @param FollowedContentUri
     *     The FollowedContentUri
     */
    public void setFollowedContentUri(Object FollowedContentUri) {
        this.FollowedContentUri = FollowedContentUri;
    }

    /**
     * 
     * @return
     *     The Id
     */
    public String getId() {
        return Id;
    }

    /**
     * 
     * @param Id
     *     The Id
     */
    public void setId(String Id) {
        this.Id = Id;
    }

    /**
     * 
     * @return
     *     The ImageUri
     */
    public Object getImageUri() {
        return ImageUri;
    }

    /**
     * 
     * @param ImageUri
     *     The ImageUri
     */
    public void setImageUri(Object ImageUri) {
        this.ImageUri = ImageUri;
    }

    /**
     * 
     * @return
     *     The IsFollowed
     */
    public Boolean getIsFollowed() {
        return IsFollowed;
    }

    /**
     * 
     * @param IsFollowed
     *     The IsFollowed
     */
    public void setIsFollowed(Boolean IsFollowed) {
        this.IsFollowed = IsFollowed;
    }

    /**
     * 
     * @return
     *     The LibraryUri
     */
    public Object getLibraryUri() {
        return LibraryUri;
    }

    /**
     * 
     * @param LibraryUri
     *     The LibraryUri
     */
    public void setLibraryUri(Object LibraryUri) {
        this.LibraryUri = LibraryUri;
    }

    /**
     * 
     * @return
     *     The Name
     */
    public String getName() {
        return Name;
    }

    /**
     * 
     * @param Name
     *     The Name
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * 
     * @return
     *     The PersonalSiteUri
     */
    public Object getPersonalSiteUri() {
        return PersonalSiteUri;
    }

    /**
     * 
     * @param PersonalSiteUri
     *     The PersonalSiteUri
     */
    public void setPersonalSiteUri(Object PersonalSiteUri) {
        this.PersonalSiteUri = PersonalSiteUri;
    }

    /**
     * 
     * @return
     *     The Status
     */
    public Integer getStatus() {
        return Status;
    }

    /**
     * 
     * @param Status
     *     The Status
     */
    public void setStatus(Integer Status) {
        this.Status = Status;
    }

    /**
     * 
     * @return
     *     The StatusText
     */
    public Object getStatusText() {
        return StatusText;
    }

    /**
     * 
     * @param StatusText
     *     The StatusText
     */
    public void setStatusText(Object StatusText) {
        this.StatusText = StatusText;
    }

    /**
     * 
     * @return
     *     The TagGuid
     */
    public String getTagGuid() {
        return TagGuid;
    }

    /**
     * 
     * @param TagGuid
     *     The TagGuid
     */
    public void setTagGuid(String TagGuid) {
        this.TagGuid = TagGuid;
    }

    /**
     * 
     * @return
     *     The Title
     */
    public Object getTitle() {
        return Title;
    }

    /**
     * 
     * @param Title
     *     The Title
     */
    public void setTitle(Object Title) {
        this.Title = Title;
    }

    /**
     * 
     * @return
     *     The Uri
     */
    public String getUri() {
        return Uri;
    }

    /**
     * 
     * @param Uri
     *     The Uri
     */
    public void setUri(String Uri) {
        this.Uri = Uri;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
