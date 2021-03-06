package io.github.kobylynskyi.graphql.codegen.gradle;

import com.kobylynskyi.graphql.codegen.GraphQLCodegen;
import com.kobylynskyi.graphql.codegen.model.ApiNamePrefixStrategy;
import com.kobylynskyi.graphql.codegen.model.ApiRootInterfaceStrategy;
import com.kobylynskyi.graphql.codegen.model.GraphQLCodegenConfiguration;
import com.kobylynskyi.graphql.codegen.model.MappingConfig;
import com.kobylynskyi.graphql.codegen.model.MappingConfigConstants;
import com.kobylynskyi.graphql.codegen.supplier.JsonMappingConfigSupplier;
import com.kobylynskyi.graphql.codegen.supplier.MappingConfigSupplier;
import com.kobylynskyi.graphql.codegen.supplier.SchemaFinder;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gradle task for GraphQL code generation
 *
 * @author kobylynskyi
 */
public class GraphQLCodegenGradleTask extends DefaultTask implements GraphQLCodegenConfiguration {

    private List<String> graphqlSchemaPaths;
    private final SchemaFinderConfig graphqlSchemas = new SchemaFinderConfig();
    private File outputDir;

    private Map<String, String> customTypesMapping = new HashMap<>();
    private Map<String, String> customAnnotationsMapping = new HashMap<>();
    private String packageName;
    private String apiPackageName;
    private ApiNamePrefixStrategy apiNamePrefixStrategy = MappingConfigConstants.DEFAULT_API_NAME_PREFIX_STRATEGY;
    private ApiRootInterfaceStrategy apiRootInterfaceStrategy = MappingConfigConstants.DEFAULT_API_ROOT_INTERFACE_STRATEGY;
    private String apiNamePrefix;
    private String apiNameSuffix = MappingConfigConstants.DEFAULT_RESOLVER_SUFFIX;;
    private String typeResolverPrefix;
    private String typeResolverSuffix = MappingConfigConstants.DEFAULT_RESOLVER_SUFFIX;
    private String modelPackageName;
    private String modelNamePrefix;
    private String modelNameSuffix;
    private String subscriptionReturnType;
    private Boolean generateBuilder = MappingConfigConstants.DEFAULT_BUILDER;
    private Boolean generateApis = MappingConfigConstants.DEFAULT_GENERATE_APIS;
    private String modelValidationAnnotation;
    private Boolean generateEqualsAndHashCode = MappingConfigConstants.DEFAULT_EQUALS_AND_HASHCODE;
    private Boolean generateImmutableModels = MappingConfigConstants.DEFAULT_GENERATE_IMMUTABLE_MODELS;
    private Boolean generateToString = MappingConfigConstants.DEFAULT_TO_STRING;
    private Boolean generateAsyncApi = MappingConfigConstants.DEFAULT_GENERATE_ASYNC_APIS;
    private Boolean generateParameterizedFieldsResolvers = MappingConfigConstants.DEFAULT_GENERATE_PARAMETERIZED_FIELDS_RESOLVERS;
    private Boolean generateExtensionFieldsResolvers = MappingConfigConstants.DEFAULT_GENERATE_EXTENSION_FIELDS_RESOLVERS;
    private Boolean generateDataFetchingEnvironmentArgumentInApis = MappingConfigConstants.DEFAULT_GENERATE_DATA_FETCHING_ENV;
    private Boolean generateModelsForRootTypes = MappingConfigConstants.DEFAULT_GENERATE_MODELS_FOR_ROOT_TYPES;
    private Set<String> fieldsWithResolvers = new HashSet<>();
    private Set<String> fieldsWithoutResolvers = new HashSet<>();


    private Boolean generateClient;
    private String requestSuffix;
    private String responseSuffix;
    private String responseProjectionSuffix;
    private String parametrizedInputSuffix;
    private final ParentInterfacesConfig parentInterfaces = new ParentInterfacesConfig();
    private String jsonConfigurationFile;

    public GraphQLCodegenGradleTask() {
        setGroup("codegen");
        setDescription("Generates Java POJOs and interfaces based on GraphQL schemas");
    }

