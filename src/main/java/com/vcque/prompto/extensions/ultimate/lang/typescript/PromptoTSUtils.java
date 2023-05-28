package com.vcque.prompto.extensions.ultimate.lang.typescript;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.psi.PsiElementFactory;

public class PromptoTSUtils {

    public static void removeBody(TypeScriptFunction function) {
        var codeBlock = PsiElementFactory
                .getInstance(function.getProject())
                .createCodeBlockFromText("{ /* ... */ }", null);
        var body = function.getBlock();
        if (body != null) {
            body.replace(codeBlock);
        }
    }
}
