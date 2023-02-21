package com.github.nghiatm.robotframeworkplugin.psi.dto;

import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.util.PatternBuilder;
import com.github.nghiatm.robotframeworkplugin.psi.util.PatternUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * This acts as a wrapper for python definitions.
 *
 * @author mrubino
 * @since 2014-06-06
 */
public class KeywordDto implements DefinedKeyword {



    private final PsiElement reference;
    private final String name;
    private final boolean args;
    private final Pattern namePattern;

    public KeywordDto(@NotNull PsiElement reference, @NotNull String namespace, @NotNull String name, boolean args) {
        this.reference = reference;
        this.name = PatternUtil.functionToKeyword(name).trim();
        this.namePattern = Pattern.compile(PatternBuilder.parseNamespaceKeyword(namespace, this.name), Pattern.CASE_INSENSITIVE);
        this.args = args;
    }

    @Override
    public String getKeywordName() {
        return this.name;
    }

    @Override
    public boolean hasArguments() {
        return this.args;
    }

    @Override
    public boolean matches(String text) {
        return text != null &&
                this.namePattern.matcher(PatternUtil.functionToKeyword(text).trim()).matches();
    }

    @Override
    public PsiElement reference() {
        return this.reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeywordDto that = (KeywordDto) o;

        // I am not sure if we care about arguments in terms of uniqueness here
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
