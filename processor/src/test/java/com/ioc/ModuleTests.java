package com.ioc;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.tools.JavaFileObject;

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
                "       ParentDependency singleton_getDependency = ParentDependencySingleton.get();",
                "       target.dependency = singleton_getDependency;",
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
                "       ParentDependency singleton_getDependency = ParentDependencySingleton.get();",
                "       target.setDependency(singleton_getDependency);",
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
                "       ParentDependency singleton_getDependency = ParentDependencySingleton.get();",
                "       target.dependency = singleton_getDependency;",
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
                "       DependencyModel dependencyModel = ModuleClass.getDependency();",
                "       Dependency2 singleton_getDependency2 = Dependency2Singleton.get();",
                "       ParentDependency parentDependency = ModuleClass.getParentDependency(dependencyModel, singleton_getDependency2);",
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
                "       ParentDependency singleton_parentDependency = ParentDependencySingleton.get();",
                "       target.dependency = singleton_parentDependency;",
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
                "       ParentDependency singleton_parentDependency = ParentDependencySingleton.get();",
                "       target.setDependency(singleton_parentDependency);",
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
                "       DependencyModel dependencyModel = new DependencyModel();",
                "       Singleton2 singleton_singleton2 = Singleton2Singleton.get();",
                "       singleton = new ParentDependency(dependencyModel, singleton_singleton2);",
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
                "       DependencyModel dependencyModel = new DependencyModel();",
                "       Singleton2 singleton_singleton2 = Singleton2Singleton.get();",
                "       singleton = new ParentDependency(dependencyModel, singleton_singleton2);",
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
                "       DependencyModel dependencyModel = new DependencyModel();",
                "       Singleton2 singleton_singleton2 = Singleton2Singleton.get();",
                "       ParentDependency parentDependency = new ParentDependency(dependencyModel, singleton_singleton2);",
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
                "       DependencyModel dependencyModel = new DependencyModel();",
                "       Singleton2 singleton_singleton2 = Singleton2Singleton.get();",
                "       ParentDependency parentDependency = new ParentDependency(dependencyModel, singleton_singleton2);",
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
                "       CountryServiceImplementation countryService = new CountryServiceImplementation();",
                "       target.service = countryService;",
                "   }",
                "}");

        assertAbout(javaSources())
                .that(Arrays.asList(activityFile, countryServiceImplementation, countryServiceFile, moduleFile))
                .processedWith(new IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile);
    }
}
