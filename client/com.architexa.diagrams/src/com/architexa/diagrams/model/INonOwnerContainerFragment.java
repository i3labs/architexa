package com.architexa.diagrams.model;

/**
 * This interface tag is used for those containers that want to allow their
 * children to be owned another ArtFrag. Such ArtFrag's children do not have
 * them as their parent (and often have their parent being the same as the
 * containers parent). Such ArtFrag's can also be used as temporary collections
 * of ArtFrag's.
 * 
 * @author vineet
 */
public interface INonOwnerContainerFragment {

}
