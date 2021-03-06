package com.kobylynskyi.graphql.codegen.model;

import graphql.schema.DataFetchingEnvironment;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Freemarker-understandable format of method parameter and field definition
 *
 * @author kobylynskyi
 */
public class ParameterDefinition {

    public static final ParameterDefinition DATA_FETCHING_ENVIRONMENT = new ParameterDefinition(
            DataFetchingEnvironment.class.getName(), "env", null, emptyList(), emptyList(), false);

    private String type;
    private String name;
    private String defaultValue;
    private List<String> annotations = new ArrayList<>();
    private List<String> javaDoc = new ArrayList<>();
    private boolean deprecated;

    public ParameterDefinition() {
    }

    public ParameterDefinition(String type, String name, String defaultValue,
                               List<String> annotations,
                               List<String> javaDoc,
                               boolean deprecated) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
        this.annotations = annotations;
        this.javaDoc = javaDoc;
        this.deprecated = deprecated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public List<String> getJavaDoc() {
        return javaDoc;
    }

    public void setJavaDoc(List<String> javaDoc) {
        this.javaDoc = javaDoc;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
}
