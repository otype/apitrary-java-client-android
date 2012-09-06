package com.apitrary.clientsdks.android;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Apitrary entity class. Instances of this class have four standard fields: id, name, createdAt and updatedAt.
 * For additional properties use the get(), put() and delete() methods. 
 * 
 * @author Sebastian Engel <se@apitrary.com>
 *
 */
public class APYEntity {

    /**
     * The id used to identify the entity.
     */
    private String id;

    /**
     * The name of the entity matching an entity's name in an apitrary API.
     */
    private String name;

    /**
     * Time when this entity was initially created (in milliseconds since Jan. 1, 1970, midnight GMT).
     */
    private long createdAt;

    /**
     * Time when this entity was lastly updated (in milliseconds since Jan. 1, 1970, midnight GMT).
     */
    private long updatedAt;

    /**
     * The properties of this entity as a map of key/value pairs.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Constructs an instance of {@link APYEntity} with the given name.
     * 
     * @param name
     *            case insensitive name of the entity. The name must match an
     *            entity's name of your apitrary API.
     * @throws IllegalArgumentException
     *             when the given name is null or empty
     */
    public APYEntity(String name) throws IllegalArgumentException {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("The entity's name must not be null or empty.");
        }
        this.name = name.trim();
    }

    /**
     * Returns the value for the given key.
     * 
     * @param propertyKey
     *            the key of the property to get the value for.
     * @return the value of the property matching the given key or null if none
     *         matches.
     */
    public String get(String propertyKey) {
        return properties.get(propertyKey);
    }

    /**
     * Adds a property for the given key/value pair to this entity or updates
     * the value if a property with the given key already exists.
     * 
     * @param propertyKey
     *            the property's key
     * @param propertyValue
     *            the value to set
     * @throws IllegalArgumentException
     *             when the property key is null or empty
     */
    public void put(String propertyKey, String propertyValue) throws IllegalArgumentException {
        if (APYUtils.isNullOrEmpty(propertyKey)) {
            throw new IllegalArgumentException("The property key must not be null or empty.");
        }
        properties.put(propertyKey, propertyValue);
    }

    /**
     * Removes the property matching the given key.
     * 
     * @param propertyKey
     *            the key of the property to delete
     */
    public void remove(String propertyKey) {
        properties.remove(propertyKey);
    }

    /**
     * Returns the name of the entity.
     * 
     * @return the name of the entity as a {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of the entity. The entity only has an id when it is
     * already persisted.
     * 
     * @return the id of the entity or null if this entity has no id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the entity id.
     * 
     * @param id the entity id to set
     */
    void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the time this entity was initially created.
     *
     * @return the time this entity was initially created (in milliseconds since Jan. 1, 1970, midnight GMT).
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the time this entity was initially created.
     *
     * @param createdAt the time this entity was initially created (in milliseconds since Jan. 1, 1970, midnight GMT).
     */
    void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;   
    }

    /**
     * Returns the time this entity was lastly updated created.
     *
     * @return the time this entity was lastly updated (in milliseconds since Jan. 1, 1970, midnight GMT).
     */
    public long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the time this entity was lastly updated.
     *
     * @param updatedAt the time this entity was lastly updated (in milliseconds since Jan. 1, 1970, midnight GMT).
     */
    void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;   
    }

    /**
     * Returns a copy of the entity's {@link Map} of properties.
     * 
     * @return a copy of the entity's {@link Map} of properties
     */
    Map<String, String> getProperties() {
        return new LinkedHashMap<String, String>(properties);
    }

    @Override
    public String toString() {
        return "Entity '" + name + "'\n" + properties.toString();
    }

}
