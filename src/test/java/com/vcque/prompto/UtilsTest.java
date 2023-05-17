package com.vcque.prompto;

import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.vcque.prompto.lang.java.PromptoJavaUtils;
import org.junit.jupiter.api.Test;

public class UtilsTest extends BasePlatformTestCase {

    @Test
    public void testMergeCodeIntoPsiClass_singleMethod() {
        // Create a new PsiClass
        var psiClass = JavaPsiFacade.getInstance(getProject()).getElementFactory().createClass("MyClass");

        // Add a method to the PsiClass
        var existingMethod = PsiElementFactory.getInstance(getProject()).createMethod("myMethod", PsiType.VOID);
        psiClass.add(existingMethod);

        // Merge new code into the PsiClass
        String newCode = """
                public void myMethod() {
                    System.out.println("Hello world!");
                }
                """;
        Utils.mergePsiClasses(psiClass, PromptoJavaUtils.asPsiClass(getProject(), newCode));

        // Verify that the method was updated
        PsiMethod updatedMethod = psiClass.findMethodsByName("myMethod", false)[0];
        assertEquals(newCode.trim(), updatedMethod.getText().trim());
    }

    @Test
    public void testMergeCodeIntoPsiClass_multipleMethods() {
        // Create a new PsiClass
        var psiClass = JavaPsiFacade.getInstance(getProject()).getElementFactory().createClass("MyClass");

        // Add an existing method to the PsiClass
        var existingMethod = PsiElementFactory.getInstance(getProject()).createMethod("myMethod", PsiType.VOID);
        psiClass.add(existingMethod);

        // Merge new code into the PsiClass
        String newCode = """
                public void myMethod() {
                    System.out.println("Hello world!");
                }
                            
                public void myOtherMethod() {
                    System.out.println("This is another method!");
                }
                """;
        Utils.mergePsiClasses(psiClass, PromptoJavaUtils.asPsiClass(getProject(), newCode));

        // Verify that the methods were updated
        PsiMethod updatedMethod = psiClass.findMethodsByName("myMethod", false)[0];
        assertEquals("myMethod".trim(), updatedMethod.getName());

        PsiMethod updatedOtherMethod = psiClass.findMethodsByName("myOtherMethod", false)[0];
        assertEquals("myOtherMethod".trim(), updatedOtherMethod.getName());
    }

}