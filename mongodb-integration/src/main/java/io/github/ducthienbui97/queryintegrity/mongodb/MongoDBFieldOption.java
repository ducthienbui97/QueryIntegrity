package io.github.ducthienbui97.queryintegrity.mongodb;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

/**
 * Configurations to create {@link org.bson.conversions.Bson} query filter
 */
@Data
@Builder
class MongoDBFieldOption {
    /**
     * Field name for the query.
     */
    @NonNull
    private String fieldName;
    /**
     * Operator on the field.
     */
    @NonNull
    private String operator;
    /**
     * Parameters on of the query.
     */
    @Singular
    private List<Object> parameters;
}
