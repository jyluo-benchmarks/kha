package com.kalixia.netty.rest;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({ "javax.ws.rs.*" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StaticAnalysisCompiler extends AbstractProcessor {
    private Filer filer;
    private Elements elementUtils;
    private Messager messager;
    private final JaxRsAnalyzer analyzer = new JaxRsAnalyzer();
    private JaxRsMethodGenerator methodGenerator;
    private JaxRsModuleGenerator moduleGenerator;
    public static final String GENERATOR_NAME = "netty-rest";

    @Override
    public void init(ProcessingEnvironment environment) {
        super.init(environment);
        this.filer = environment.getFiler();
        this.elementUtils = environment.getElementUtils();
        this.messager = environment.getMessager();
        methodGenerator = new JaxRsMethodGenerator(filer, messager);
        moduleGenerator = new JaxRsModuleGenerator(filer, messager);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> resources = roundEnv.getElementsAnnotatedWith(Path.class);
        List<String> generatedHandlers = new ArrayList<>();

        for (Element resource : resources) {
            // only keep classes
            if (!resource.getKind().isClass())
                continue;

            // extract class being analyzed
            PackageElement resourcePackage = elementUtils.getPackageOf(resource);
            String resourceClassName = resource.getSimpleName().toString();

            List<? extends Element> enclosedElements = resource.getEnclosedElements();
            for (Element elem : enclosedElements) {
                if (!ElementKind.METHOD.equals(elem.getKind()))
                    continue;
                ExecutableElement methodElement = (ExecutableElement) elem;

                // figure out if @GET, @POST, @DELETE, @PUT, etc are annotated on the method
                String verb = analyzer.extractVerb(elem);
                if (verb == null)
                    continue;
                String uriTemplate = analyzer.extractUriTemplate(resource, elem);
                String methodName = elem.getSimpleName().toString();
                String returnType = methodElement.getReturnType().toString();
                List<JaxRsParamInfo> parameters = analyzer.extractParameters(methodElement);
                // process @Produces annotations
                Produces producesAnnotation = resource.getAnnotation(Produces.class);
                if (producesAnnotation == null)
                    producesAnnotation = methodElement.getAnnotation(Produces.class);
                String[] produces = null;
                if (producesAnnotation != null)
                    produces = producesAnnotation.value();
                else
                    produces = new String[] { MediaType.TEXT_PLAIN };
                JaxRsMethodInfo methodInfo = new JaxRsMethodInfo(elem, verb, uriTemplate, methodName, returnType, parameters, produces);
                generatedHandlers.add(methodGenerator.generateHandlerClass(resourceClassName, resourcePackage, uriTemplate, methodInfo));
            }
        }

        // TODO: use package from APT processor options
        if (!resources.isEmpty() && generatedHandlers.size() > 0) {
            String firstHandlerName = generatedHandlers.get(0);
            String packageName = firstHandlerName.substring(0, firstHandlerName.lastIndexOf('.'));
//            System.out.printf("Package name %s%n", packageName);
            moduleGenerator.generateModuleClass(packageName, generatedHandlers);
        }

        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptyList();
    }

}
