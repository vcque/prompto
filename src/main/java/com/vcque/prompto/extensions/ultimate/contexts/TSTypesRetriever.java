package com.vcque.prompto.extensions.ultimate.contexts;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptCompileTimeType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiUtilBase;
import com.vcque.prompto.Utils;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.contexts.PromptoRetriever;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Retrieve recursively the class definitions that the user can access from the editor.
 */
public class TSTypesRetriever implements PromptoRetriever {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        return Optional.ofNullable(psiFile)
                .map(PsiElement::getLanguage)
                .filter(lang -> lang.getID().contains("TypeScript"))
                .isPresent();
    }

    /**
     * Retrieve the context of the current method by finding all adjacent classes and returning their text.
     * The context is defined as the text of all classes that are directly or indirectly used by the current method.
     * The search is limited to a maximum depth of config.maxDepth and a maximum cost of config.maxCost.
     *
     * @param project the current project
     * @param editor  the current editor
     * @param element the current element
     * @return the context of the current method as a string containing the text of all adjacent classes
     */
    @Override
    public List<PromptoContext> retrieveContexts(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var results = new ArrayList<PromptoContext>();
        var target = Utils.findParentOfType(element, TypeScriptFunction.class);
        if (target == null) {
            return List.of();
        }

        var toVisit = List.<PsiElement>of(target);
        var visited = new HashSet<>(toVisit);
        var depth = 5;
        for (int i =0; i < depth; i++) {
            var nextBatch = new ArrayList<PsiElement>();
            for (var elementToVisit : toVisit) {
                var resolved = findTypeDefinitions(elementToVisit);
                for (var resolvedType : resolved) {
                    if (!visited.contains(resolvedType)) {
                        nextBatch.add(resolvedType);
                        visited.add(resolvedType);
                        results.add(toPromptoContext(resolvedType));
                    }
                }
            }
            toVisit = nextBatch;
        }

        return results;
    }

    /**
     * Find types references in the psiElement
     * @param element the psElement to scan
     * @return The list of resolved type
     */
    private List<TypeScriptCompileTimeType> findTypeDefinitions(PsiElement element) {
        var initialTypes = retrieveAllTypes(element);

        return initialTypes.stream()
                .filter(TypeScriptCompileTimeType.class::isInstance)
                .map(TypeScriptCompileTimeType.class::cast)
                .filter(Utils::isInSources)
                .toList();
    }

    private PromptoContext toPromptoContext(TypeScriptCompileTimeType type) {
        return PromptoContext.builder()
                .id(type.getName())
                .value(Utils.cleanWhitespaces(type.getText()))
                .type(PromptoContext.Type.TYPE)
                .build();
    }

    private Set<PsiElement> retrieveAllTypes(PsiElement toVisit) {
        var results = new HashSet<PsiElement>();

        // Traverse the AST and collect JSType instances
        toVisit.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof TypeScriptSingleType type) {
                    Optional.of(type)
                            .map(PsiElement::getFirstChild)
                            .map(PsiElement::getReference)
                            .map(PsiReference::resolve)
                            .filter(Utils::isInSources)
                            .ifPresent(results::add);
                }
                super.visitElement(element);
            }
        });

        return results;
    }

    @Override
    public String name() {
        return "TS Types";
    }

}