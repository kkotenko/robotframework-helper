package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.github.nghiatm.robotframeworkplugin.psi.dto.ImportType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: mrubino
 * Date: 1/28/14
 * Time: 8:01 PM
 */
public interface KeywordFile {

    @NotNull
    Collection<DefinedKeyword> getDefinedKeywords();
    
    @NotNull
    Collection<DefinedVariable> getDefinedVariables();

    @NotNull
    Collection<DefinedVariable> getOwnDefinedVariables();
    
    @NotNull
    ImportType getImportType();

    @NotNull
    Collection<KeywordFile> getImportedFiles(boolean includeTransitive);
}
