
package com.app.model;

/**
 *
 * @author G4T6
 */
public class Location {

    private int locationId;
    private String semanticPlace;

    /**
     *Construct a Location Object with the following parameters
     * @param locationId a integer of location Id
     * @param semanticPlace a string of semantic Place
     */
    public Location(int locationId, String semanticPlace) {
        this.locationId = locationId;
        this.semanticPlace = semanticPlace;
    }

    /**
     *Gets the location Id
     * @return the location Id
     */
    public int getLocationId() {
        return locationId;
    }

    /**
     *Gets the Semantic Place
     * @return the semantic place
     */
    public String getSemanticPlace() {
        return semanticPlace;
    }

}
