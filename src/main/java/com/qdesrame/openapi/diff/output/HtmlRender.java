package com.qdesrame.openapi.diff.output;

import com.qdesrame.openapi.diff.OpenApiDiff;
import com.qdesrame.openapi.diff.compare.ParameterDiffResult;
import com.qdesrame.openapi.diff.model.ChangedEndpoint;
import com.qdesrame.openapi.diff.model.ChangedOperation;
import com.qdesrame.openapi.diff.model.ElSchema;
import com.qdesrame.openapi.diff.model.Endpoint;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import j2html.tags.ContainerTag;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static j2html.TagCreator.*;

public class HtmlRender implements Render {

    private String title;
    private String linkCss;

    public HtmlRender() {
        this("Api Change Log", "http://deepoove.com/swagger-diff/stylesheets/demo.css");
    }

    public HtmlRender(String title, String linkCss) {
        this.title = title;
        this.linkCss = linkCss;
    }


    public String render(OpenApiDiff diff) {
        List<Endpoint> newEndpoints = diff.getNewEndpoints();
        ContainerTag ol_newEndpoint = ol_newEndpoint(newEndpoints);

        List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
        ContainerTag ol_missingEndpoint = ol_missingEndpoint(missingEndpoints);

        List<Endpoint> deprecatedEndpoints = diff.getDeprecatedEndpoints();
        ContainerTag ol_deprecatedEndpoint = ol_deprecatedEndpoint(deprecatedEndpoints);

        List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
        ContainerTag ol_changed = ol_changed(changedEndpoints);

        return renderHtml(ol_newEndpoint, ol_missingEndpoint, ol_deprecatedEndpoint, ol_changed);
    }

    public String renderHtml(ContainerTag ol_new, ContainerTag ol_miss, ContainerTag ol_deprec, ContainerTag ol_changed) {
        ContainerTag html = html().attr("lang", "en").with(
                head().with(
                        meta().withCharset("utf-8"),
                        title(title),
                        link().withRel("stylesheet").withHref(linkCss)
                ),
                body().with(
                        header().with(h1(title)),
                        div().withClass("article").with(
                                div().with(h2("What's New"), hr(), ol_new),
                                div().with(h2("What's Deleted"), hr(), ol_miss),
                                div().with(h2("What's Deprecated"), hr(), ol_deprec),
                                div().with(h2("What's Changed"), hr(), ol_changed)
                        )
                )
        );

        return document().render() + html.render();
    }


    private ContainerTag ol_newEndpoint(List<Endpoint> endpoints) {
        if (null == endpoints) return ol();
        ContainerTag ol = ol();
        for (Endpoint endpoint : endpoints) {
            ol.with(li_newEndpoint(endpoint.getMethod().toString(),
                    endpoint.getPathUrl(), endpoint.getSummary()));
        }
        return ol;
    }

    private ContainerTag li_newEndpoint(String method, String path,
                                        String desc) {
        return li().with(span(method).withClass(method)).withText(path + " ")
                .with(span(desc));
    }

    private ContainerTag ol_missingEndpoint(List<Endpoint> endpoints) {
        if (null == endpoints) return ol();
        ContainerTag ol = ol();
        for (Endpoint endpoint : endpoints) {
            ol.with(li_missingEndpoint(endpoint.getMethod().toString(),
                    endpoint.getPathUrl(), endpoint.getSummary()));
        }
        return ol;
    }

    private ContainerTag li_missingEndpoint(String method, String path,
                                            String desc) {
        return li().with(span(method).withClass(method),
                del().withText(path)).with(span(" " + desc));
    }

    private ContainerTag ol_deprecatedEndpoint(List<Endpoint> endpoints) {
        if (null == endpoints) return ol();
        ContainerTag ol = ol();
        for (Endpoint endpoint : endpoints) {
            ol.with(li_deprecatedEndpoint(endpoint.getMethod().toString(),
                    endpoint.getPathUrl(), endpoint.getSummary()));
        }
        return ol;
    }

