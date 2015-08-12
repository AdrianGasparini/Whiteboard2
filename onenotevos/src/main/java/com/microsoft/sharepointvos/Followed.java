
package com.microsoft.sharepointvos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Followed {

    private com.microsoft.sharepointvos.Metadata Metadata;
    private List<Result> results = new ArrayList<Result>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The Metadata
     */
    public com.microsoft.sharepointvos.Metadata getMetadata() {
        return Metadata;
    }

    /**
     * 
     * @param Metadata
     *     The __metadata
     */
    public void setMetadata(com.microsoft.sharepointvos.Metadata Metadata) {
        this.Metadata = Metadata;
    }

    /**
     * 
     * @return
     *     The results
     */
    public List<Result> getResults() {
        return results;
    }

    /**
     * 
     * @param results
     *     The results
     */
    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
