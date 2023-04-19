package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Retrieve the definition of arguments and return types.
 */
public class MethodTypesContext implements PromptoContext {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return getMethod(element) != null;
    }

    @Override
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var method = getMethod(element);

        var types = new ArrayList<PsiType>();
        types.add(method.getReturnType());
        types.addAll(Arrays.asList(method.getHierarchicalMethodSignature().getParameterTypes()));

        var acceptedPsiTypes = types.stream()
                .flatMap(this::collectParameterTypes)
                .distinct()
                .map(this::retrieveSources).filter(Objects::nonNull)
                .toList();

        return acceptedPsiTypes.stream()
                .map(PsiClass::getText)
                .collect(Collectors.joining("\n\n"));
    }

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

    private Stream<PsiType> collectParameterTypes(PsiType psiType) {
        var collectedTypes = new ArrayList<PsiType>();
        var stack = new ArrayDeque<PsiType>();
        stack.push(psiType);

        while (!stack.isEmpty()) {
            var currentType = stack.pop();

            if (currentType instanceof PsiClassType classType) {
                var typeParameters = classType.getParameters();
                for (var typeParameter : typeParameters) {
                    stack.push(typeParameter);
                }
            } else if (currentType instanceof PsiArrayType arrayType) {
                stack.push(arrayType.getComponentType());
            } else if (currentType instanceof PsiWildcardType wildcardType) {
                var bound = wildcardType.getBound();
                if (bound != null) {
                    stack.push(bound);
                }
            }

            collectedTypes.add(currentType);
        }

        return collectedTypes.stream();
    }

    @Override
    public ChatMessage toMessage(String contextValue) {
        return Prompts.typesContext(contextValue);
    }

    private PsiMethod getMethod(@NotNull PsiElement element) {
        return element instanceof PsiMethod ? (PsiMethod) element : PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    }
}
