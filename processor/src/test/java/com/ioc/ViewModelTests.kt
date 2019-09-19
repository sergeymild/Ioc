package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.tools.JavaFileObject

@RunWith(JUnit4::class)
class ViewModelTests {
    private val androidViewModel = JavaFileObjects.forSourceLines("$lifecyclePackage.ViewModel",
        "package $lifecyclePackage;",
        "",
        "public class ViewModel {",
        "   public ViewModel() {};",
        "}")

    private val androidLiveData = JavaFileObjects.forSourceLines("$lifecyclePackage.LiveData",
        "package $lifecyclePackage;",
        "",
        "public class LiveData<T> {",
        "   public void observe(LifecycleOwner owner, Observer<? super T> observer) {}",
        "   public void observeForever(Observer<? super T> observer) {}",
        "}")

    private val androidMutableLiveData = JavaFileObjects.forSourceLines("$lifecyclePackage.MutableLiveData",
        "package $lifecyclePackage;",
        "public class MutableLiveData<T> extends LiveData<T> {",
        "}")

    private val androidLifecycleOwner = JavaFileObjects.forSourceLines("$lifecyclePackage.LifecycleOwner",
        "package $lifecyclePackage;",
        "",
        "public interface LifecycleOwner {",
        "}")

    private val androidLiveDataObserver = JavaFileObjects.forSourceLines("$lifecyclePackage.Observer",
        "package $lifecyclePackage;",
        "",
        "public interface Observer<T> {",
        "   void onChanged(T t);",
        "}")

    private val androidViewModelProvider = JavaFileObjects.forSourceLines(viewModelProvider,
        "package $lifecyclePackage;",
        "import java.lang.Class;",
        "",
        "public class ViewModelProvider {",
        "   public ViewModelProvider() {};",
        "   public interface Factory {",
        "       <T extends ViewModel> T create(Class<T> modelClass);",
        "   }",
        "",
        "   public <T extends ViewModel> T get(Class<T> modelClass) { return null; }",
        "}")

    private val androidViewModelProviders = JavaFileObjects.forSourceLines(viewModelProviders,
        "package $lifecyclePackage;",
        "import $fragmentActivityPackage.FragmentActivity;",
        "import $lifecyclePackage.ViewModelProvider.Factory;",
        "",
        "public class ViewModelProviders {",
        "   public static ViewModelProvider of(FragmentActivity fragment, Factory factory) { return null; }",
        "}")

    private val androidFragmentActivity = JavaFileObjects.forSourceLines("$fragmentActivityPackage.FragmentActivity",
        "package $fragmentActivityPackage;",
        "import $lifecyclePackage.LifecycleOwner;",
        "",
        "public class FragmentActivity implements LifecycleOwner {",

        "}")

    private val androidAppCompatActivity = JavaFileObjects.forSourceLines("androidx.appcompat.app.AppCompatActivity",
        "package androidx.appcompat.app;",
        "import $fragmentActivityPackage.FragmentActivity;",
        "",
        "public class AppCompatActivity extends FragmentActivity {",
        "}")

    private val androidFragment = JavaFileObjects.forSourceLines("androidx.fragment.app.Fragment",
        "package androidx.fragment.app;",
        "import $lifecyclePackage.LiveData;",
        "import $lifecyclePackage.LifecycleOwner;",
        "",
        "public class Fragment implements LifecycleOwner {",
        "   public LifecycleOwner getViewLifecycleOwner() { return null; }",
        "}")


    private val dependencyWithEmptyConstructor = JavaFileObjects.forSourceLines("test.DependencyWithEmptyConstructor",
        "package test;",
        "",
        "public class DependencyWithEmptyConstructor {}")

