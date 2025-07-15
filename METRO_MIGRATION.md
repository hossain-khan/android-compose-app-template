# Metro 0.5.0 Migration Guide

This document outlines the changes and improvements made during the migration from Metro 0.4.0 to 0.5.0 in this Android Compose App Template.

## Migration Summary

The app has been successfully migrated from Metro 0.4.0 to 0.5.0 with full compatibility maintained. All existing Metro annotations and patterns continue to work unchanged.

## Metro 0.5.0 Key Improvements

### 1. Enhanced Nullable Binding Support
- **What changed**: Removed `Any` constraint from `binding<T>()` function
- **Benefits**: Allows bindings to satisfy nullable variants, improving type safety
- **Implementation**: No changes needed - existing `binding<Activity>()` usage benefits automatically

### 2. New @BindingContainer Feature (Experimental)
- **What's new**: Experimental support for organizing dependencies using `@BindingContainer`
- **Benefits**: Better code organization and cleaner separation of concerns
- **Implementation**: Added `DataBindingContainer.kt` as an example

### 3. Enhanced Diagnostics
- **What's new**: Additional compile-time checks for common mistakes
- **Benefits**: Catches issues early in development
- **Diagnostics added**:
  - Scoped `@Binds` declarations validation
  - Graph classes directly extending other graph classes
  - `@Assisted` parameters in provides functions
  - Duplicate `@Provides` declaration names

### 4. Better Multi-binding Support
- **What's new**: Enhanced validation and improved `allowEmpty` behavior
- **Benefits**: Better error messages and more robust multi-binding handling
- **Implementation**: Existing `@Multibinds` usage benefits automatically

### 5. Enhanced @AssistedFactory Support
- **What's new**: Better interop with kotlin-inject's `@AssistedFactory` annotations
- **Benefits**: Improved validation and error reporting
- **Implementation**: Existing Circuit presenter factories benefit automatically

### 6. Field Reports Generation
- **What's new**: Automatic generation of detailed field reports for debugging
- **Benefits**: Better insights into generated code with `keys-scopedProviderFields-*.txt` and `keys-providerFields-*.txt`
- **Implementation**: Generated automatically in build output (no configuration needed)

## Configuration Changes

### build.gradle.kts Updates

```kotlin
ksp {
    // Existing configuration
    arg("circuit.codegen.mode", "metro")
    arg("metro.enableScopedInjectClassHints", "true")
    
    // Metro 0.5.0 generates field reports automatically
    // Look for keys-scopedProviderFields-*.txt and keys-providerFields-*.txt in build output
}

metro {
    debug.set(true)
    // Added comprehensive documentation about 0.5.0 features
}
```

## Code Organization Improvements

### New Binding Container Example
- Added `DataBindingContainer.kt` demonstrating the new `@BindingContainer` feature
- Shows how to organize data layer dependencies more effectively
- Provides alternative to traditional `@ContributesTo` modules

### Enhanced Documentation
- Updated all Metro-related code with comprehensive comments
- Added Metro 0.5.0 feature explanations
- Documented benefits and improvements for each feature

## Compatibility Notes

### ✅ Fully Compatible
- All existing Metro annotations work unchanged
- No breaking changes in public API
- Existing dependency injection patterns continue to work

### ✅ Enhanced Features
- `@AssistedFactory` usage benefits from improved validation
- `@ContributesBinding` usage benefits from better diagnostics
- Multi-binding sets benefit from enhanced validation
- Activity injection benefits from improved `binding<T>()` function

### ✅ New Capabilities
- Optional use of `@BindingContainer` for better organization
- Enhanced debugging with field reports
- Better error messages and validation

## Testing

The migration maintains full backward compatibility. All existing functionality continues to work:
- Constructor injection via `@Inject`
- Activity injection via `@ContributesIntoMap`
- Multi-binding for Circuit presenter and UI factories
- Interface binding via `@ContributesBinding`
- Scoped dependencies with `@SingleIn`

## Next Steps

1. **Optional**: Consider adopting `@BindingContainer` for better dependency organization
2. **Optional**: Enable field reports for debugging insights
3. **Recommended**: Review new diagnostic messages during development
4. **Optional**: Explore new nullable binding capabilities if needed

## Resources

- [Metro 0.5.0 Release Notes](https://github.com/ZacSweers/metro/releases/tag/0.5.0)
- [Metro Documentation](https://zacsweers.github.io/metro/)
- [Binding Containers Guide](https://zacsweers.github.io/metro/dependency-graphs#binding-containers)