package com.ioc

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import com.ioc.Helpers.importType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Provider
import javax.inject.Singleton
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
            "import $inject;",
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
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun skipExcludedPackage() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "import $assetManager;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public AssetManager assetManagerDependency;",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("Can't find implementations of `android.content.res.AssetManager android.content.res.AssetManager` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method")
            .`in`(activityFile)
            .onLine(6)
    }

    @Test
    @Throws(Exception::class)
    fun failOnWrongSetter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
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
            "import $inject;",
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
            "import $inject;",
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
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "       injectLoggerInLogger(target);",
            "       injectPreferencesInSetPreferences(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "",
            "   private final void injectLoggerInLogger(@NonNull final Activity target) {",
            "       Logger logger = new Logger();",
            "       target.setLogger(logger);",
            "   }",
            "",
            "   private final void injectPreferencesInSetPreferences(@NonNull final Activity target) {",
            "       Preferences preferences = new Preferences();",
            "       target.setPreferences(preferences);",
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
            "import $inject;",
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
            "import $inject;",
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
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile))
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
            "import $inject;",
            importType(Provider::class.java),
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
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocProvider::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       IocProvider<DependencyModel> providerDependencyModel = new IocProvider<DependencyModel>() {",
            "         protected DependencyModel initialize() {",
            "           DependencyModel dependencyModel = new DependencyModel();",
            "           return dependencyModel;",
            "         }",
            "       };",
            "       target.dependency = providerDependencyModel;",
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
    fun providerFieldWithInterface() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            importType(Provider::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Provider<DependencyInterface> dependency;",
            "}")

        val dependencyInterface = JavaFileObjects.forSourceLines("test.DependencyInterface",
            "package test;",
            "",
            "interface DependencyInterface {}")

        val dependencyType = JavaFileObjects.forSourceLines("test.DependencyType",
            "package test;",
            importType(Dependency::class.java),
            "@Dependency",
            "class DependencyType implements DependencyInterface {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocProvider::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyInterfaceInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyInterfaceInDependency(@NonNull final Activity target) {",
            "       IocProvider<DependencyInterface> providerDependencyInterface = new IocProvider<DependencyInterface>() {",
            "         protected DependencyInterface initialize() {",
            "           DependencyInterface dependencyInterface = new DependencyType();",
            "           return dependencyInterface;",
            "         }",
            "       };",
            "       target.dependency = providerDependencyInterface;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyInterface, dependencyType))
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
            "import $inject;",
            importType(Lazy::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Lazy<DependencyInterface> dependency;",
            "}")

        val dependencyInterface = JavaFileObjects.forSourceLines("test.DependencyInterface",
            "package test;",
            "",
            "interface DependencyInterface {}")

        val dependencyType = JavaFileObjects.forSourceLines("test.DependencyType",
            "package test;",
            importType(Dependency::class.java),
            "@Dependency",
            "class DependencyType implements DependencyInterface {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyInterfaceInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyInterfaceInDependency(@NonNull final Activity target) {",
            "       IocLazy<DependencyInterface> lazyDependencyInterface = new IocLazy<DependencyInterface>() {",
            "         protected DependencyInterface initialize() {",
            "           DependencyInterface dependencyInterface = new DependencyType();",
            "           return dependencyInterface;",
            "         }",
            "       };",
            "       target.dependency = lazyDependencyInterface;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyInterface, dependencyType))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       target.dependency = dependencyModel;",
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
    fun injectWithTargetAndDependencyInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(Activity activity, DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       ParentDependency parentDependency = new ParentDependency(target, dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile, parentDependencyFile))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile, parentDependencyFile))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile, parentDependencyFile))
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
            "import $inject;",
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel() {};",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun preferTargetConstructorWithArguments() {
        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "import $inject;",
            "public class ParentActivity {",
            "",
            "   @Inject",
            "   DependencyModel dependencyModel;",
            "   public DependencyModel getDependency() { return null; }",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel() {};",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       new ParentActivityInjector().inject(target);",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile, parentActivityFile, parentDependencyFile))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "class DependencyModel {",
            "   public DependencyModel() {};",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency(DependencyModel childDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       injectParentDependencyInParentDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel2 = new DependencyModel();",
            "       target.dependency = dependencyModel2;",
            "   }",
            "",
            "   private final void injectParentDependencyInParentDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       target.parentDependency = parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, dependencyFile, parentDependencyFile))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            "",
            "@Dependency",
            "public class ReleaseDependency implements DependencyModel {",
            "",
            "   public ReleaseDependency() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "interface DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       DependencyModel dependencyModel = new ReleaseDependency();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun correctInjection4() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            "",
            "@Dependency",
            "public class ReleaseDependency extends DependencyModel {",
            "",
            "   public ReleaseDependency() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "",
            "class DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, dependencyFile))
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
            "import $inject;",
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
            "import $inject;",
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
            importType(com.ioc.Dependency::class.java),
            "",
            "@Dependency",
            "class Context {",
            "}")

        val resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            "import $inject;",
            "",
            "@Dependency",
            "class Resource {",
            "",
            "   @Inject",
            "   Resource(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       Context context = new Context();",
            "       Context context2 = new Context();",
            "       Resource resource = new Resource(context2);",
            "       DependencyModel dependencyModel = new DependencyModel(context, resource);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, contextFile, resourceFile))
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
            "import $inject;",
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
            "import $inject;",
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
            "class Context {",
            "}")

        val contextModuleFile = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            "",
            "class ContextModule {",
            "  @Dependency",
            "  public static Context context() { return new Context(); }",
            "}")

        val resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            importType(Singleton::class.java),
            "import $inject;",
            "",
            "@Dependency",
            "@Singleton",
            "class Resource {",
            "",
            "   @Inject",
            "   Resource(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       Context context = ContextModule.context();",
            "       DependencyModel dependencyModel = new DependencyModel(context, Ioc.singleton(Resource.class));",
            "       target.dependency = dependencyModel;",
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            "import $inject;",
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
            "interface DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       DependencyModel dependencyModel = new AppModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, superParentFile, parentFile, moduleFile))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            "import $inject;",
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
            "interface DependencyModel {",
            "}")

        val dependencyParentFile = JavaFileObjects.forSourceLines("test.DependencyParent",
            "package test;",
            "",
            "interface DependencyParent extends DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       DependencyModel dependencyModel = new AppModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, superParentFile, parentFile, moduleFile, dependencyParentFile))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            "import $inject;",
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
            "interface DependencyModel {",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            importType(Singleton::class.java),
            "import $inject;",
            "",
            "@Dependency",
            "@Singleton",
            "class Context implements Resource {",
            "}")

        val resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "",
            "interface Resource {",
            "",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new AppModel(Ioc.singleton(Context.class));",
            "       target.dependency = dependencyModel;",
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public BaseModel dependency;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.SpeedDialTileClosedEventLogger",
            "package test;",
            "",
            "interface SpeedDialTileClosedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.SpeedDialTileClickedEventLogger",
            "package test;",
            "",
            "interface SpeedDialTileClickedEventLogger {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            "import $dependency;",
            "import $singleton;",
            "",
            "@Dependency",
            "@Singleton",
            "class Amplitude implements SpeedDialTileClickedEventLogger, SpeedDialTileClosedEventLogger {",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $inject;",
            "import $dependency;",
            "",
            "@Dependency",
            "class BaseModel {",
            "   @Inject",
            "   BaseModel(SpeedDialTileClickedEventLogger speedDialTileClickedEventLogger, SpeedDialTileClosedEventLogger speedDialTileClosedEventLogger) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "import $ioc;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectBaseModelInDependency(target);",
            "   }",
            "",
            "   private final void injectBaseModelInDependency(@NonNull final Activity target) {",
            "       BaseModel baseModel = new BaseModel(Ioc.singleton(Amplitude.class), Ioc.singleton(Amplitude.class));",
            "       target.dependency = baseModel;",
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
    fun injectInParentClass() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity extends BaseActivity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}")

        val baseActivityFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public abstract class BaseActivity {",
            "",
            "   @Inject",
            "   public ClosedEventLogger closedEventLogger;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.ClosedEventLogger",
            "package test;",
            "",
            "interface ClosedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "interface ClickedEventLogger {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
            "",
            "@Dependency",
            "@Singleton",
            "class Amplitude implements ClickedEventLogger, ClosedEventLogger {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       new BaseActivityInjector().inject(target);",
            "       injectClickedEventLoggerInClickedEventLogger(target);",
            "   }",
            "",
            "   private final void injectClickedEventLoggerInClickedEventLogger(@NonNull final Activity target) {",
            "       Amplitude amplitude = Ioc.singleton(Amplitude.class);",
            "       target.clickedEventLogger = amplitude;",
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Logger logger;",
            "}")

        val loggerFile = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            "import $inject;",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
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
            "interface ClosedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "interface ClickedEventLogger {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
            "",
            "@Dependency",
            "@Singleton",
            "class Amplitude implements ClickedEventLogger, ClosedEventLogger {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.LoggerSingleton",
            "package test;",
            "",
            "import $keep",
            "import $ioc",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class LoggerSingleton extends IocLazy<Logger> {",
            "   private static LoggerSingleton instance;",
            "",
            "   public static final LoggerSingleton getInstance() {",
            "       if (instance == null) instance = new LoggerSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final Logger initialize() {",
            "       return new Logger(Ioc.singleton(Amplitude.class), Ioc.singleton(Amplitude.class));",
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
            "import $inject;",
            "",
            "public class DownloadsFragment {",
            "",
            "   @Inject",
            "   public DownloadsNavigationLogger downloadsNavigationLogger;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationPathIndicatorClickedEventLogger",
            "package test;",
            "",
            "interface DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationSystemBackClickedEventLogger",
            "package test;",
            "",
            "interface DownloadsNavigationSystemBackClickedEventLogger {",
            "}")


        val amplitudeLoggerFile = JavaFileObjects.forSourceLines("test.AmplitudeLogger",
            "package test;",
            "",
            "public abstract class AmplitudeLogger implements DownloadsNavigationPathIndicatorClickedEventLogger, DownloadsNavigationSystemBackClickedEventLogger {",
            "}")

        val preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
            "",
            "@Dependency",
            "@Singleton",
            "public class Preferences {",
            "}")


        val moduleFile = JavaFileObjects.forSourceLines("test.AmplitudeService",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
            "import $inject;",
            "",
            "@Dependency",
            "@Singleton",
            "class AmplitudeService extends AmplitudeLogger {",
            "   @Inject",
            "   public AmplitudeService(Preferences prefs) {}",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLogger",
            "package test;",
            "",
            "import $inject;",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
            "",
            "@Dependency",
            "@Singleton",
            "class DownloadsNavigationLogger {",
            "   @Inject",
            "   DownloadsNavigationLogger(DownloadsNavigationPathIndicatorClickedEventLogger downloadsNavigationPathIndicatorClickedEventLogger, DownloadsNavigationSystemBackClickedEventLogger downloadsNavigationSystemBackClickedEventLogger) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLoggerSingleton",
            "package test;",
            "",
            "import $keep",
            "import $ioc",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class DownloadsNavigationLoggerSingleton extends IocLazy<DownloadsNavigationLogger> {",
            "   private static DownloadsNavigationLoggerSingleton instance;",
            "",
            "   public static final DownloadsNavigationLoggerSingleton getInstance() {",
            "       if (instance == null) instance = new DownloadsNavigationLoggerSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final DownloadsNavigationLogger initialize() {",
            "       return new DownloadsNavigationLogger(Ioc.singleton(AmplitudeService.class), Ioc.singleton(AmplitudeService.class));",
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
            importType(Dependency::class.java),
            "",
            "public class ContextModule {",
            "   @Dependency",
            "   public static Context getContext() { return null; };",
            "}")

        val preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            "import $inject;",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
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
            "import $inject;",
            "import $dependency;",
            "import $singleton;",
            "",
            "@Dependency",
            "public class BuildCheck {",
            "   @Inject",
            "   public BuildCheck(Preferences prefs) {};",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
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
            "import $keep;",
            "import $nonNull;",
            "import $ioc;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectBuildCheckInAppendBuildCheck(target);",
            "   }",
            "",
            "   private final void injectBuildCheckInAppendBuildCheck(@NonNull final Activity target) {",
            "       BuildCheck buildCheck = new BuildCheck(Ioc.singleton(Preferences.class));",
            "       target.appendBuildCheck(buildCheck);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, contextModuleFile, contextFile, preferencesFile, buildCheckFile))
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
            importType(Dependency::class.java),
            importType(Context::class.java),
            "",
            "@Dependency",
            "public class AutoCompleteListenerImpl implements Listener {",
            "",
            "   AutoCompleteListenerImpl(Context context) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
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
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MyActivity target) {",
            "       injectControllerInAppendBuildCheck(target);",
            "   }",
            "",
            "   private final void injectControllerInAppendBuildCheck(@NonNull final MyActivity target) {",
            "       Listener listener = new AutoCompleteListenerImpl(target);",
            "       Controller controller = new Controller(listener);",
            "       target.appendBuildCheck(controller);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, listenerFile, controllerFile, autoCompleteListenerImplFile))
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
            importType(Dependency::class.java),
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
            importType(Dependency::class.java),
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
            "import $inject;",
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
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectControllerInAppendBuildCheck(target);",
            "   }",
            "",
            "   private final void injectControllerInAppendBuildCheck(@NonNull final Activity target) {",
            "       HttpLoggingInterceptor httpLoggingInterceptor = RestModule.provideHttpLoggingInterceptor();",
            "       Controller controller = new Controller(httpLoggingInterceptor);",
            "       target.appendBuildCheck(controller);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, interceptor, restModule, controllerFile))
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
            "import $inject;",
            importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {}",
            "}")

        val amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            "import $inject;",
            importType(Singleton::class.java),
            importType(Dependency::class.java),
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
            importType(Dependency::class.java),
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
            "import $inject;",
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
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectSpeedDialDisplayedEventLoggerInLogger(target);",
            "   }",
            "",
            "   private final void injectSpeedDialDisplayedEventLoggerInLogger(@NonNull final Activity target) {",
            "       AmplitudeDefaultLogger amplitudeDefaultLogger = Ioc.singleton(AmplitudeDefaultLogger.class);",
            "       target.setLogger(amplitudeDefaultLogger);",
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
            "import $inject;",
            importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {}",
            "}")

        val amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            "import $inject;",
            importType(Singleton::class.java),
            importType(Dependency::class.java),
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
            importType(Dependency::class.java),
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
            "import $inject;",
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
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class PreferencesSingleton extends IocLazy<Preferences> {",
            "   private static PreferencesSingleton instance;",
            "",
            "   public static final PreferencesSingleton getInstance() {",
            "       if (instance == null) instance = new PreferencesSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final Preferences initialize() {",
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
            "import $inject;",
            "",
            "public class Presenter implements Runnable {",
            "   @Inject",
            "   public Presenter(Context context, Runnable runnable) {}",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
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
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "     injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Presenter presenter = new Presenter(target, target);",
            "       target.set(presenter);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, runnable, parentActivity, context, presenter))
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
            "import $inject;",
            "",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context, DefaultLogger defaultLogger) {}",
            "}")

        val amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            "import $inject;",
            importType(Singleton::class.java),
            importType(Dependency::class.java),
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
            importType(Dependency::class.java),
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Preferences preferences;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectPreferencesInPreferences(target);",
            "   }",
            "",
            "   private final void injectPreferencesInPreferences(@NonNull final Activity target) {",
            "       Context context = ContextModule.context(Ioc.singleton(AmplitudeDefaultLogger.class));",
            "       Preferences preferences = new Preferences(context, Ioc.singleton(AmplitudeDefaultLogger.class));",
            "       target.preferences = preferences;",
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
            importType(Dependency::class.java),
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
            importType(WeakReference::class.java),
            "public class Presenter {",
            "   Presenter(WeakReference<Context> context) {}",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Context context = ContextModule.context();",
            "       WeakReference<Context> weakContext = new WeakReference<Context>(context);",
            "       Presenter presenter = new Presenter(weakContext);",
            "       target.presenter = presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, presenter, contextModule, context))
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
            importType(Dependency::class.java),
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

            importType(Provider::class.java),

            "public class Presenter {",
            "   Presenter(Provider<Context> context) {}",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocProvider::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       IocProvider<Context> providerContext = new IocProvider<Context>() {",
            "           protected Context initialize() {",
            "             Context context = ContextModule.context();",
            "             return context;",
            "           }",
            "       }",
            "       Presenter presenter = new Presenter(providerContext);",
            "       target.presenter = presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, presenter, contextModule, context))
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
            "import $inject;",
            importType(CompositeDisposable::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CompositeDisposable subscription;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(CompositeDisposable::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectCompositeDisposableInSubscription(target);",
            "   }",
            "",
            "   private final void injectCompositeDisposableInSubscription(@NonNull final Activity target) {",
            "       CompositeDisposable compositeDisposable = new CompositeDisposable();",
            "       target.subscription = compositeDisposable;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile))
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
            importType(Dependency::class.java),
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

            importType(Lazy::class.java),

            "public class Presenter {",
            "   Presenter(Lazy<Context> context) {}",
            "",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       IocLazy<Context> lazyContext = new IocLazy<Context>() {",
            "           protected Context initialize() {",
            "             Context context = ContextModule.context();",
            "             return context;",
            "           }",
            "       }",
            "       Presenter presenter = new Presenter(lazyContext);",
            "       target.presenter = presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, presenter, contextModule, context))
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
            importType(Dependency::class.java),
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
            "import $inject;",
            importType(Lazy::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Lazy<Presenter> presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy::class.java),
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       IocLazy<Presenter> lazyPresenter = new IocLazy<Presenter>() {",
            "           protected Presenter initialize() {",
            "             Context context = ContextModule.context();",
            "             Presenter presenter = new Presenter(context);",
            "             return presenter;",
            "           }",
            "       }",
            "       target.presenter = lazyPresenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, presenter, contextModule, context))
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
            importType(Dependency::class.java),
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Resource resource = ContextModule.resource();",
            "       Context context = ContextModule.context(resource, target);",
            "       Presenter presenter = ContextModule.presenter(context);",
            "       target.presenter = presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, resources, presenter, contextModule, context))
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
            importType(Dependency::class.java),
            importType(Singleton::class.java),
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Presenter presenter = ContextModule.presenter(Ioc.singleton(Context.class));",
            "       target.presenter = presenter;",
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
    fun doNotpassTargetInSingleton() {


        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, presenter, contextModule, context))
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
            importType(Singleton::class.java),
            "@Singleton",
            "public class Context {",
            "",
            "}")

        val resource = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            importType(Singleton::class.java),

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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Resource resource = new Resource(Ioc.singleton(Context.class));",
            "       Presenter presenter = new Presenter(Ioc.singleton(Context.class), resource);",
            "       target.presenter = presenter;",
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
            importType(Singleton::class.java),
            importType(Dependency::class.java),

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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Context context;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "       injectContextInContext(target);",
            "   }",
            "",
            "   private final void injectContextInContext(@NonNull final Activity target) {",
            "       Preferences preferences = new Preferences();",
            "       Context context = new Resource(preferences);",
            "       target.context = context;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, presenter, resource, context))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun preferModuleOverTarget() {


        val preferences = JavaFileObjects.forSourceLines("test.PreferencesModule",
            "package test;",
            importType(SharedPreferences::class.java),
            importType(Context::class.java),
            importType(Dependency::class.java),
            "public class PreferencesModule {",
            "   @Dependency",
            "   public static SharedPreferences getPreferences(Context context) { return null; }",
            "}")


        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",
            importType(SharedPreferences::class.java),

            "public class Presenter {",
            "   Presenter(SharedPreferences preferences) {}",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importType(SharedPreferences::class.java),
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MainActivity target) {",
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final MainActivity target) {",
            "       SharedPreferences sharedPreferences = PreferencesModule.getPreferences(target);",
            "       Presenter presenter = new Presenter(sharedPreferences);",
            "       target.presenter = presenter;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, presenter, preferences))
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
            importType(Activity::class.java),
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity::class.java),
            "",
            "public class FileManager {",
            "}")

        val superFile = JavaFileObjects.forSourceLines("test.SuperActivity",
            "package test;",
            "",
            importType(Activity::class.java),
            "import $inject;",
            "",
            "public class SuperActivity extends Activity {",
            "   @Inject",
            "   public Logger logger;",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
            "",
            "public class ParentActivity extends SuperActivity {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
            "",
            "public class MainActivity extends ParentActivity {",
            "",
            "   @Inject",
            "   public FileManager fileManager;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MainActivity target) {",
            "       new SuperActivityInjector().inject(target);",
            "       injectFileManagerInFileManager(target);",
            "   }",
            "",
            "   private final void injectFileManagerInFileManager(@NonNull final MainActivity target) {",
            "       FileManager fileManager = new FileManager();",
            "       target.fileManager = fileManager;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, logger, fileManager, superFile, parentFile))
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
            importType(Activity::class.java),
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity::class.java),
            "",
            "public class FileManager {",
            "}")

        val superFile = JavaFileObjects.forSourceLines("test.SuperActivity",
            "package test;",
            "",
            importType(Activity::class.java),
            "",
            "public class SuperActivity extends Activity {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
            "",
            "public class ParentActivity extends SuperActivity {",
            "   @Inject",
            "   public Logger logger;",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
            "",
            "public class MainActivity extends ParentActivity {",
            "",
            "   @Inject",
            "   public FileManager fileManager;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MainActivity target) {",
            "       new ParentActivityInjector().inject(target);",
            "       injectFileManagerInFileManager(target);",
            "   }",
            "",
            "   private final void injectFileManagerInFileManager(@NonNull final MainActivity target) {",
            "       FileManager fileManager = new FileManager();",
            "       target.fileManager = fileManager;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, logger, fileManager, superFile, parentFile))
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
            importType(Activity::class.java),
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity::class.java),
            "",
            "public class FileManager {",
            "}")


        val parentFile = JavaFileObjects.forSourceLines("test.Dependency",
            "package test;",
            "",
            "import $inject;",
            importType(Provider::class.java),
            importType(Activity::class.java),
            "",
            "public class Dependency {",
            "   Dependency(Logger logger, Provider<FileManager> fileManager) { }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Dependency dependency;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocProvider::class.java),
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MainActivity target) {",
            "       injectDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyInDependency(@NonNull final MainActivity target) {",
            "       Logger logger = new Logger();",
            "       IocProvider<FileManager> providerFileManager = new IocProvider<FileManager>() {",
            "           protected FileManager initialize() {",
            "               FileManager fileManager = new FileManager();",
            "               return fileManager;",
            "          }",
            "      };",
            "      Dependency dependency = new Dependency(logger, providerFileManager);",
            "      target.dependency = dependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, logger, fileManager, parentFile))
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
            importType(Activity::class.java),
            "",
            "public class Logger {",
            "}")

        val fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity::class.java),
            "",
            "public class FileManager {",
            "}")


        val parentFile = JavaFileObjects.forSourceLines("test.Dependency",
            "package test;",
            "",
            "import $inject;",
            importType(Lazy::class.java),
            importType(Activity::class.java),
            "",
            "public class Dependency {",
            "   Dependency(Logger logger, Lazy<FileManager> fileManager) { }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Activity::class.java),
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Dependency dependency;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy::class.java),
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MainActivity target) {",
            "       injectDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyInDependency(@NonNull final MainActivity target) {",
            "       Logger logger = new Logger();",
            "       IocLazy<FileManager> lazyFileManager = new IocLazy<FileManager>() {",
            "           protected FileManager initialize() {",
            "               FileManager fileManager = new FileManager();",
            "               return fileManager;",
            "          }",
            "      };",
            "      Dependency dependency = new Dependency(logger, lazyFileManager);",
            "      target.dependency = dependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, logger, fileManager, parentFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun test() {
        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "import $inject;",
            importType(Activity::class.java),
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
            "import $inject;",
            "",
            "public class Activity extends ParentActivity implements BrightnessChangeListener {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "class DependencyModel {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       new ParentActivityInjector().inject(target);",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       target.dependency = dependencyModel;",
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
            importType(Subject::class.java),
            importType(BehaviorSubject::class.java),
            importType(Dependency::class.java),
            "public class SubjectModule {",
            "   @Dependency",
            "   public static Subject<Boolean> get() { return BehaviorSubject.<Boolean>create(); }",
            "   @Dependency",
            "   public static Subject<Integer> getIntegerSubject() { return BehaviorSubject.<Integer>create(); }",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Subject::class.java),
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
            "import $keep;",
            "import $nonNull",
            importType(Subject::class.java),
            "import java.lang.Boolean;",
            "import java.lang.Integer;",
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MyActivity target) {",
            "       injectSubjectInBooleanSubject(target);",
            "       injectSubjectInIntegerSubject(target);",
            "   }",
            "",
            "   private final void injectSubjectInBooleanSubject(@NonNull final MyActivity target) {",
            "       Subject<Boolean> subject = SubjectModule.get();",
            "       target.booleanSubject = subject;",
            "   }",
            "",
            "   private final void injectSubjectInIntegerSubject(@NonNull final MyActivity target) {",
            "       Subject<Integer> subject2 = SubjectModule.getIntegerSubject();",
            "       target.integerSubject = subject2;",
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
            "import $inject;",
            importType(Dependency::class.java),
            "",
            "@Dependency",
            "public class Settings implements Privacy {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Subject::class.java),
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Incognito incognito;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final MyActivity target) {",
            "       injectIncognitoInIncognito(target);",
            "   }",
            "",
            "   private final void injectIncognitoInIncognito(@NonNull final MyActivity target) {",
            "       Incognito incognito = new Settings();",
            "       target.incognito = incognito;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, incognito, privacy, settings))
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
            "import $inject;",
            importType(Dependency::class.java),
            "",
            "@Dependency",
            "public class Settings extends Privacy {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            "import $inject;",
            importType(Subject::class.java),
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Incognito incognito;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class MyActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final MyActivity target) {",
            "       injectIncognitoInIncognito(target);",
            "   }",
            "",
            "   private final void injectIncognitoInIncognito(@NonNull final MyActivity target) {",
            "       Incognito incognito = new Settings();",
            "       target.incognito = incognito;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, incognito, privacy, settings))
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
            "import $inject;",
            "",
            "public class DownloadsFragment {",
            "",
            "   @Inject",
            "   public DownloadsNavigationLogger downloadsNavigationLogger;",
            "}")


        val superParentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationPathIndicatorClickedEventLogger",
            "package test;",
            "",
            "interface DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}")


        val amplitudeLoggerFile = JavaFileObjects.forSourceLines("test.AmplitudeLogger",
            "package test;",
            "",
            "public abstract class AmplitudeLogger implements DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}")


        val moduleFile = JavaFileObjects.forSourceLines("test.AmplitudeService",
            "package test;",
            "",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
            "import $inject;",
            "",
            "@Dependency",
            "@Singleton",
            "class AmplitudeService extends AmplitudeLogger {",
            "   @Inject",
            "   public AmplitudeService() {}",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLogger",
            "package test;",
            "",
            "import $inject;",
            importType(Dependency::class.java),
            importType(Singleton::class.java),
            "",
            "@Dependency",
            "@Singleton",
            "class DownloadsNavigationLogger {",
            "   @Inject",
            "   DownloadsNavigationLogger(DownloadsNavigationPathIndicatorClickedEventLogger downloadsNavigationPathIndicatorClickedEventLogger) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLoggerSingleton",
            "package test;",
            "",
            "import $keep",
            "import $ioc",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class DownloadsNavigationLoggerSingleton extends IocLazy<DownloadsNavigationLogger> {",
            "   private static DownloadsNavigationLoggerSingleton instance;",
            "",
            "   public static final DownloadsNavigationLoggerSingleton getInstance() {",
            "       if (instance == null) instance = new DownloadsNavigationLoggerSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final DownloadsNavigationLogger initialize() {",
            "       return new DownloadsNavigationLogger(Ioc.singleton(AmplitudeService.class));",
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
            "import $inject;",
            "public class ParentActivity {",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "}")
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "import $inject;",
            "public class Activity extends ParentActivity{",
            "   @Inject",
            "   public DependencyModel childDependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "import $inject;",
            "public class ParentDependency {}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "import $inject;",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       new ParentActivityInjector().inject(target);",
            "       injectDependencyModelInChildDependency(target);",
            "   }",
            "   private final void injectDependencyModelInChildDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       target.childDependency = dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, activityParentFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}