    private ContainerTag li_deprecatedEndpoint(String method, String path,
                                            String desc) {
        return li().with(span(method).withClass(method),
                del().withText(path)).with(span(" " + desc));
    }

    private ContainerTag ol_changed(List<ChangedEndpoint> changedEndpoints) {
        if (null == changedEndpoints) return ol();
        ContainerTag ol = ol();
        for (ChangedEndpoint changedEndpoint : changedEndpoints) {
            String pathUrl = changedEndpoint.getPathUrl();
            Map<PathItem.HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();
            for (Entry<PathItem.HttpMethod, ChangedOperation> entry : changedOperations.entrySet()) {
                String method = entry.getKey().toString();
                ChangedOperation changedOperation = entry.getValue();
                String desc = changedOperation.getSummary();

                ContainerTag ul_detail = ul().withClass("detail");
                if (changedOperation.isDiffParam()) {
                    ul_detail.with(li().with(h3("Parameter")).with(ul_param(changedOperation)));
                }
                if (changedOperation.isDiffProp()) {
                    ul_detail.with(li().with(h3("Return Type")).with(ul_response(changedOperation)));
                }
                ol.with(li().with(span(method).withClass(method)).withText(pathUrl + " ").with(span(desc))
                        .with(ul_detail));
            }
        }
        return ol;
    }

    private ContainerTag ul_response(ChangedOperation changedOperation) {
        List<ElSchema> addProps = changedOperation.getAddProps();
        List<ElSchema> delProps = changedOperation.getMissingProps();
        ContainerTag ul = ul().withClass("change response");
        for (ElSchema prop : addProps) {
            ul.with(li_addProp(prop));
        }
        for (ElSchema prop : delProps) {
            ul.with(li_missingProp(prop));
        }
        return ul;
    }

    private ContainerTag li_missingProp(ElSchema prop) {
        Schema schema = prop.getSchema();
        return li().withClass("missing").withText("Delete").with(del(prop.getEl())).with(span(null == schema.getDescription() ? "" : ("//" + schema.getDescription())).withClass("comment"));
    }

    private ContainerTag li_addProp(ElSchema prop) {
        Schema schema = prop.getSchema();
        return li().withText("Add " + prop.getEl()).with(span(null == schema.getDescription() ? "" : ("//" + schema.getDescription())).withClass("comment"));
    }

    private ContainerTag ul_param(ChangedOperation changedOperation) {
        List<Parameter> addParameters = changedOperation.getAddParameters();
        List<Parameter> delParameters = changedOperation.getMissingParameters();
        List<ParameterDiffResult> changedParameters = changedOperation.getChangedParameter();
        ContainerTag ul = ul().withClass("change param");
        for (Parameter param : addParameters) {
            ul.with(li_addParam(param));
        }
        for (ParameterDiffResult param : changedParameters) {
            boolean changeRequired = param.isChangeRequired();
            boolean changeDescription = param.isChangeDescription();
            if (changeRequired || changeDescription)
                ul.with(li_changedParam(param));
        }
        for (Parameter param : delParameters) {
            ul.with(li_missingParam(param));
        }
        return ul;
    }

    private ContainerTag li_addParam(Parameter param) {
        return li().withText("Add " + param.getName()).with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
    }

    private ContainerTag li_missingParam(Parameter param) {
        return li().withClass("missing").with(span("Delete")).with(del(param.getName())).with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
    }

    private ContainerTag li_changedParam(ParameterDiffResult changeParam) {
        boolean changeRequired = changeParam.isChangeRequired();
        boolean changeDescription = changeParam.isChangeDescription();
        Parameter rightParam = changeParam.getRightParameter();
        Parameter leftParam = changeParam.getLeftParameter();
        ContainerTag li = li().withText(rightParam.getName());
        if (changeRequired) {
            li.withText(" change into " + (rightParam.getRequired() ? "required" : "not required"));
        }
        if (changeDescription) {
            li.withText(" Notes ").with(del(leftParam.getDescription()).withClass("comment")).withText(" change into ").with(span(rightParam.getDescription()).withClass("comment"));
        }
        return li;
    }

}