    @Test
    @Throws(Exception::class)
    fun viewModel() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public ActivityViewModel(DependencyModel dependency) {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            importKeepAnnotation,
            importNonNullAnnotation,
            "import $lifecyclePackage.ViewModel;",
            "import $viewModelProvider;",
            "import $viewModelProviders;",
            "import java.lang.Class;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.activityViewModel = provideActivityViewModel(target);",
            "   }",
            "",
            "   private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {",
            "       ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {",
            "           @NonNull",
            "           public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {",
            "               Context context = new Context();",
            "               DependencyModel dependencyModel = new DependencyModel(context);",
            "               return (T) new ActivityViewModel(dependencyModel);",
            "           }",
            "       };",
            "       ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);",
            "       return activityViewModel;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, androidLifecycleOwner, activityViewModel, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun viewModelWithLocalScopedArgument() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public ActivityViewModel(DependencyModel dependency) {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",

            "   @Inject",
            "   @LocalScope",
            "   public DependencyModel dependency;",
            "}")


        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            importKeepAnnotation,
            importNonNullAnnotation,
            "import $lifecyclePackage.ViewModel;",
            "import $viewModelProvider;",
            "import $viewModelProviders;",
            "import java.lang.Class;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new DependencyModel();",
            "       target.activityViewModel = provideActivityViewModel(target);",
            "   }",
            "",
            "   private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {",
            "       ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {",
            "           @NonNull",
            "           public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {",
            "               return (T) new ActivityViewModel(target.dependency);",
            "           }",
            "       };",
            "       ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);",
            "       return activityViewModel;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, androidLifecycleOwner, activityViewModel, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverFromViewModel() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "import java.lang.String;",
            "import java.lang.Integer;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   private MutableLiveData<Integer> integerLiveData = new MutableLiveData<Integer>();",
            "   public MutableLiveData<Integer> getIntegerLiveData() { return null; }",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   public void observeStringLiveData(String data) {}",
            "   @DataObserver",
            "   public void observeIntegerLiveData(Integer data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import androidx.lifecycle.ViewModel;
                import androidx.lifecycle.ViewModelProvider;
                import androidx.lifecycle.ViewModelProviders;
                import java.lang.Class;
                import java.lang.Integer;
                import java.lang.String;
                
