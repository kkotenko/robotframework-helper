package com.github.nghiatm.robotframeworkplugin.ide.usage;

import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.PsiNamedElementUsageGroupBase;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.impl.FileStructureGroupRuleProvider;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.SingleParentUsageGroupingRule;
import com.intellij.usages.rules.UsageGroupingRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author mrubino
 * @since 2016-01-28
 */
public class RobotKeywordGroupingRuleProvider implements FileStructureGroupRuleProvider {

    @Nullable
    @Override
    public UsageGroupingRule getUsageGroupingRule(@NotNull Project project) {
        return new RobotKeywordGroupingRule();
    }

    private static class RobotKeywordGroupingRule extends SingleParentUsageGroupingRule  {

        private RobotKeywordGroupingRule() {
        }

        @Override
        protected @Nullable UsageGroup getParentGroupFor(@NotNull Usage usage, UsageTarget @NotNull [] targets) {
            if (!(usage instanceof PsiElementUsage)) {
                return null;
            } else {
                PsiElement psiElement = ((PsiElementUsage) usage).getElement();
                KeywordDefinitionImpl definition = PsiTreeUtil.getParentOfType(psiElement, KeywordDefinitionImpl.class, false);
                return definition == null ? null : new PsiNamedElementUsageGroupBase<>(definition);
            }
        }
    }
}
