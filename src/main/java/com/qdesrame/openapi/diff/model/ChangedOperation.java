package com.qdesrame.openapi.diff.model;

import com.qdesrame.openapi.diff.compare.ParameterDiffResult;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.responses.ApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChangedOperation implements Changed {

    private String summary;

    private List<Parameter> addParameters = new ArrayList<Parameter>();
    private List<Parameter> missingParameters = new ArrayList<Parameter>();

    private List<ParameterDiffResult> changedParameter = new ArrayList<>();

    private Map<String, ApiResponse> missingResponses;
    private Map<String, ApiResponse> addResponses;
    private Map<String, ChangedResponse> changedResponses;
    private boolean deprecated;
    private Map<String, MediaType> missingRequestMediaTypes;
    private Map<String, MediaType> addRequestMediaTypes;
    private Map<String, ChangedMediaType> changedRequestMediaTypes;

    public ChangedOperation() {
        missingRequestMediaTypes = new HashMap<>();
        addRequestMediaTypes = new HashMap<>();
        changedRequestMediaTypes = new HashMap<>();
    }

    public List<Parameter> getAddParameters() {
        return addParameters;
    }

    public void setAddParameters(List<Parameter> addParameters) {
        this.addParameters = addParameters;
    }

    public List<Parameter> getMissingParameters() {
        return missingParameters;
    }

    public void setMissingParameters(List<Parameter> missingParameters) {
        this.missingParameters = missingParameters;
    }

    public List<ParameterDiffResult> getChangedParameter() {
        return changedParameter;
    }

    public void setChangedParameter(List<ParameterDiffResult> changedParameter) {
        this.changedParameter = changedParameter;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public boolean isDiff() {
        return isDiffParam() || isDiffRequest() || isDiffResponse();
    }

    public boolean isDiffParam() {
        return !addParameters.isEmpty() || !missingParameters.isEmpty()
                || !changedParameter.isEmpty();
    }

    public boolean isDiffResponse() {
        return !addResponses.isEmpty() || !missingResponses.isEmpty() || !changedResponses.isEmpty();
    }

    public boolean isDiffRequest() {
        return !addRequestMediaTypes.isEmpty() || !missingRequestMediaTypes.isEmpty() || !changedRequestMediaTypes.isEmpty();
    }

    public void setAddResponses(Map<String, ApiResponse> addResponses) {
        this.addResponses = addResponses;
    }

    public Map<String, ApiResponse> getAddResponses() {
        return addResponses;
    }

    public void setMissingResponses(Map<String, ApiResponse> missingResponses) {
        this.missingResponses = missingResponses;
    }

    public Map<String, ApiResponse> getMissingResponses() {
        return missingResponses;
    }

    public Map<String, ChangedResponse> getChangedResponses() {
        return changedResponses;
    }

    public ChangedOperation setChangedResponses(Map<String, ChangedResponse> changedResponses) {
        this.changedResponses = changedResponses;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Map<String, MediaType> getMissingRequestMediaTypes() {
        return missingRequestMediaTypes;
    }

    public void setMissingRequestMediaTypes(Map<String, MediaType> missingRequestMediaTypes) {
        this.missingRequestMediaTypes = missingRequestMediaTypes;
    }

    public Map<String, MediaType> getAddRequestMediaTypes() {
        return addRequestMediaTypes;
    }

    public void setAddRequestMediaTypes(Map<String, MediaType> addRequestMediaTypes) {
        this.addRequestMediaTypes = addRequestMediaTypes;
    }

    public Map<String, ChangedMediaType> getChangedRequestMediaTypes() {
        return changedRequestMediaTypes;
    }

    public void setChangedRequestMediaTypes(Map<String, ChangedMediaType> changedRequestMediaTypes) {
        this.changedRequestMediaTypes = changedRequestMediaTypes;
    }
}
