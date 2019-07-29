package com.ioc

import android.app.Activity
import android.content.Context
import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 28/12/2017.
 */
@RunWith(JUnit4::class)
class ScopesTest : BaseTest {


    /**
     *  NEW TESTS
     * */

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
            Dependency::class.java.import(),
            "@Dependency",
            "public class SessionImplementation implements Session {",
            "   SessionImplementation(BrowserUi browserUi, SecondScoped secondScoped) {}",
            "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "public class Logger {",
            "   Logger(BrowserUi browserUi) {}",
            "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
            "package test;",
            Context::class.java.import(),

            "public abstract class BrowserUi {",
            "   BrowserUi(Context context) {}",
            "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
            "package test;",
            Context::class.java.import(),
            Dependency::class.java.import(),

            "@Dependency",
            "public class PhoneBrowserUi extends BrowserUi {",
            "   PhoneBrowserUi(Context context) { super(context); }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            Inject::class.java.import(),
            LocalScope::class.java.import(),
            Activity::class.java.import(),
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
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MainActivity target) {",
            "       injectSecondScopedInSecondScoped(target);",
            "       injectBrowserUiInBrowserUi(target);",
            "       injectSessionInSession(target);",
            "       injectLoggerInLogger(target);",
            "   }",
            "",
            "   private final void injectSecondScopedInSecondScoped(@NonNull final MainActivity target) {",
            "       SecondScoped secondScoped = new SecondScoped();",
            "       target.setSecondScoped(secondScoped);",
            "   }",
            "",
            "   private final void injectBrowserUiInBrowserUi(@NonNull final MainActivity target) {",
            "       BrowserUi browserUi2 = new PhoneBrowserUi(target);",
            "       target.browserUi = browserUi2;",
            "   }",
            "",
            "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
            "       Session session = new SessionImplementation(target.browserUi, target.getSecondScoped());",
            "       target.session = session;",
            "   }",
            "",
            "   private final void injectLoggerInLogger(@NonNull final MainActivity target) {",
            "       Logger logger = new Logger(target.browserUi);",
            "       target.logger = logger;",
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
            Dependency::class.java.import(),
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
            LocalScope::class.java.import(),
            Inject::class.java.import(),
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
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class AddressBarListenerImplInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final AddressBarListenerImpl target) {",
            "       injectAutocompleteControllerInController(target);",
            "   }",
            "",
            "   private final void injectAutocompleteControllerInController(@NonNull final AddressBarListenerImpl target) {",
            "       AutoCompleteListener autoCompleteListener = new AutoCompleteListenerImpl(target.getAlohaBrowserUi());",
            "       AutocompleteController autocompleteController = new AutocompleteController(autoCompleteListener);",
            "       target.controller = autocompleteController;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(autoCompleteListener, autoCompleteListenerImpl, autocompleteController, logger, alohabrowser))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}