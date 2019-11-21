package com.ioc

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 14/08/2017.
 */
@RunWith(JUnit4::class)
class SingletonNameTests {

    @Test
    @Throws(Exception::class)
    fun crashlitycs() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CrashlitycsService service;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.CrashlitycsService",
            "package test;",
            "",
            "public interface CrashlitycsService {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.CrashlyticsLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class CrashlyticsLogger implements CrashlitycsService {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIoc,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "     target.service = Ioc.getSingleton(CrashlyticsLogger.class);",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, release, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun correctInjection6() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel(CrashlitycsService service) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.CrashlitycsService",
            "package test;",
            "",
            "public interface CrashlitycsService {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.CrashlyticsLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class CrashlyticsLogger implements CrashlitycsService {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DependencyModelSingleton",
            "package test;",
            "",
            importKeepAnnotation,
            importIoc,
            importProvider,
            "",
            "@Keep",
            "public final class DependencyModelSingleton implements Provider<DependencyModel> {",
            "",
            "   public final DependencyModel get() {",
            "       return new DependencyModel(Ioc.getSingleton(CrashlyticsLogger.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, moduleFile, release))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonAndArgumentsAsDependencyInActivityMethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return  null; };",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getParentDependency(DependencyModel dependency, Dependency2 dependency2) { return new ParentDependency(); }",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return new DependencyModel(); }",
            "   @Dependency @Singleton",
            "   public static Dependency2 getDependency2() { return new Dependency2(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val dependencyFile2 = JavaFileObjects.forSourceLines("test.Dependency2",
            "package test;",
            "",
            "public class Dependency2 {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIoc,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.setDependency(provideParentDependency());",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       ParentDependency parentDependency = ModuleClass.getParentDependency(dependencyModel,Ioc.getSingleton(Dependency2.class));",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, moduleFile, dependencyFile, dependencyFile2, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun testInjectSingletone() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CrashlyticsLogger service;",
            "}")

        val release = JavaFileObjects.forSourceLines("test.CrashlyticsLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class CrashlyticsLogger {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIoc,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "     target.service = Ioc.getSingleton(CrashlyticsLogger.class);",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, release))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}