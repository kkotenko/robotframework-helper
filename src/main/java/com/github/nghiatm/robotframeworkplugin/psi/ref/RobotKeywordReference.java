package com.github.nghiatm.robotframeworkplugin.psi.ref;

import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordInvokable;
import com.github.nghiatm.robotframeworkplugin.psi.util.LogUtil;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceCollector;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceEntity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author mrubino
 */
public class RobotKeywordReference extends PsiReferenceBase<KeywordInvokable> {

    public RobotKeywordReference(@NotNull KeywordInvokable element) {
        super(element, false);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        KeywordInvokable element = getElement();
        String keyword = element.getPresentableText();
//        LogUtil.debug("Start resolve keyword: \""+ keyword + "\" in ["+((PerformanceEntity)element).getDebugFileName()+"]", "RobotKeywordReference", "resolve", element.getProject());
        // all files we import are based off the file we are currently in
        // TODO: potentially unsafe cast
        PerformanceCollector debug = new PerformanceCollector((PerformanceEntity) element, "resolve");
        PsiElement results = ResolverUtils.resolveKeywordFromFile(keyword, element.getContainingFile());
        debug.complete();
        return results;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }
}
