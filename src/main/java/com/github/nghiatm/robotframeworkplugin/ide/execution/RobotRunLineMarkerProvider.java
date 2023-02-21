package com.github.nghiatm.robotframeworkplugin.ide.execution;

import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.intellij.openapi.util.text.StringUtil.join;
import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public class RobotRunLineMarkerProvider extends RunLineMarkerContributor {

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element.getContainingFile().getVirtualFile().getFileType() instanceof RobotFeatureFileType) {
            PsiElement targetElement = element.getParent().getParent();
            if (element instanceof LeafElement &&
                    targetElement instanceof KeywordDefinitionImpl) {
                Collection<DefinedKeyword> testCases = RobotUtil.getTestCasesFromElement(element);
                for (DefinedKeyword testCase: testCases) {
                    if (testCase.getKeywordName().equals(element.getText())) {
                        final AnAction[] actions = ExecutorAction.getActions();
                        return new Info(AllIcons.RunConfigurations.TestState.Run, actions,
                                e -> join(mapNotNull(actions, action -> getText(action, e)), "\n"));
                    }
                }
            }
        }
        return null;
    }


}
