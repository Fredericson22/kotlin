/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.load.java.structure.impl;

import com.intellij.codeInsight.ExternalAnnotationsManager;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities;
import org.jetbrains.kotlin.descriptors.DescriptorVisibility;
import org.jetbrains.kotlin.load.java.JavaDescriptorVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.name.FqName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.jetbrains.kotlin.load.java.structure.impl.JavaElementCollectionFromPsiArrayUtil.annotations;
import static org.jetbrains.kotlin.load.java.structure.impl.JavaElementCollectionFromPsiArrayUtil.nullabilityAnnotations;

/* package */ class JavaElementUtil {
    private JavaElementUtil() {
    }

    public static boolean isAbstract(@NotNull JavaModifierListOwnerImpl owner) {
        return owner.getPsi().hasModifierProperty(PsiModifier.ABSTRACT);
    }

    public static boolean isStatic(@NotNull JavaModifierListOwnerImpl owner) {
        return owner.getPsi().hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean isFinal(@NotNull JavaModifierListOwnerImpl owner) {
        return owner.getPsi().hasModifierProperty(PsiModifier.FINAL);
    }

    @NotNull
    public static DescriptorVisibility getVisibility(@NotNull JavaModifierListOwnerImpl owner) {
        PsiModifierListOwner psiOwner = owner.getPsi();
        if (psiOwner.hasModifierProperty(PsiModifier.PUBLIC)) {
            return DescriptorVisibilities.PUBLIC;
        }
        if (psiOwner.hasModifierProperty(PsiModifier.PRIVATE)) {
            return DescriptorVisibilities.PRIVATE;
        }
        if (psiOwner.hasModifierProperty(PsiModifier.PROTECTED)) {
            return owner.isStatic() ? JavaDescriptorVisibilities.PROTECTED_STATIC_VISIBILITY : JavaDescriptorVisibilities.PROTECTED_AND_PACKAGE;
        }
        return JavaDescriptorVisibilities.PACKAGE_VISIBILITY;
    }

    @NotNull
    public static Collection<JavaAnnotation> getAnnotations(@NotNull JavaAnnotationOwnerImpl owner) {
        PsiAnnotationOwner annotationOwnerPsi = owner.getAnnotationOwnerPsi();
        if (annotationOwnerPsi != null) {
            return annotations(annotationOwnerPsi.getAnnotations());
        }
        return Collections.emptyList();
    }

    @Nullable
    private static PsiAnnotation[] getExternalAnnotations(@NotNull JavaModifierListOwnerImpl modifierListOwner) {
        PsiModifierListOwner psiModifierListOwner = modifierListOwner.getPsi();
        ExternalAnnotationsManager externalAnnotationManager = ExternalAnnotationsManager
                .getInstance(psiModifierListOwner.getProject());
        return externalAnnotationManager.findExternalAnnotations(psiModifierListOwner);
    }

    @NotNull
    static <T extends JavaAnnotationOwnerImpl & JavaModifierListOwnerImpl>
    Collection<JavaAnnotation> getRegularAndExternalAnnotations(@NotNull T owner) {
        PsiAnnotation[] externalAnnotations = getExternalAnnotations(owner);
        if (externalAnnotations == null) {
            return getAnnotations(owner);
        }
        Collection<JavaAnnotation> annotations = new ArrayList<>(getAnnotations(owner));
        annotations.addAll(nullabilityAnnotations(externalAnnotations));
        return annotations;
    }


    @Nullable
    public static JavaAnnotation findAnnotation(@NotNull JavaAnnotationOwnerImpl owner, @NotNull FqName fqName) {
        PsiAnnotationOwner annotationOwnerPsi = owner.getAnnotationOwnerPsi();
        if (annotationOwnerPsi != null) {
            PsiAnnotation psiAnnotation = annotationOwnerPsi.findAnnotation(fqName.asString());
            return psiAnnotation == null ? null : new JavaAnnotationImpl(psiAnnotation);
        }
        return null;
    }
}
