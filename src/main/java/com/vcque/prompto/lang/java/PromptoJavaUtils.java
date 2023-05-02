package com.vcque.prompto.lang.java;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
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
}
