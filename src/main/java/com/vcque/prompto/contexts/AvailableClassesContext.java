package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.*;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;
import com.vcque.prompto.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieve the definition of arguments and return types.
 */
public class AvailableClassesContext implements PromptoContext {

    /** Max token cost of this context. Need to make this configurable. */
    private static final int MAX_COST = 2500;
    /** Max depth of the class search. */
    private static final int MAX_DEPTH = 5;


    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return Utils.findParentOfType(element, PsiClass.class) != null;
    }

    /**
     * Retrieve the context of the current method by finding all adjacent classes and returning their text.
     * The context is defined as the text of all classes that are directly or indirectly used by the current method.
     * The search is limited to a maximum depth of {@link #MAX_DEPTH} and a maximum cost of {@link #MAX_COST}.
     *
     * @param project the current project
     * @param editor the current editor
     * @param element the current element
     * @return the context of the current method as a string containing the text of all adjacent classes
     */
    @Override
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var editorPsiClass = Utils.findParentOfType(element, PsiClass.class);

        // will contain the final classes to add to context
        var results = new HashSet<PsiClass>();

        var toVisit = retrieveAllAdjacentClasses(editorPsiClass);
        var depth = 0;
        var totalCost = 0;

        while (depth++ < MAX_DEPTH && !toVisit.isEmpty()) {
            var nextBatch = new HashSet<PsiClass>();
            // Would it be better to prioritize based on minimum cost of each class ?
            for (PsiClass psiClass : toVisit) {
                var text = psiClass.getText();
                if (results.contains(psiClass) || text == null) {
                    // Some generated classes can return null text (e.g. lombok stuff)
                    continue;
                }
                var classCost = Utils.countTokens(text);
                if (totalCost + classCost < MAX_COST) {
                    results.add(psiClass);
                    nextBatch.addAll(retrieveAllAdjacentClasses(psiClass));
                    totalCost += classCost;
                }
            }
            toVisit = nextBatch;
        }

        // Just in case there's some cyclic dependencies
        results.remove(editorPsiClass);
        return results.stream().map(PsiElement::getContainingFile)
                .distinct()
                .map(PsiElement::getText)
                .collect(Collectors.joining("\n\n"))
                .replaceAll("import .*;", "")
                .replaceAll("package .*;", "")
                .replaceAll("(\n\\s*){3,}", "\n\n");
    }

    @NotNull
    private Set<PsiClass> retrieveAllAdjacentClasses(PsiClass psiClass) {
        return Utils.retrieveAllPsiTypes(psiClass, false).stream()
                .map(this::retrieveSources).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieve sources of a psitype if the code is in the project.
     */
    private PsiClass retrieveSources(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var project = type.getResolveScope().getProject();
            var psiClass = classType.resolve();
            var containingFile = psiClass.getContainingFile();
            var projectFileIndex = ProjectFileIndex.getInstance(project);
            var virtualFile = containingFile.getVirtualFile();
            return projectFileIndex.isInSource(virtualFile) ? psiClass : null;
        } else {
            return null;
        }
    }

    @Override
    public ChatMessage toMessage(String contextValue) {
        return Prompts.typesContext(contextValue);
    }
}