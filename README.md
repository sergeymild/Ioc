## Declaring Dependencies

###### Installation
Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}

```
Add this dependencies to your project's `build.gradle`.
```
compile 'com.github.sergeymild.ioc:core:1.1'
kapt 'com.github.sergeymild.ioc:processor:1.1'
```

Ioc uses the [javax.inject.Inject](https://docs.oracle.com/javaee/7/api/javax/inject/Inject.html) annotation to identify which constructors and fields it is interested in.

Use `@Inject` to annotate the constructor that Ioc should use to create instances of a class. When a new instance is requested, Ioc will obtain the required parameters values and invoke this constructor.
```
class Thermosiphon
@Inject
constructor(val heather: Heater): Pump {
  ...
}
```

Also if dependency have only one constructor with arguments, Ioc use this constructor for create instance as well.

Ioc can inject fields directly. In this example it obtains a Heater instance for the heater field and a Pump instance for the pump field.

```
class CoffeeMaker {
  @Inject
  lateinit var heater: Heater
  @Inject
  var pump: Pump? = null
  ...
}
```

If your class has `@Inject`-annotated fields but no `@Inject`-annotated constructor, Ioc will inject those fields with no-argument constructor.

Ioc also supports method injection, though constructor or field injection are typically preferred.

## Satisfying Dependencies

By default, Ioc satisfies each dependency by constructing an instance of the requested type as described above. When you request a CoffeeMaker, it’ll obtain one by calling `new CoffeeMaker()` and setting its injectable fields.

But `@Inject` doesn’t work everywhere:
1. Interfaces can’t be constructed.
2. Third-party classes can’t be annotated
3. Configurable objects must be configured!

For these cases where `@Inject` is insufficient or awkward, use an `@Dependency`-annotated `static` method to satisfy a dependency. The method’s return type defines which dependency it satisfies.

For example, `provideHeater()` is invoked whenever a Heater is required:
```
@Dependency
@JvmStatic
fun provideHeater(): Heater {
  return ElectricHeater()
}
```

It’s possible for `@Dependency` methods to have dependencies of their own. This one returns a Thermosiphon whenever a Pump is required:

All `@Dependency` methods must be `static` and placed anywhere.

```
class DripCoffeeModule {
  @Dependency
  @JvmStatic
  fun provideHeater(): Heater {
    return ElectricHeater()
  }

  @Dependency
  @JvmStatic
  fun providePump(pump: Thermosiphon): Pump {
    return pump
  }
}
```

Tou can declare `@Dependency` methods right in the file without class declaration and `@JvmStatic` annotation.

`DripCoffeeModule.kt`

```
@Dependency
fun provideHeater(): Heater {
    return ElectricHeater()
}

@Dependency
fun providePump(pump: Thermosiphon): Pump {
    return pump
}
```

Also you can define dependencies jus declaring `@Dependency` annotation on top of class.

```
@Dependency
class ElectricHeater {}
```

Ioc will use this as dependency.

## Singletons

You can Annotate an `@Dependency` method or injectable class with `@Singleton`. Ioc will use a single instance of the value for all of its clients.
```
@Depedency
@Singleton
static Heater provideHeater() {
  return new ElectricHeater();
}

@Singleton
@Dependency
class CoffeeMaker {
  ...
}
```

## Provider injections

Sometimes you need multiple instances to be returned instead of just injecting a single value. While you have several options (Factories, Builders, etc.), one option is to inject a `Provider<T>` instead of just `T`. A `Provider<T>` invokes the binding logic for `T` each time `.get()` is called.
```
class BigCoffeeMaker {
  @Inject Provider<Filter> filterProvider;

  public void brew(int numberOfPots) {
  ...
    for (int p = 0; p < numberOfPots; p++) {
      maker.addFilter(filterProvider.get()); //new filter every time.
      maker.addCoffee(...);
      maker.percolate();
      ...
    }
  }
}
```

## WeakReference injections

Sometimes you need weak instances.
```
class BigCoffeeMaker {
  @Inject
  lateinit var filterProvider: WeakReference<Filter>
}
```
Filter dependency will be injected as Weak reference.


## Named

Sometimes the type alone is insufficient to identify a dependency. For example, a sophisticated coffee maker app may want separate heaters for the water and the hot plate.

In this case, we add a `qualifier annotation`. This is any annotation that itself has a `@Qualifier` annotation. Here’s the declaration of `@Named`, a qualifier annotation included in [javax.inject.Inject](https://docs.oracle.com/javaee/7/api/javax/inject/Inject.html):
```
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Named {
  String value() default "";
}
```

You can create your own qualifier annotations, or just use @Named. Apply qualifiers by annotating the field or parameter of interest. The type and qualifier annotation will both be used to identify the dependency.

```
class ExpensiveCoffeeMaker {
  @Inject @Named("water") Heater waterHeater;
  @Inject @Named("hot plate") Heater hotPlateHeater;
  ...
}
```

Supply `@Named` values by annotating the corresponding `@Dependency` method.
```
@Depdendency
@Named("hot plate")
static Heater provideHotPlateHeater() {
  return new ElectricHeater(70);
}

@Depdendency
@Named("water")
static Heater provideWaterHeater() {
  return new ElectricHeater(93);
}
```

Or on top of dependency class.

```
@Depdendency
@Named("hot plate")
class ElectricHeater implements Heater {

}

@Depdendency
@Named("manualHeather")
class ManualHeater implements Heater {

}
```
Dependencies may not have multiple qualifier annotations.

### Target as dependency

If dependency needs parent as dependency or one of it's superclass or interface, you'll need create a method with @TargetDependency annotation.
```
class LoginActivity: AppCompatActivity() {
  @Inject
  lateinit var preferences: Preferences;

  @TargetDependency
  fun getLocalContext(): Context = this
}

@Dependency
class AndroidPreferences: Preferences {
    val sharedPreferences: SharedPreferences
    @Inject
    AndroidPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(STORAGE_NAME, Activity.MODE_PRIVATE)
    }
}
```
### View models

`android.arch.lifecycle.ViewModel` can also may be injected but only in `AppCompatActivity` or `android.support.v4.app.Fragment`
```
class LoginActivity: AppCompatActivity() {
  @Inject
  lateinit var loginViewModel: LoginViewModel;
}
class LoginInteractor
class LoginViewModel(val loginInteractor: LoginInteractor): android.arch.lifecycle.ViewModel {}
```

## Inject

To inject dependencies just call `Ioc.inject(target)` in constructor.

```
class ExpensiveCoffeeMaker {
  @Inject
  @Named("water")
  waterHeater: Heater;

  @Inject
  @Named("hot plate")
  hotPlateHeater: Heater;

  init {
    Ioc.inject(this)
  }
}
```

## Compile-time Code Generation

Ioc will generate class with target name + `Injector` suffix e.g. for `LoginActivity` will be generated class `LoginActivityInjector`.
After build project you can just find this class and see how `Ioc` inject dependencies.

Ioc uses reflection only to find generated `*Injector` class.
If you do not want this you can always call `inject` method directly.

```
class LoginActivity {
  @Inject
  lateinit var preferences: Preferences;

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LoginActivityInjector.inject(this)
  }
}
```

Generated class will be looks like:

```
@Keep
public final class LoginActivityInjector {
    @Keep
    public static final void inject(final LoginActivity target) {
        injectPreferencesInPreferences(target);
    }

    private static final void injectPreferencesInPreferences(final LoginActivity target) {
        target.setPreferences(AndroidPreferences(target));
    }
}
```

All generated code is human readable, you can always understand that's going on and how it's working.
