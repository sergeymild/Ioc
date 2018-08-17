package com.ioc;

import android.content.Context;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import javax.inject.Inject;
import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static com.ioc.Helpers.importType;

/**
 * Created by sergeygolishnikov on 09/01/2018.
 */
@RunWith(JUnit4.class)
public class CycleTest {
//    @Test
//    public void cyclicInConstructors() throws Exception {
//        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class Activity {",
//                "",
//                "   @Inject",
//                "   public DependencyModel dependency;",
//                "}");
//
//        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class DependencyModel {",
//                "",
//                "   @Inject",
//                "   public DependencyModel(ParentDependencyModel parent) {}",
//                "",
//                "}");
//
//        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependencyModel",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class ParentDependencyModel {",
//                "",
//                "   @Inject",
//                "   public ParentDependencyModel(DependencyModel child) {}",
//                "}");
//
//        assertAbout(javaSources())
//                .that(Arrays.asList(activityFile, parentDependencyFile, dependencyFile))
//                .processedWith(new IProcessor())
//                .failsToCompile()
//                .withErrorContaining("Cyclic graph detected building test.ParentDependencyModel cyclic: test.DependencyModel")
//                .in(dependencyFile)
//                .onLine(8);
//    }
//
//    @Test
//    public void cyclicInConstructorsWithField() throws Exception {
//        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class Activity {",
//                "",
//                "   @Inject",
//                "   public DependencyModel dependency;",
//                "}");
//
//        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class DependencyModel {",
//                "",
//                "   @Inject",
//                "   public ParentDependencyModel parent;",
//                "",
//                "}");
//
//        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependencyModel",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class ParentDependencyModel {",
//                "",
//                "   @Inject",
//                "   public ParentDependencyModel(DependencyModel dependency) {}",
//                "}");
//
//        assertAbout(javaSources())
//                .that(Arrays.asList(activityFile, parentDependencyFile, dependencyFile))
//                .processedWith(new IProcessor())
//                .failsToCompile()
//                .withErrorContaining("Cyclic graph detected building test.ParentDependencyModel cyclic: test.DependencyModel")
//                .in(dependencyFile)
//                .onLine(8);
//    }
//
//    @Test
//    public void breakCyclicWithLazy() throws Exception {
//        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class Activity {",
//                "",
//                "   @Inject",
//                "   public DependencyModel dependency;",
//                "}");
//
//        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
//                "package test;",
//                "",
//                importType(Inject.class),
//                importType(Lazy.class),
//                "",
//                "public class DependencyModel {",
//                "",
//                "   @Inject",
//                "   public Lazy<ParentDependencyModel> parent;",
//                "",
//                "}");
//
//        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependencyModel",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class ParentDependencyModel {",
//                "",
//                "   @Inject",
//                "   public ParentDependencyModel(DependencyModel dependency) {}",
//                "}");
//
//        assertAbout(javaSources())
//                .that(Arrays.asList(activityFile, dependencyFile, parentDependencyFile))
//                .processedWith(new IProcessor())
//                .compilesWithoutError();
//    }
//
//    @Test
//    public void noCyclic() throws Exception {
//        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class Activity {",
//                "",
//                "   @Inject",
//                "   public ParentDependency dependency;",
//                "}");
//
//        JavaFileObject parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class ParentDependency {",
//                "   @Inject",
//                "   public ParentDependency(Activity activity, DependencyModel dependency) {}",
//                "}");
//
//        JavaFileObject dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class DependencyModel {",
//                "   public DependencyModel() {}",
//                "}");
//
//        JavaFileObject injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
//                "package test;",
//                "",
//                importType(Keep.class),
//                importType(NonNull.class),
//                "",
//                "@Keep",
//                "public final class ActivityInjector {",
//                "   @Keep",
//                "   public final void inject(@NonNull final Activity target) {",
//                "       injectParentDependencyInDependency(target);",
//                "   }",
//                "",
//                "   private final void injectParentDependencyInDependency(@NonNull final Activity target) {",
//                "       DependencyModel dependencyModel = new DependencyModel();",
//                "       ParentDependency parentDependency = new ParentDependency(target, dependencyModel);",
//                "       target.dependency = parentDependency;",
//                "   }",
//                "}");
//
//        assertAbout(javaSources())
//                .that(Arrays.asList(activityFile, dependencyFile, parentDependencyFile))
//                .processedWith(new IProcessor())
//                .compilesWithoutError()
//                .and().generatesSources(injectedFile);
//    }
//
//    @Test
//    public void cyclicWithParent() throws Exception {
//        JavaFileObject musicManager = JavaFileObjects.forSourceLines("test.MusicManager",
//                "package test;",
//                "",
//                "public interface MusicManager {",
//                "}");
//
//        JavaFileObject musicNotificationManager = JavaFileObjects.forSourceLines("test.MusicNotificationManager",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public interface MusicNotificationManager {",
//                "}");
//
//
//        JavaFileObject notificationManager = JavaFileObjects.forSourceLines("test.NotificationManager",
//                "package test;",
//                "",
//                importType(Inject.class),
//                importType(Dependency.class),
//                "@Dependency",
//                "public class NotificationManager implements MusicNotificationManager {",
//                "   public NotificationManager(MusicManager musicManager) {}",
//                "}");
//
//        JavaFileObject webMusicManager = JavaFileObjects.forSourceLines("test.WebMusicManager",
//                "package test;",
//                "",
//                importType(Inject.class),
//                importType(Dependency.class),
//                "@Dependency",
//                "public class WebMusicManager implements MusicManager {",
//                "",
//                "   @Inject",
//                "   public MusicNotificationManager manager;",
//                "}");
//
//        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class Activity {",
//                "",
//                "   @Inject",
//                "   public MusicManager musicManager;",
//                "}");
//
//        assertAbout(javaSources())
//                .that(Arrays.asList(activityFile, musicManager, notificationManager, musicNotificationManager, webMusicManager))
//                .processedWith(new IProcessor())
//                .failsToCompile()
//                .withErrorContaining("Cyclic graph detected building test.NotificationManager cyclic: test.MusicManager")
//                .in(notificationManager)
//                .onLine(6);
//    }
//
//    @Test
//    public void cyclicWithParent2() throws Exception {
//        JavaFileObject browserUiCallback = JavaFileObjects.forSourceLines("test.BrowserUiCallback",
//                "package test;",
//                "",
//                "public interface BrowserUiCallback {",
//                "}");
//
//        JavaFileObject browserUiCallbackImplementation = JavaFileObjects.forSourceLines("test.BrowserUiCallbackImplementation",
//                "package test;",
//                importType(Dependency.class),
//                "@Dependency",
//                "public class BrowserUiCallbackImplementation implements BrowserUiCallback {",
//                "   public BrowserUiCallbackImplementation(VideoPlayerManager manager) {}",
//                "}");
//
//        JavaFileObject musicNotificationManager = JavaFileObjects.forSourceLines("test.VideoPlayerManager",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class VideoPlayerManager {",
//                "   public VideoPlayerManager(BrowserUi browserUi) {}",
//                "}");
//
//
//        JavaFileObject browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
//                "package test;",
//                "",
//                "public interface BrowserUi {",
//                "}");
//
//        JavaFileObject phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
//                "package test;",
//                "",
//                importType(Inject.class),
//                importType(Dependency.class),
//                "@Dependency",
//                "public class PhoneBrowserUi implements BrowserUi {",
//                "   public PhoneBrowserUi(BrowserUiCallback callbacks) {}",
//                "}");
//
//        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
//                "package test;",
//                "",
//                importType(Inject.class),
//                "",
//                "public class Activity {",
//                "",
//                "   @Inject",
//                "   public BrowserUiCallback callbacks;",
//                "}");
//
//        assertAbout(javaSources())
//                .that(Arrays.asList(activityFile, browserUiCallback, browserUiCallbackImplementation, musicNotificationManager, browserUi, phoneBrowserUi))
//                .processedWith(new IProcessor())
//                .failsToCompile()
//                .withErrorContaining("Cyclic graph detected building test.PhoneBrowserUi cyclic: test.BrowserUiCallback")
//                .in(phoneBrowserUi)
//                .onLine(6);
//    }


