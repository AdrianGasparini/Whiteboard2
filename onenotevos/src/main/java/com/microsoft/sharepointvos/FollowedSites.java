
package com.microsoft.sharepointvos;

import java.util.HashMap;
import java.util.Map;

public class FollowedSites {

    private D d;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The d
     */
    public D getD() {
        return d;
    }

    /**
     * 
     * @param d
     *     The d
     */
    public void setD(D d) {
        this.d = d;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
