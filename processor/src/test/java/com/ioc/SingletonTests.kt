package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton
import javax.tools.JavaFileObject

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
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
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
            "public class ReleaseModel implements DependencyModel {",
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
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Named(\"nescafe\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"cappuccino\")",
            "@Dependency",
            "public class Cappuccino implements Coffee {",
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
            "public class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ReleaseModelSingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class ReleaseModelSingleton extends IocLazy<ReleaseModel>",
            "   private static ReleaseModelSingleton instance;",
            "",
            "   public static final ReleaseModelSingleton getInstance() {",
            "       if (instance == null) instance = new ReleaseModelSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ReleaseModel initialize() {",
            "       Cappuccino coffee = new Cappuccino();",
            "       return new ReleaseModel(coffee);",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, cappuccino, nescafe, release, debug, presenter, coffee, dependencyFile))
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
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
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
            "public class ReleaseModel implements DependencyModel {",
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
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Named(\"cappuccino\")",
            "@Dependency",
            "public class Cappuccino implements Coffee {",
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
            "public class Nescafe implements Coffee {",
            "   @Inject",
            "   public Nescafe(Sugar sugar) {};",
            "}")

        val sugar = JavaFileObjects.forSourceLines("test.Sugar",
            "package test;",
            "",
            Named::class.java.import(),
            "",
            "public class Sugar {",
            "   public Sugar() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ReleaseModelSingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class ReleaseModelSingleton extends IocLazy<ReleaseModel> {",
            "   private static ReleaseModelSingleton instance;",
            "",
            "   public static final ReleaseModelSingleton getInstance() {",
            "       if (instance == null) instance = new ReleaseModelSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ReleaseModel initialize() {",
            "       Sugar sugar = new Sugar();",
            "       Nescafe coffee = new Nescafe(sugar);",
            "       return new ReleaseModel(coffee);",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, cappuccino, nescafe, sugar, release, debug, presenter, coffee, dependencyFile))
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
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            "import $singleton;",
            "",
            "@Singleton",
            "public class MainPresenter {",
            "   MainPresenter(DependencyModel dependency) {}",
            "}")

        val release = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "import $singleton;",
            "",
            "@Singleton",
            "public class DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainPresenterSingleton",
            "package test;",
            "",
            "import $keep",
            "import $ioc",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class MainPresenterSingleton extends IocLazy<MainPresenter> {",
            "   private static MainPresenterSingleton instance;",
            "",
            "   public static final MainPresenterSingleton getInstance() {",
            "       if (instance == null) instance = new MainPresenterSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final MainPresenter initialize() {",
            "       return new MainPresenter(Ioc.singleton(DependencyModel.class));",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, release, presenter))
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
            "public class CookieManagerWorker {",
            "   CookieManagerWorker(PrivacySettings privacySettings, ThemeSettings themeSettings) {}",
            "}")

        val privacySettings = JavaFileObjects.forSourceLines("test.PrivacySettings",
            "package test;",
            "",
            "public interface PrivacySettings {",
            "}")

        val themeSettings = JavaFileObjects.forSourceLines("test.ThemeSettings",
            "package test;",
            "",
            "public interface ThemeSettings {",
            "}")

        val settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "public class Settings implements PrivacySettings, ThemeSettings {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.CookieManagerWorkerSingleton",
            "package test;",
            "",
            "import $keep;",
            "import $iocLazy;",
            "",
            "@Keep",
            "public final class CookieManagerWorkerSingleton extends IocLazy<CookieManagerWorker> {",
            "   private static CookieManagerWorkerSingleton instance;",
            "",
            "   public static final CookieManagerWorkerSingleton getInstance() {",
            "       if (instance == null) instance = new CookieManagerWorkerSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final CookieManagerWorker initialize() {",
            "       Settings privacySettings = new Settings();",
            "       Settings themeSettings = new Settings();",
            "       return new CookieManagerWorker(privacySettings, themeSettings);",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, cookieManager, privacySettings, themeSettings, settings))
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

        val cookieManager = JavaFileObjects.forSourceLines("test.WebMusicManager",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "public class WebMusicManager {",
            "   WebMusicManager(Session session) {}",
            "}")

        val privacySettings = JavaFileObjects.forSourceLines("test.Session",
            "package test;",
            "",
            "public class Session {",
            "   Session(PrivacySettings privacySettings) {}",
            "}")

        val themeSettings = JavaFileObjects.forSourceLines("test.PrivacySettings",
            "package test;",
            "",
            "public interface PrivacySettings {",
            "}")

        val settings = JavaFileObjects.forSourceLines("test.Settings",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "public class Settings implements PrivacySettings {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.WebMusicManagerSingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class WebMusicManagerSingleton extends IocLazy<WebMusicManager> {",
            "   private static WebMusicManagerSingleton instance;",
            "",
            "   public static final WebMusicManagerSingleton getInstance() {",
            "       if (instance == null) instance = new WebMusicManagerSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final WebMusicManager initialize() {",
            "       Settings privacySettings = new Settings();",
            "       Session session = new Session(privacySettings);",
            "       return new WebMusicManager(session);",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, cookieManager, privacySettings, themeSettings, settings))
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
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(Lazy<DependencyModel> lazyDependency, SingletonDependency singletonDependency) {}",
            "}")

        val singletonDependency = JavaFileObjects.forSourceLines("test.SingletonDependency",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "public class SingletonDependency {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   DependencyModel(SingletonDependency singletonDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "import $ioc;",
            "import $iocLazy;",
            "",
            "@Keep",
            "public final class ActivityInjector",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private static final MainPresenter provideMainPresenter() {",
            "       IocLazy<DependencyModel> lazyDependencyModel = new IocLazy<DependencyModel>() {",
            "           protected DependencyModel initialize() {",
            "               DependencyModel dependencyModel = new DependencyModel(Ioc.singleton(SingletonDependency.class));",
            "               return dependencyModel;",
            "           }",
            "       };",
            "       MainPresenter mainPresenter = new MainPresenter(lazyDependencyModel, Ioc.singleton(SingletonDependency.class));",
            "       return mainPresenter;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, singletonDependency, presenter, dependencyFile))
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
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(Provider<DependencyModel> providerDependency, SingletonDependency singletonDependency) {}",
            "}")

        val singletonDependency = JavaFileObjects.forSourceLines("test.SingletonDependency",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "public class SingletonDependency {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   DependencyModel(SingletonDependency singletonDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "import $ioc",
            "import $iosProvider",
            "",
            "@Keep",
            "public final class ActivityInjector",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private static final MainPresenter provideMainPresenter() {",
            "       IocProvider<DependencyModel> providerDependencyModel = new IocProvider<DependencyModel>() {",
            "           protected DependencyModel initialize() {",
            "               DependencyModel dependencyModel = new DependencyModel(Ioc.singleton(SingletonDependency.class));",
            "               return dependencyModel;",
            "           }",
            "       };",
            "       MainPresenter mainPresenter = new MainPresenter(providerDependencyModel, Ioc.singleton(SingletonDependency.class));",
            "       return mainPresenter;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, singletonDependency, presenter, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectSameSingletonInDifferentConstructors() {

        val eventLogger = JavaFileObjects.forSourceLines("test.EventLogger",
            "package test;",
            "public interface EventLogger {",
            "}")

        val defaultLogger = JavaFileObjects.forSourceLines("test.DefaultLogger",
            "package test;",
            "",
            "import $singleton;",
            "import $dependency;",
            "",
            "@Singleton",
            "@Dependency",
            "public class DefaultLogger implements EventLogger {",
            "}")


        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            "public class Context {",
            "   public Context(EventLogger logger) {}",
            "}")

        val db = JavaFileObjects.forSourceLines("test.Db",
            "package test;",
            "",
            "public class Db {",
            "",
            "   public Db(EventLogger logger) {}",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public EventLogger logger;",
            "   @Inject",
            "   public Context context;",
            "   @Inject",
            "   public Db db;",
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
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.logger = Ioc.singleton(DefaultLogger.class);",
            "       target.context = provideContext();",
            "       target.db = provideDb();",
            "   }",
            "",
            "   private static final Context provideContext() {",
            "       Context context = new Context(Ioc.singleton(DefaultLogger.class));",
            "       return context;",
            "   }",
            "",
            "   private static final Db provideDb() {",
            "       Db db = new Db(Ioc.singleton(DefaultLogger.class));",
            "       return db;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, eventLogger, defaultLogger, context, db))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}