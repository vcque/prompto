package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.*;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;
import com.vcque.prompto.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieve recursively the class definitions that the user can access from the editor.
 */
public class AvailableClassesContext implements PromptoContext {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {
        /**
         * Max token cost of this context. Need to make this configurable.
         */
        private int maxCost = 2000;
        /**
         * Max depth of the recursive search.
         */
        private int maxDepth = 5;
    }

    private final Config config;

    /**
     * With default config.
     */
    public AvailableClassesContext() {
        this.config = new Config();
    }

    public AvailableClassesContext(Config config) {
        this.config = config;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return Utils.findParentOfType(element, PsiClass.class) != null;
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
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var editorPsiClass = Utils.findParentOfType(element, PsiClass.class);

        // will contain the final classes to add to context
        var results = new HashSet<PsiClassResult>();
        var alreadyVisited = new HashSet<PsiClass>();
        alreadyVisited.add(editorPsiClass);

        var toVisit = retrieveAllAdjacentClasses(editorPsiClass);
        toVisit.addAll(retrieveAllImports(editor));

        var depth = 0;
        var totalCost = 0;

        while (depth++ < config.maxDepth && !toVisit.isEmpty()) {
            var nextBatch = new HashSet<PsiClass>();
            // Would it be better to prioritize based on minimum cost of each class ?
            for (PsiClass psiClass : toVisit) {
                var text = psiClass.getText();
                if (alreadyVisited.contains(psiClass) || text == null) {
                    // Some generated classes can return null text (e.g. lombok stuff)
                    continue;
                }
                var resultOpt = toResult(psiClass, depth);
                if (resultOpt.isEmpty()) {
                    continue;
                }
                var result = resultOpt.get();

                if (totalCost + result.cost() < config.maxCost) {
                    results.add(result);
                    alreadyVisited.add(psiClass);
                    nextBatch.addAll(retrieveAllAdjacentClasses(psiClass));
                    totalCost += result.cost();
                }
            }
            toVisit = nextBatch;
        }

        return results.stream()
                .sorted(Comparator.comparing(PsiClassResult::depth))
                .map(PsiClassResult::text)
                .collect(Collectors.joining("\n\n"));
    }

    private Collection<? extends PsiClass> retrieveAllImports(Editor editor) {
        var psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
        if (psiFile instanceof PsiJavaFile javaFile) {
            var importList = javaFile.getImportList();
            if (importList == null) {
                return List.of();
            }
            return Arrays.stream(importList.getImportStatements())
                    .map(PsiImportStatement::resolve)
                    .filter(Objects::nonNull)
                    .filter(PsiClass.class::isInstance)
                    .map(PsiClass.class::cast)
                    .filter(this::isInSources)
                    .toList();
        }
        return List.of();
    }

    record PsiClassResult(PsiClass psiClass, int depth, String text, int cost) {
    }

    private Optional<PsiClassResult> toResult(PsiClass psiClass, int depth) {
        var text = shrink(psiClass).getText();
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        text = text
                .replaceAll("\\s+\n", "\n") // remove trailing spaces
                .replaceAll("\n{2,}", "\n"); // remove multi-newlines
        var cost = Utils.countTokens(text);
        return Optional.of(new PsiClassResult(psiClass, depth, text, cost));
    }

    private PsiClass shrink(PsiClass psiClass) {
        var codeBlock = PsiElementFactory
                .getInstance(psiClass.getProject())
                .createCodeBlockFromText("{ /* ... */ }", null);

        var clonedPsiClass = (PsiClass) psiClass.copy();
        // Who cares about private methods implementation ? Every abstraction is perfect.
        for (var method : clonedPsiClass.getMethods()) {
            if (!method.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)) {
                var body = method.getBody();
                if (body != null) {
                    body.replace(codeBlock);
                }
            }
        }

        // Inner classes can be matched by the recursive search, no need to print them twice
        for (var innerClass : clonedPsiClass.getAllInnerClasses()) {
            try {
                innerClass.delete();
            } catch (Exception e) {
                // do nothing
            }
        }
        return clonedPsiClass;
    }

    @NotNull
    private Set<PsiClass> retrieveAllAdjacentClasses(PsiClass psiClass) {
        return Utils.retrieveAllPsiTypes(psiClass, false)
                .stream()
                .map(this::retrieveSources).filter(Objects::nonNull)
                .filter(PsiClass::isPhysical) // No generated classes
                .filter(pc -> !(pc instanceof PsiTypeParameter)) // No generic types like (T or ID)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieve sources of a psitype if the code is in the project.
     */
    private PsiClass retrieveSources(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var psiClass = classType.resolve();
            if (psiClass != null && isInSources(psiClass)) {
                return psiClass;
            }
        }
        return null;
    }

    private boolean isInSources(PsiClass psiClass) {
        var containingFile = psiClass.getContainingFile();
        var projectFileIndex = ProjectFileIndex.getInstance(psiClass.getProject());
        var virtualFile = containingFile.getVirtualFile();
        return projectFileIndex.isInSource(virtualFile);
    }
}