    @TaskAction
    public void generate() throws Exception {
        MappingConfig mappingConfig = new MappingConfig();
        mappingConfig.setPackageName(packageName);
        mappingConfig.setCustomTypesMapping(customTypesMapping != null ? customTypesMapping : new HashMap<>());
        mappingConfig.setApiNameSuffix(apiNameSuffix);
        mappingConfig.setApiNamePrefix(apiNamePrefix);
        mappingConfig.setApiRootInterfaceStrategy(apiRootInterfaceStrategy);
        mappingConfig.setApiNamePrefixStrategy(apiNamePrefixStrategy);
        mappingConfig.setModelNamePrefix(modelNamePrefix);
        mappingConfig.setModelNameSuffix(modelNameSuffix);
        mappingConfig.setApiPackageName(apiPackageName);
        mappingConfig.setModelPackageName(modelPackageName);
        mappingConfig.setGenerateBuilder(generateBuilder);
        mappingConfig.setGenerateApis(generateApis);
        mappingConfig.setTypeResolverSuffix(typeResolverSuffix);
        mappingConfig.setTypeResolverPrefix(typeResolverPrefix);
        mappingConfig.setModelValidationAnnotation(modelValidationAnnotation);
        mappingConfig.setCustomAnnotationsMapping(customAnnotationsMapping != null ? customAnnotationsMapping : new HashMap<>());
        mappingConfig.setGenerateEqualsAndHashCode(generateEqualsAndHashCode);
        mappingConfig.setGenerateImmutableModels(generateImmutableModels);
        mappingConfig.setGenerateToString(generateToString);
        mappingConfig.setSubscriptionReturnType(subscriptionReturnType);
        mappingConfig.setGenerateAsyncApi(generateAsyncApi);
        mappingConfig.setGenerateParameterizedFieldsResolvers(generateParameterizedFieldsResolvers);
        mappingConfig.setGenerateDataFetchingEnvironmentArgumentInApis(generateDataFetchingEnvironmentArgumentInApis);
        mappingConfig.setGenerateExtensionFieldsResolvers(generateExtensionFieldsResolvers);
        mappingConfig.setGenerateModelsForRootTypes(generateModelsForRootTypes);
        mappingConfig.setFieldsWithResolvers(fieldsWithResolvers != null ? fieldsWithResolvers : new HashSet<>());
        mappingConfig.setFieldsWithoutResolvers(fieldsWithoutResolvers != null ? fieldsWithoutResolvers : new HashSet<>());
        mappingConfig.setGenerateClient(generateClient);
        mappingConfig.setRequestSuffix(requestSuffix);
        mappingConfig.setResponseSuffix(responseSuffix);
        mappingConfig.setResponseProjectionSuffix(responseProjectionSuffix);
        mappingConfig.setParametrizedInputSuffix(parametrizedInputSuffix);
        mappingConfig.setResolverParentInterface(getResolverParentInterface());
        mappingConfig.setQueryResolverParentInterface(getQueryResolverParentInterface());
        mappingConfig.setMutationResolverParentInterface(getMutationResolverParentInterface());
        mappingConfig.setSubscriptionResolverParentInterface(getSubscriptionResolverParentInterface());

        new GraphQLCodegen(getActualSchemaPaths(), outputDir, mappingConfig, buildJsonSupplier()).generate();
    }

    // This is only public so that it can be part of the inputs.
    // Changes to schema contents need to invalidate this task, and require re-run.
    // Using the SchemaFinder, we need to calculate the resulting list of paths as @InputFiles for it to work.
    @InputFiles
    public List<String> getActualSchemaPaths() throws IOException {
        if (graphqlSchemaPaths != null) {
            return graphqlSchemaPaths;
        }
        Path rootDir = getSchemasRootDir();
        SchemaFinder finder = new SchemaFinder(rootDir);
        finder.setRecursive(graphqlSchemas.isRecursive());
        finder.setIncludePattern(graphqlSchemas.getIncludePattern());
        finder.setExcludedFiles(graphqlSchemas.getExcludedFiles());
        return finder.findSchemas();
    }

    private Path getSchemasRootDir() {
        String rootDir = graphqlSchemas.getRootDir();
        if (rootDir == null) {
            return findDefaultResourcesDir().orElseThrow(() -> new IllegalStateException(
                    "Default resource folder not found, please provide graphqlSchemas.rootDir"));
        }
        return Paths.get(rootDir);
    }

    private java.util.Optional<Path> findDefaultResourcesDir() {
        return getProject().getConvention()
                .getPlugin(JavaPluginConvention.class)
                .getSourceSets()
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                .getResources()
                .getSourceDirectories()
                .getFiles()
                .stream()
                .findFirst()
                .map(File::toPath);
    }

    private MappingConfigSupplier buildJsonSupplier() {
        if (jsonConfigurationFile != null && !jsonConfigurationFile.isEmpty()) {
            return new JsonMappingConfigSupplier(jsonConfigurationFile);
        }
        return null;
    }

    @InputFiles
    @Optional
    public List<String> getGraphqlSchemaPaths() {
        return graphqlSchemaPaths;
    }

