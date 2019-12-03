package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 28/12/2017.
 */
@RunWith(JUnit4::class)
class ScopesTest {

    @Test
    @Throws(Exception::class)
    fun nestedInjection() {

        val session = JavaFileObjects.forSourceLines("test.Session",
            "package test;",
            "public interface Session {",
            "}")

        val secondScoped = JavaFileObjects.forSourceLines("test.SecondScoped",
            "package test;",
            "public class SecondScoped {",
            "}")

        val sessionImpl = JavaFileObjects.forSourceLines("test.SessionImplementation",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class SessionImplementation implements Session {",
            "   SessionImplementation(BrowserUi browserUi, SecondScoped secondScoped) {}",
            "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class Logger {",
            "   Logger(BrowserUi browserUi) {}",
            "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
            "package test;",
            importAndroidContext,

            "public abstract class BrowserUi {",
            "   BrowserUi(Context context) {}",
            "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
            "package test;",
            importAndroidContext,
            importDependencyAnnotation,

            "@Dependency",
            "public class PhoneBrowserUi extends BrowserUi {",
            "   PhoneBrowserUi(Context context) { super(context); }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importLocalScopeAnnotation,
            importAndroidActivity,
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Session session;",

            "   @Inject",
            "   public Logger logger;",

            "   @LocalScope",
            "   @Inject",
            "   public BrowserUi browserUi;",

            "   @LocalScope",
            "   @Inject",
            "   private SecondScoped secondScoped;",

            "   public void setSecondScoped(SecondScoped secondScoped) {};",
            "   public SecondScoped getSecondScoped() { return null; };",
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
            "       target.setSecondScoped(new SecondScoped());",
            "       target.browserUi = providePhoneBrowserUi(target);",
            "       target.session = provideSessionImplementation(target);",
            "       target.logger = provideLogger(target);",
            "   }",
            "",
            "   private static final PhoneBrowserUi providePhoneBrowserUi(@NonNull final MainActivity target) {",
            "       PhoneBrowserUi browserUi = new PhoneBrowserUi(target);",
            "       return browserUi;",
            "   }",
            "",
            "   private static final SessionImplementation provideSessionImplementation(@NonNull final MainActivity target) {",
            "       SessionImplementation session = new SessionImplementation(target.browserUi, target.getSecondScoped());",
            "       return session;",
            "   }",
            "",
            "   private static final Logger provideLogger(@NonNull final MainActivity target) {",
            "       Logger logger = new Logger(target.browserUi);",
            "       return logger;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, sessionImpl, secondScoped, logger, session, browserUi, phoneBrowserUi))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun localScopeInConstructor() {
        val alohabrowser = JavaFileObjects.forSourceLines("test.AlohaBrowserUi",
            "package test;",
            "public class AlohaBrowserUi {",
            "}")

        val autoCompleteListener = JavaFileObjects.forSourceLines("test.AutoCompleteListener",
            "package test;",
            "public interface AutoCompleteListener {",
            "}")

        val autoCompleteListenerImpl = JavaFileObjects.forSourceLines("test.AutoCompleteListenerImpl",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class AutoCompleteListenerImpl implements AutoCompleteListener {",
            "   public AutoCompleteListenerImpl(AlohaBrowserUi browserUi) {}",
            "}")

        val autocompleteController = JavaFileObjects.forSourceLines("test.AutocompleteController",
            "package test;",
            "public class AutocompleteController {",
            "   public AutocompleteController(AutoCompleteListener listener) {}",
            "}")


        val logger = JavaFileObjects.forSourceLines("test.AddressBarListenerImpl",
            "package test;",
            importLocalScopeAnnotation,
            importInjectAnnotation,
            "public class AddressBarListenerImpl {",
            "   @Inject",
            "   public AutocompleteController controller;",
            "   @LocalScope",
            "   private AlohaBrowserUi browserUi;",
            "   AddressBarListenerImpl(AlohaBrowserUi browserUi) {}",
            "   public AlohaBrowserUi getAlohaBrowserUi() { return null; }",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.AddressBarListenerImplInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class AddressBarListenerImplInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final AddressBarListenerImpl target) {",
            "       target.controller = provideAutocompleteController(target);",
            "   }",
            "",
            "   private static final AutocompleteController provideAutocompleteController(@NonNull final AddressBarListenerImpl target) {",
            "       AutoCompleteListenerImpl autoCompleteListener = new AutoCompleteListenerImpl(target.getAlohaBrowserUi());",
            "       AutocompleteController autocompleteController = new AutocompleteController(autoCompleteListener);",
            "       return autocompleteController;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(autoCompleteListener, autoCompleteListenerImpl, autocompleteController, logger, alohabrowser))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun localScopeOnImplementationAndInterface() {

        val session = JavaFileObjects.forSourceLines("test.Session",
            "package test;",
            "public interface Session {",
            "}")

        val secondScoped = JavaFileObjects.forSourceLines("test.SecondScoped",
            "package test;",
            importLocalScopeAnnotation,
            "@LocalScope",
            "public class SecondScoped {",
            "   public SecondScoped(String scoped) {}",
            "}")

        val sessionImpl = JavaFileObjects.forSourceLines("test.SessionImplementation",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class SessionImplementation implements Session {",
            "   SessionImplementation(BrowserUi browserUi, SecondScoped secondScoped, String scopedString) {}",
            "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class Logger {",
            "   Logger(BrowserUi browserUi, String scopedString) {}",
            "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
            "package test;",
            importAndroidContext,
            importLocalScopeAnnotation,
            "@LocalScope",
            "public abstract class BrowserUi {",
            "   BrowserUi(Context context) {}",
            "}")

        val moduleScoped = JavaFileObjects.forSourceLines("test.ModuleScoped",
            "package test;",
            importDependencyAnnotation,
            importLocalScopeAnnotation,
            "public class ModuleScoped {",
            "   @LocalScope",
            "   @Dependency",
            "   public static String provideLocalString() { return null; }",
            "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
            "package test;",
            importAndroidContext,
            importDependencyAnnotation,

            "@Dependency",
            "public class PhoneBrowserUi extends BrowserUi {",
            "   PhoneBrowserUi(Context context) { super(context); }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            importInjectAnnotation,
            importAndroidActivity,
            importLocalScopeAnnotation,
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Session session;",

            "   @Inject",
            "   public Logger logger;",

            "   @Inject",
            "   public BrowserUi browserUi;",

            "   @Inject",
            "   public SecondScoped secondScoped;",
            "   @Inject",
            "   public String scopedString;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            """
                package test;

                $importKeepAnnotation
                $importNonNullAnnotation
                
                @Keep
                public final class MainActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final MainActivity target) {
                    target.scopedString = ModuleScoped.provideLocalString();
                    target.browserUi = providePhoneBrowserUi(target);
                    target.secondScoped = provideSecondScoped(target);
                    target.session = provideSessionImplementation(target);
                    target.logger = provideLogger(target);
                  }
                
                  private static final PhoneBrowserUi providePhoneBrowserUi(@NonNull final MainActivity target) {
                    PhoneBrowserUi browserUi = new PhoneBrowserUi(target);
                    return browserUi;
                  }
                  
                  private static final SecondScoped provideSecondScoped(@NonNull final MainActivity target) {
                    SecondScoped secondScoped = new SecondScoped(target.scopedString);
                    return secondScoped;
                  }
                
                  private static final SessionImplementation provideSessionImplementation(
                      @NonNull final MainActivity target) {
                    SessionImplementation session = new SessionImplementation(target.browserUi,target.secondScoped,target.scopedString);
                    return session;
                  }
                
                  private static final Logger provideLogger(@NonNull final MainActivity target) {
                    Logger logger = new Logger(target.browserUi,target.scopedString);
                    return logger;
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, sessionImpl, moduleScoped, secondScoped, logger, session, browserUi, phoneBrowserUi))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun localScopeOnGetterAndAbstractMethod() {

        val session = JavaFileObjects.forSourceLines("test.Session",
            "package test;",
            "public interface Session {",
            "}")

        val abstractDep = JavaFileObjects.forSourceLines("test.AbstractDep",
            "package test;",
            "public interface AbstractDep {",
            "}")

        val dep = JavaFileObjects.forSourceLines("test.Dep",
            "package test;",
            "public class Dep implements AbstractDep {",
            "}")

        val sessionImpl = JavaFileObjects.forSourceLines("test.SessionImplementation",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class SessionImplementation implements Session {",
            "   SessionImplementation(BrowserUi browserUi, AbstractDep scopedAbstractDep) {}",
            "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            importDependencyAnnotation,
            "@Dependency",
            "public class Logger {",
            "   Logger(BrowserUi browserUi, AbstractDep scopedAbstractDep) {}",
            "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
            "package test;",
            importAndroidContext,
            importLocalScopeAnnotation,
            "@LocalScope",
            "public abstract class BrowserUi {",
            "   BrowserUi(Context context) {}",
            "}")

        val moduleScoped = JavaFileObjects.forSourceLines("test.ModuleScoped",
            "package test;",
            importDependencyAnnotation,
            importLocalScopeAnnotation,
            "public interface ModuleScoped {",
            "   @LocalScope",
            "   @Dependency",
            "   public AbstractDep provideLocalAbstractDep(Dep dep);",
            "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
            "package test;",
            importAndroidContext,
            importDependencyAnnotation,

            "@Dependency",
            "public class PhoneBrowserUi extends BrowserUi {",
            "   PhoneBrowserUi(Context context) { super(context); }",
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
            "   public Session session;",

            "   @Inject",
            "   public Logger logger;",

            "   @Inject",
            "   public BrowserUi browserUi;",
            "   @Inject",
            "   public AbstractDep scopedAbstractDep;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            """
                package test;

                $importKeepAnnotation
                $importNonNullAnnotation
                
                @Keep
                public final class MainActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final MainActivity target) {
                    target.scopedAbstractDep = new Dep();
                    target.browserUi = providePhoneBrowserUi(target);
                    target.session = provideSessionImplementation(target);
                    target.logger = provideLogger(target);
                  }
                
                  private static final PhoneBrowserUi providePhoneBrowserUi(@NonNull final MainActivity target) {
                    PhoneBrowserUi browserUi = new PhoneBrowserUi(target);
                    return browserUi;
                  }
                
                  private static final SessionImplementation provideSessionImplementation(
                      @NonNull final MainActivity target) {
                    SessionImplementation session = new SessionImplementation(target.browserUi,target.scopedAbstractDep);
                    return session;
                  }
                
                  private static final Logger provideLogger(@NonNull final MainActivity target) {
                    Logger logger = new Logger(target.browserUi,target.scopedAbstractDep);
                    return logger;
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, sessionImpl, abstractDep, dep, moduleScoped, logger, session, browserUi, phoneBrowserUi))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun multipleInjection() {

        val context = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "public class Context {",
            "}")

        val session = JavaFileObjects.forSourceLines("test.Session",
            "package test;",
            "public class Session {",
            "   Session(Context context) {}",
            "}")

        val secondScoped = JavaFileObjects.forSourceLines("test.SecondScoped",
            "package test;",
            importInjectAnnotation,
            importLocalScopeAnnotation,
            "public class SecondScoped {",
            "   @Inject",
            "   public Session session;",
            "   @LocalScope",
            "   public Context context() { return null; }",
            "}")

        val thirdScoped = JavaFileObjects.forSourceLines("test.ThirdScoped",
            "package test;",
            importInjectAnnotation,
            importLocalScopeAnnotation,
            "public class ThirdScoped {",
            "   @Inject",
            "   public Session session;",
            "   @LocalScope",
            "   public Context localContext;",
            "}")


        val injectedFile1 = JavaFileObjects.forSourceLines("test.ThirdScopedInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                
                @Keep
                public final class ThirdScopedInjector {
                  @Keep
                  public static final void inject(@NonNull final ThirdScoped target) {
                    target.session = provideSession(target);
                  }
                
                  private static final Session provideSession(@NonNull final ThirdScoped target) {
                    Session session = new Session(target.localContext);
                    return session;
                  }
                }
            """.trimIndent())

        val injectedFile2 = JavaFileObjects.forSourceLines("test.SecondScopedInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                
                @Keep
                public final class SecondScopedInjector {
                  @Keep
                  public static final void inject(@NonNull final SecondScoped target) {
                    target.session = provideSession(target);
                  }
                
                  private static final Session provideSession(@NonNull final SecondScoped target) {
                    Session session = new Session(target.context());
                    return session;
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(context, secondScoped, thirdScoped, session))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile1)

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(context, secondScoped, thirdScoped, session))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile2)
    }
}