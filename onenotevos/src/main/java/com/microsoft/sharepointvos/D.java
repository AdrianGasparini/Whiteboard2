
package com.microsoft.sharepointvos;

import java.util.HashMap;
import java.util.Map;

public class D {

    private com.microsoft.sharepointvos.Followed Followed;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The Followed
     */
    public com.microsoft.sharepointvos.Followed getFollowed() {
        return Followed;
    }

    /**
     * 
     * @param Followed
     *     The Followed
     */
    public void setFollowed(com.microsoft.sharepointvos.Followed Followed) {
        this.Followed = Followed;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
