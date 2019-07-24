package com.ioc;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import javax.inject.Inject;
import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static com.ioc.Helpers.importType;

/**
 * Created by sergeygolishnikov on 01/11/2017.
 */
@RunWith(JUnit4.class)
public class WeakDependencyTests {
    @Test
    public void fieldInjectWithoutParams() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> weakDependency;",
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
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInWeakDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInWeakDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       WeakReference<DependencyModel> weak_dependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       target.weakDependency = weak_dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void fieldInjectWithParam() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> weakDependency;",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "@Dependency",
            "public class Context {",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInWeakDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInWeakDependency(@NonNull final Activity target) {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = new DependencyModel(context);",
            "       WeakReference<DependencyModel> weak_dependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       target.weakDependency = weak_dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, contextFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void methodInjectWithoutParams() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private WeakReference<DependencyModel> weakDependency;",
            "",
            "   public void weakDependency(WeakReference<DependencyModel> dependency) {};",
            "   public WeakReference<DependencyModel> getWeakDependency() { return null; };",
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
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInWeakDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInWeakDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       WeakReference<DependencyModel> weak_dependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       target.weakDependency(weak_dependencyModel);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void methodInjectWithParam() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private WeakReference<DependencyModel> weakDependency;",
            "",
            "   public void weakDependency(WeakReference<DependencyModel> dependency) {};",
            "   public WeakReference<DependencyModel> getWeakDependency() {return null;};",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "@Dependency",
            "public class Context {",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInWeakDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInWeakDependency(@NonNull final Activity target) {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = new DependencyModel(context);",
            "       WeakReference<DependencyModel> weak_dependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       target.weakDependency(weak_dependencyModel);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, contextFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void provideMethodInjectWithParam() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}");

        JavaFileObject dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency.class),
            importType(WeakReference.class),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(WeakReference<Context> context) { return null; }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       Context context = new Context();",
            "       WeakReference<Context> weak_context = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(weak_context);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyProvideModuleFile, contextFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void provideWeakMethodInjectWithParam() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "public class Resource {",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "   Context(Resource resource) {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}");

        JavaFileObject dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency.class),
            importType(WeakReference.class),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(WeakReference<Context> context) { return null; }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       Resource resource = new Resource();",
            "       Context context = new Context(resource);",
            "       WeakReference<Context> weak_context = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(weak_context);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, resourceFile, dependencyProvideModuleFile, contextFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void provideMethodInject() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel() {}",
            "}");

        JavaFileObject dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency.class),
            importType(WeakReference.class),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return null; }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency();",
            "       WeakReference<DependencyModel> weak_dependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       target.dependency = weak_dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyProvideModuleFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void provideMethodInjectWithParamWeak() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> dependency;",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}");

        JavaFileObject dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency.class),
            importType(WeakReference.class),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(Context context) { return null; }",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(context);",
            "       WeakReference<DependencyModel> weak_dependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       target.dependency = weak_dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyProvideModuleFile, contextFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void fieldInjectWithConstructorParamWeak() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "@Dependency",
            "public class Context {",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            importType(WeakReference.class),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(WeakReference<Context> context) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference.class),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       Context context = new Context();",
            "       WeakReference<Context> weak_context = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = new DependencyModel(weak_context);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, contextFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

}
