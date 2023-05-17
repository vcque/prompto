package com.vcque.prompto.lang.java;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.Utils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PromptoJavaUtils {

    public static void elideBody(PsiMethod method) {
        var codeBlock = PsiElementFactory
                .getInstance(method.getProject())
                .createCodeBlockFromText("{ /* ... */ }", null);

        var body = method.getBody();
        if (body != null) {
            body.replace(codeBlock);
        }
    }

    /**
     * Return a PsiClass if the code represents a psiclass. Empty otherwise.
     */
    public static PsiClass asPsiClass(Project project, String sourceCode) {
        // Method 1: wrap the code into a class
        var elementFactory = PsiElementFactory.getInstance(project);
        var psiClass = elementFactory.createClassFromText(sourceCode, null);
        if (psiClass.getMethods().length > 0) {
            return psiClass;
        }

        // Method 2: consider it as PsiJavaFile and find the first class of it.
        var javaFile = (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText("temp.java", JavaLanguage.INSTANCE, sourceCode);
        return javaFile.getClasses().length > 0 ? javaFile.getClasses()[0] : null;
    }

}
