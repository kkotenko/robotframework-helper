package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.RobotLanguage;
import com.github.nghiatm.robotframeworkplugin.psi.RobotProjectData;
import com.github.nghiatm.robotframeworkplugin.psi.dto.ImportType;
import com.github.nghiatm.robotframeworkplugin.psi.dto.VariableDto;
import com.github.nghiatm.robotframeworkplugin.psi.ref.PythonResolver;
import com.github.nghiatm.robotframeworkplugin.psi.ref.RobotPythonClass;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceCollector;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceEntity;
import com.github.nghiatm.robotframeworkplugin.psi.util.ReservedVariable;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static com.github.nghiatm.robotframeworkplugin.psi.element.HeadingImpl.ROBOT_BUILT_IN;

/**
 * @author Stephen Abrams
 */
public class RobotFileImpl extends PsiFileBase implements RobotFile, KeywordFile, PerformanceEntity {

    private Collection<Heading> headings;

    public RobotFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, RobotLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return RobotFeatureFileType.getInstance();
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        this.headings = null;
    }

    @NotNull
    @Override
    public Collection<DefinedVariable> getDefinedVariables() {
        // now getDefinedVariables include all variable in imported files, also see ResolverUtil.java and RobotCompletionContributor.java
        // ROBOTFRAMEWORK only import variable from Variable and Resource
        // to avoid recursive call, any method called by getDefinedVariables (transitively) should call getOwnDefinedVariables
        Collection<DefinedVariable> results = new LinkedHashSet<>();
        addBuiltInVariables(results);
        results.addAll(getOwnDefinedVariables());

        boolean includeTransitive = RobotOptionsProvider.getInstance(getProject()).allowTransitiveImports();
        for (KeywordFile imported : getImportedFiles(includeTransitive)) {
            if (imported.getImportType() == ImportType.VARIABLES || imported.getImportType() == ImportType.RESOURCE) {
                results.addAll(imported.getOwnDefinedVariables());
            }
        }
        return results;
    }

    @NotNull
    @Override
    public Collection<DefinedVariable> getOwnDefinedVariables() {
        Collection<DefinedVariable> results = new LinkedHashSet<DefinedVariable>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getDefinedVariables());
        }
        return results;
    }

    @NotNull
    @Override
    public ImportType getImportType() {
        return ImportType.RESOURCE;
    }

    @NotNull
    @Override
    public Collection<DefinedKeyword> getDefinedKeywords() {
        Collection<DefinedKeyword> results = new LinkedHashSet<DefinedKeyword>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getDefinedKeywords());
        }
        return results;
    }

    @NotNull
    @Override
    public Collection<PsiFile> getFilesFromInvokedKeywordsAndVariables() {
        Collection<PsiFile> results = new HashSet<PsiFile>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getFilesFromInvokedKeywordsAndVariables());
        }
        return results;
    }

    @NotNull
    @Override
    public Collection<KeywordFile> getImportedFiles(boolean includeTransitive) {
        Collection<KeywordFile> results = new LinkedHashSet<KeywordFile>();
        addBuiltInImports(results);
        for (Heading heading : getHeadings()) {
            for (KeywordFile file : heading.getImportedFiles()) {
                addKeywordFiles(results, file, includeTransitive);
            }
        }
        return results;
    }

    private void addKeywordFiles(Collection<KeywordFile> files, KeywordFile current, boolean includeTransitive) {
        if (files.add(current)) {
            if (includeTransitive) {
                for (KeywordFile file : current.getImportedFiles(false)) {
                    // avoid recursive import
                    if (! files.contains(file)) {
                        addKeywordFiles(files, file, true);
                    }
                    // TODO: check same python file imported twice, but with different import type
                }
            }
        }
    }

    @Override
    public void importsChanged() {
        for (Heading heading : getHeadings()) {
            heading.importsChanged();
        }
    }

    @NotNull
    @Override
    public Collection<KeywordInvokable> getKeywordReferences(@Nullable KeywordDefinition definition) {
        Collection<KeywordInvokable> results = new LinkedHashSet<KeywordInvokable>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getKeywordReferences(definition));
        }
        return results;
    }

    @NotNull
    public Collection<Heading> getHeadings() {
        Collection<Heading> results = this.headings;
        if (results == null) {
            PerformanceCollector debug = new PerformanceCollector(this, "headings");
            results = collectHeadings();
            this.headings = results;
            debug.complete();
        }
        return results;
    }

    @NotNull
    private Collection<Heading> collectHeadings() {
        Collection<Heading> results = new LinkedHashSet<Heading>();
        for (PsiElement child : getChildren()) {
            if (child instanceof Heading) {
                results.add((Heading) child);
            }
        }
        return results;
    }

    @NotNull
    @Override
    public String getDebugFileName() {
        return getVirtualFile().getName();
    }

    @NotNull
    @Override
    public String getDebugText() {
        return ".";
    }


    private void addBuiltInVariables(@NotNull Collection<DefinedVariable> variables) {
        variables.addAll(getBuiltInVariables());
    }

    // TODO: code highlight is not quite working; see KyleEtlPubAdPart.robot; think it has to do with name difference GLOBAL_VARIABLE vs CURDIR etc
    private synchronized Collection<DefinedVariable> getBuiltInVariables() {
        Collection<DefinedVariable> variables = RobotProjectData.getInstance(getProject()).builtInVariables();
        if (variables == null) {
            Collection<DefinedVariable> results = new LinkedHashSet<DefinedVariable>();

            // optimized here: all variable.getVariable(getProject()) return same object,
            // so now only call variable.getVariable(getProject()) only once
            // move the optimization to ReservedVariableScope.java as the implementation of getVariable may be changed.
            PsiElement pythonVariable = null;
            for (ReservedVariable variable : ReservedVariable.values()) {
                pythonVariable = variable.getVariable(getProject());
                if (pythonVariable != null) {
                    // already formatted ${X}
                    results.add(new VariableDto(pythonVariable, variable.getVariable(), variable.getScope()));
                }
            }

            variables = results.isEmpty() ?
                    Collections.emptySet() :
                    Collections.unmodifiableCollection(results);
            RobotProjectData.getInstance(getProject()).setBuiltInVariables(variables);
        }

        return variables;
    }

    private void addBuiltInImports(@NotNull Collection<KeywordFile> files) {
        PyClass builtIn = PythonResolver.findClass(ROBOT_BUILT_IN, getProject());
        if (builtIn != null) {
            files.add(new RobotPythonClass(ROBOT_BUILT_IN, ROBOT_BUILT_IN, builtIn, ImportType.LIBRARY));
        }
    }

}