    public void setGraphqlSchemaPaths(List<String> graphqlSchemaPaths) {
        this.graphqlSchemaPaths = graphqlSchemaPaths;
    }

    @Nested
    @Optional
    public SchemaFinderConfig getGraphqlSchemas() {
        return graphqlSchemas;
    }

    public void graphqlSchemas(Action<? super SchemaFinderConfig> action) {
        action.execute(graphqlSchemas);
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @Input
    @Optional
    @Override
    public Map<String, String> getCustomTypesMapping() {
        return customTypesMapping;
    }

    public void setCustomTypesMapping(Map<String, String> customTypesMapping) {
        this.customTypesMapping = customTypesMapping;
    }

    @Input
    @Optional
    @Override
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Input
    @Optional
    @Override
    public String getModelNamePrefix() {
        return modelNamePrefix;
    }

    public void setModelNameSuffix(String modelNameSuffix) {
        this.modelNameSuffix = modelNameSuffix;
    }

    @Input
    @Optional
    @Override
    public String getModelNameSuffix() {
        return modelNameSuffix;
    }

    public void setModelNamePrefix(String modelNamePrefix) {
        this.modelNamePrefix = modelNamePrefix;
    }

    @Input
    @Optional
    @Override
    public String getApiPackageName() {
        return apiPackageName;
    }

    public void setApiPackageName(String apiPackageName) {
        this.apiPackageName = apiPackageName;
    }

    @Input
    @Optional
    @Override
    public ApiRootInterfaceStrategy getApiRootInterfaceStrategy() {
        return apiRootInterfaceStrategy;
    }

    public void setApiRootInterfaceStrategy(ApiRootInterfaceStrategy apiRootInterfaceStrategy) {
        this.apiRootInterfaceStrategy = apiRootInterfaceStrategy;
    }

    @Input
    @Optional
    @Override
    public ApiNamePrefixStrategy getApiNamePrefixStrategy() {
        return apiNamePrefixStrategy;
    }

    public void setApiNamePrefixStrategy(ApiNamePrefixStrategy apiNamePrefixStrategy) {
        this.apiNamePrefixStrategy = apiNamePrefixStrategy;
    }

    @Input
    @Optional
    @Override
    public String getApiNamePrefix() {
        return apiNamePrefix;
    }

    public void setApiNamePrefix(String apiNamePrefix) {
        this.apiNamePrefix = apiNamePrefix;
    }

    @Input
    @Optional
    @Override
    public String getApiNameSuffix() {
        return apiNameSuffix;
    }

    public void setApiNameSuffix(String apiNameSuffix) {
        this.apiNameSuffix = apiNameSuffix;
    }

    @Input
    @Optional
    @Override
    public String getModelPackageName() {
        return modelPackageName;
    }

    public void setModelPackageName(String modelPackageName) {
        this.modelPackageName = modelPackageName;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateBuilder() {
        return generateBuilder;
    }

    public void setGenerateBuilder(Boolean generateBuilder) {
        this.generateBuilder = generateBuilder;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateApis() {
        return generateApis;
    }

    public void setGenerateApis(Boolean generateApis) {
        this.generateApis = generateApis;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateModelsForRootTypes() {
        return generateModelsForRootTypes;
    }

    public void setGenerateModelsForRootTypes(Boolean generateModelsForRootTypes) {
        this.generateModelsForRootTypes = generateModelsForRootTypes;
    }

    @Input
    @Optional
    @Override
    public String getModelValidationAnnotation() {
        return modelValidationAnnotation;
    }

    public void setModelValidationAnnotation(String modelValidationAnnotation) {
        this.modelValidationAnnotation = modelValidationAnnotation;
    }

    @Input
    @Optional
    @Override
    public Map<String, String> getCustomAnnotationsMapping() {
        return customAnnotationsMapping;
    }

    public void setCustomAnnotationsMapping(Map<String, String> customAnnotationsMapping) {
        this.customAnnotationsMapping = customAnnotationsMapping;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateEqualsAndHashCode() {
        return generateEqualsAndHashCode;
    }

    public void setGenerateEqualsAndHashCode(Boolean generateEqualsAndHashCode) {
        this.generateEqualsAndHashCode = generateEqualsAndHashCode;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateImmutableModels() {
        return generateImmutableModels;
    }

    public void setGenerateImmutableModels(Boolean generateImmutableModels) {
        this.generateImmutableModels = generateImmutableModels;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateToString() {
        return generateToString;
    }

    public void setGenerateToString(Boolean generateToString) {
        this.generateToString = generateToString;
    }

    @Input
    @Optional
    @Override
    public String getSubscriptionReturnType() {
        return subscriptionReturnType;
    }

    public void setSubscriptionReturnType(String subscriptionReturnType) {
        this.subscriptionReturnType = subscriptionReturnType;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateAsyncApi() {
        return generateAsyncApi;
    }

    public void setGenerateAsyncApi(Boolean generateAsyncApi) {
        this.generateAsyncApi = generateAsyncApi;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateParameterizedFieldsResolvers() {
        return generateParameterizedFieldsResolvers;
    }

    public void setGenerateParameterizedFieldsResolvers(Boolean generateParameterizedFieldsResolvers) {
        this.generateParameterizedFieldsResolvers = generateParameterizedFieldsResolvers;
    }

    @Input
    @Optional
    @Override
    public String getTypeResolverPrefix() {
        return typeResolverPrefix;
    }

    public void setTypeResolverPrefix(String typeResolverPrefix) {
        this.typeResolverPrefix = typeResolverPrefix;
    }

    @Input
    @Optional
    @Override
    public String getTypeResolverSuffix() {
        return typeResolverSuffix;
    }

    public void setTypeResolverSuffix(String typeResolverSuffix) {
        this.typeResolverSuffix = typeResolverSuffix;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateExtensionFieldsResolvers() {
        return generateExtensionFieldsResolvers;
    }

    public void setGenerateExtensionFieldsResolvers(Boolean generateExtensionFieldsResolvers) {
        this.generateExtensionFieldsResolvers = generateExtensionFieldsResolvers;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateDataFetchingEnvironmentArgumentInApis() {
        return generateDataFetchingEnvironmentArgumentInApis;
    }

    public void setGenerateDataFetchingEnvironmentArgumentInApis(Boolean generateDataFetchingEnvironmentArgumentInApis) {
        this.generateDataFetchingEnvironmentArgumentInApis = generateDataFetchingEnvironmentArgumentInApis;
    }

    @Input
    @Optional
    @Override
    public Set<String> getFieldsWithResolvers() {
        return fieldsWithResolvers;
    }

    public void setFieldsWithResolvers(Set<String> fieldsWithResolvers) {
        this.fieldsWithResolvers = fieldsWithResolvers;
    }

    @Input
    @Optional
    @Override
    public Set<String> getFieldsWithoutResolvers() {
        return fieldsWithoutResolvers;
    }

    public void setFieldsWithoutResolvers(Set<String> fieldsWithoutResolvers) {
        this.fieldsWithoutResolvers = fieldsWithoutResolvers;
    }

    @Input
    @Optional
    @Override
    public Boolean getGenerateClient() {
        return generateClient;
    }

    public void setGenerateClient(Boolean generateClient) {
        this.generateClient = generateClient;
    }

    @Input
    @Optional
    @Override
    public String getRequestSuffix() {
        return requestSuffix;
    }

    public void setRequestSuffix(String requestSuffix) {
        this.requestSuffix = requestSuffix;
    }

    @Input
    @Optional
    @Override
    public String getResponseSuffix() {
        return responseSuffix;
    }

    public void setResponseSuffix(String responseSuffix) {
        this.responseSuffix = responseSuffix;
    }

    @Input
    @Optional
    @Override
    public String getResponseProjectionSuffix() {
        return responseProjectionSuffix;
    }

    public void setResponseProjectionSuffix(String responseProjectionSuffix) {
        this.responseProjectionSuffix = responseProjectionSuffix;
    }

    @Input
    @Optional
    @Override
    public String getParametrizedInputSuffix() {
        return parametrizedInputSuffix;
    }

    public void setParametrizedInputSuffix(String parametrizedInputSuffix) {
        this.parametrizedInputSuffix = parametrizedInputSuffix;
    }

    @Nested
    @Optional
    public ParentInterfacesConfig getParentInterfaces() {
        return parentInterfaces;
    }

    public void parentInterfaces(Action<? super ParentInterfacesConfig> action) {
        action.execute(parentInterfaces);
    }

    @Internal
    @Override
    public String getQueryResolverParentInterface() {
        return parentInterfaces.getQueryResolver();
    }

    @Internal
    @Override
    public String getMutationResolverParentInterface() {
        return parentInterfaces.getMutationResolver();
    }

    @Internal
    @Override
    public String getSubscriptionResolverParentInterface() {
        return parentInterfaces.getSubscriptionResolver();
    }

    @Internal
    @Override
    public String getResolverParentInterface() {
        return parentInterfaces.getResolver();
    }

    @InputFile
    @Optional
    public String getJsonConfigurationFile() {
        return jsonConfigurationFile;
    }

    public void setJsonConfigurationFile(String jsonConfigurationFile) {
        this.jsonConfigurationFile = jsonConfigurationFile;
    }

}
