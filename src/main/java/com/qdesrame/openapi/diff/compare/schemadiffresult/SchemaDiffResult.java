package com.qdesrame.openapi.diff.compare.schemadiffresult;

import com.qdesrame.openapi.diff.compare.MapKeyDiff;
import com.qdesrame.openapi.diff.compare.OpenApiDiff;
import com.qdesrame.openapi.diff.model.ChangedSchema;
import com.qdesrame.openapi.diff.model.ListDiff;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
public class SchemaDiffResult {
    protected ChangedSchema changedSchema;
    protected OpenApiDiff openApiDiff;

    public SchemaDiffResult(OpenApiDiff openApiDiff) {
        this.openApiDiff = openApiDiff;
        this.changedSchema = new ChangedSchema();
    }

    public SchemaDiffResult(String type, OpenApiDiff openApiDiff) {
        this(openApiDiff);
        this.changedSchema.setType(type);
    }

    public Optional<ChangedSchema> diff(HashSet<String> refSet, Components leftComponents, Components rightComponents, Schema left, Schema right) {
        changedSchema.setOldSchema(left);
        changedSchema.setNewSchema(right);
        changedSchema.setChangeDeprecated(!Boolean.TRUE.equals(left.getDeprecated()) && Boolean.TRUE.equals(right.getDeprecated()));
        changedSchema.setChangeDescription(!Objects.equals(left.getDescription(), right.getDescription()));
        changedSchema.setChangeTitle(!Objects.equals(left.getTitle(), right.getTitle()));
        changedSchema.setChangeRequired(ListDiff.diff(left.getRequired(), right.getRequired()));
        changedSchema.setChangeDefault(!Objects.equals(left.getDefault(), right.getDefault()));
        changedSchema.setChangeEnum(ListDiff.diff(left.getEnum(), right.getEnum()));
        changedSchema.setChangeFormat(!Objects.equals(left.getFormat(), right.getFormat()));
        changedSchema.setChangeReadOnly(!Boolean.TRUE.equals(left.getReadOnly()) && Boolean.TRUE.equals(right.getReadOnly()));
        changedSchema.setChangeWriteOnly(!Boolean.TRUE.equals(left.getWriteOnly()) && Boolean.TRUE.equals(right.getWriteOnly()));
        changedSchema.setChangedMaxLength(!Objects.equals(left.getMaxLength(), right.getMaxLength()));

        Map<String, Schema> leftProperties = null == left ? null : left.getProperties();
        Map<String, Schema> rightProperties = null == right ? null : right.getProperties();
        MapKeyDiff<String, Schema> propertyDiff = MapKeyDiff.diff(leftProperties, rightProperties);
        Map<String, Schema> increasedProp = propertyDiff.getIncreased();
        Map<String, Schema> missingProp = propertyDiff.getMissing();

        for (String key : propertyDiff.getSharedKey()) {
//            openApiDiff.getSchemaDiff().diff(leftProperties.get(key), rightProperties.get(key))
//                    .ifPresent(resultSchema -> );
            Optional<ChangedSchema> resultSchema = openApiDiff.getSchemaDiff().diff(refSet, leftProperties.get(key), rightProperties.get(key));
            if (resultSchema.isPresent() && resultSchema.get().isDiff()) {
                changedSchema.getChangedProperties().put(key, resultSchema.get());
            }
        }

        if(left.getAdditionalProperties() != null || right.getAdditionalProperties() != null) {
            ChangedSchema apChangedSchema = new ChangedSchema();
            apChangedSchema.setOldSchema(left.getAdditionalProperties());
            apChangedSchema.setNewSchema(right.getAdditionalProperties());
            if(left.getAdditionalProperties() != null && right.getAdditionalProperties() != null) {
                Optional<ChangedSchema> addPropChangedSchemaOP
                        = openApiDiff.getSchemaDiff().diff(refSet, left.getAdditionalProperties(), right.getAdditionalProperties());
                addPropChangedSchemaOP.ifPresent(x -> changedSchema.setAddPropChangedSchema(x));
            } else {
                changedSchema.setAddPropChangedSchema(apChangedSchema);
            }
        }

        changedSchema.getIncreasedProperties().putAll(increasedProp);
        changedSchema.getMissingProperties().putAll(missingProp);
        return changedSchema.isDiff()? Optional.of(changedSchema): Optional.empty();
    }

}
