package com.github.nghiatm.robotframeworkplugin.ide.inspections.cleanup;

import com.github.nghiatm.robotframeworkplugin.RobotBundle;
import com.github.nghiatm.robotframeworkplugin.ide.inspections.SimpleRobotInspection;
import com.github.nghiatm.robotframeworkplugin.psi.RobotTokenTypes;
import com.github.nghiatm.robotframeworkplugin.psi.element.Argument;
import com.github.nghiatm.robotframeworkplugin.psi.element.Import;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author mrubino
 * @since 2014-06-07
 */
public class RobotImportNotUsed extends SimpleRobotInspection {

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return RobotBundle.message("INSP.NAME.import.unused");
    }

    @Override
    public boolean skip(PsiElement element) {
        if (element.getNode().getElementType() != RobotTokenTypes.ARGUMENT) {
            return true;
        } else if (!(element instanceof Argument)) {
            return true;
        }
        PsiFile eFile = element.getContainingFile();
        if (!(eFile instanceof RobotFile)) {
            return true;
        }

        PsiElement parent = element.getParent();

        if (parent instanceof Import) {
            if (!((Import) parent).isResource()) {
                // TODO: python libraries
                // TODO: variables
                return true;
            }
            PsiElement[] children = parent.getChildren();
            // first child seems to be different than this
            if (children.length > 0 && children[0] == element) {
                PsiReference reference = element.getReference();
                if (reference == null) {
                    return true;
                }
                PsiElement importFile = reference.resolve();
                if (importFile == null) {
                    return true; // we cannot find the file thus we do not know if we use it
                }

                PsiFile file = element.getContainingFile();
                if (!(file instanceof RobotFile)) {
                    return true;
                }
                Collection<PsiFile> referenced = ((RobotFile) file).getFilesFromInvokedKeywordsAndVariables();
                return referenced.contains(importFile.getContainingFile());
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public String getMessage() {
        return RobotBundle.message("INSP.import.unused");
    }

    @NotNull
    @Override
    protected String getGroupNameKey() {
        return "INSP.GROUP.cleanup";
    }
}
