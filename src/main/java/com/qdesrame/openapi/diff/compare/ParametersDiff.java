package com.qdesrame.openapi.diff.compare;

import com.qdesrame.openapi.diff.model.ChangedParameter;
import com.qdesrame.openapi.diff.model.ChangedParameters;
import com.qdesrame.openapi.diff.utils.RefPointer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * compare two parameter
 *
 * @author QDesrame
 */
public class ParametersDiff {

    private Components leftComponents;
    private Components rightComponents;
    private OpenApiDiff openApiDiff;

    public ParametersDiff(OpenApiDiff openApiDiff) {
        this.openApiDiff = openApiDiff;
        this.leftComponents = openApiDiff.getOldSpecOpenApi() != null ? openApiDiff.getOldSpecOpenApi().getComponents() : null;
        this.rightComponents = openApiDiff.getNewSpecOpenApi() != null ? openApiDiff.getNewSpecOpenApi().getComponents() : null;
    }

    public static Optional<Parameter> contains(Components components, List<Parameter> parameters, Parameter parameter) {
        return parameters.stream().filter(param -> same(RefPointer.Replace.parameter(components, param), parameter)).findFirst();
    }

    public static boolean same(Parameter left, Parameter right) {
        return Objects.equals(left.getName(), right.getName()) && Objects.equals(left.getIn(), right.getIn());
    }

    public ChangedParameters diff(List<Parameter> left, List<Parameter> right) {
        ChangedParameters result = new ChangedParameters(left, right != null ? new ArrayList<>(right) : null);
        if (null == left) left = new ArrayList<>();
        if (null == right) right = new ArrayList<>();

        for (Parameter leftPara : left) {
            RefPointer.Replace.parameter(leftComponents, leftPara);
            Optional<Parameter> rightParam = contains(rightComponents, right, leftPara);
            if (!rightParam.isPresent()) {
                result.getMissing().add(leftPara);
            } else {
                Parameter rightPara = rightParam.get();
                right.remove(rightPara);
                ChangedParameter changedParameter = openApiDiff.getParameterDiff().diff(leftPara, rightPara);
                if (changedParameter.isDiff()) {
                    result.getChanged().add(changedParameter);
                }
            }
        }
        result.getIncreased().addAll(right);
        return result;
    }
}
