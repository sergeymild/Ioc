package com.ioc

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import com.ioc.Helpers.importType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 01/11/2017.
 */
@RunWith(JUnit4::class)
class WeakDependencyTests {
    @Test
    @Throws(Exception::class)
    fun fieldInjectWithoutParams() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> weakDependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency = provideDependencyModel();",
            "   }",
            "",
            "   private static final WeakReference<DependencyModel> provideDependencyModel() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       WeakReference<DependencyModel> weakDependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       return weakDependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun fieldInjectWithParam() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> weakDependency;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(Dependency::class.java),
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency = provideDependencyModel();",
            "   }",
            "",
            "   private static final WeakReference<DependencyModel> provideDependencyModel() {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = new DependencyModel(context);",
            "       WeakReference<DependencyModel> weakDependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       return weakDependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun methodInjectWithoutParams() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private WeakReference<DependencyModel> weakDependency;",
            "",
            "   public void weakDependency(WeakReference<DependencyModel> dependency) {};",
            "   public WeakReference<DependencyModel> getWeakDependency() { return null; };",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency(provideDependencyModel());",
            "   }",
            "",
            "   private static final WeakReference<DependencyModel> provideDependencyModel() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       WeakReference<DependencyModel> weakDependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       return weakDependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun methodInjectWithParam() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private WeakReference<DependencyModel> weakDependency;",
            "",
            "   public void weakDependency(WeakReference<DependencyModel> dependency) {};",
            "   public WeakReference<DependencyModel> getWeakDependency() {return null;};",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(Dependency::class.java),
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency(provideDependencyModel());",
            "   }",
            "",
            "   private static final WeakReference<DependencyModel> provideDependencyModel() {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = new DependencyModel(context);",
            "       WeakReference<DependencyModel> weakDependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       return weakDependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun provideMethodInjectWithParam() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(WeakReference::class.java),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(WeakReference<Context> context) { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   private static final DependencyModel provideDependencyModel() {",
            "       Context context = new Context();",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(weakContext);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyProvideModuleFile, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun provideWeakMethodInjectWithParam() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "public class Resource {",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "   Context(Resource resource) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(WeakReference::class.java),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(WeakReference<Context> context) { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   private static final DependencyModel provideDependencyModel() {",
            "       Resource resource = new Resource();",
            "       Context context = new Context(resource);",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(weakContext);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, resourceFile, dependencyProvideModuleFile, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun provideMethodInject() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel() {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(WeakReference::class.java),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   private static final WeakReference<DependencyModel> provideDependencyModel() {",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency();",
            "       WeakReference<DependencyModel> weakDependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       return weakDependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyProvideModuleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun provideMethodInjectWithParamWeak() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> dependency;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(WeakReference::class.java),
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(Context context) { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   private static final WeakReference<DependencyModel> provideDependencyModel() {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(context);",
            "       WeakReference<DependencyModel> weakDependencyModel = new WeakReference<DependencyModel>(dependencyModel);",
            "       return weakDependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyProvideModuleFile, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun fieldInjectWithConstructorParamWeak() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(Dependency::class.java),
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject::class.java),
            importType(WeakReference::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(WeakReference<Context> context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(WeakReference::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   private static final DependencyModel provideDependencyModel() {",
            "       Context context = new Context();",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = new DependencyModel(weakContext);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}
