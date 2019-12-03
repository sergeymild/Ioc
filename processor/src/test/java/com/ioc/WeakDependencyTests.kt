package com.ioc

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
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
            importInjectAnnotation,
            importWeakReference,
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
            importKeepAnnotation,
            importNonNullAnnotation,
            "import $weakReferenceType",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency = new WeakReference<>(new DependencyModel());",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> weakDependency;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency = new WeakReference<>(provideDependencyModel());",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = new DependencyModel(context);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, contextFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
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
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency(new WeakReference<>(new DependencyModel()));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
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
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.weakDependency(new WeakReference<>(provideDependencyModel()));",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = new DependencyModel(context);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, contextFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
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
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importDependencyAnnotation,
            importWeakReference,
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(WeakReference<Context> context) { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       Context context = new Context();",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(weakContext);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyProvideModuleFile, contextFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
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
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importDependencyAnnotation,
            importWeakReference,
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(WeakReference<Context> context) { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       Resource resource = new Resource();",
            "       Context context = new Context(resource);",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(weakContext);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, resourceFile, dependencyProvideModuleFile, contextFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WeakReference<DependencyModel> dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel() {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importDependencyAnnotation,
            importWeakReference,
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new WeakReference<>(DependencyProvide.getDependency());",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyProvideModuleFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
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
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   DependencyModel(Context context) {}",
            "}")

        val dependencyProvideModuleFile = JavaFileObjects.forSourceLines("test.DependencyProvide",
            "package test;",
            "",
            importDependencyAnnotation,
            importWeakReference,
            "",
            "public class DependencyProvide {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(Context context) { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new WeakReference<>(provideDependencyModel());",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       Context context = new Context();",
            "       DependencyModel dependencyModel = DependencyProvide.getDependency(context);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyProvideModuleFile, contextFile, dependencyFile))
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
            importInjectAnnotation,
            importWeakReference,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            importWeakReference,
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(WeakReference<Context> context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importWeakReference,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       Context context = new Context();",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       DependencyModel dependencyModel = new DependencyModel(weakContext);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}
