package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.github.nghiatm.robotframeworkplugin.ide.icons.RobotIcons;
import com.github.nghiatm.robotframeworkplugin.psi.util.ExtRobotPsiUtils;
import com.github.nghiatm.robotframeworkplugin.psi.util.PatternUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 * @author mrubino
 */
public class VariableDefinitionImpl extends RobotPsiElementBase implements VariableDefinition, DefinedVariable, PsiNameIdentifierOwner {

    private Pattern pattern;

    public VariableDefinitionImpl(@NotNull final ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        this.pattern = null;
    }

    @Override
    public boolean matches(String text) {
        if (text == null) {
            return false;
        }
        String myText = getPresentableText();
        Pattern pattern = this.pattern;
        if (pattern == null) {
            pattern = Pattern.compile(PatternUtil.getVariablePattern(myText), Pattern.CASE_INSENSITIVE);
            this.pattern = pattern;
        }
        return pattern.matcher(text).matches();
    }

    @Override
    public boolean isInScope(@Nullable PsiElement position) {
        return true;
    }

    @Nullable
    @Override
    public PsiElement reference() {
        return this;
    }

    @Nullable
    @Override
    public String getLookup() {
        // getName() return full variable definition line in robot file
        // getText() return the part before SUPER_SPACE or TAB
        return getText();
    }

    @Override
    public boolean isNested() {
        String text = getPresentableText();
        return StringUtil.getOccurrenceCount(text, "}") > 1 &&
                (StringUtil.getOccurrenceCount(text, "${") + StringUtil.getOccurrenceCount(text, "@{") + StringUtil.getOccurrenceCount(text, "%{") > 1);
    }

    @Override
    @NotNull
    public Icon getIcon(int flags) {
        return RobotIcons.VARIABLE_DEFINITION;
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return PsiTreeUtil.findChildOfType(this, VariableDefinitionId.class);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        super.setName(name);
        VariableDefinitionImpl newElement = new ExtRobotPsiUtils(this).createVariableDefinition(name);
        this.getNode().getTreeParent().replaceChild(this.getNode(), newElement.getNode());
        return this;
    }
}
