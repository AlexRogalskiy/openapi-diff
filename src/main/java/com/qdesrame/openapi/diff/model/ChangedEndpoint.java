package com.qdesrame.openapi.diff.model;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;
import java.util.stream.Collectors;


public class ChangedEndpoint implements Changed {

    private String pathUrl;
    private PathItem oldPathItem;
    private PathItem newPathItem;

    private Map<PathItem.HttpMethod, Operation> newOperations;
    private Map<PathItem.HttpMethod, Operation> missingOperations;
    private Map<PathItem.HttpMethod, ChangedOperation> changedOperations;

    public ChangedEndpoint(String pathUrl, PathItem oldPathItem, PathItem newPathItem) {
        this.pathUrl = pathUrl;
        this.oldPathItem = oldPathItem;
        this.newPathItem = newPathItem;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    public PathItem getOldPathItem() {
        return oldPathItem;
    }

    public PathItem getNewPathItem() {
        return newPathItem;
    }

    public Map<PathItem.HttpMethod, Operation> getNewOperations() {
        return newOperations;
    }

    public void setNewOperations(Map<PathItem.HttpMethod, Operation> newOperations) {
        this.newOperations = newOperations;
    }

    public Map<PathItem.HttpMethod, Operation> getMissingOperations() {
        return missingOperations;
    }

    public void setMissingOperations(
            Map<PathItem.HttpMethod, Operation> missingOperations) {
        this.missingOperations = missingOperations;
    }


    public Map<PathItem.HttpMethod, ChangedOperation> getChangedOperations() {
        return changedOperations;
    }

    public Map<PathItem.HttpMethod, Operation> getDeprecatedOperations() {
        return changedOperations.entrySet()
                .stream()
                .filter(p -> p.getValue().isDeprecated())
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue().getNewOperation()));
    }

    public void setChangedOperations(
            Map<PathItem.HttpMethod, ChangedOperation> changedOperations) {
        this.changedOperations = changedOperations;
    }

    public boolean isDiff() {
        return !newOperations.isEmpty()
                || !missingOperations.isEmpty()
                || !changedOperations.isEmpty();
    }

    @Override
    public boolean isDiffBackwardCompatible() {
        return missingOperations.isEmpty()
                && changedOperations.values().stream().allMatch(c -> c.isDiffBackwardCompatible());
    }

    @Override
    public String toString() {
        return String.format("Changed endpoint '%s': %d missing, %d changed", pathUrl, getMissingOperations().size(), getChangedOperations().size());
    }

}