    @Test
    @Ignore
    public void cyclicInModuleDependency() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
                "package test;",
                "",
                importType(Inject.class),
                "",
                "public class Activity {",
                "",
                "   @Inject",
                "   public LocaleHelper localeHelper;",
                "}");

        JavaFileObject localeHelper = JavaFileObjects.forSourceLines("test.LocaleHelper",
                "package test;",

                "class LocaleHelper {",
                "   LocaleHelper(Preferences preferences, StringProvider stringProvider) {}",
                "",
                "}");

        JavaFileObject preferences = JavaFileObjects.forSourceLines("test.Preferences",
                "package test;",

                "class Preferences {",
                "   Preferences(ApplicationContextProvider context) {}",
                "",
                "}");

        JavaFileObject stringProvider = JavaFileObjects.forSourceLines("test.StringProvider",
                "package test;",

                "interface StringProvider {",
                "",
                "}");

        JavaFileObject applicationContextProvider = JavaFileObjects.forSourceLines("test.ApplicationContextProvider",
                "package test;",
                "",
                importType(Context.class),
                "",
                "interface ApplicationContextProvider {",
                "   Context context();",
                "}");

        JavaFileObject alohaStringProvider = JavaFileObjects.forSourceLines("test.AlohaStringProvider",
                "package test;",
                "",
                importType(Context.class),
                "",
                "class AlohaStringProvider implements StringProvider {",
                "   AlohaStringProvider(ApplicationContextProvider context) {}",
                "}");

        JavaFileObject applicationModule = JavaFileObjects.forSourceLines("test.ApplicationModule",
                "package test;",
                "",
                importType(Dependency.class),
                "",
                "public class ApplicationModule {",

                "   @Dependency",
                "   public static ApplicationContextProvider context(LocaleHelper locale) {}",
                "",
                "}");

        assertAbout(javaSources())
                .that(Arrays.asList(activityFile, localeHelper, preferences, stringProvider, applicationContextProvider, alohaStringProvider, applicationModule))
                .processedWith(new IProcessor())
                .failsToCompile()
                .withErrorContaining("Cyclic graph searching: test.ApplicationContextProvider parents: (test.Preferences, test.LocaleHelper) parameters: (test.LocaleHelper)");
    }

    @Test
    @Ignore
    public void cyclicInConstructorDependency() throws Exception {
        JavaFileObject activityFile = JavaFileObjects.forSourceLines("test.Activity",
                "package test;",
                "",
                importType(Inject.class),
                "",
                "public class Activity {",
                "",
                "   @Inject",
                "   public LocaleHelper localeHelper;",
                "}");

        JavaFileObject localeHelper = JavaFileObjects.forSourceLines("test.LocaleHelper",
                "package test;",

                "class LocaleHelper {",
                "   LocaleHelper(Preferences preferences, StringProvider stringProvider) {}",
                "",
                "}");

        JavaFileObject preferences = JavaFileObjects.forSourceLines("test.Preferences",
                "package test;",

                "class Preferences {",
                "   Preferences(ApplicationContextProvider context) {}",
                "",
                "}");

        JavaFileObject stringProvider = JavaFileObjects.forSourceLines("test.StringProvider",
                "package test;",

                "interface StringProvider {",
                "",
                "}");

        JavaFileObject applicationContextProvider = JavaFileObjects.forSourceLines("test.ApplicationContextProvider",
                "package test;",
                "",
                importType(Context.class),
                "",
                "interface ApplicationContextProvider {",
                "   Context context();",
                "}");

        JavaFileObject alohaStringProvider = JavaFileObjects.forSourceLines("test.AlohaStringProvider",
                "package test;",
                "",
                importType(Context.class),
                "",
                "class AlohaStringProvider implements StringProvider {",
                "   AlohaStringProvider(ApplicationContextProvider context) {}",
                "}");

        JavaFileObject applicationModule = JavaFileObjects.forSourceLines("test.AlohaApplicationContextProvider",
                "package test;",
                "",
                importType(Dependency.class),
                "",
                "@Dependency",
                "public class AlohaApplicationContextProvider implements ApplicationContextProvider {",

                "   public AlohaApplicationContextProvider(LocaleHelper locale) {}",
                "",
                "}");

        assertAbout(javaSources())
                .that(Arrays.asList(activityFile, localeHelper, preferences, stringProvider, applicationContextProvider, alohaStringProvider, applicationModule))
                .processedWith(new IProcessor())
                .failsToCompile()
                .withErrorContaining("Cyclic graph creating: test.AlohaApplicationContextProvider parents: (test.Preferences, test.LocaleHelper) parameters: (test.LocaleHelper)");
    }
}
