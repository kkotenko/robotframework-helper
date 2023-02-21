package com.github.nghiatm.robotframeworkplugin.psi;

import com.github.nghiatm.robotframeworkplugin.ide.icons.RobotIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author mrubino
 */
public class RobotFeatureFileType extends LanguageFileType {

    private static final RobotFeatureFileType INSTANCE = new RobotFeatureFileType();

    private RobotFeatureFileType() {
        super(RobotLanguage.INSTANCE);
    }

    public static RobotFeatureFileType getInstance() {
        return INSTANCE;
    }

    @NotNull
    public String getName() {
        return "Robot Feature";
    }

    @NotNull
    public String getDescription() {
        return "Robot Feature Files";
    }

    @NotNull
    public String getDefaultExtension() {
        return "robot";
    }

    @Nullable
    public Icon getIcon() {
        return RobotIcons.FILE;
    }
}
