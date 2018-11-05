package com.qdesrame.openapi.diff.compare.schemadiffresult;

import static com.qdesrame.openapi.diff.utils.ChangedUtils.isChanged;
import static java.util.Optional.ofNullable;

import com.qdesrame.openapi.diff.compare.MapKeyDiff;
import com.qdesrame.openapi.diff.compare.OpenApiDiff;
import com.qdesrame.openapi.diff.model.Change;
import com.qdesrame.openapi.diff.model.ChangedSchema;
import com.qdesrame.openapi.diff.model.DiffContext;
import com.qdesrame.openapi.diff.model.ListDiff;
import com.qdesrame.openapi.diff.model.schema.ChangedReadOnly;
import com.qdesrame.openapi.diff.model.schema.ChangedWriteOnly;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import java.util.*;
import lombok.Getter;

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

  public <T extends Schema<X>, X> Optional<ChangedSchema> diff(
      HashSet<String> refSet,
      Components leftComponents,
      Components rightComponents,
      T left,
      T right,
      DiffContext context) {
    changedSchema
        .setContext(context)
        .setOldSchema(left)
        .setNewSchema(right)
        .setChangeDeprecated(
            !Boolean.TRUE.equals(left.getDeprecated())
                && Boolean.TRUE.equals(right.getDeprecated()))
        .setChangeTitle(!Objects.equals(left.getTitle(), right.getTitle()))
        .setChangeRequired(ListDiff.diff(left.getRequired(), right.getRequired()))
        .setChangeDefault(!Objects.equals(left.getDefault(), right.getDefault()))
        .setChangeEnum(ListDiff.diff(left.getEnum(), right.getEnum()))
        .setChangeFormat(!Objects.equals(left.getFormat(), right.getFormat()))
        .setReadOnly(new ChangedReadOnly(context, left.getReadOnly(), right.getReadOnly()))
        .setWriteOnly(new ChangedWriteOnly(context, left.getWriteOnly(), right.getWriteOnly()))
        .setChangedMaxLength(!Objects.equals(left.getMaxLength(), right.getMaxLength()));

    openApiDiff
        .getExtensionsDiff()
        .diff(left.getExtensions(), right.getExtensions())
        .ifPresent(changedSchema::setExtensions);
    openApiDiff
        .getMetadataDiff()
        .diff(left.getDescription(), right.getDescription(), context)
        .ifPresent(changedSchema::setDescription);

    Map<String, Schema> leftProperties = null == left ? null : left.getProperties();
    Map<String, Schema> rightProperties = null == right ? null : right.getProperties();
    MapKeyDiff<String, Schema> propertyDiff = MapKeyDiff.diff(leftProperties, rightProperties);

    for (String key : propertyDiff.getSharedKey()) {
      openApiDiff
          .getSchemaDiff()
          .diff(
              refSet,
              leftProperties.get(key),
              rightProperties.get(key),
              required(context, key, right.getRequired()))
          .ifPresent(
              changedSchema1 -> changedSchema.getChangedProperties().put(key, changedSchema1));
    }

    compareAdditionalProperties(refSet, left, right, context);

    changedSchema
        .getIncreasedProperties()
        .putAll(filterProperties(Change.Type.ADDED, propertyDiff.getIncreased(), context));
    changedSchema
        .getMissingProperties()
        .putAll(filterProperties(Change.Type.REMOVED, propertyDiff.getMissing(), context));
    return isApplicable(context);
  }

  protected Optional<ChangedSchema> isApplicable(DiffContext context) {
    if (changedSchema.getReadOnly().isUnchanged()
        && changedSchema.getWriteOnly().isUnchanged()
        && !isPropertyApplicable(changedSchema.getNewSchema(), context)) {
      return Optional.empty();
    }
    return isChanged(changedSchema);
  }

  private Map<String, Schema> filterProperties(
      Change.Type type, Map<String, Schema> properties, DiffContext context) {
    Map<String, Schema> result = new LinkedHashMap<>();
    for (Map.Entry<String, Schema> entry : properties.entrySet()) {
      if (isPropertyApplicable(entry.getValue(), context)
          && openApiDiff
              .getExtensionsDiff()
              .isParentApplicable(
                  type,
                  entry.getValue(),
                  ofNullable(entry.getValue().getExtensions()).orElse(new LinkedHashMap()),
                  context)) {
        result.put(entry.getKey(), entry.getValue());
      } else {
        // Child property is not applicable, so required cannot be applied
        changedSchema.getChangeRequired().getIncreased().remove(entry.getKey());
      }
    }
    return result;
  }

  private boolean isPropertyApplicable(Schema schema, DiffContext context) {
    return !(context.isResponse() && Boolean.TRUE.equals(schema.getWriteOnly()))
        && !(context.isRequest() && Boolean.TRUE.equals(schema.getReadOnly()));
  }

  private DiffContext required(DiffContext context, String key, List<String> required) {
    return context.copyWithRequired(required != null && required.contains(key));
  }

  private void compareAdditionalProperties(
      HashSet<String> refSet, Schema leftSchema, Schema rightSchema, DiffContext context) {
    Object left = leftSchema.getAdditionalProperties();
    Object right = rightSchema.getAdditionalProperties();
    if ((left != null && left instanceof Schema) || (right != null && right instanceof Schema)) {
      Schema leftAdditionalSchema = (Schema) left;
      Schema rightAdditionalSchema = (Schema) right;
      ChangedSchema apChangedSchema =
          new ChangedSchema()
              .setContext(context)
              .setOldSchema(leftAdditionalSchema)
              .setNewSchema(rightAdditionalSchema);
      if (left != null && right != null) {
        Optional<ChangedSchema> addPropChangedSchemaOP =
            openApiDiff
                .getSchemaDiff()
                .diff(
                    refSet,
                    leftAdditionalSchema,
                    rightAdditionalSchema,
                    context.copyWithRequired(false));
        apChangedSchema = addPropChangedSchemaOP.orElse(apChangedSchema);
      }
      isChanged(apChangedSchema).ifPresent(changedSchema::setAddProp);
    }
  }
}
