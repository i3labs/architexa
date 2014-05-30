/**
 * 
 */
package com.architexa.diagrams.model;

import org.openrdf.model.URI;

public class DocPath {
    // src, dst should really be resources, but we allow them to be converted later on
    public Object src;
    public URI rel;
    public Object dst;
    public DocPath(Object src, URI rel, Object dst) {
        this.src = src;
        this.rel = rel;
        this.dst = dst;
    }
}