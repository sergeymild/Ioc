package com.ioc;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.tools.JavaFileObject;

import kotlin.Metadata;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

/**
 * Created by sergeygolishnikov on 08/08/2017.
 */
@RunWith(JUnit4.class)
public class ModuleTests {
    @Test
    public void emptyParams() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return new DependencyModel(); }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void emptyParamsMethodInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "   public void setDependency(DependencyModel dependency) {};",
            "   public DependencyModel getDependency() { return null; }",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return new DependencyModel(); }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       target.setDependency(dependencyModel);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void targetAsParam() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(Activity activity) { return new DependencyModel(); }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency(target);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void targetAsParamMethodInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "   public void setDependency(DependencyModel dep) {};",
            "   public DependencyModel getDependency() { return null; }",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(Activity activity) { return new DependencyModel(); }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency(target);",
            "       target.setDependency(dependencyModel);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void targetAndDependencyAsParam() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void targetAndDependencyAsParamMethodInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       target.setDependency(parentDependency);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void TargetInDependencyOfParentDependencyAsParam() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void TargetInDependencyOfParentDependencyAsParamMethodInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       target.setDependency(parentDependency);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonAsDependency() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency() { return new ParentDependency(); }",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       ParentDependency parentDependency = ParentDependencySingleton.get();",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonAsDependencyMethodInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency() { return new ParentDependency(); }",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       ParentDependency parentDependency = ParentDependencySingleton.get();",
            "       target.setDependency(parentDependency);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonWithArgumentsAsDependency() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency(DependencyModel dependency) { return new ParentDependency(); }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ParentDependencySingleton {",
            "   private static ParentDependency singleton;",
            "",
            "   private static final ParentDependencySingleton instance = new ParentDependencySingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ParentDependency get() {",
            "       if (singleton != null) return singleton;",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       singleton = ModuleClass.getDependency(dependencyModel);",
            "       return singleton",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonWithArgumentsAsDependencyInActivity() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency(DependencyModel dependency) { return new ParentDependency(); }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       ParentDependency parentDependency = ParentDependencySingleton.get();",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonAndArgumentsAsDependencyInActivity() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getParentDependency(DependencyModel dependency, Dependency2 dependency2) { return new ParentDependency(); }",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return new DependencyModel(); }",
            "   @Dependency @Singleton",
            "   public static Dependency2 getDependency2() { return new Dependency2(); }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject dependencyFile2 = JavaFileObjects.forSourceLines("test.Dependency2",
            "package test;",
            "",
            "public class Dependency2 {",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       Dependency2 dependency2 = Dependency2Singleton.get();",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       ParentDependency parentDependency = ModuleClass.getParentDependency(dependencyModel, dependency2);",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile, dependencyFile2, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void preferMethodOverEmptyConstructor() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getParentDependency() { return new ParentDependency(); }",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       ParentDependency parentDependency = ModuleClass.getParentDependency();",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void preferMethodOverEmptyConstructorInSingleton() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public class ModuleClass {",
            "",
            "   @Singleton",
            "   @Dependency",
            "   public static ParentDependency getParentDependency(DependencyModel dependency) { return new ParentDependency(); }",
            "   @Dependency",
            "   public static DependencyModel dependency() { return null; }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ParentDependencySingleton",
            "   private static ParentDependency singleton;",
            "",
            "   private static final ParentDependencySingleton instance = new ParentDependencySingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ParentDependency get() {",
            "       if (singleton != null) return singleton;",
            "       DependencyModel dependencyModel = ModuleClass.dependency();",
            "       singleton = ModuleClass.getParentDependency(dependencyModel);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonFromClass() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ParentDependencySingleton",
            "   private static ParentDependency singleton;",
            "",
            "   private static final ParentDependencySingleton instance = new ParentDependencySingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ParentDependency get() {",
            "       if (singleton != null) return singleton;",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       singleton = new ParentDependency(dependencyModel);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonFromClass2() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       ParentDependency parentDependency = ParentDependencySingleton.get();",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonFromClass2MethodInjectionSingleton() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ParentDependencySingleton {",
            "   private static ParentDependency singleton;",
            "   private static final ParentDependencySingleton instance = new ParentDependencySingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ParentDependency get() {",
            "       if (singleton != null) return singleton;",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       singleton = new ParentDependency(dependencyModel);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonFromClass2MethodInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       ParentDependency parentDependency = ParentDependencySingleton.get();",
            "       target.setDependency(parentDependency);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonFromClass3() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ParentDependencySingleton",
            "   private static ParentDependency singleton;",
            "",
            "   private static final ParentDependencySingleton instance = new ParentDependencySingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ParentDependency get() {",
            "       if (singleton != null) return singleton;",
            "       Singleton2 singleton2 = Singleton2Singleton.get();",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       singleton = new ParentDependency(dependencyModel, singleton2);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void onlyOneSingleton() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "   @Inject",
            "   public ParentDependency oneMoreDependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ParentDependencySingleton",
            "   private static ParentDependency singleton;",
            "",
            "   private static final ParentDependencySingleton instance = new ParentDependencySingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ParentDependency get() {",
            "       if (singleton != null) return singleton;",
            "       Singleton2 singleton2 = Singleton2Singleton.get();",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       singleton = new ParentDependency(dependencyModel, singleton2);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonFromClass4() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       Singleton2 singleton2 = Singleton2Singleton.get();",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel, singleton2);",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void singletonFromClass4MethodInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}");

        JavaFileObject singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       Singleton2 singleton2 = Singleton2Singleton.get();",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel, singleton2);",
            "       target.setDependency(parentDependency);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void correctParamInjection() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public GetCountryService service;",
            "}");

        JavaFileObject retrofitFile = JavaFileObjects.forSourceLines("test.Retrofit",
            "package test;",
            "",
            "public class Retrofit {",
            "}");

        JavaFileObject countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}");

        JavaFileObject getCountryServiceFile = JavaFileObjects.forSourceLines("test.GetCountryService",
            "package test;",
            "",
            Helpers.importType(Singleton.class),
            "",
            "@Singleton",
            "public class GetCountryService {",
            "   GetCountryService(CountryService countryService) {}",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public class ModuleFile {",
            "   @Dependency",
            "   public static CountryService getService(Retrofit retrofit) {return null;}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.GetCountryServiceSingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class GetCountryServiceSingleton {",
            "   private static GetCountryService singleton;",
            "",
            "   private static final GetCountryServiceSingleton instance = new GetCountryServiceSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final GetCountryService get() {",
            "       if (singleton != null) return singleton;",
            "       Retrofit retrofit = new Retrofit();",
            "       CountryService countryService = ModuleFile.getService(retrofit);",
            "       singleton = new GetCountryService(countryService);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, retrofitFile, countryServiceFile, getCountryServiceFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void interfaceModules() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "}");

        JavaFileObject countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}");

        JavaFileObject countryServiceImplementation = JavaFileObjects.forSourceLines("test.CountryServiceImplementation",
            "package test;",

            "public class CountryServiceImplementation implements CountryService {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "interface ModuleFile {",
            "   @Dependency",
            "   public CountryService getService(CountryServiceImplementation implementation);",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCountryServiceInService(target);",
            "   }",
            "",
            "   private final void injectCountryServiceInService(@NonNull final Activity target) {",
            "       CountryService countryService = new CountryServiceImplementation();",
            "       target.service = countryService;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, countryServiceImplementation, countryServiceFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void abstractModules() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "   @Inject",
            "   public DependencyService dependencyService;",
            "}");

        JavaFileObject countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}");

        JavaFileObject dependencyService = JavaFileObjects.forSourceLines("test.DependencyService",
            "package test;",
            "",
            "public class DependencyService {",
            "}");

        JavaFileObject countryServiceImplementation = JavaFileObjects.forSourceLines("test.CountryServiceImplementation",
            "package test;",

            "public class CountryServiceImplementation implements CountryService {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Singleton.class),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   @Singleton",
            "   public abstract CountryService getService(CountryServiceImplementation implementation);",
            "   @Dependency",
            "   public abstract DependencyService getDependencyService();",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCountryServiceInService(target);",
            "       injectDependencyServiceInDependencyService(target);",
            "   }",
            "",
            "   private final void injectCountryServiceInService(@NonNull final Activity target) {",
            "       CountryServiceImplementation countryServiceImplementation = CountryServiceImplementationSingleton.get();",
            "       target.service = countryServiceImplementation;",
            "   }",
            "",
            "   private final void injectDependencyServiceInDependencyService(@NonNull final Activity target) {",
            "       DependencyService dependencyService = new DependencyService();",
            "       target.dependencyService = dependencyService;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyService, countryServiceImplementation, countryServiceFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void failNotReturnImplementation() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "   @Inject",
            "   public DependencyService dependencyService;",
            "}");

        JavaFileObject countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   public abstract CountryService getService();",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, countryServiceFile, moduleFile))
            .processedWith(new IProcessor())
            .failsToCompile()
            .withErrorContaining("ModuleFile.getService() returns test.CountryService which is interface also must contain implementation as parameter")
            .in(moduleFile)
            .onLine(7);
    }

    @Test
    public void failPassInterfaceAsParameter() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "   @Inject",
            "   public DependencyService dependencyService;",
            "}");

        JavaFileObject countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   public abstract CountryService getService(CountryService service);",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, countryServiceFile, moduleFile))
            .processedWith(new IProcessor())
            .failsToCompile()
            .withErrorContaining("ModuleFile.getService(test.CountryService) returns test.CountryService which is interface also contains interface as parameter must be implementation")
            .in(moduleFile)
            .onLine(7);
    }

    @Test
    public void methodMustBeStatic() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Named.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public String serviceName;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Named.class),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   @Named(\"named\")",
            "   public String getServiceName() { return \"some name\"; }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile))
            .processedWith(new IProcessor())
            .failsToCompile()
            .withErrorContaining("ModuleFile.getServiceName() is annotated with @Dependency must be static and public")
            .in(moduleFile)
            .onLine(9);
    }

    @Test
    public void injectStringFromModuleMethod() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Named.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public String serviceName;",
            "   @Inject",
            "   public Service service;",
            "}");

        JavaFileObject service = JavaFileObjects.forSourceLines("test.Service",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Named.class),
            "",
            "public class Service {",
            "   Service(@Named(\"named\") String serviceName) {}",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public String serviceName;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Named.class),
            "",
            "public class ModuleFile {",
            "   @Dependency",
            "   @Named(\"named\")",
            "   public static String getServiceName() { return \"some name\"; }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "import java.lang.String;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectStringInServiceName(target);",
            "       injectServiceInService(target);",
            "   }",
            "",
            "   private final void injectStringInServiceName(@NonNull final Activity target) {",
            "       String string = ModuleFile.getServiceName();",
            "       target.serviceName = string;",
            "   }",
            "",
            "   private final void injectServiceInService(@NonNull final Activity target) {",
            "       String string2 = ModuleFile.getServiceName();",
            "       Service service = new Service(string2);",
            "       target.service = service;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, service, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectIntegerFromModuleMethod() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Named.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public Integer serviceNumber;",
            "   @Inject",
            "   public Service service;",
            "}");

        JavaFileObject service = JavaFileObjects.forSourceLines("test.Service",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            Helpers.importType(Named.class),
            "",
            "public class Service {",
            "   Service(@Named(\"named\") Integer serviceNumber) {}",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public Integer serviceNumber;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Named.class),
            "",
            "public class ModuleFile {",
            "   @Dependency",
            "   @Named(\"named\")",
            "   public static Integer getServiceName() { return 10; }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "import java.lang.Integer;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectIntegerInServiceNumber(target);",
            "       injectServiceInService(target);",
            "   }",
            "",
            "   private final void injectIntegerInServiceNumber(@NonNull final Activity target) {",
            "       Integer integer = ModuleFile.getServiceName();",
            "       target.serviceNumber = integer;",
            "   }",
            "",
            "   private final void injectServiceInService(@NonNull final Activity target) {",
            "       Integer integer2 = ModuleFile.getServiceName();",
            "       Service service = new Service(integer2);",
            "       target.service = service;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, service, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void nestedModule() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "",
            "   @Inject",
            "   public String someString;",
            "}");

        JavaFileObject countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class CountryService {}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            "",
            "public abstract class ModuleFile {",
            "   public abstract static class NestedModule {",
            "       @Dependency",
            "       public abstract CountryService getService();",
            "       @Dependency",
            "       public static String getString() { return null; };",
            "   }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "import java.lang.String;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCountryServiceInService(target);",
            "       injectStringInSomeString(target);",
            "   }",
            "",
            "   private final void injectCountryServiceInService(@NonNull final Activity target) {",
            "       CountryService countryService = new CountryService();",
            "       target.service = countryService;",
            "   }",
            "",
            "   private final void injectStringInSomeString(@NonNull final Activity target) {",
            "       String string = ModuleFile.NestedModule.getString();",
            "       target.someString = string;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, countryService))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void kotlinModuleInField() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "}");

        JavaFileObject countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class CountryService {}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Metadata.class),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService() { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCountryServiceInService(target);",
            "   }",
            "",
            "   private final void injectCountryServiceInService(@NonNull final Activity target) {",
            "       CountryService countryService = Module.INSTANCE.getCountryService();",
            "       target.service = countryService;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, countryService))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void kotlinModuleInFieldWithParameter() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "}");

        JavaFileObject countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class CountryService {}");

        JavaFileObject countryRepository = JavaFileObjects.forSourceLines("test.CountryRepository",
            "package test;",
            "",
            "public class CountryRepository {",
            "   CountryRepository() {}",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Metadata.class),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService(CountryRepository repository) { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCountryServiceInService(target);",
            "   }",
            "",
            "   private final void injectCountryServiceInService(@NonNull final Activity target) {",
            "       CountryRepository countryRepository = new CountryRepository();",
            "       CountryService countryService = Module.INSTANCE.getCountryService(countryRepository);",
            "       target.service = countryService;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, countryRepository, countryService))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void kotlinModuleInConstructor() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryProvider service;",
            "}");

        JavaFileObject countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "public class CountryService {}");

        JavaFileObject countryProvider = JavaFileObjects.forSourceLines("test.CountryProvider",
            "package test;",
            "",
            "public class CountryProvider {",
            "   CountryProvider(CountryService service) {}",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Metadata.class),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService() { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCountryProviderInService(target);",
            "   }",
            "",
            "   private final void injectCountryProviderInService(@NonNull final Activity target) {",
            "       CountryService countryService = Module.INSTANCE.getCountryService();",
            "       CountryProvider countryProvider = new CountryProvider(countryService);",
            "       target.service = countryProvider;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, countryProvider, countryService))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void kotlinModuleInConstructorWithParameters() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryProvider service;",
            "}");

        JavaFileObject countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "public class CountryService {}");

        JavaFileObject countryRepository = JavaFileObjects.forSourceLines("test.CountryRepository",
            "package test;",
            "",
            "public class CountryRepository {",
            "   CountryRepository() {}",
            "}");

        JavaFileObject countryProvider = JavaFileObjects.forSourceLines("test.CountryProvider",
            "package test;",
            "",
            "public class CountryProvider {",
            "   CountryProvider(CountryService service) {}",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Metadata.class),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService(CountryRepository arg0) { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCountryProviderInService(target);",
            "   }",
            "",
            "   private final void injectCountryProviderInService(@NonNull final Activity target) {",
            "       CountryRepository countryRepository = new CountryRepository();",
            "       CountryService countryService = Module.INSTANCE.getCountryService(countryRepository);",
            "       CountryProvider countryProvider = new CountryProvider(countryService);",
            "       target.service = countryProvider;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, countryProvider, countryRepository, countryService))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void kotlinModuleSingletonParameter() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Factory fy;",
            "}");

        JavaFileObject singletonParameter = JavaFileObjects.forSourceLines("test.SingletonParameter",
            "package test;",
            Helpers.importType(Singleton.class),
            "@Singleton",
            "public class SingletonParameter {}");

        JavaFileObject countryProvider = JavaFileObjects.forSourceLines("test.CountryProvider",
            "package test;",
            "",
            "public class CountryProvider {",
            "   CountryProvider(SingletonParameter sp) {}",
            "}");

        JavaFileObject searchEngineService = JavaFileObjects.forSourceLines("test.SearchEngineService",
            "package test;",
            "",
            "public class SearchEngineService {",
            "}");

        JavaFileObject factory = JavaFileObjects.forSourceLines("test.Factory",
            "package test;",


            "public class Factory {",
            "   Factory(SearchEngineService s) {}",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Metadata.class),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryProvider getCountryService(SingletonParameter arg0) { return null; }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}");

        JavaFileObject suggestionModule = JavaFileObjects.forSourceLines("test.SuggestionModule",
            "package test;",
            "",
            Helpers.importType(Dependency.class),
            Helpers.importType(Metadata.class),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class SuggestionModule {",
            "   public static final SuggestionModule INSTANCE;",
            "   @Dependency",
            "   public final SearchEngineService getSearchEngineService(CountryProvider arg0) { return null; }",
            "",
            "   static {",
            "     SuggestionModule var0 = new SuggestionModule();",
            "     INSTANCE = var0;",
            "   }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectFactoryInFy(target);",
            "   }",
            "",
            "   private final void injectFactoryInFy(@NonNull final Activity target) {",
            "       SingletonParameter singletonParameter = SingletonParameterSingleton.get();",
            "       CountryProvider countryProvider = Module.INSTANCE.getCountryService(singletonParameter);",
            "       SearchEngineService searchEngineService = SuggestionModule.INSTANCE.getSearchEngineService(countryProvider);",
            "       Factory factory = new Factory(searchEngineService);",
            "       target.fy = factory;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, factory, suggestionModule, searchEngineService, singletonParameter, countryProvider))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }
}