                @Keep
                public final class ActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final Activity target) {
                    target.activityViewModel = provideActivityViewModel(target);
                    observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(target);
                    observeMutableLiveDataIntegerFromActivityViewModelByObserveIntegerLiveData(target);
                  }
                
                  private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {
                    ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {
                      @NonNull
                      public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                        return (T) new ActivityViewModel();
                      }
                    };
                    ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);
                    return activityViewModel;
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(
                      @NonNull final Activity target) {
                    target.activityViewModel.stringLiveData.observe(target, new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                
                  private static final void observeMutableLiveDataIntegerFromActivityViewModelByObserveIntegerLiveData(
                      @NonNull final Activity target) {
                    target.activityViewModel.getIntegerLiveData().observe(target, new Observer<Integer>() {
                      public void onChanged(Integer observingData) {
                        target.observeIntegerLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverFromGetterViewModel() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "import java.lang.String;",
            "import java.lang.Integer;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   private ActivityViewModel activityViewModel;",
            "   public ActivityViewModel getViewModel() { return null; };",
            "   public void setViewModel(ActivityViewModel viewModel) {};",
            "   @DataObserver",
            "   public void observeStringLiveData(String data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import androidx.lifecycle.ViewModel;
                import androidx.lifecycle.ViewModelProvider;
                import androidx.lifecycle.ViewModelProviders;
                import java.lang.Class;
                import java.lang.String;
                
                @Keep
                public final class ActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final Activity target) {
                    target.setViewModel(provideActivityViewModel(target));
                    observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(target);
                  }
                
                  private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {
                    ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {
                      @NonNull
                      public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                        return (T) new ActivityViewModel();
                      }
                    };
                    ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);
                    return activityViewModel;
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(
                      @NonNull final Activity target) {
                    target.getViewModel().stringLiveData.observe(target, new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverFromNamed() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            importQualifierAnnotation,
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   @Qualifier(\"fromFirstViewModel\")",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "}")

        val activityViewModel2 = JavaFileObjects.forSourceLines("test.ActivityViewModel2",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            importQualifierAnnotation,
            "",
            "public class ActivityViewModel2 extends ViewModel {",
            "   @Qualifier(\"fromSecondViewModel\")",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            importQualifierAnnotation,
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @Inject",
            "   public ActivityViewModel2 activityViewModel2;",
            "   @DataObserver",
            "   @Qualifier(\"fromSecondViewModel\")",
            "   public void observeStringLiveData(String data) {}",
            "   @DataObserver",
            "   @Qualifier(\"fromFirstViewModel\")",
            "   public void observeStringLiveData2(String data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import androidx.lifecycle.ViewModel;
                import androidx.lifecycle.ViewModelProvider;
                import androidx.lifecycle.ViewModelProviders;
                import java.lang.Class;
                import java.lang.String;
                
                @Keep
                public final class ActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final Activity target) {
                    target.activityViewModel = provideActivityViewModel(target);
                    target.activityViewModel2 = provideActivityViewModel2(target);
                    observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData2(target);
                    observeMutableLiveDataStringFromActivityViewModel2ByObserveStringLiveData(target);
                  }
                
                  private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {
                    ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {
                      @NonNull
                      public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                        return (T) new ActivityViewModel();
                      }
                    };
                    ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);
                    return activityViewModel;
                  }
                
                  private static final ActivityViewModel2 provideActivityViewModel2(
                      @NonNull final Activity target) {
                    ViewModelProvider.Factory factory_activityViewModel2 = new ViewModelProvider.Factory() {
                      @NonNull
                      public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                        return (T) new ActivityViewModel2();
                      }
                    };
                    ActivityViewModel2 activityViewModel2 = ViewModelProviders.of(target, factory_activityViewModel2).get(ActivityViewModel2.class);
                    return activityViewModel2;
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData2(
                      @NonNull final Activity target) {
                    target.activityViewModel.stringLiveData.observe(target, new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData2(observingData);
                      }
                    });
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModel2ByObserveStringLiveData(
                      @NonNull final Activity target) {
                    target.activityViewModel2.stringLiveData.observe(target, new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, activityViewModel2, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverNotMutable() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "import $lifecyclePackage.LiveData;",
            importQualifierAnnotation,
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   private MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public LiveData<Integer> getIntegerLiveData() { return null; }",
            "   public LiveData<String> getStringLiveData() { return null; }",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            importQualifierAnnotation,
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   public void observeStringLiveData(String data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import androidx.lifecycle.ViewModel;
                import androidx.lifecycle.ViewModelProvider;
                import androidx.lifecycle.ViewModelProviders;
                import java.lang.Class;
                import java.lang.String;
                
                @Keep
                public final class ActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final Activity target) {
                    target.activityViewModel = provideActivityViewModel(target);
                    observeLiveDataStringFromActivityViewModelByObserveStringLiveData(target);
                  }
                
                  private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {
                    ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {
                      @NonNull
                      public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                        return (T) new ActivityViewModel();
                      }
                    };
                    ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);
                    return activityViewModel;
                  }
                
                  private static final void observeLiveDataStringFromActivityViewModelByObserveStringLiveData(
                      @NonNull final Activity target) {
                    target.activityViewModel.getStringLiveData().observe(target, new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverForever() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            "import $iocDataObserver.ObserveType;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver(DataObserver.ObserveType.FOREVER)",
            "   public void observeStringLiveData(String data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import androidx.lifecycle.ViewModel;
                import androidx.lifecycle.ViewModelProvider;
                import androidx.lifecycle.ViewModelProviders;
                import java.lang.Class;
                import java.lang.String;
                
                @Keep
                public final class ActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final Activity target) {
                    target.activityViewModel = provideActivityViewModel(target);
                    observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(target);
                  }
                
                  private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {
                    ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {
                      @NonNull
                      public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                        return (T) new ActivityViewModel();
                      }
                    };
                    ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);
                    return activityViewModel;
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(
                      @NonNull final Activity target) {
                    target.activityViewModel.stringLiveData.observeForever(new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverForever2() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            "import $iocDataObserver.ObserveType;",
            "import $lifecyclePackage.LifecycleOwner;",
            importViewModelAnnotation,

            "public class Activity implements LifecycleOwner {",
            "",
            "   @Inject",
            "   @ViewModel",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver(DataObserver.ObserveType.FOREVER)",
            "   public void observeStringLiveData(String data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import java.lang.String;
                
                @Keep
                public final class ActivityInjector {
                  @Keep
                  public static final void inject(@NonNull final Activity target) {
                    target.activityViewModel = new ActivityViewModel();
                    observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(target);
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(
                      @NonNull final Activity target) {
                    target.activityViewModel.stringLiveData.observeForever(new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverInFragment() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.AppFragment",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            "import $lifecyclePackage.LifecycleOwner;",
            "import androidx.fragment.app.Fragment;",
            importViewModelAnnotation,

            "public class AppFragment extends Fragment {",
            "",
            "   @Inject",
            "   @ViewModel",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   public void observeStringLiveData(String data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.AppFragmentInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import java.lang.String;
                
                @Keep
                public final class AppFragmentInjector {
                  @Keep
                  public static final void inject(@NonNull final AppFragment target) {
                    target.activityViewModel = new ActivityViewModel();
                    observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(target);
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(
                      @NonNull final AppFragment target) {
                    target.activityViewModel.stringLiveData.observe(target.getViewLifecycleOwner(), new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidFragment, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun liveDataObserverForeverInFragment() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.AppFragment",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocLocalScope;",
            "import $iocDataObserver;",
            "import $lifecyclePackage.LifecycleOwner;",
            "import $iocDataObserver.ObserveType;",
            "import androidx.fragment.app.Fragment;",
            importViewModelAnnotation,

            "public class AppFragment extends Fragment {",
            "",
            "   @Inject",
            "   @ViewModel",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver(DataObserver.ObserveType.FOREVER)",
            "   public void observeStringLiveData(String data) {}",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.AppFragmentInjector",
            """
                package test;

                import androidx.annotation.Keep;
                import androidx.annotation.NonNull;
                import androidx.lifecycle.Observer;
                import java.lang.String;
                
                @Keep
                public final class AppFragmentInjector {
                  @Keep
                  public static final void inject(@NonNull final AppFragment target) {
                    target.activityViewModel = new ActivityViewModel();
                    observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(target);
                  }
                
                  private static final void observeMutableLiveDataStringFromActivityViewModelByObserveStringLiveData(
                      @NonNull final AppFragment target) {
                    target.activityViewModel.stringLiveData.observeForever(new Observer<String>() {
                      public void onChanged(String observingData) {
                        target.observeStringLiveData(observingData);
                      }
                    });
                  }
                }
            """.trimIndent())

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidFragment, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun failLiveDataObserverMethodIsPrivate() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "import java.lang.String;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocDataObserver;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   private void observeStringLiveData(String data) {}",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("method test.Activity.observeStringLiveData(java.lang.String) annotated with @DataObserver must be public.")
            .`in`(activityFile)
            .onLine(6)
    }

    @Test
    @Throws(Exception::class)
    fun failLiveDataObserverMethodContainsMoreThanOneParameter() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocDataObserver;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   public void observeStringLiveData(String data, Integer another) {}",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("method test.Activity.observeStringLiveData(java.lang.String,java.lang.Integer) annotated with @DataObserver must contains only one parameter.")
            .`in`(activityFile)
            .onLine(6)
    }

    @Test
    @Throws(Exception::class)
    fun failLiveDataObserverMethodContainsReturnType() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocDataObserver;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   public Integer observeStringLiveData(String data) { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("method test.Activity.observeStringLiveData(java.lang.String) annotated with @DataObserver must not contains return type.")
            .`in`(activityFile)
            .onLine(6)
    }

    @Test
    @Throws(Exception::class)
    fun failLiveDataObserverMethodWithoutLiveData() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocDataObserver;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   public void observeStringLiveData(String data) { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("test.Activity contains methods [observeStringLiveData] which annotated as @DataObserver but didn't find any view models with LiveData.")
            .`in`(activityFile)
            .onLine(6)
    }

    @Test
    @Throws(Exception::class)
    fun wrongTargetForDataObserver() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocDataObserver;",

            "public class Activity {",
            "   @Inject",
            "   public DependencyWithEmptyConstructor dependencyWithEmptyConstructor;",
            "   @DataObserver",
            "   public void observeStringLiveData(String data) { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, dependencyWithEmptyConstructor))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@DataObserver methods may be placed only in Activity or Fragment but was found in test.Activity.")
            .`in`(activityFile)
            .onLine(5)
    }

    @Test
    @Throws(Exception::class)
    fun failAmbiguousDataObserver() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocDataObserver;",

            "public class Activity {",
            "   @Inject",
            "   public DependencyWithEmptyConstructor dependencyWithEmptyConstructor;",
            "   @DataObserver",
            "   public void observeStringLiveData(String data) { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, dependencyWithEmptyConstructor))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@DataObserver methods may be placed only in Activity or Fragment but was found in test.Activity.")
            .`in`(activityFile)
            .onLine(5)
    }

    @Test
    @Throws(Exception::class)
    fun failEmptyParameters() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "import $lifecyclePackage.MutableLiveData;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public MutableLiveData<String> stringLiveData = new MutableLiveData<String>();",
            "   public ActivityViewModel() {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "import $iocDataObserver;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "   @DataObserver",
            "   public void observeStringLiveData() {}",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, androidMutableLiveData, androidLiveData, androidLiveDataObserver, androidLifecycleOwner))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("method test.Activity.observeStringLiveData() annotated with @DataObserver must contains only one parameter.")
            .`in`(activityFile)
            .onLine(6)
    }
}