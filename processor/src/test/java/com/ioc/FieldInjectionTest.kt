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
 * Created by sergeygolishnikov on 08/08/2017.
 */
@RunWith(JUnit4::class)
class FieldInjectionTest {

    @Test
    @Throws(Exception::class)
    fun emptyConstructor() {
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

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",

            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new DependencyModel();",
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
    fun skipExcludedPackage1() {
        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   private DependencyModel() {}",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $assetManager;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel model;",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("Cant find suitable constructors test.DependencyModel")
    }

    @Test
    @Throws(Exception::class)
    fun failOnWrongSetter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private void setPreferences(Preferences preferences, String param) {};",
            "}")

        val preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            "public class Preferences {}")


        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, preferencesFile))
            .processedWith(IProcessor())
            .failsToCompile().withErrorContaining("@Inject annotation is placed on method `setPreferences(test.Preferences,java.lang.String)` in `test.Activity` with private access")
            .`in`(activityFile)
            .onLine(8)
    }

    @Test
    @Throws(Exception::class)
    fun privateSetter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "   @Inject",
            "   private void setPreferences(Preferences preferences) {};",
            "}")

        val preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            "public class Preferences {}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, preferencesFile))
            .processedWith(IProcessor())
            .failsToCompile().withErrorContaining("@Inject annotation is placed on method `setPreferences(test.Preferences)` in `test.Activity` with private access")
            .`in`(activityFile)
            .onLine(7)
    }

    @Test
    @Throws(Exception::class)
    fun publicAndPrivateFields() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",

            "   @Inject",
            "   private Logger logger;",

            "   public void setLogger(Logger logger) {};",
            "   public Logger getLogger() {return null;};",
            "",
            "   @Inject",
            "   public void setPreferences(Preferences preferences) {};",
            "   public Preferences setPreferences() { return null; };",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val loggerFile = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            "public class Logger {}")

        val preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            "public class Preferences {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new DependencyModel();",
            "       target.setLogger(new Logger());",
            "       target.setPreferences(new Preferences());",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, loggerFile, preferencesFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun failOnFindPrivateFieldWithoutSetter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@Inject annotation placed on field `dependency` in `Activity` with private access and which does't have public setter method.")
            .`in`(activityFile)
            .onLine(8)
    }


    @Test
    @Throws(Exception::class)
    fun failOnFindPrivateFieldWithPrivateSetter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "   private void setDependency(DependencyModel dep) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@Inject annotation placed on field `dependency` in `Activity` with private access and which does't have public setter method.")
            .`in`(activityFile)
            .onLine(8)
    }

    @Test
    @Throws(Exception::class)
    fun providerField() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importProvider,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Provider<DependencyModel> dependency;",
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
            importIocProvider,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new IocProvider<DependencyModel>() {",
            "           protected DependencyModel initialize() {",
            "               return new DependencyModel();",
            "           }",
            "       }",
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
    fun providerFieldWithInterface() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importProvider,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Provider<DependencyInterface> dependency;",
            "}")

        val dependencyInterface = JavaFileObjects.forSourceLines("test.DependencyInterface",
            "package test;",
            "",
            "public interface DependencyInterface {}")

        val dependencyType = JavaFileObjects.forSourceLines("test.DependencyType",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class DependencyType implements DependencyInterface {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIocProvider,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new IocProvider<DependencyInterface>() {",
            "           protected DependencyInterface initialize() {",
            "               return new DependencyType();",
            "           }",
            "       }",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyInterface, dependencyType))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun lazyFieldWithInterface() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importLazy,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Lazy<DependencyInterface> dependency;",
            "}")

        val dependencyInterface = JavaFileObjects.forSourceLines("test.DependencyInterface",
            "package test;",
            "",
            "public interface DependencyInterface {}")

        val dependencyType = JavaFileObjects.forSourceLines("test.DependencyType",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class DependencyType implements DependencyInterface {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIocLazy,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new IocLazy<DependencyInterface>() {",
            "           protected DependencyInterface initialize() {",
            "               return new DependencyType();",
            "           }",
            "       }",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyInterface, dependencyType))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun targetAsParameterInConstructor() {
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

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideDependencyModel(target);",
            "   }",
            "",
            "   private static final DependencyModel provideDependencyModel(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       return dependencyModel;",
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
    fun injectWithTargetAndDependencyInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(Activity activity, DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideParentDependency(target);",
            "   }",
            "",
            "   private static final ParentDependency provideParentDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(target, dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectWithDependencyInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideParentDependency();",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectWithTargetInDependencyOfParentDependencyInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideParentDependency(target);",
            "   }",
            "",
            "   private static final ParentDependency provideParentDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun preferConstructorWithArguments() {
        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "public class ParentActivity {",
            "",
            "   public DependencyModel getDependency() { return null; }",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel() {};",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = provideParentDependency();",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun preferTargetConstructorWithArguments() {
        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            importInjectAnnotation,
            "public class ParentActivity {",
            "",
            "   @Inject",
            "   DependencyModel dependencyModel;",
            "   public DependencyModel getDependency() { return null; }",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel() {};",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       ParentActivityInjector.inject(target);",
            "       target.dependency = provideParentDependency();",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun correctInjectionOrder() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "   @Inject",
            "   public DependencyModel dependency;",
            "   @Inject",
            "   public ChildDependencyModel child;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "   public DependencyModel(ChildDependencyModel child) {};",
            "}")

        val childDependencyFile = JavaFileObjects.forSourceLines("test.ChildDependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class ChildDependencyModel {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class ParentDependency {",
            "   public ParentDependency(DependencyModel childDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.child = new ChildDependencyModel();",
            "       target.dependency = provideDependencyModel();",
            "       target.parentDependency = provideParentDependency();",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       ChildDependencyModel childDependencyModel = new ChildDependencyModel();",
            "       DependencyModel dependencyModel = new DependencyModel(childDependencyModel);",
            "       return dependencyModel;",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       ChildDependencyModel childDependencyModel = new ChildDependencyModel();",
            "       DependencyModel dependencyModel = new DependencyModel(childDependencyModel);",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, childDependencyFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun correctInjection3() {

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

        val moduleFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class ReleaseDependency implements DependencyModel {",
            "",
            "   public ReleaseDependency() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new ReleaseDependency();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun correctInjection5() {

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
            importInjectAnnotation,
            "",
            "@Dependency",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel(Context context, Resource resource) {}",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            importDependencyAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "public class Resource {",
            "",
            "   @Inject",
            "   Resource(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
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
            "       Context context2 = new Context();",
            "       Resource resource = new Resource(context2);",
            "       DependencyModel dependencyModel = new DependencyModel(context, resource);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, contextFile, resourceFile))
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
            importInjectAnnotation,
            "",
            "@Dependency",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel(Context context, Resource resource) {}",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "}")

        val contextModuleFile = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "  @Dependency",
            "  public static Context context() { return new Context(); }",
            "}")

        val resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Resource {",
            "",
            "   @Inject",
            "   Resource(Context context) {}",
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
            "       target.dependency = provideDependencyModel();",
            "   }",
            "",
            "   public static final DependencyModel provideDependencyModel() {",
            "       Context context = ContextModule.context();",
            "       DependencyModel dependencyModel = new DependencyModel(context,Ioc.getSingleton(Resource.class));",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, contextModuleFile, moduleFile, contextFile, resourceFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun findImplementationInParent() {

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


        val superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importDependencyAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "public class AppModel extends BaseModel {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.BaseModel",
            "package test;",
            "",
            "public class BaseModel implements DependencyModel {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new AppModel();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, superParentFile, parentFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun findImplementationInInterfaceParent() {

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


        val superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importDependencyAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "public class AppModel extends BaseModel {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.BaseModel",
            "package test;",
            "",
            "public class BaseModel implements DependencyParent {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val dependencyParentFile = JavaFileObjects.forSourceLines("test.DependencyParent",
            "package test;",
            "",
            "public interface DependencyParent extends DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new AppModel();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, superParentFile, parentFile, moduleFile, dependencyParentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun findImplementationInParent2() {

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


        val superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importDependencyAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "public class AppModel extends BaseModel {",
            "   @Inject",
            "   public AppModel(Resource resources) {}",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.BaseModel",
            "package test;",
            "",
            "public class BaseModel implements DependencyModel {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Context implements Resource {",
            "}")

        val resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "",
            "public interface Resource {",
            "",
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
            "       target.dependency = provideAppModel();",
            "   }",
            "",
            "   public static final AppModel provideAppModel() {",
            "       AppModel dependencyModel = new AppModel(Ioc.getSingleton(Context.class));",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, superParentFile, contextFile, resourceFile, parentFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun twoInterfacesFromOnImplementationInConstructor() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public BaseModel dependency;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.SpeedDialTileClosedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialTileClosedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.SpeedDialTileClickedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialTileClickedEventLogger {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Amplitude implements SpeedDialTileClickedEventLogger, SpeedDialTileClosedEventLogger {",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.BaseModel",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class BaseModel {",
            "   @Inject",
            "   BaseModel(SpeedDialTileClickedEventLogger speedDialTileClickedEventLogger, SpeedDialTileClosedEventLogger speedDialTileClosedEventLogger) {}",
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
            "       target.dependency = provideBaseModel();",
            "   }",
            "",
            "   public static final BaseModel provideBaseModel() {",
            "       BaseModel baseModel = new BaseModel(Ioc.getSingleton(Amplitude.class),Ioc.getSingleton(Amplitude.class));",
            "       return baseModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, superParentFile, baseFile, parentFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectInParentClass2() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity extends BaseActivity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}")

        val baseActivityFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public abstract class BaseActivity {",
            "",
            "   @Inject",
            "   public ClosedEventLogger closedEventLogger;",
            "   @Inject",
            "   public void setEventLogger(ClosedEventLogger logger) {};",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.ClosedEventLogger",
            "package test;",
            "",
            "public interface ClosedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "public interface ClickedEventLogger {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Amplitude implements ClickedEventLogger, ClosedEventLogger {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIoc,
            "",
            "@Keep",
            "public final class BaseActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final BaseActivity target) {",
            "       target.closedEventLogger = Ioc.getSingleton(Amplitude.class);",
            "       target.setEventLogger(Ioc.getSingleton(Amplitude.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, baseActivityFile, superParentFile, parentFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectInParentClass() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity extends BaseActivity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}")

        val baseActivityFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public abstract class BaseActivity {",
            "",
            "   @Inject",
            "   public ClosedEventLogger closedEventLogger;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.ClosedEventLogger",
            "package test;",
            "",
            "public interface ClosedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "public interface ClickedEventLogger {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Amplitude implements ClickedEventLogger, ClosedEventLogger {",
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
            "       BaseActivityInjector.inject(target);",
            "       target.clickedEventLogger = Ioc.getSingleton(Amplitude.class);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, baseActivityFile, superParentFile, parentFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectInParentClass1() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Logger logger;",
            "}")

        val loggerFile = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Singleton",
            "@Dependency",
            "public class Logger {",
            "",
            "   @Inject",
            "   public Logger(ClosedEventLogger closedEventLogger, Amplitude amplitude) {};",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.ClosedEventLogger",
            "package test;",
            "",
            "public interface ClosedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "public interface ClickedEventLogger {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Amplitude implements ClickedEventLogger, ClosedEventLogger {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.LoggerSingleton",
            "package test;",
            "",
            importKeepAnnotation,
            importIoc,
            importProvider,
            "",
            "@Keep",
            "public final class LoggerSingleton implements Provider<Logger> {",
            "",
            "   public final Logger get() {",
            "       return new Logger(Ioc.getSingleton(Amplitude.class),Ioc.getSingleton(Amplitude.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, loggerFile, superParentFile, parentFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun twoInterfacesFromOnImplementationInConstructorForSingleton() {

        val activityFile = JavaFileObjects.forSourceLines("test.DownloadsFragment",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DownloadsFragment {",
            "",
            "   @Inject",
            "   public DownloadsNavigationLogger downloadsNavigationLogger;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationPathIndicatorClickedEventLogger",
            "package test;",
            "",
            "public interface DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationSystemBackClickedEventLogger",
            "package test;",
            "",
            "public interface DownloadsNavigationSystemBackClickedEventLogger {",
            "}")


        val amplitudeLoggerFile = JavaFileObjects.forSourceLines("test.AmplitudeLogger",
            "package test;",
            "",
            "public abstract class AmplitudeLogger implements DownloadsNavigationPathIndicatorClickedEventLogger, DownloadsNavigationSystemBackClickedEventLogger {",
            "}")

        val preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Preferences {",
            "}")


        val moduleFile = JavaFileObjects.forSourceLines("test.AmplitudeService",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class AmplitudeService extends AmplitudeLogger {",
            "   @Inject",
            "   public AmplitudeService(Preferences prefs) {}",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class DownloadsNavigationLogger {",
            "   @Inject",
            "   DownloadsNavigationLogger(DownloadsNavigationPathIndicatorClickedEventLogger downloadsNavigationPathIndicatorClickedEventLogger, DownloadsNavigationSystemBackClickedEventLogger downloadsNavigationSystemBackClickedEventLogger) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLoggerSingleton",
            "package test;",
            "",
            importKeepAnnotation,
            importIoc,
            importProvider,
            "",
            "@Keep",
            "public final class DownloadsNavigationLoggerSingleton implements Provider<DownloadsNavigationLogger> {",
            "",
            "   public final DownloadsNavigationLogger get() {",
            "       return new DownloadsNavigationLogger(Ioc.getSingleton(AmplitudeService.class),Ioc.getSingleton(AmplitudeService.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, preferencesFile, amplitudeLoggerFile, superParentFile, baseFile, parentFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun inject() {

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {}")


        val contextModuleFile = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "   @Dependency",
            "   public static Context getContext() { return null; };",
            "}")

        val preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {};",
            "}")


        val buildCheckFile = JavaFileObjects.forSourceLines("test.BuildCheck",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "public class BuildCheck {",
            "   @Inject",
            "   public BuildCheck(Preferences prefs) {};",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   private BuildCheck buildCheck;",
            "   @Inject",
            "   public void appendBuildCheck(BuildCheck buildCheck) {};",
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
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.appendBuildCheck(provideBuildCheck());",
            "   }",
            "",
            "   public static final BuildCheck provideBuildCheck() {",
            "       BuildCheck buildCheck = new BuildCheck(Ioc.getSingleton(Preferences.class));",
            "       return buildCheck;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, contextModuleFile, contextFile, preferencesFile, buildCheckFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    fun injectTargetInDependency() {

        val listenerFile = JavaFileObjects.forSourceLines("test.Listener",
            "package test;",
            "",
            "public interface Listener {}")

        val controllerFile = JavaFileObjects.forSourceLines("test.Controller",
            "package test;",
            "",
            "public class Controller {",
            "",
            "   Controller(Listener listener) {}",
            "}")

        val autoCompleteListenerImplFile = JavaFileObjects.forSourceLines("test.AutoCompleteListenerImpl",
            "package test;",
            "",
            importDependencyAnnotation,
            importAndroidContext,
            "",
            "@Dependency",
            "public class AutoCompleteListenerImpl implements Listener {",
            "",
            "   AutoCompleteListenerImpl(Context context) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class MyActivity extends Activity {",
            "",
            "   private Controller controller;",
            "   @Inject",
            "   public void appendBuildCheck(Controller controller) {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final MyActivity target) {",
            "       target.appendBuildCheck(provideController(target));",
            "   }",
            "",
            "   private static final Controller provideController(@NonNull final MyActivity target) {",
            "       AutoCompleteListenerImpl listener = new AutoCompleteListenerImpl(target);",
            "       Controller controller = new Controller(listener);",
            "       return controller;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, listenerFile, controllerFile, autoCompleteListenerImplFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun injectTargetInDependency1() {

        val interceptor = JavaFileObjects.forSourceLines("test.HttpLoggingInterceptor",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class HttpLoggingInterceptor {",
            "",
            "   public HttpLoggingInterceptor(Level level) {}",
            "",
            "   public enum Level { NONE, BASIC }",
            "}")


        val restModule = JavaFileObjects.forSourceLines("test.RestModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class RestModule {",
            "",
            "   @Dependency",
            "   public static HttpLoggingInterceptor provideHttpLoggingInterceptor() {  return null; }",
            "}")

        val controllerFile = JavaFileObjects.forSourceLines("test.Controller",
            "package test;",
            "",
            "public class Controller {",
            "",
            "   Controller(HttpLoggingInterceptor interceptor) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   private Controller controller;",
            "   @Inject",
            "   public void appendBuildCheck(Controller controller) {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.appendBuildCheck(provideController());",
            "   }",
            "",
            "   public static final Controller provideController() {",
            "       HttpLoggingInterceptor httpLoggingInterceptor = RestModule.provideHttpLoggingInterceptor();",
            "       Controller controller = new Controller(httpLoggingInterceptor);",
            "       return controller;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, interceptor, restModule, controllerFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectTargetInDependency2() {

        val speedDialDisplayedEventLogger = JavaFileObjects.forSourceLines("test.SpeedDialDisplayedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialDisplayedEventLogger {",
            "}")

        val preferences = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            "",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {}",
            "}")

        val amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Singleton",
            "@Dependency",
            "public class AmplitudeDefaultLogger implements SpeedDialDisplayedEventLogger {",
            "   @Inject",
            "   public AmplitudeDefaultLogger(Preferences preferences) {}",
            "}")


        val restModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}")

        val controllerFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private SpeedDialDisplayedEventLogger logger;",
            "   public void setLogger(SpeedDialDisplayedEventLogger logger) {};",
            "   public SpeedDialDisplayedEventLogger setLogger() { return null; };",
            "   public Context context() { return null; };",
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
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.setLogger(Ioc.getSingleton(AmplitudeDefaultLogger.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, speedDialDisplayedEventLogger, preferences, amplitudeDefaultLogger, restModule, controllerFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun moduleOverTarget() {

        val speedDialDisplayedEventLogger = JavaFileObjects.forSourceLines("test.SpeedDialDisplayedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialDisplayedEventLogger {",
            "}")

        val preferences = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            "",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {}",
            "}")

        val amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Singleton",
            "@Dependency",
            "public class AmplitudeDefaultLogger implements SpeedDialDisplayedEventLogger {",
            "   @Inject",
            "   public AmplitudeDefaultLogger(Preferences preferences) {}",
            "}")


        val restModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}")

        val controllerFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private SpeedDialDisplayedEventLogger logger;",
            "   public void setLogger(SpeedDialDisplayedEventLogger logger) {};",
            "   public SpeedDialDisplayedEventLogger getLogger() { return null; };",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.PreferencesSingleton",
            "package test;",
            "",
            importKeepAnnotation,
            importProvider,
            "",
            "@Keep",
            "public final class PreferencesSingleton implements Provider<Preferences> {",
            "",
            "   public final Preferences get() {",
            "       Context context = ContextModule.context();",
            "       return new Preferences(context);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, speedDialDisplayedEventLogger, preferences, amplitudeDefaultLogger, restModule, controllerFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun stackOverflowError() {

        val runnable = JavaFileObjects.forSourceLines("test.Runnable",
            "package test;",
            "",
            "public interface Runnable {",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public abstract class Context {",
            "}")

        val parentActivity = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "public class ParentActivity extends Context {",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Presenter implements Runnable {",
            "   @Inject",
            "   public Presenter(Context context, Runnable runnable) {}",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity extends ParentActivity implements Runnable {",
            "",
            "   @Inject",
            "   private Presenter presenter;",
            "   public void set(Presenter presenter) {};",
            "   public Presenter get() { return null; };",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "     target.set(providePresenter(target));",
            "   }",
            "",
            "   private static final Presenter providePresenter(@NonNull final Activity target) {",
            "       Presenter presenter = new Presenter(target, target);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, runnable, parentActivity, context, presenter))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun oneSingletonForDependencies() {

        val speedDialDisplayedEventLogger = JavaFileObjects.forSourceLines("test.SpeedDialDisplayedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialDisplayedEventLogger {",
            "}")

        val defaultLogger = JavaFileObjects.forSourceLines("test.DefaultLogger",
            "package test;",
            "",
            "public interface DefaultLogger {",
            "}")

        val preferences = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context, DefaultLogger defaultLogger) {}",
            "}")

        val amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Singleton",
            "@Dependency",
            "public class AmplitudeDefaultLogger implements SpeedDialDisplayedEventLogger, DefaultLogger {",
            "   @Inject",
            "   public AmplitudeDefaultLogger() {}",
            "}")


        val restModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context(SpeedDialDisplayedEventLogger eventLogger) {  return null; }",
            "}")

        val controllerFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Preferences preferences;",
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
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.preferences = providePreferences();",
            "   }",
            "",
            "   public static final Preferences providePreferences() {",
            "       Context context = ContextModule.context(Ioc.getSingleton(AmplitudeDefaultLogger.class));",
            "       Preferences preferences = new Preferences(context,Ioc.getSingleton(AmplitudeDefaultLogger.class));",
            "       return preferences;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, speedDialDisplayedEventLogger, defaultLogger, preferences, amplitudeDefaultLogger, restModule, controllerFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun weakModule() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",
            importWeakReference,
            "public class Presenter {",
            "   Presenter(WeakReference<Context> context) {}",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter();",
            "   }",
            "",
            "   public static final Presenter providePresenter() {",
            "       Context context = ContextModule.context();",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       Presenter presenter = new Presenter(weakContext);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, contextModule, context))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun providerModule() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            importProvider,

            "public class Presenter {",
            "   Presenter(Provider<Context> context) {}",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIocProvider,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter();",
            "   }",
            "",
            "   public static final Presenter providePresenter() {",
            "       IocProvider<Context> providerContext = new IocProvider<Context>() {",
            "           protected Context initialize() {",
            "             Context context = ContextModule.context();",
            "             return context;",
            "           }",
            "       }",
            "       Presenter presenter = new Presenter(providerContext);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, contextModule, context))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun preferEmptyConstructor() {


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importRxJavaCompositeDisposable,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CompositeDisposable subscription;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importRxJavaCompositeDisposable,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.subscription = new CompositeDisposable();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun providerLazy() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            importLazy,

            "public class Presenter {",
            "   Presenter(Lazy<Context> context) {}",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIocLazy,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter();",
            "   }",
            "",
            "   public static final Presenter providePresenter() {",
            "       IocLazy<Context> lazyContext = new IocLazy<Context>() {",
            "           protected Context initialize() {",
            "             Context context = ContextModule.context();",
            "             return context;",
            "           }",
            "       }",
            "       Presenter presenter = new Presenter(lazyContext);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, contextModule, context))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun lazyParam() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importLazy,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Lazy<Presenter> presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIocLazy,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = new IocLazy<Presenter>() {",
            "           protected Presenter initialize() {",
            "               return providePresenter();",
            "           }",
            "       }",
            "   }",
            "",
            "   public static final Presenter providePresenter() {",
            "       Context context = ContextModule.context();",
            "       Presenter presenter = new Presenter(context);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, contextModule, context))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun module() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Resource resource() {  return null; }",
            "   @Dependency",
            "   public static Context context(Resource resource, Activity activity) {  return null; }",
            "   @Dependency",
            "   public static Presenter presenter(Context context) {  return null; }",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")

        val resources = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "public class Resource {",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter(target);",
            "   }",
            "",
            "   private static final Presenter providePresenter(@NonNull final Activity target) {",
            "       Resource resource = ContextModule.resource();",
            "       Context context = ContextModule.context(resource, target);",
            "       Presenter presenter = ContextModule.presenter(context);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, resources, presenter, contextModule, context))
            .processedWith(IProcessor())

            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun moduleSingleton() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Resource resource() {  return null; }",
            "   @Dependency",
            "   @Singleton",
            "   public static Context context(Resource resource) {  return null; }",
            "   @Dependency",
            "   public static Presenter presenter(Context context) {  return null; }",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")

        val resources = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "public class Resource {",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter();",
            "   }",
            "",
            "   public static final Presenter providePresenter() {",
            "       Presenter presenter = ContextModule.presenter(Ioc.getSingleton(Context.class));",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, resources, presenter, contextModule, context))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun doNotPassTargetInSingleton() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   @Singleton",
            "   public static Context context(Activity activity) {  return null; }",
            "   @Dependency",
            "   public static Presenter presenter(Context context) {  return null; }",
            "}")

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, contextModule, context))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("target can't be user as dependency in Singleton")
            .`in`(contextModule).onLine(10)
    }

    @Test
    @Throws(Exception::class)
    fun doNotpassTargetInSingleton2() {


        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            importSingletonAnnotation,
            "@Singleton",
            "public class Context {",
            "",
            "}")

        val resource = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            importSingletonAnnotation,

            "public class Resource {",
            "   Resource(Context context) {}",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context, Resource resource) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter();",
            "   }",
            "",
            "   public static final Presenter providePresenter() {",
            "       Resource resource = new Resource(Ioc.getSingleton(Context.class));",
            "       Presenter presenter = new Presenter(Ioc.getSingleton(Context.class),resource);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, resource, context))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun abstractClass() {


        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "abstract class Context {",
            "",
            "   Context(Preferences preferences) {}",
            "",
            "}")

        val resource = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            importSingletonAnnotation,
            importDependencyAnnotation,

            "@Dependency",
            "public class Resource extends Context {",
            "",
            "   Resource(Preferences preferences) { super(preferences);}",
            "",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",

            "public class Preferences {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Context context;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.context = provideResource();",
            "   }",
            "",
            "   public static final Resource provideResource() {",
            "       Preferences preferences = new Preferences();",
            "       Resource context = new Resource(preferences);",
            "       return context;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, resource, context))
            .processedWith(IProcessor())

            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun preferModuleOverTarget() {


        val preferences = JavaFileObjects.forSourceLines("test.PreferencesModule",
            "package test;",
            importAndroidSharedPreferences,
            importAndroidContext,
            importDependencyAnnotation,
            "public class PreferencesModule {",
            "   @Dependency",
            "   public static SharedPreferences getPreferences(Context context) { return null; }",
            "}")


        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",
            importAndroidSharedPreferences,

            "public class Presenter {",
            "   Presenter(SharedPreferences preferences) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importAndroidSharedPreferences,
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final MainActivity target) {",
            "       target.presenter = providePresenter(target);",
            "   }",
            "",
            "   private static final Presenter providePresenter(@NonNull final MainActivity target) {",
            "       SharedPreferences sharedPreferences = PreferencesModule.getPreferences(target);",
            "       Presenter presenter = new Presenter(sharedPreferences);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, presenter, preferences))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectSuperParent() {


        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class FileManager {",
            "}")

        val superFile = JavaFileObjects.forSourceLines("test.SuperActivity",
            "package test;",
            "",
            importAndroidActivity,
            importInjectAnnotation,
            "",
            "public class SuperActivity extends Activity {",
            "   @Inject",
            "   public Logger logger;",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class ParentActivity extends SuperActivity {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class MainActivity extends ParentActivity {",
            "",
            "   @Inject",
            "   public FileManager fileManager;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final MainActivity target) {",
            "       SuperActivityInjector.inject(target);",
            "       target.fileManager = new FileManager();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, logger, fileManager, superFile, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectSuperParent2() {


        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class FileManager {",
            "}")

        val superFile = JavaFileObjects.forSourceLines("test.SuperActivity",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class SuperActivity extends Activity {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class ParentActivity extends SuperActivity {",
            "   @Inject",
            "   public Logger logger;",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class MainActivity extends ParentActivity {",
            "",
            "   @Inject",
            "   public FileManager fileManager;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final MainActivity target) {",
            "       ParentActivityInjector.inject(target);",
            "       target.fileManager = new FileManager();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, logger, fileManager, superFile, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun providerInConstructor() {


        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class FileManager {",
            "}")


        val parentFile = JavaFileObjects.forSourceLines("test.Dependency",
            "package test;",
            "",
            importInjectAnnotation,
            importProvider,
            importAndroidActivity,
            "",
            "public class Dependency {",
            "   Dependency(Logger logger, Provider<FileManager> fileManager) { }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Dependency dependency;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIocProvider,
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final MainActivity target) {",
            "       target.dependency = provideDependency();",
            "   }",
            "",
            "   public static final Dependency provideDependency() {",
            "       Logger logger = new Logger();",
            "       IocProvider<FileManager> providerFileManager = new IocProvider<FileManager>() {",
            "           protected FileManager initialize() {",
            "               FileManager fileManager = new FileManager();",
            "               return fileManager;",
            "          }",
            "      };",
            "      Dependency dependency = new Dependency(logger, providerFileManager);",
            "      return dependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, logger, fileManager, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun lazyInConstructor() {


        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importAndroidActivity,
            "",
            "public class FileManager {",
            "}")


        val parentFile = JavaFileObjects.forSourceLines("test.Dependency",
            "package test;",
            "",
            importInjectAnnotation,
            importLazy,
            importAndroidActivity,
            "",
            "public class Dependency {",
            "   Dependency(Logger logger, Lazy<FileManager> fileManager) { }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Dependency dependency;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            importIocLazy,
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final MainActivity target) {",
            "       target.dependency = provideDependency();",
            "   }",
            "",
            "   public static final Dependency provideDependency() {",
            "       Logger logger = new Logger();",
            "       IocLazy<FileManager> lazyFileManager = new IocLazy<FileManager>() {",
            "           protected FileManager initialize() {",
            "               FileManager fileManager = new FileManager();",
            "               return fileManager;",
            "          }",
            "      };",
            "      Dependency dependency = new Dependency(logger, lazyFileManager);",
            "      return dependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, logger, fileManager, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun test() {
        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            importInjectAnnotation,
            importAndroidActivity,
            "public class ParentActivity extends Activity {",
            "",
            "   @Inject",
            "   public ParentDependency parent;",
            "}")

        val brightnessChangeListener = JavaFileObjects.forSourceLines("test.BrightnessChangeListener",
            "package test;",
            "public interface BrightnessChangeListener {",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity extends ParentActivity implements BrightnessChangeListener {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       ParentActivityInjector.inject(target);",
            "       target.dependency = new DependencyModel();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, brightnessChangeListener, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    fun successMultipleGenericType() {

        val subjectModule = JavaFileObjects.forSourceLines("test.SubjectModule",
            "package test;",
            importRxJavaSubject,
            importRxJavaBehaviorSubject,
            importDependencyAnnotation,
            "public class SubjectModule {",
            "   @Dependency",
            "   public static Subject<Boolean> get() { return BehaviorSubject.<Boolean>create(); }",
            "   @Dependency",
            "   public static Subject<Integer> getIntegerSubject() { return BehaviorSubject.<Integer>create(); }",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importRxJavaSubject,
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Subject<Boolean> booleanSubject;",
            "",
            "   @Inject",
            "   public Subject<Integer> integerSubject;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final MyActivity target) {",
            "       target.booleanSubject = SubjectModule.get();",
            "       target.integerSubject = SubjectModule.getIntegerSubject();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, subjectModule))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    fun findParentOnInterface() {

        val incognito = JavaFileObjects.forSourceLines("test.Incognito",
            "package test;",
            "",
            "public interface Incognito {",
            "}")

        val privacy = JavaFileObjects.forSourceLines("test.Privacy",
            "package test;",
            "",
            "public interface Privacy extends Incognito {",
            "}")

        val settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Settings implements Privacy {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importRxJavaSubject,
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Incognito incognito;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final MyActivity target) {",
            "       target.incognito = new Settings();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, incognito, privacy, settings))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    fun findParentOnAbstractClass() {

        val incognito = JavaFileObjects.forSourceLines("test.Incognito",
            "package test;",
            "",
            "public abstract class Incognito {",
            "}")

        val privacy = JavaFileObjects.forSourceLines("test.Privacy",
            "package test;",
            "",
            "public class Privacy extends Incognito {",
            "}")

        val settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Settings extends Privacy {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importRxJavaSubject,
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Incognito incognito;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final MyActivity target) {",
            "       target.incognito = new Settings();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, incognito, privacy, settings))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun twoInterfacesFromOnImplementationInConstructorForSingleton2() {

        val activityFile = JavaFileObjects.forSourceLines("test.DownloadsFragment",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DownloadsFragment {",
            "",
            "   @Inject",
            "   public DownloadsNavigationLogger downloadsNavigationLogger;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationPathIndicatorClickedEventLogger",
            "package test;",
            "",
            "public interface DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}")


        val amplitudeLoggerFile = JavaFileObjects.forSourceLines("test.AmplitudeLogger",
            "package test;",
            "",
            "public abstract class AmplitudeLogger implements DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}")


        val moduleFile = JavaFileObjects.forSourceLines("test.AmplitudeService",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            importInjectAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class AmplitudeService extends AmplitudeLogger {",
            "   @Inject",
            "   public AmplitudeService() {}",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLogger",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            importSingletonAnnotation,
            "",
            "@Dependency",
            "@Singleton",
            "public class DownloadsNavigationLogger {",
            "   @Inject",
            "   DownloadsNavigationLogger(DownloadsNavigationPathIndicatorClickedEventLogger downloadsNavigationPathIndicatorClickedEventLogger) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLoggerSingleton",
            "package test;",
            "",
            importKeepAnnotation,
            importIoc,
            importProvider,
            "",
            "@Keep",
            "public final class DownloadsNavigationLoggerSingleton implements Provider<DownloadsNavigationLogger> {",
            "",
            "   public final DownloadsNavigationLogger get() {",
            "       return new DownloadsNavigationLogger(Ioc.getSingleton(AmplitudeService.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, amplitudeLoggerFile, superParentFile, baseFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun findDependencyInParent() {
        val activityParentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            importInjectAnnotation,
            "public class ParentActivity {",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "}")
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            importInjectAnnotation,
            "public class Activity extends ParentActivity{",
            "   @Inject",
            "   public DependencyModel childDependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "public class ParentDependency {}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            importKeepAnnotation,
            importNonNullAnnotation,
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       ParentActivityInjector.inject(target);",
            "       target.childDependency = new DependencyModel();",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, activityParentFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectFromDifferentModule() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}")

        val baseActivityFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class BaseActivity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "public class ClickedEventLogger {",
            "   public ClickedEventLogger(SimpleDependency simple) {}",
            "}")

        val simpleDependency = JavaFileObjects.forSourceLines("test.SimpleDependency",
            "package test;",
            "",
            "public class SimpleDependency {",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.clickedEventLogger = provideClickedEventLogger();",
            "   }",
            "",
            "   public static final ClickedEventLogger provideClickedEventLogger() {",
            "       SimpleDependency simpleDependency = new SimpleDependency();",
            "       ClickedEventLogger clickedEventLogger = new ClickedEventLogger(simpleDependency);",
            "       return clickedEventLogger;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, baseActivityFile, simpleDependency, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectFromDifferentBaseActivityModule() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}")

        val baseActivityFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class BaseActivity {",
            "",
            "   @Inject",
            "   private ClickedEventLogger clickedEventLogger;",
            "   public ClickedEventLogger get() { return null; };",
            "   public void set(ClickedEventLogger logger) {};",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "public class ClickedEventLogger {",
            "   public ClickedEventLogger(SimpleDependency simple) {}",
            "}")

        val simpleDependency = JavaFileObjects.forSourceLines("test.SimpleDependency",
            "package test;",
            "",
            "public class SimpleDependency {",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.BaseActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class BaseActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final BaseActivity target) {",
            "       target.set(ActivityInjector.provideClickedEventLogger());",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, baseActivityFile, simpleDependency, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectFromDifferentModuleLazy() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}")

        val baseActivityFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $lazyType;",
            "import $providerType;",
            "import $weakReferenceType;",
            "",
            "public class BaseActivity {",
            "",
            "   @Inject",
            "   public Lazy<ClickedEventLogger> lazyEventLogger;",
            "   @Inject",
            "   public Provider<ClickedEventLogger> providerEventLogger;",
            "   @Inject",
            "   public WeakReference<ClickedEventLogger> weakEventLogger;",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "public class ClickedEventLogger {",
            "   public ClickedEventLogger(SimpleDependency simple) {}",
            "}")

        val simpleDependency = JavaFileObjects.forSourceLines("test.SimpleDependency",
            "package test;",
            "",
            "public class SimpleDependency {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.BaseActivityInjector",
            """
            package test;
            $importKeepAnnotation
            $importNonNullAnnotation
            $importIocLazy
            import com.ioc.IocProvider;
            import java.lang.ref.WeakReference;
            
            @Keep
            public final class BaseActivityInjector {
              @Keep
              public static final void inject(@NonNull final BaseActivity target) {
                target.lazyEventLogger = new IocLazy<ClickedEventLogger>() {
                  protected ClickedEventLogger initialize() {
                    return ActivityInjector.provideClickedEventLogger();
                  }
                };
                target.providerEventLogger = new IocProvider<ClickedEventLogger>() {
                  protected ClickedEventLogger initialize() {
                    return ActivityInjector.provideClickedEventLogger();
                  }
                };
                target.weakEventLogger = new WeakReference<>(ActivityInjector.provideClickedEventLogger());
              }
            }
        """.trimIndent())

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, baseActivityFile, simpleDependency, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun findNestedClass() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            """
                package test;
                $importInjectAnnotation
                public class Activity {
                    
                    public class Nested {
                        @Inject
                        public SimpleDependency simpleDependency;
                    }
                
                }
            """.trimIndent())


        val simpleDependency = JavaFileObjects.forSourceLines("test.SimpleDependency",
            "package test;",
            "",
            "public class SimpleDependency {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.NestedInjector",
            """
            package test;

            $importKeepAnnotation
            $importNonNullAnnotation
            
            @Keep
            public final class NestedInjector {
              @Keep
              public static final void inject(@NonNull final Activity.Nested target) {
                target.simpleDependency = new SimpleDependency();
              }
            }
        """.trimIndent())

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, simpleDependency))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}
