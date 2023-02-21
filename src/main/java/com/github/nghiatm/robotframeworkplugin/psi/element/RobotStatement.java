package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author mrubino
 * @since 2015-07-23
 */
public interface RobotStatement extends PsiElement {

    @NotNull
    String getPresentableText();
}
