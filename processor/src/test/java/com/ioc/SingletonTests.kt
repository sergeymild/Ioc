package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Created by sergeygolishnikov on 14/08/2017.
 */
@RunWith(JUnit4::class)
class SingletonTests : BaseTest {
    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamedDebugNescafe() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"release\")",
            "@Singleton",
            "@Dependency",
            "class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"debug\")",
            "@Dependency",
            "class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Named(\"nescafe\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"cappuccino\")",
            "@Dependency",
            "class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"nescafe\")",
            "@Dependency",
            "class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ReleaseModelSingleton",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            "",
            "@Keep",
            "public final class ReleaseModelSingleton",
            "   private static ReleaseModel singleton;",
            "",
            "   private static final ReleaseModelSingleton instance = new ReleaseModelSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ReleaseModel get() {",
            "       if (singleton != null) return singleton;",
            "       Coffee coffee = new Cappuccino();",
            "       singleton = new ReleaseModel(coffee);",
            "       return singleton",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, cappuccino, nescafe, release, debug, presenter, coffee, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamedDebugNescafeWithSugar() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"release\")",
            "@Singleton",
            "@Dependency",
            "class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Named(\"nescafe\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"debug\")",
            "@Dependency",
            "class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"cappuccino\")",
            "@Dependency",
            "class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"nescafe\")",
            "@Dependency",
            "class Nescafe implements Coffee {",
            "   @Inject",
            "   public Nescafe(Sugar sugar) {};",
            "}")

        val sugar = JavaFileObjects.forSourceLines("test.Sugar",
            "package test;",
            "",
            Named::class.java.import(),
            "",
            "class Sugar {",
            "   public Sugar() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ReleaseModelSingleton",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            "",
            "@Keep",
            "public final class ReleaseModelSingleton",
            "   private static ReleaseModel singleton;",
            "",
            "   private static final ReleaseModelSingleton instance = new ReleaseModelSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final ReleaseModel get() {",
            "       if (singleton != null) return singleton;",
            "       Sugar sugar = new Sugar();",
            "       Coffee coffee = new Nescafe(sugar);",
            "       singleton = new ReleaseModel(coffee);",
            "       return singleton",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, cappuccino, nescafe, sugar, release, debug, presenter, coffee, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonAsDependency() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "class MainPresenter {",
            "   MainPresenter(DependencyModel dependency) {}",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "class DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainPresenterSingleton",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            "",
            "@Keep",
            "public final class MainPresenterSingleton",
            "   private static MainPresenter singleton;",
            "",
            "   private static final MainPresenterSingleton instance = new MainPresenterSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final MainPresenter get() {",
            "       if (singleton != null) return singleton;",
            "       DependencyModel dependencyModel = DependencyModelSingleton.get();",
            "       singleton = new MainPresenter(dependencyModel);",
            "       return singleton",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, release, presenter))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonAsDependency2() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CookieManagerWorker manager;",
            "   @Inject",
            "   private PrivacySettings privacySettings;",
            "   @Inject",
            "   private ThemeSettings themeSettings;",
            "   public void privacySettings(PrivacySettings settings) {}",
            "   public void themeSettings(ThemeSettings settings) {}",
            "   public PrivacySettings privacySettings() {return null;}",
            "   public ThemeSettings themeSettings() {return null;}",
            "}")

        val cookieManager = JavaFileObjects.forSourceLines("test.CookieManagerWorker",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "class CookieManagerWorker {",
            "   CookieManagerWorker(PrivacySettings privacySettings, ThemeSettings themeSettings) {}",
            "}")

        val privacySettings = JavaFileObjects.forSourceLines("test.PrivacySettings",
            "package test;",
            "",
            "interface PrivacySettings {",
            "}")

        val themeSettings = JavaFileObjects.forSourceLines("test.ThemeSettings",
            "package test;",
            "",
            "interface ThemeSettings {",
            "}")

        val settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "class Settings implements PrivacySettings, ThemeSettings {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.CookieManagerWorkerSingleton",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            "",
            "@Keep",
            "public final class CookieManagerWorkerSingleton",
            "   private static CookieManagerWorker singleton;",
            "",
            "   private static final CookieManagerWorkerSingleton instance = new CookieManagerWorkerSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final CookieManagerWorker get() {",
            "       if (singleton != null) return singleton;",
            "       PrivacySettings privacySettings = new Settings();",
            "       ThemeSettings themeSettings = new Settings();",
            "       singleton = new CookieManagerWorker(privacySettings, themeSettings);",
            "       return singleton",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, cookieManager, privacySettings, themeSettings, settings))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun singletonAsDependency3() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public WebMusicManager webMusicManager;",

            "   public Session getSession() {return null;}",
            "   public PrivacySettings getPrivacySettings() {return null;}",
            "}")

        val cookieManager = JavaFileObjects.forSourceLines("test.CookieManagerWorker",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "class WebMusicManager {",
            "   WebMusicManager(Session session) {}",
            "}")

        val privacySettings = JavaFileObjects.forSourceLines("test.PrivacySettings",
            "package test;",
            "",
            "class Session {",
            "   Session(PrivacySettings privacySettings) {}",
            "}")

        val themeSettings = JavaFileObjects.forSourceLines("test.PrivacySettings",
            "package test;",
            "",
            "interface PrivacySettings {",
            "}")

        val settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "class Settings implements PrivacySettings {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.WebMusicManagerSingleton",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            "",
            "@Keep",
            "public final class WebMusicManagerSingleton",
            "   private static WebMusicManager singleton;",
            "",
            "   private static final WebMusicManagerSingleton instance = new WebMusicManagerSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final WebMusicManager get() {",
            "       if (singleton != null) return singleton;",
            "       PrivacySettings privacySettings = new Settings();",
            "       Session session = new Session(privacySettings);",
            "       singleton = new WebMusicManager(session);",
            "       return singleton",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, cookieManager, privacySettings, themeSettings, settings))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun successPostInitialization() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            PostInitialization::class.java.import(),
            "",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "   @PostInitialization",
            "   public void postInitialization() {}",
            "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            "",
            "class Logger {",
            "}")

        val settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            "",
            "class Settings {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            PostInitialization::class.java.import(),
            Singleton::class.java.import(),
            Inject::class.java.import(),
            "",
            "@Singleton",
            "class DependencyModel {",
            "   @Inject",
            "   Settings settings;",
            "",
            "   DependencyModel(Logger logger) {}",
            "   @PostInitialization",
            "   public void postInitialization() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DependencyModelSingleton",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            "",
            "@Keep",
            "public final class DependencyModelSingleton {",
            "   private static DependencyModel singleton;",
            "   private static final DependencyModelSingleton instance = new DependencyModelSingleton();",
            "",
            "   @Keep",
            "   @NonNull",
            "   public static final DependencyModel get() {",
            "       if (singleton != null) return singleton;",
            "       Logger logger = new Logger();",
            "       singleton = new DependencyModel(logger);",
            "       singleton.postInitialization();",
            "       return singleton;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, logger, settings, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonInLazyConstruction() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Lazy::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(Lazy<DependencyModel> lazyDependency, SingletonDependency singletonDependency) {}",
            "}")

        val singletonDependency = JavaFileObjects.forSourceLines("test.SingletonDependency",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "class SingletonDependency {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "class DependencyModel {",
            "   @Inject",
            "   DependencyModel(SingletonDependency singletonDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            IocLazy::class.java.import(),
            "",
            "@Keep",
            "public final class ActivityInjector",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectMainPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectMainPresenterInPresenter(@NonNull final Activity target) {",
            "       SingletonDependency singletonDependency = SingletonDependencySingleton.get();",
            "       IocLazy<DependencyModel> dependencyModel = new IocLazy<DependencyModel>() {",
            "           protected DependencyModel initialize() {",
            "               DependencyModel dependencyModel = new DependencyModel(singletonDependency);",
            "               return dependencyModel;",
            "           }",
            "       };",
            "       MainPresenter mainPresenter = new MainPresenter(dependencyModel, singletonDependency);",
            "       target.presenter = mainPresenter;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, singletonDependency, presenter, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonInProviderConstruction() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Provider::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(Provider<DependencyModel> providerDependency, SingletonDependency singletonDependency) {}",
            "}")

        val singletonDependency = JavaFileObjects.forSourceLines("test.SingletonDependency",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "class SingletonDependency {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "class DependencyModel {",
            "   @Inject",
            "   DependencyModel(SingletonDependency singletonDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            keepAnnotation,
            nonNullAnnotation,
            Override::class.java.import(),
            Provider::class.java.import(),
            "",
            "@Keep",
            "public final class ActivityInjector",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectMainPresenterInPresenter(target);",
            "   }",
            "",
            "   private final void injectMainPresenterInPresenter(@NonNull final Activity target) {",
            "       SingletonDependency singletonDependency = SingletonDependencySingleton.get();",
            "       Provider<DependencyModel> provider_dependencyModel = new Provider<DependencyModel>() {",
            "           @Override",
            "           @NonNull",
            "           public DependencyModel get() {",
            "               DependencyModel dependencyModel = new DependencyModel(singletonDependency);",
            "               return dependencyModel;",
            "           }",
            "       };",
            "       MainPresenter mainPresenter = new MainPresenter(provider_dependencyModel, singletonDependency);",
            "       target.presenter = mainPresenter;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, singletonDependency, presenter, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}