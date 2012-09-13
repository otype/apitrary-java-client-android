package com.apitrary.sdk;

/**
 * Class defining constants.
 */
abstract class APYConstants {

    private APYConstants() {}

    // Entity constants

    /**
     * String identifying the key of a result object's ID property.
     */
    static final String KEY_RESULT_OBJECT_ID = "_id";

    /**
     * String identifying the key of the entity's "createdAt" property.
     */
    static final String KEY_ENTITY_CREATED_AT = "_createdAt";

    /**
     * String identifying the key of the entity's "updatedAt" property.
     */
    static final String KEY_ENTITY_UPDATED_AT = "_updatedAt";

    // JSON response constants

    /**
     * String identifying the key of the response "statusCode" property.
     */
    static final String KEY_RESPONSE_STATUS_CODE = "statusCode";

    /**
     * String identifying the key of the response "statusMessage" property.
     */
    static final String KEY_RESPONSE_STATUS_MESSAGE = "statusMessage";

    /**
     * String identifying the key of the response "result" property.
     */
    static final String KEY_RESPONSE_RESULT = "result";

    /**
     * String identifying the key of a JSON response object's '_id' property.
     */
    static final String KEY_RESPONSE_OBJECT_ID = "_id";

    /**
     * String identifying the key of a JSON response object's 'data' property.
     */
    static final String KEY_RESPONSE_OBJECT_DATA = "_data";

}
