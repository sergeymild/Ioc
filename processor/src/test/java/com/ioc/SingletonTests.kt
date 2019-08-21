package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 14/08/2017.
 */
@RunWith(JUnit4::class)
class SingletonTests {
    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamedDebugNescafe() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Qualifier(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"release\")",
            "@Singleton",
            "@Dependency",
            "public class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Qualifier(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"debug\")",
            "@Dependency",
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Qualifier(\"nescafe\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"cappuccino\")",
            "@Dependency",
            "public class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"nescafe\")",
            "@Dependency",
            "public class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ReleaseModelSingleton",
            """
            package test;

            $importKeepAnnotation
            $importIocLazy
            
            @Keep
            public final class ReleaseModelSingleton extends IocLazy<ReleaseModel> {
              private static ReleaseModelSingleton instance;
            
              public static final ReleaseModel getInstance() {
                if (instance == null) instance = new ReleaseModelSingleton();
                return instance.get();
              }
            
              protected final ReleaseModel initialize() {
                Cappuccino coffee = new Cappuccino();
                return new ReleaseModel(coffee);
              }
            }
        """.trimIndent())

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
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Qualifier(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importSingletonAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"release\")",
            "@Singleton",
            "@Dependency",
            "public class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Qualifier(\"nescafe\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"debug\")",
            "@Dependency",
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Qualifier(\"cappuccino\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"cappuccino\")",
            "@Dependency",
            "public class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Qualifier(\"nescafe\")",
            "@Dependency",
            "public class Nescafe implements Coffee {",
            "   @Inject",
            "   public Nescafe(Sugar sugar) {};",
            "}")

        val sugar = JavaFileObjects.forSourceLines("test.Sugar",
            "package test;",
            "",
            importQualifierAnnotation,
            "",
            "public class Sugar {",
            "   public Sugar() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ReleaseModelSingleton",
            """
            package test;

            $importKeepAnnotation
            $importIocLazy
            
            @Keep
            public final class ReleaseModelSingleton extends IocLazy<ReleaseModel> {
              private static ReleaseModelSingleton instance;
            
              public static final ReleaseModel getInstance() {
                if (instance == null) instance = new ReleaseModelSingleton();
                return instance.get();
              }
            
              protected final ReleaseModel initialize() {
                Sugar sugar = new Sugar();
                Nescafe coffee = new Nescafe(sugar);
                return new ReleaseModel(coffee);
              }
            }
        """.trimIndent())

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
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importSingletonAnnotation,
            "",
            "@Singleton",
            "public class MainPresenter {",
            "   MainPresenter(DependencyModel dependency) {}",
            "}")

        val release = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importSingletonAnnotation,
            "",
            "@Singleton",
            "public class DependencyModel {",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.MainPresenterSingleton",
            """
            package test;
            $importKeepAnnotation
            $importIocLazy
            @Keep
            public final class MainPresenterSingleton extends IocLazy<MainPresenter> {
               private static MainPresenterSingleton instance;
            
               public static final MainPresenter getInstance() {
                   if (instance == null) instance = new MainPresenterSingleton();
                   return instance.get();
               }
            
               protected final MainPresenter initialize() {
                   return new MainPresenter(DependencyModelSingleton.getInstance());
               }
            }
        """.trimIndent())

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
            importInjectAnnotation,
            importQualifierAnnotation,
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
            importSingletonAnnotation,
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
            importDependencyAnnotation,
            "@Dependency",
            "public class Settings implements PrivacySettings, ThemeSettings {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.CookieManagerWorkerSingleton",
            """
            package test;

            $importKeepAnnotation
            $importIocLazy
            
            @Keep
            public final class CookieManagerWorkerSingleton extends IocLazy<CookieManagerWorker> {
              private static CookieManagerWorkerSingleton instance;
            
              public static final CookieManagerWorker getInstance() {
                if (instance == null) instance = new CookieManagerWorkerSingleton();
                return instance.get();
              }
            
              protected final CookieManagerWorker initialize() {
                Settings privacySettings = new Settings();
                Settings themeSettings = new Settings();
                return new CookieManagerWorker(privacySettings,themeSettings);
              }
            }
        """.trimIndent())

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
            importInjectAnnotation,
            importQualifierAnnotation,
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
            importSingletonAnnotation,
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
            importDependencyAnnotation,
            "@Dependency",
            "public class Settings implements PrivacySettings {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.WebMusicManagerSingleton",
            """
            package test;

            $importKeepAnnotation
            $importIocLazy
            
            @Keep
            public final class WebMusicManagerSingleton extends IocLazy<WebMusicManager> {
              private static WebMusicManagerSingleton instance;
            
              public static final WebMusicManager getInstance() {
                if (instance == null) instance = new WebMusicManagerSingleton();
                return instance.get();
              }
            
              protected final WebMusicManager initialize() {
                Settings privacySettings = new Settings();
                Session session = new Session(privacySettings);
                return new WebMusicManager(session);
              }
            }
        """.trimIndent())

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
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importLazy,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(Lazy<DependencyModel> lazyDependency, SingletonDependency singletonDependency) {}",
            "}")

        val singletonDependency = JavaFileObjects.forSourceLines("test.SingletonDependency",
            "package test;",
            "",
            importSingletonAnnotation,
            "",
            "@Singleton",
            "public class SingletonDependency {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   DependencyModel(SingletonDependency singletonDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
            package test;

            $importKeepAnnotation
            $importNonNullAnnotation
            $importIocLazy
            
            @Keep
            public final class ActivityInjector {
              @Keep
              public static final void inject(@NonNull final Activity target) {
                target.presenter = provideMainPresenter();
              }
            
              public static final MainPresenter provideMainPresenter() {
                IocLazy<DependencyModel> lazyDependencyModel = new IocLazy<DependencyModel>() {
                  protected DependencyModel initialize() {
                    DependencyModel dependencyModel = new DependencyModel(SingletonDependencySingleton.getInstance());
                    return dependencyModel;
                  }
                };
                MainPresenter mainPresenter = new MainPresenter(lazyDependencyModel,SingletonDependencySingleton.getInstance());
                return mainPresenter;
              }
            }
        """.trimIndent())

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
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importProvider,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(Provider<DependencyModel> providerDependency, SingletonDependency singletonDependency) {}",
            "}")

        val singletonDependency = JavaFileObjects.forSourceLines("test.SingletonDependency",
            "package test;",
            "",
            importSingletonAnnotation,
            "",
            "@Singleton",
            "public class SingletonDependency {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   DependencyModel(SingletonDependency singletonDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
            package test;

            $importKeepAnnotation
            $importNonNullAnnotation
            import com.ioc.IocProvider;
            
            @Keep
            public final class ActivityInjector {
              @Keep
              public static final void inject(@NonNull final Activity target) {
                target.presenter = provideMainPresenter();
              }
            
              public static final MainPresenter provideMainPresenter() {
                IocProvider<DependencyModel> providerDependencyModel = new IocProvider<DependencyModel>() {
                  protected DependencyModel initialize() {
                    DependencyModel dependencyModel = new DependencyModel(SingletonDependencySingleton.getInstance());
                    return dependencyModel;
                  }
                };
                MainPresenter mainPresenter = new MainPresenter(providerDependencyModel,SingletonDependencySingleton.getInstance());
                return mainPresenter;
              }
            }
        """.trimIndent())

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
            importSingletonAnnotation,
            importDependencyAnnotation,
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
            importInjectAnnotation,
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
            """
            package test;

            $importKeepAnnotation
            $importNonNullAnnotation
            
            @Keep
            public final class ActivityInjector {
              @Keep
              public static final void inject(@NonNull final Activity target) {
                target.logger = DefaultLoggerSingleton.getInstance();
                target.context = provideContext();
                target.db = provideDb();
              }
            
              public static final Context provideContext() {
                Context context = new Context(DefaultLoggerSingleton.getInstance());
                return context;
              }
            
              public static final Db provideDb() {
                Db db = new Db(DefaultLoggerSingleton.getInstance());
                return db;
              }
            }
        """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, eventLogger, defaultLogger, context, db))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    fun kotlinModuleInSingleton() {
        val exampleInterface = JavaFileObjects.forSourceLines("test.ExampleInterface",
            "package test;",
            "",
            "interface ExampleInterface {",
            "}")

        val parentFile = JavaFileObjects.forSourceLines("test.Parent",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public abstract class Parent <T extends ExampleInterface> {",
            "   @Inject",
            "   public String exampleString;",
            "}")

        val childFile = JavaFileObjects.forSourceLines("test.Child",
            "package test;",
            "",
            importInjectAnnotation,
            importList,
            "",
            "public abstract class Child <T extends ExampleInterface> extends Parent<T> {",
            "   @Inject",
            "   public List<String> exampleList;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            importDependencyAnnotation,
            importSingletonAnnotation,
            importKotlinMetadataAnnotation,
            importList,
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   @Singleton",
            "   public final String provideExampleDependencyString() { return null; }",
            "   @Dependency",
            "   @Singleton",
            "   public final List<String> provideExampleDependencyList() { return null; }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("com.ioc.ListSingleton",
            """
            package com.ioc;

            $importKeepAnnotation
            import java.lang.String;
            $importList
            import test.Module;
            
            @Keep
            public final class ListSingleton extends IocLazy<List<String>> {
              private static ListSingleton instance;
            
              public static final List<String> getInstance() {
                if (instance == null) instance = new ListSingleton();
                return instance.get();
              }
            
              protected final List<String> initialize() {
                return Module.INSTANCE.provideExampleDependencyList();
              }
            }
        """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(moduleFile, exampleInterface, parentFile, childFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    fun failSingletonMethodMustReturnValidType() {

        val singletonModule = JavaFileObjects.forSourceLines("test.SingletonModule",
            "package test;",
            importSingletonAnnotation,
            "public class SingletonModule {",
            "   @Singleton",
            "   public static Object fromModule() { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(singletonModule))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("test.SingletonModule.fromModule() annotated with @Singleton return type is not valid.")
            .`in`(singletonModule)
            .onLine(5)
    }

    @Test
    fun failSingletonMethodMustReturnType() {

        val singletonModule = JavaFileObjects.forSourceLines("test.SingletonModule",
            "package test;",
            importSingletonAnnotation,
            "public class SingletonModule {",
            "   @Singleton",
            "   public static void fromModule() { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(singletonModule))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("test.SingletonModule.fromModule() annotated with @Singleton must return type")
            .`in`(singletonModule)
            .onLine(5)
    }

    @Test
    fun failSingletonClassMustBePublic() {

        val singletonClass = JavaFileObjects.forSourceLines("test.SingletonClass",
            "package test;",
            "",
            importSingletonAnnotation,
            "",
            "@Singleton",
            "class SingletonClass {}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(singletonClass))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("test.SingletonClass annotated with @Singleton must be public")
            .`in`(singletonClass)
            .onLine(6)
    }
}