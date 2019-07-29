package com.ioc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.tools.JavaFileObject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static com.ioc.Helpers.importType;

/**
 * Created by sergeygolishnikov on 08/08/2017.
 */
@RunWith(JUnit4.class)
public class FieldInjectionTest {

    @Test
    public void emptyConstructor() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
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
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void skipExcludedPackage() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(AssetManager.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public AssetManager assetManagerDependency;",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile))
            .processedWith(new IProcessor())
            .failsToCompile()
            .withErrorContaining("Can't find implementations of `android.content.res.AssetManager android.content.res.AssetManager` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method")
            .in(activityFile)
            .onLine(6);
    }

    @Test
    public void failOnWrongSetter() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private void setPreferences(Preferences preferences, String param) {};",
            "}");

        JavaFileObject preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            "public class Preferences {}");


        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, preferencesFile))
            .processedWith(new IProcessor())
            .failsToCompile().withErrorContaining("@Inject annotation is placed on method `setPreferences(test.Preferences,java.lang.String)` in `test.Activity` with private access")
            .in(activityFile)
            .onLine(8);
    }

    @Test
    public void privateSetter() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "   @Inject",
            "   private void setPreferences(Preferences preferences) {};",
            "}");

        JavaFileObject preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            "public class Preferences {}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, preferencesFile))
            .processedWith(new IProcessor())
            .failsToCompile().withErrorContaining("@Inject annotation is placed on method `setPreferences(test.Preferences)` in `test.Activity` with private access")
            .in(activityFile)
            .onLine(7);
    }

    @Test
    public void publicAndPrivateFields() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
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
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}");

        JavaFileObject loggerFile = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            "public class Logger {}");

        JavaFileObject preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            "public class Preferences {}");

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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, loggerFile, preferencesFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void failOnFindPrivateFieldWithoutSetter() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(new IProcessor())
            .failsToCompile()
            .withErrorContaining("@Inject annotation placed on field `dependency` in `Activity` with private access and which does't have public setter method.")
            .in(activityFile)
            .onLine(8);
    }


    @Test
    public void failOnFindPrivateFieldWithPrivateSetter() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "   private void setDependency(DependencyModel dep) {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(new IProcessor())
            .failsToCompile()
            .withErrorContaining("@Inject annotation placed on field `dependency` in `Activity` with private access and which does't have public setter method.")
            .in(activityFile)
            .onLine(8);
    }

    @Test
    public void providerField() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Provider.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Provider<DependencyModel> dependency;",
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
            importType(IocProvider.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void providerFieldWithInterface() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Provider.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Provider<DependencyInterface> dependency;",
            "}");

        JavaFileObject dependencyInterface = JavaFileObjects.forSourceLines("test.DependencyInterface",
            "package test;",
            "",
            "interface DependencyInterface {}");

        JavaFileObject dependencyType = JavaFileObjects.forSourceLines("test.DependencyType",
            "package test;",
            importType(Dependency.class),
            "@Dependency",
            "class DependencyType implements DependencyInterface {}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocProvider.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyInterface, dependencyType))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void lazyFieldWithInterface() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Lazy.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Lazy<DependencyInterface> dependency;",
            "}");

        JavaFileObject dependencyInterface = JavaFileObjects.forSourceLines("test.DependencyInterface",
            "package test;",
            "",
            "interface DependencyInterface {}");

        JavaFileObject dependencyType = JavaFileObjects.forSourceLines("test.DependencyType",
            "package test;",
            importType(Dependency.class),
            "@Dependency",
            "class DependencyType implements DependencyInterface {}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyInterface, dependencyType))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void targetAsParameterInConstructor() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
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
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectWithTargetAndDependencyInConstructor() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(Activity activity, DependencyModel dependency) {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
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
            "       ParentDependency parentDependency = new ParentDependency(target, dependencyModel);",
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
    public void injectWithDependencyInConstructor() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
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
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
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
    public void injectWithTargetInDependencyOfParentDependencyInConstructor() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
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
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
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
    public void preferConstructorWithArguments() throws Exception {
        JavaFileObject parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "public class ParentActivity {",
            "",
            "   public DependencyModel getDependency() { return null; }",
            "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel() {};",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importType(Inject.class),
            "",
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
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void preferTargetConstructorWithArguments() throws Exception {
        JavaFileObject parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            importType(Inject.class),
            "public class ParentActivity {",
            "",
            "   @Inject",
            "   DependencyModel dependencyModel;",
            "   public DependencyModel getDependency() { return null; }",
            "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel() {};",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importType(Inject.class),
            "",
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
            "       new ParentActivityInjector().inject(target);",
            "       injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       target.dependency = parentDependency;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void correctInjectionOrder() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "class DependencyModel {",
            "   public DependencyModel() {};",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class ParentDependency {",
            "   public ParentDependency(DependencyModel childDependency) {}",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void correctInjection3() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            "",
            "@Dependency",
            "public class ReleaseDependency implements DependencyModel {",
            "",
            "   public ReleaseDependency() {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "interface DependencyModel {",
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
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new ReleaseDependency();",
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
    public void correctInjection4() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            "",
            "@Dependency",
            "public class ReleaseDependency extends DependencyModel {",
            "",
            "   public ReleaseDependency() {}",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "class DependencyModel {",
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
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
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
    public void correctInjection5() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel(Context context, Resource resource) {}",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            "",
            "@Dependency",
            "class Context {",
            "}");

        JavaFileObject resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "class Resource {",
            "",
            "   @Inject",
            "   Resource(Context context) {}",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, contextFile, resourceFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void correctInjection6() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel(Context context, Resource resource) {}",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "class Context {",
            "}");

        JavaFileObject contextModuleFile = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            "",
            "class ContextModule {",
            "  @Dependency",
            "  public static Context context() { return new Context(); }",
            "}");

        JavaFileObject resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Singleton.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "@Singleton",
            "class Resource {",
            "",
            "   @Inject",
            "   Resource(Context context) {}",
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
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       Resource resource = ResourceSingleton.get();",
            "       Context context = ContextModule.context();",
            "       DependencyModel dependencyModel = new DependencyModel(context, resource);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, contextModuleFile, moduleFile, contextFile, resourceFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void findImplementationInParent() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "public class AppModel extends BaseModel {",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.BaseModel",
            "package test;",
            "",
            "public class BaseModel implements DependencyModel {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
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
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new AppModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, superParentFile, parentFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void findImplementationInInterfaceParent() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "public class AppModel extends BaseModel {",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.BaseModel",
            "package test;",
            "",
            "public class BaseModel implements DependencyParent {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}");

        JavaFileObject dependencyParentFile = JavaFileObjects.forSourceLines("test.DependencyParent",
            "package test;",
            "",
            "interface DependencyParent extends DependencyModel {",
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
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new AppModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, superParentFile, parentFile, moduleFile, dependencyParentFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void findImplementationInParent2() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.AppModel",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "public class AppModel extends BaseModel {",
            "   @Inject",
            "   public AppModel(Resource resources) {}",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.BaseModel",
            "package test;",
            "",
            "public class BaseModel implements DependencyModel {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}");

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importType(com.ioc.Dependency.class),
            importType(Singleton.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "@Singleton",
            "class Context implements Resource {",
            "}");

        JavaFileObject resourceFile = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "",
            "interface Resource {",
            "",
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
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       Context context = ContextSingleton.get();",
            "       DependencyModel dependencyModel = new AppModel(context);",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, superParentFile, contextFile, resourceFile, parentFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void twoInterfacesFromOnImplementationInConstructor() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public BaseModel dependency;",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.SpeedDialTileClosedEventLogger",
            "package test;",
            "",
            "interface SpeedDialTileClosedEventLogger {",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.SpeedDialTileClickedEventLogger",
            "package test;",
            "",
            "interface SpeedDialTileClickedEventLogger {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "@Singleton",
            "class Amplitude implements SpeedDialTileClickedEventLogger, SpeedDialTileClosedEventLogger {",
            "}");

        JavaFileObject baseFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            "",
            "@Dependency",
            "class BaseModel {",
            "   @Inject",
            "   BaseModel(SpeedDialTileClickedEventLogger speedDialTileClickedEventLogger, SpeedDialTileClosedEventLogger speedDialTileClosedEventLogger) {}",
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
            "       injectBaseModelInDependency(target);",
            "   }",
            "",
            "   private final void injectBaseModelInDependency(@NonNull final Activity target) {",
            "       Amplitude amplitude = AmplitudeSingleton.get();",
            "       BaseModel baseModel = new BaseModel(amplitude, amplitude);",
            "       target.dependency = baseModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, superParentFile, baseFile, parentFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectInParentClass() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity extends BaseActivity {",
            "",
            "   @Inject",
            "   public ClickedEventLogger clickedEventLogger;",
            "}");

        JavaFileObject baseActivityFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public abstract class BaseActivity {",
            "",
            "   @Inject",
            "   public ClosedEventLogger closedEventLogger;",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.ClosedEventLogger",
            "package test;",
            "",
            "interface ClosedEventLogger {",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "interface ClickedEventLogger {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "@Singleton",
            "class Amplitude implements ClickedEventLogger, ClosedEventLogger {",
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
            "       new BaseActivityInjector().inject(target);",
            "       injectClickedEventLoggerInClickedEventLogger(target);",
            "   }",
            "",
            "   private final void injectClickedEventLoggerInClickedEventLogger(@NonNull final Activity target) {",
            "       Amplitude amplitude = AmplitudeSingleton.get();",
            "       target.clickedEventLogger = amplitude;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, baseActivityFile, superParentFile, parentFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectInParentClass1() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Logger logger;",
            "}");

        JavaFileObject loggerFile = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Singleton",
            "@Dependency",
            "public class Logger {",
            "",
            "   @Inject",
            "   public Logger(ClosedEventLogger closedEventLogger, Amplitude amplitude) {};",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.ClosedEventLogger",
            "package test;",
            "",
            "interface ClosedEventLogger {",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.ClickedEventLogger",
            "package test;",
            "",
            "interface ClickedEventLogger {",
            "}");

        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.Amplitude",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "@Singleton",
            "class Amplitude implements ClickedEventLogger, ClosedEventLogger {",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.LoggerSingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class LoggerSingleton {",
            "   private static Logger singleton;",
            "",
            "   private static final LoggerSingleton instance = new LoggerSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final Logger get() {",
            "       if (singleton != null) return singleton;",
            "       Amplitude amplitude = AmplitudeSingleton.get();",
            "       singleton = new Logger(amplitude, amplitude);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, loggerFile, superParentFile, parentFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void twoInterfacesFromOnImplementationInConstructorForSingleton() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.DownloadsFragment",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DownloadsFragment {",
            "",
            "   @Inject",
            "   public DownloadsNavigationLogger downloadsNavigationLogger;",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationPathIndicatorClickedEventLogger",
            "package test;",
            "",
            "interface DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationSystemBackClickedEventLogger",
            "package test;",
            "",
            "interface DownloadsNavigationSystemBackClickedEventLogger {",
            "}");


        JavaFileObject amplitudeLoggerFile = JavaFileObjects.forSourceLines("test.AmplitudeLogger",
            "package test;",
            "",
            "public abstract class AmplitudeLogger implements DownloadsNavigationPathIndicatorClickedEventLogger, DownloadsNavigationSystemBackClickedEventLogger {",
            "}");

        JavaFileObject preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "@Singleton",
            "public class Preferences {",
            "}");


        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.AmplitudeService",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "@Singleton",
            "class AmplitudeService extends AmplitudeLogger {",
            "   @Inject",
            "   public AmplitudeService(Preferences prefs) {}",
            "}");

        JavaFileObject baseFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLogger",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "@Singleton",
            "class DownloadsNavigationLogger {",
            "   @Inject",
            "   DownloadsNavigationLogger(DownloadsNavigationPathIndicatorClickedEventLogger downloadsNavigationPathIndicatorClickedEventLogger, DownloadsNavigationSystemBackClickedEventLogger downloadsNavigationSystemBackClickedEventLogger) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLoggerSingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class DownloadsNavigationLoggerSingleton {",
            "   private static DownloadsNavigationLogger singleton;",
            "",
            "   private static final DownloadsNavigationLoggerSingleton instance = new DownloadsNavigationLoggerSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final DownloadsNavigationLogger get() {",
            "       if (singleton != null) return singleton;",
            "       AmplitudeService amplitudeService = AmplitudeServiceSingleton.get();",
            "       singleton = new DownloadsNavigationLogger(amplitudeService, amplitudeService);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, preferencesFile, amplitudeLoggerFile, superParentFile, baseFile, parentFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void inject() throws Exception {

        JavaFileObject contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {}");


        JavaFileObject contextModuleFile = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "   @Dependency",
            "   public static Context getContext() { return null; };",
            "}");

        JavaFileObject preferencesFile = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {};",
            "}");


        JavaFileObject buildCheckFile = JavaFileObjects.forSourceLines("test.BuildCheck",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "public class BuildCheck {",
            "   @Inject",
            "   public BuildCheck(Preferences prefs) {};",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   private BuildCheck buildCheck;",
            "   @Inject",
            "   public void appendBuildCheck(BuildCheck buildCheck) {};",
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
            "       injectBuildCheckInAppendBuildCheck(target);",
            "   }",
            "",
            "   private final void injectBuildCheckInAppendBuildCheck(@NonNull final Activity target) {",
            "       Preferences preferences = PreferencesSingleton.get();",
            "       BuildCheck buildCheck = new BuildCheck(preferences);",
            "       target.appendBuildCheck(buildCheck);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, contextModuleFile, contextFile, preferencesFile, buildCheckFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectTargetInDependency() {

        JavaFileObject listenerFile = JavaFileObjects.forSourceLines("test.Listener",
            "package test;",
            "",
            "public interface Listener {}");

        JavaFileObject controllerFile = JavaFileObjects.forSourceLines("test.Controller",
            "package test;",
            "",
            "public class Controller {",
            "",
            "   Controller(Listener listener) {}",
            "}");

        JavaFileObject autoCompleteListenerImplFile = JavaFileObjects.forSourceLines("test.AutoCompleteListenerImpl",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Context.class),
            "",
            "@Dependency",
            "public class AutoCompleteListenerImpl implements Listener {",
            "",
            "   AutoCompleteListenerImpl(Context context) {}",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class MyActivity extends Activity {",
            "",
            "   private Controller controller;",
            "   @Inject",
            "   public void appendBuildCheck(Controller controller) {};",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, listenerFile, controllerFile, autoCompleteListenerImplFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void injectTargetInDependency1() throws Exception {

        JavaFileObject interceptor = JavaFileObjects.forSourceLines("test.HttpLoggingInterceptor",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class HttpLoggingInterceptor {",
            "",
            "   public HttpLoggingInterceptor(Level level) {}",
            "",
            "   public enum Level { NONE, BASIC }",
            "}");


        JavaFileObject restModule = JavaFileObjects.forSourceLines("test.RestModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class RestModule {",
            "",
            "   @Dependency",
            "   public static HttpLoggingInterceptor provideHttpLoggingInterceptor() {  return null; }",
            "}");

        JavaFileObject controllerFile = JavaFileObjects.forSourceLines("test.Controller",
            "package test;",
            "",
            "public class Controller {",
            "",
            "   Controller(HttpLoggingInterceptor interceptor) {}",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   private Controller controller;",
            "   @Inject",
            "   public void appendBuildCheck(Controller controller) {};",
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
            "       injectControllerInAppendBuildCheck(target);",
            "   }",
            "",
            "   private final void injectControllerInAppendBuildCheck(@NonNull final Activity target) {",
            "       HttpLoggingInterceptor httpLoggingInterceptor = RestModule.provideHttpLoggingInterceptor();",
            "       Controller controller = new Controller(httpLoggingInterceptor);",
            "       target.appendBuildCheck(controller);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, interceptor, restModule, controllerFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectTargetInDependency2() throws Exception {

        JavaFileObject speedDialDisplayedEventLogger = JavaFileObjects.forSourceLines("test.SpeedDialDisplayedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialDisplayedEventLogger {",
            "}");

        JavaFileObject preferences = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importType(Inject.class),
            importType(Singleton.class),
            "",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {}",
            "}");

        JavaFileObject amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            importType(Inject.class),
            importType(Singleton.class),
            importType(Dependency.class),
            "",
            "@Singleton",
            "@Dependency",
            "public class AmplitudeDefaultLogger implements SpeedDialDisplayedEventLogger {",
            "   @Inject",
            "   public AmplitudeDefaultLogger(Preferences preferences) {}",
            "}");


        JavaFileObject restModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}");

        JavaFileObject controllerFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private SpeedDialDisplayedEventLogger logger;",
            "   public void setLogger(SpeedDialDisplayedEventLogger logger) {};",
            "   public SpeedDialDisplayedEventLogger setLogger() { return null; };",
            "   public Context context() { return null; };",
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
            "       injectSpeedDialDisplayedEventLoggerInLogger(target);",
            "   }",
            "",
            "   private final void injectSpeedDialDisplayedEventLoggerInLogger(@NonNull final Activity target) {",
            "       AmplitudeDefaultLogger amplitudeDefaultLogger = AmplitudeDefaultLoggerSingleton.get();",
            "       target.setLogger(amplitudeDefaultLogger);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, speedDialDisplayedEventLogger, preferences, amplitudeDefaultLogger, restModule, controllerFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void moduleOverTarget() throws Exception {

        JavaFileObject speedDialDisplayedEventLogger = JavaFileObjects.forSourceLines("test.SpeedDialDisplayedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialDisplayedEventLogger {",
            "}");

        JavaFileObject preferences = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importType(Inject.class),
            importType(Singleton.class),
            "",
            "@Singleton",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context) {}",
            "}");

        JavaFileObject amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            importType(Inject.class),
            importType(Singleton.class),
            importType(Dependency.class),
            "",
            "@Singleton",
            "@Dependency",
            "public class AmplitudeDefaultLogger implements SpeedDialDisplayedEventLogger {",
            "   @Inject",
            "   public AmplitudeDefaultLogger(Preferences preferences) {}",
            "}");


        JavaFileObject restModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}");

        JavaFileObject controllerFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private SpeedDialDisplayedEventLogger logger;",
            "   public void setLogger(SpeedDialDisplayedEventLogger logger) {};",
            "   public SpeedDialDisplayedEventLogger getLogger() { return null; };",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.PreferencesSingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class PreferencesSingleton {",
            "   private static Preferences singleton;",
            "",
            "   private static final PreferencesSingleton instance = new PreferencesSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final Preferences get() {",
            "       if (singleton != null) return singleton;",
            "       Context context = ContextModule.context();",
            "       singleton = new Preferences(context);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, speedDialDisplayedEventLogger, preferences, amplitudeDefaultLogger, restModule, controllerFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void stackOverflowError() throws Exception {

        JavaFileObject runnable = JavaFileObjects.forSourceLines("test.Runnable",
            "package test;",
            "",
            "public interface Runnable {",
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public abstract class Context {",
            "}");

        JavaFileObject parentActivity = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "public class ParentActivity extends Context {",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Presenter implements Runnable {",
            "   @Inject",
            "   public Presenter(Context context, Runnable runnable) {}",
            "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity extends ParentActivity implements Runnable {",
            "",
            "   @Inject",
            "   private Presenter presenter;",
            "   public void set(Presenter presenter) {};",
            "   public Presenter get() { return null; };",
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
            "     injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Presenter presenter = new Presenter(target, target);",
            "       target.set(presenter);",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, runnable, parentActivity, context, presenter))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void oneSingletonForDependencies() throws Exception {

        JavaFileObject speedDialDisplayedEventLogger = JavaFileObjects.forSourceLines("test.SpeedDialDisplayedEventLogger",
            "package test;",
            "",
            "public interface SpeedDialDisplayedEventLogger {",
            "}");

        JavaFileObject defaultLogger = JavaFileObjects.forSourceLines("test.DefaultLogger",
            "package test;",
            "",
            "public interface DefaultLogger {",
            "}");

        JavaFileObject preferences = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Preferences {",
            "   @Inject",
            "   public Preferences(Context context, DefaultLogger defaultLogger) {}",
            "}");

        JavaFileObject amplitudeDefaultLogger = JavaFileObjects.forSourceLines("test.AmplitudeDefaultLogger",
            "package test;",
            "",
            importType(Inject.class),
            importType(Singleton.class),
            importType(Dependency.class),
            "",
            "@Singleton",
            "@Dependency",
            "public class AmplitudeDefaultLogger implements SpeedDialDisplayedEventLogger, DefaultLogger {",
            "   @Inject",
            "   public AmplitudeDefaultLogger() {}",
            "}");


        JavaFileObject restModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context(SpeedDialDisplayedEventLogger eventLogger) {  return null; }",
            "}");

        JavaFileObject controllerFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Preferences preferences;",
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
            "       injectPreferencesInPreferences(target);",
            "   }",
            "",
            "   private final void injectPreferencesInPreferences(@NonNull final Activity target) {",
            "       AmplitudeDefaultLogger amplitudeDefaultLogger = AmplitudeDefaultLoggerSingleton.get();",
            "       Context context = ContextModule.context(amplitudeDefaultLogger);",
            "       Preferences preferences = new Preferences(context, amplitudeDefaultLogger);",
            "       target.preferences = preferences;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, speedDialDisplayedEventLogger, defaultLogger, preferences, amplitudeDefaultLogger, restModule, controllerFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void weakModule() throws Exception {


        JavaFileObject contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",
            importType(WeakReference.class),
            "public class Presenter {",
            "   Presenter(WeakReference<Context> context) {}",
            "",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, contextModule, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void providerModule() throws Exception {


        JavaFileObject contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            importType(Provider.class),

            "public class Presenter {",
            "   Presenter(Provider<Context> context) {}",
            "",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocProvider.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, contextModule, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void preferEmptyConstructor() throws Exception {


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(CompositeDisposable.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CompositeDisposable subscription;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(CompositeDisposable.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void providerLazy() throws Exception {


        JavaFileObject contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            importType(Lazy.class),

            "public class Presenter {",
            "   Presenter(Lazy<Context> context) {}",
            "",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, contextModule, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void lazyParam() throws Exception {


        JavaFileObject contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Context context() {  return null; }",
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Lazy.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Lazy<Presenter> presenter;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, contextModule, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void module() throws Exception {


        JavaFileObject contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   public static Resource resource() {  return null; }",
            "   @Dependency",
            "   public static Context context(Resource resource, Activity activity) {  return null; }",
            "   @Dependency",
            "   public static Presenter presenter(Context context) {  return null; }",
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");

        JavaFileObject resources = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "public class Resource {",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Resource resource = ContextModule.resource();",
            "       Context context = ContextModule.context(resource, target);",
            "       Presenter presenter = ContextModule.presenter(context);",
            "       target.presenter = presenter;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, resources, presenter, contextModule, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void moduleSingleton() throws Exception {


        JavaFileObject contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
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
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");

        JavaFileObject resources = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            "",
            "public class Resource {",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Context context = ContextSingleton.get();",
            "       Presenter presenter = ContextModule.presenter(context);",
            "       target.presenter = presenter;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, resources, presenter, contextModule, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void doNotpassTargetInSingleton() throws Exception {


        JavaFileObject contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "public class ContextModule {",
            "",
            "   @Dependency",
            "   @Singleton",
            "   public static Context context(Activity activity) {  return null; }",
            "   @Dependency",
            "   public static Presenter presenter(Context context) {  return null; }",
            "}");

        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context) {}",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, contextModule, context))
            .processedWith(new IProcessor())
            .failsToCompile()
            .withErrorContaining("target can't be user as dependency in Singleton")
            .in(contextModule).onLine(10);
    }

    @Test
    public void doNotpassTargetInSingleton2() throws Exception {


        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            importType(Singleton.class),
            "@Singleton",
            "public class Context {",
            "",
            "}");

        JavaFileObject resource = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            importType(Singleton.class),

            "public class Resource {",
            "   Resource(Context context) {}",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(Context context, Resource resource) {}",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
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
            "       injectPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectPresenterInPresenter(@NonNull final Activity target) {",
            "       Context context = ContextSingleton.get();",
            "       Resource resource = new Resource(context);",
            "       Presenter presenter = new Presenter(context, resource);",
            "       target.presenter = presenter;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, resource, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void abstractClass() throws Exception {


        JavaFileObject context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "abstract class Context {",
            "",
            "   Context(Preferences preferences) {}",
            "",
            "}");

        JavaFileObject resource = JavaFileObjects.forSourceLines("test.Resource",
            "package test;",
            importType(Singleton.class),
            importType(Dependency.class),

            "@Dependency",
            "public class Resource extends Context {",
            "",
            "   Resource(Preferences preferences) { super(preferences);}",
            "",
            "}");

        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Preferences",
            "package test;",

            "public class Preferences {",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Context context;",
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
            "       injectContextInContext(target);",
            "   }",
            "",
            "   private final void injectContextInContext(@NonNull final Activity target) {",
            "       Preferences preferences = new Preferences();",
            "       Context context = new Resource(preferences);",
            "       target.context = context;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, resource, context))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void preferModuleOverTarget() throws Exception {


        JavaFileObject preferences = JavaFileObjects.forSourceLines("test.PreferencesModule",
            "package test;",
            importType(SharedPreferences.class),
            importType(Context.class),
            importType(Dependency.class),
            "public class PreferencesModule {",
            "   @Dependency",
            "   public static SharedPreferences getPreferences(Context context) { return null; }",
            "}");


        JavaFileObject presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",
            importType(SharedPreferences.class),

            "public class Presenter {",
            "   Presenter(SharedPreferences preferences) {}",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importType(SharedPreferences.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, presenter, preferences))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectSuperParent() throws Exception {


        JavaFileObject logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class Logger {",
            "}");

        JavaFileObject fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class FileManager {",
            "}");

        JavaFileObject superFile = JavaFileObjects.forSourceLines("test.SuperActivity",
            "package test;",
            "",
            importType(Activity.class),
            importType(Inject.class),
            "",
            "public class SuperActivity extends Activity {",
            "   @Inject",
            "   public Logger logger;",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class ParentActivity extends SuperActivity {",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class MainActivity extends ParentActivity {",
            "",
            "   @Inject",
            "   public FileManager fileManager;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, logger, fileManager, superFile, parentFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void injectSuperParent2() throws Exception {


        JavaFileObject logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class Logger {",
            "}");

        JavaFileObject fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class FileManager {",
            "}");

        JavaFileObject superFile = JavaFileObjects.forSourceLines("test.SuperActivity",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class SuperActivity extends Activity {",
            "}");

        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class ParentActivity extends SuperActivity {",
            "   @Inject",
            "   public Logger logger;",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class MainActivity extends ParentActivity {",
            "",
            "   @Inject",
            "   public FileManager fileManager;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, logger, fileManager, superFile, parentFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void providerInConstructor() throws Exception {


        JavaFileObject logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class Logger {",
            "}");

        JavaFileObject fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class FileManager {",
            "}");


        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.Dependency",
            "package test;",
            "",
            importType(Inject.class),
            importType(Provider.class),
            importType(Activity.class),
            "",
            "public class Dependency {",
            "   Dependency(Logger logger, Provider<FileManager> fileManager) { }",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Dependency dependency;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocProvider.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, logger, fileManager, parentFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void lazyInConstructor() throws Exception {


        JavaFileObject logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class Logger {",
            "}");

        JavaFileObject fileManager = JavaFileObjects.forSourceLines("test.FileManager",
            "package test;",
            "",
            importType(Activity.class),
            "",
            "public class FileManager {",
            "}");


        JavaFileObject parentFile = JavaFileObjects.forSourceLines("test.Dependency",
            "package test;",
            "",
            importType(Inject.class),
            importType(Lazy.class),
            importType(Activity.class),
            "",
            "public class Dependency {",
            "   Dependency(Logger logger, Lazy<FileManager> fileManager) { }",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Activity.class),
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Dependency dependency;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(IocLazy.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, logger, fileManager, parentFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void test() throws Exception {
        JavaFileObject parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            importType(Inject.class),
            importType(Activity.class),
            "public class ParentActivity extends Activity {",
            "",
            "   @Inject",
            "   public ParentDependency parent;",
            "}");

        JavaFileObject brightnessChangeListener = JavaFileObjects.forSourceLines("test.BrightnessChangeListener",
            "package test;",
            "public interface BrightnessChangeListener {",
            "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class Activity extends ParentActivity implements BrightnessChangeListener {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "class DependencyModel {",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
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
            "       new ParentActivityInjector().inject(target);",
            "       injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       target.dependency = dependencyModel;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, brightnessChangeListener, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void successMultipleGenericType() {

        JavaFileObject subjectModule = JavaFileObjects.forSourceLines("test.SubjectModule",
            "package test;",
            importType(Subject.class),
            importType(BehaviorSubject.class),
            importType(Dependency.class),
            "public class SubjectModule {",
            "   @Dependency",
            "   public static Subject<Boolean> get() { return BehaviorSubject.<Boolean>create(); }",
            "   @Dependency",
            "   public static Subject<Integer> getIntegerSubject() { return BehaviorSubject.<Integer>create(); }",
            "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Subject.class),
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Subject<Boolean> booleanSubject;",
            "",
            "   @Inject",
            "   public Subject<Integer> integerSubject;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            importType(Subject.class),
            importType(Boolean.class),
            importType(Integer.class),
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, subjectModule))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void findParentOnInterface() {

        JavaFileObject incognito = JavaFileObjects.forSourceLines("test.Incognito",
            "package test;",
            "",
            "public interface Incognito {",
            "}");

        JavaFileObject privacy = JavaFileObjects.forSourceLines("test.Privacy",
            "package test;",
            "",
            "public interface Privacy extends Incognito {",
            "}");

        JavaFileObject settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            "",
            "@Dependency",
            "public class Settings implements Privacy {",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Subject.class),
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Incognito incognito;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, incognito, privacy, settings))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void findParentOnAbstractClass() {

        JavaFileObject incognito = JavaFileObjects.forSourceLines("test.Incognito",
            "package test;",
            "",
            "public abstract class Incognito {",
            "}");

        JavaFileObject privacy = JavaFileObjects.forSourceLines("test.Privacy",
            "package test;",
            "",
            "public class Privacy extends Incognito {",
            "}");

        JavaFileObject settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            "",
            "@Dependency",
            "public class Settings extends Privacy {",
            "}");


        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.MyActivity",
            "package test;",
            "",
            importType(Inject.class),
            importType(Subject.class),
            "",
            "public class MyActivity {",
            "",
            "   @Inject",
            "   public Incognito incognito;",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.MyActivityInjector",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, incognito, privacy, settings))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }


    @Test
    public void twoInterfacesFromOnImplementationInConstructorForSingleton2() throws Exception {

        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.DownloadsFragment",
            "package test;",
            "",
            importType(Inject.class),
            "",
            "public class DownloadsFragment {",
            "",
            "   @Inject",
            "   public DownloadsNavigationLogger downloadsNavigationLogger;",
            "}");


        JavaFileObject superParentFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationPathIndicatorClickedEventLogger",
            "package test;",
            "",
            "interface DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}");


        JavaFileObject amplitudeLoggerFile = JavaFileObjects.forSourceLines("test.AmplitudeLogger",
            "package test;",
            "",
            "public abstract class AmplitudeLogger implements DownloadsNavigationPathIndicatorClickedEventLogger {",
            "}");


        JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.AmplitudeService",
            "package test;",
            "",
            importType(Dependency.class),
            importType(Singleton.class),
            importType(Inject.class),
            "",
            "@Dependency",
            "@Singleton",
            "class AmplitudeService extends AmplitudeLogger {",
            "   @Inject",
            "   public AmplitudeService() {}",
            "}");

        JavaFileObject baseFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLogger",
            "package test;",
            "",
            importType(Inject.class),
            importType(Dependency.class),
            importType(Singleton.class),
            "",
            "@Dependency",
            "@Singleton",
            "class DownloadsNavigationLogger {",
            "   @Inject",
            "   DownloadsNavigationLogger(DownloadsNavigationPathIndicatorClickedEventLogger downloadsNavigationPathIndicatorClickedEventLogger) {}",
            "}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.DownloadsNavigationLoggerSingleton",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class DownloadsNavigationLoggerSingleton {",
            "   private static DownloadsNavigationLogger singleton;",
            "",
            "   private static final DownloadsNavigationLoggerSingleton instance = new DownloadsNavigationLoggerSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final DownloadsNavigationLogger get() {",
            "       if (singleton != null) return singleton;",
            "       AmplitudeService amplitudeService = AmplitudeServiceSingleton.get();",
            "       singleton = new DownloadsNavigationLogger(amplitudeService);",
            "       return singleton;",
            "   }",
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, amplitudeLoggerFile, superParentFile, baseFile, moduleFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }

    @Test
    public void findDependencyInParent() throws Exception {
        JavaFileObject activityParentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            importType(Inject.class),
            "public class ParentActivity {",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "}");
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            importType(Inject.class),
            "public class Activity extends ParentActivity{",
            "   @Inject",
            "   public DependencyModel childDependency;",
            "}");

        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            importType(Inject.class),
            "public class ParentDependency {}");

        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            importType(Inject.class),
            "public class DependencyModel {}");

        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
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
            "}");

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, activityParentFile, dependencyFile, parentDependencyFile))
            .processedWith(new IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile);
    }
}
