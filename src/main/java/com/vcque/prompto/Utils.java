package com.vcque.prompto;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    private Utils() {
    }

    /**
     * @return An approximate count of the number of tokens this text represents.
     */
    public static int countTokens(String text) {
        var wordMatcher = Pattern.compile("[\\w]+").matcher(text);
        var wordCount = 0;
        while (wordMatcher.find()) {
            wordCount++;
        }

        var symbolMatcher = Pattern.compile("\\p{Punct}").matcher(text);
        var symbolCount = 0;
        while (symbolMatcher.find()) {
            symbolCount++;
        }

        return wordCount * 4 / 3 + symbolCount;
    }

    /**
     * Extracts all types used in the given method, including the return type and parameter types.
     * This includes type parameters of generic types and component types of arrays.
     *
     * @param method the method to extract types from
     * @return a set of all types used in the method
     */
    public static Set<PsiType> extractMethodTypes(PsiMethod method) {
        var types = new HashSet<PsiType>();
        types.add(method.getReturnType());
        types.addAll(Arrays.asList(method.getHierarchicalMethodSignature().getParameterTypes()));

        var parameterTypes = types.stream()
                .filter(Objects::nonNull)
                .flatMap(t -> extractParameterTypes(t).stream())
                .collect(Collectors.toSet());

        types.addAll(parameterTypes);

        return types;
    }

    /**
     * Collects all parameter types of the given {@link PsiType}, including type parameters of generic types and
     * component types of arrays.
     * TODO: needs to also retrieve classes from extends and implements
     *
     * @param psiType the {@link PsiType} to collect parameter types from
     * @return a {@link Stream} of all parameter types
     */
    public static Set<PsiType> extractParameterTypes(PsiType psiType) {
        var collectedTypes = new HashSet<PsiType>();
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

        return collectedTypes;
    }

    public static Set<PsiType> retrieveAllPsiTypes(PsiClass psiClass, boolean onlyPublic) {
        var psiTypes = new HashSet<PsiType>();
        var fields = psiClass.getAllFields();
        for (var field : fields) {
            if (!onlyPublic || field.hasModifierProperty(PsiModifier.PUBLIC)) {
                var fieldType = field.getType();
                psiTypes.add(fieldType);
            }
        }
        var methods = psiClass.getAllMethods();
        for (var method : methods) {
            if (!onlyPublic || method.hasModifierProperty(PsiModifier.PUBLIC)) {
                psiTypes.addAll(extractMethodTypes(method));
            }
        }
        return psiTypes;
    }

    /**
     * Try to merge code received as text into an existing psiClass.
     * If the code represents a method:
     * - If a method with the same signature exists in the psiClass, replace-it
     * - Otherwise add the new method
     * If the code represents multiple methods: do the same as above for each method
     * If the code represents a class: retrieves the methods and do also as above with them
     */
    public static void mergePsiClasses(PsiClass psiClass, PsiClass addition) {
        for (var newMethod : addition.getMethods()) {
            boolean replaced = false;
            for (var existingMethod : psiClass.getMethods()) {
                if (hasSameSignature(newMethod, existingMethod)) {
                    existingMethod.replace(newMethod);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                psiClass.add(newMethod);
            }
        }
    }

    public static boolean hasSameSignature(PsiMethod method, PsiMethod other) {
        if (!method.getName().equals(other.getName())) {
            return false;
        }

        var methodParams = method.getParameterList().getParameters();
        var otherParams = other.getParameterList().getParameters();

        if (methodParams.length != otherParams.length) {
            return false;
        }

        for (int i = 0; i < methodParams.length; i++) {
            var methodParam = methodParams[i];
            var otherParam = otherParams[i];

            if (!methodParam.getType().getPresentableText().equals(otherParam.getType().getPresentableText())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return a PsiClass if the code represents a psiclass. Empty otherwise.
     */
    public static PsiClass asPsiClass(String code, Project project) {
        var elementFactory = PsiElementFactory.getInstance(project);
        return elementFactory.createClassFromText(code, null);
    }

    public static <T extends PsiElement> T findParentOfType(@NotNull PsiElement element, Class<T> psi) {
        return psi.isInstance(psi) ? (T) element : PsiTreeUtil.getParentOfType(element, psi);
    }
}
