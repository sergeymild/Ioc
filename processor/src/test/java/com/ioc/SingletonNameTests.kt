package com.ioc

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import com.ioc.Helpers.importType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 14/08/2017.
 */
@RunWith(JUnit4::class)
class SingletonNameTests : BaseTest {

    @Test
    @Throws(Exception::class)
    fun crashlitycs() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CrashlitycsService service;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface CrashlitycsService {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Singleton",
            "class CrashlyticsLogger implements CrashlitycsService {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "     injectCrashlitycsServiceInService(target);",
            "   }",
            "",
            "   private final void injectCrashlitycsServiceInService(@NonNull final Activity target) {",
            "        CrashlyticsLogger crashlyticsLogger = CrashlyticsLoggerSingleton.get();",
            "        target.service = crashlyticsLogger;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, release, dependencyFile))
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
            importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            importType(Singleton::class.java),
            importType(Inject::class.java),
            "",
            "@Dependency",
            "@Singleton",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel(CrashlitycsService service) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface CrashlitycsService {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Singleton",
            "class CrashlyticsLogger implements CrashlitycsService {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DependencyModelSingleton",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class DependencyModelSingleton",
            "   private static DependencyModel singleton;",
            "",
            "   private static final DependencyModelSingleton instance = new DependencyModelSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final DependencyModel get() {",
            "       if (singleton != null) return singleton;",
            "       CrashlyticsLogger crashlyticsLogger = CrashlyticsLoggerSingleton.get();",
            "       singleton = new DependencyModel(crashlyticsLogger);",
            "       return singleton;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, moduleFile, release))
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
            Helpers.importType(Inject::class.java),
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
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
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
            "import $keep",
            "import $nonNull",
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
            "       target.setDependency(parentDependency);",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile, dependencyFile2, parentDependencyFile))
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CrashlyticsLogger service;",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Singleton",
            "class CrashlyticsLogger {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "     injectCrashlyticsLoggerInService(target);",
            "   }",
            "",
            "   private final void injectCrashlyticsLoggerInService(@NonNull final Activity target) {",
            "       CrashlyticsLogger crashlyticsLogger = CrashlyticsLoggerSingleton.get();",
            "       target.service = crashlyticsLogger;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, release))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}