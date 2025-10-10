# 📋 Generic Refactoring Prompt: Add "Allow Retry" Parameter to Extension Catalog Requests

## Overview

This document provides a standardized approach for adding an optional "Allow Retry" boolean parameter to all `@CatalogRequest` methods across Krista extensions (Gmail, HubSpot, Salesforce, OneDrive, Google Drive, etc.). This feature provides fine-grained control over validation error retry behavior.

**Applicable Extensions:**
- Gmail Extension
- HubSpot Extension
- Salesforce Extension
- OneDrive Extension
- Google Drive Extension
- SharePoint Extension
- Teams Extension
- Any other extension with `@CatalogRequest` methods

---

## Objective

Add an optional `Boolean allowRetry` parameter to all methods annotated with `@CatalogRequest`. This parameter controls whether validation errors trigger an interactive SubCatalog retry flow or return immediate errors.

**Key Requirements:**
1. ✅ Maintain backward compatibility (default behavior: no retry)
2. ✅ Provide consistent API across all catalog requests
3. ✅ Enable telemetry tracking of retry behavior
4. ✅ Update version number
5. ✅ Update documentation and release notes

---

## Implementation Pattern

### 1. Parameter Specification

Add the following parameter as the **last parameter** to every `@CatalogRequest` method:

```java
@Field.Boolean(name = "Allow Retry", required = false, 
    attributes = {@Attribute(name = "visualWidth", value = "S")}) 
Boolean allowRetry
```

**Specifications:**
- **Type**: `Boolean` (nullable, not primitive boolean)
- **Name**: "Allow Retry"
- **Required**: `false`
- **Visual Width**: "S" (small)
- **Position**: Last parameter in method signature
- **Default Behavior**: When `null` or `false`, treat as `false` (no retry)

---

### 2. Implementation for Methods WITH Validation

**Current Pattern (Before):**
```java
@CatalogRequest(
    id = "...",
    name = "Method Name",
    area = "Area Name",
    type = CatalogRequest.Type.CHANGE_SYSTEM)
public ExtensionResponse methodName(
        @Field(name = "Parameter 1", type = "Text") String param1,
        @Field(name = "Parameter 2", type = "Text") String param2) {
    
    long startTime = System.currentTimeMillis();
    try {
        telemetryHelper.incrementCount("extension.methodName");
        
        List<ValidationResult> validationResults = 
            validationOrchestrator.validate(Map.of(...));
        
        if (validationResults.isEmpty()) {
            // Execute business logic
            return ExtensionResponseFactory.create(...);
        } else {
            // ALWAYS trigger SubCatalog confirmation flow
            telemetryHelper.recordRetryPrompted("extension.methodName", startTime, ...);
            return responseGenerator.generateConfirmationResponse(...);
        }
    } catch (Exception e) {
        // Error handling
    }
}
```

**New Pattern (After):**
```java
@CatalogRequest(
    id = "...",
    name = "Method Name",
    area = "Area Name",
    type = CatalogRequest.Type.CHANGE_SYSTEM)
public ExtensionResponse methodName(
        @Field(name = "Parameter 1", type = "Text") String param1,
        @Field(name = "Parameter 2", type = "Text") String param2,
        @Field.Boolean(name = "Allow Retry", required = false,
                attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
    
    long startTime = System.currentTimeMillis();
    try {
        LOGGER.info("Executing methodName with param1: {}, param2: {}, allowRetry: {}", 
                    param1, param2, allowRetry);
        
        telemetryHelper.incrementCount("extension.methodName");
        
        List<ValidationResult> validationResults = 
            validationOrchestrator.validate(Map.of(...));
        
        if (validationResults.isEmpty()) {
            // Execute business logic
            telemetryHelper.recordSuccess("extension.methodName", startTime,
                    safeTagMap("param1", param1, "param2", param2, 
                              "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create(...);
        } else {
            // NEW: Conditional retry logic based on allowRetry parameter
            if (Boolean.TRUE.equals(allowRetry)) {
                // Trigger SubCatalog confirmation flow
                telemetryHelper.recordRetryPrompted("extension.methodName", startTime,
                        safeTagMap("param1", param1, "param2", param2,
                                  "allow_retry", String.valueOf(allowRetry),
                                  "validation_count", String.valueOf(validationResults.size())));
                
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON.toJson(Map.of(
                        "PARAM1", param1,
                        "VALIDATION_RESULTS", validationResults)));
                
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_METHOD_NAME,
                        Map.of("STATE_ID", stateId, "PARAM1", param1, "PARAM2", param2));
            } else {
                // Return immediate error without retry option
                telemetryHelper.recordValidationError("extension.methodName", startTime,
                        "Validation failed without retry",
                        safeTagMap("param1", param1, "param2", param2,
                                  "allow_retry", String.valueOf(allowRetry),
                                  "validation_count", String.valueOf(validationResults.size())));
                
                return responseGenerator.generateFetchDenyResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        null,
                        Map.of());
            }
        }
    } catch (MustAuthorizeException cause) {
        telemetryHelper.recordValidationError("extension.methodName", startTime, 
                cause.getMessage(),
                safeTagMap("param1", param1, "param2", param2,
                          "allow_retry", String.valueOf(allowRetry)));
        return handleAuthorizationException(cause, requestContext.invokeAsUser());
    } catch (Exception cause) {
        LOGGER.error("Error occurred in methodName: {}", cause.getMessage());
        telemetryHelper.recordError("extension.methodName", startTime, cause,
                safeTagMap("param1", param1, "param2", param2,
                          "allow_retry", String.valueOf(allowRetry)));
        return ExtensionResponseFactory.create("Error occurred in methodName",
                ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                List.of(RemediationActionFactory.createInformActionALLParticipants(
                        "Error occurred in methodName", List.of())),
                null, null);
    }
}
```

**Key Changes:**
1. ✅ Add `allowRetry` parameter as last parameter
2. ✅ Add conditional logic: `if (Boolean.TRUE.equals(allowRetry))`
3. ✅ Call `generateConfirmationResponse()` when `allowRetry == true`
4. ✅ Call `generateFetchDenyResponse()` when `allowRetry == false/null`
5. ✅ Update all telemetry calls to include `"allow_retry"` tag
6. ✅ Update logging to include `allowRetry` parameter

---

### 3. Implementation for Methods WITHOUT Validation

For methods that do NOT have validation logic, still add the parameter for API consistency:

```java
@CatalogRequest(
    id = "...",
    name = "Method Name",
    area = "Area Name",
    type = CatalogRequest.Type.QUERY_SYSTEM)
public ExtensionResponse methodName(
        @Field.Boolean(name = "Allow Retry", required = false,
                attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
    
    long startTime = System.currentTimeMillis();
    try {
        telemetryHelper.incrementCount("extension.methodName");
        
        // No validation logic - parameter is ignored
        // Execute business logic directly
        
        telemetryHelper.recordSuccess("extension.methodName", startTime,
                safeTagMap("allow_retry", String.valueOf(allowRetry)));
        
        return ExtensionResponseFactory.create(...);
    } catch (Exception e) {
        // Error handling
    }
}
```

**Note**: The parameter is added but ignored since there's no validation logic.

---

## Telemetry Updates

### Required Telemetry Changes

Update ALL telemetry calls to include the `allow_retry` tag:

**Success Scenario:**
```java
telemetryHelper.recordSuccess("extension.methodName", startTime,
        safeTagMap("param1", value1, "param2", value2,
                  "allow_retry", String.valueOf(allowRetry)));
```

**Retry Prompted Scenario:**
```java
telemetryHelper.recordRetryPrompted("extension.methodName", startTime,
        safeTagMap("param1", value1, "param2", value2,
                  "allow_retry", String.valueOf(allowRetry),
                  "validation_count", String.valueOf(validationResults.size())));
```

**Validation Error Scenario:**
```java
telemetryHelper.recordValidationError("extension.methodName", startTime,
        "Validation failed without retry",
        safeTagMap("param1", value1, "param2", value2,
                  "allow_retry", String.valueOf(allowRetry),
                  "validation_count", String.valueOf(validationResults.size())));
```

**Error Scenario:**
```java
telemetryHelper.recordError("extension.methodName", startTime, cause,
        safeTagMap("param1", value1, "param2", value2,
                  "allow_retry", String.valueOf(allowRetry)));
```

---

## Version Update

### Update Extension Version

Locate your extension's main class (e.g., `GmailExtension.java`, `HubSpotExtension.java`, etc.) and increment the version:

**Before:**
```java
@Extension(version = "X.Y.Z", name = "ExtensionName")
public class ExtensionNameExtension {
    // ...
}
```

**After:**
```java
@Extension(version = "X.Y.(Z+1)", name = "ExtensionName")
public class ExtensionNameExtension {
    // ...
}
```

**Example:**
- `2.5.10` → `2.5.11`
- `3.0.15` → `3.0.16`
- `1.2.3` → `1.2.4`

---

## Documentation Updates

### 1. Update Catalog Request Documentation Files

For each catalog request, update the corresponding `.md` file:

**Location**: `src/main/resources/docs/pages/`

#### Add to Input Parameters Table:

```markdown
| Parameter Name | Type    | Required | Description                                                    | Example |
|----------------|---------|----------|----------------------------------------------------------------|---------|
| Allow Retry    | Boolean | No       | Enable retry prompt on validation failure (default: false)     | true    |
```

#### Add Parameter Details Section:

```markdown
#### Allow Retry

- **Type**: Boolean (true/false)
- **Default**: false (if not provided or null)
- **Purpose**: Controls whether user is prompted to retry on validation errors
- **Behavior**:
  - **true**: On validation failure, system prompts user to correct input and retry
  - **false** or **null**: On validation failure, system returns error immediately without retry prompt
- **Use Cases**:
  - Set to `true` for interactive user workflows where correction is expected
  - Set to `false` for automated processes where immediate error handling is preferred
- **Validation Flow**: Only applies when input validation fails; has no effect on successful operations
- **Available Since**: Version X.Y.Z
```

#### Add Usage Examples:

```markdown
### Example: Operation with Retry Enabled

**Input**:
\`\`\`
Parameter 1: "value1"
Parameter 2: "value2"
Allow Retry: true
\`\`\`

**Behavior**: If validation fails, user will be prompted to correct the input and retry.

### Example: Operation with Retry Disabled (Default)

**Input**:
\`\`\`
Parameter 1: "value1"
Parameter 2: "value2"
Allow Retry: false
\`\`\`

**Behavior**: If validation fails, error is returned immediately without retry option.
```

---

### 2. Update Release Notes

**Location**: `src/main/resources/docs/pages/release-notes.md`

Add a new version section:

```markdown
## Version X.Y.Z - Current Release

- **Release Date**: [Month Year]
- **Developer**: [Your Name]

### New Features

#### Allow Retry Parameter for All Catalog Requests

- **Feature**: Added optional "Allow Retry" boolean parameter to all @CatalogRequest methods
- **Purpose**: Provides fine-grained control over validation error retry behavior
- **Default**: false (maintains backward compatibility)
- **Impact**: All catalog requests now support retry configuration

**Benefits:**
- **Interactive Workflows**: Set to `true` for user-driven operations requiring input correction
- **Automated Workflows**: Set to `false` (default) for batch processing with immediate error handling
- **Backward Compatible**: Existing integrations continue to work without changes
- **Improved UX**: Users can choose between immediate errors or interactive retry prompts

### Improvements

- **Enhanced Error Handling**: Validation errors now support both immediate return and interactive retry flows
- **Consistent API**: All catalog requests now have uniform parameter structure
- **Better Telemetry**: Added `allow_retry` tag to all retry-related telemetry metrics
- **Documentation**: Comprehensive documentation updates for all catalog requests with retry examples

### Technical Details

- **Parameter Type**: Boolean (nullable)
- **Parameter Name**: "Allow Retry"
- **Required**: false
- **Default Behavior**: When null or false, validation errors return immediately
- **Retry Flow**: When true, validation errors trigger SubCatalog confirmation prompts

### Migration Guide

**No Breaking Changes**: This release is fully backward compatible.

**For New Implementations:**
\`\`\`java
// Interactive user workflow - enable retry
methodName(param1, param2, true);

// Automated batch processing - disable retry (default)
methodName(param1, param2, false);
// or simply
methodName(param1, param2, null);
\`\`\`

**For Existing Implementations:**
- No code changes required
- Default behavior (no retry) is maintained
- Optionally add `allowRetry` parameter to enable interactive retry
```

---

## Testing Requirements

### 1. Create Unit Tests

Create a test class `AllowRetryParameterTest.java` in your test directory:

**Location**: `src/test/java/.../catalog/AllowRetryParameterTest.java`

**Required Test Categories:**

1. **Parameter Signature Tests**
   - Test method accepts `allowRetry=null`
   - Test method accepts `allowRetry=false`
   - Test method accepts `allowRetry=true`

2. **Backward Compatibility Tests**
   - Test `null` executes successfully (no retry)
   - Test `false` executes successfully (no retry)

3. **Telemetry Tests**
   - Test `allow_retry` tag is included in telemetry
   - Test telemetry methods are called correctly

4. **Integration Tests**
   - Test complete flow with `allowRetry=null`
   - Test complete flow with `allowRetry=true`

**Minimum Test Coverage**: 10-15 tests per extension

---

### 2. Update Existing Tests

Update any existing tests that call `@CatalogRequest` methods to include the new parameter:

**Before:**
```java
ExtensionResponse response = area.methodName(param1, param2);
```

**After:**
```java
ExtensionResponse response = area.methodName(param1, param2, null);
```

---

## Implementation Checklist

### Phase 1: Code Changes
- [ ] Identify all classes with `@CatalogRequest` methods (typically `*Area.java` classes)
- [ ] Add `allowRetry` parameter to all `@CatalogRequest` methods
- [ ] Implement conditional logic for methods WITH validation
- [ ] Add parameter (ignored) for methods WITHOUT validation
- [ ] Update all telemetry calls to include `allow_retry` tag
- [ ] Update all logging statements to include `allowRetry` parameter
- [ ] Update extension version number

### Phase 2: Testing
- [ ] Create `AllowRetryParameterTest.java` with comprehensive tests
- [ ] Update existing tests to include new parameter
- [ ] Run full test suite and ensure all tests pass
- [ ] Perform manual testing of retry flows
- [ ] Validate telemetry data collection

### Phase 3: Documentation
- [ ] Update all catalog request `.md` files
- [ ] Add "Allow Retry" to Input Parameters tables
- [ ] Add Parameter Details sections
- [ ] Add usage examples
- [ ] Update release notes with new version section
- [ ] Update Version History Summary table

### Phase 4: Quality Assurance
- [ ] Code review
- [ ] Integration testing
- [ ] Performance testing
- [ ] Security review
- [ ] Documentation review

---

## Expected Outcomes

### Benefits

**For Developers:**
- ✅ Fine-grained control over retry behavior
- ✅ Consistent API across all catalog requests
- ✅ Clear documentation and examples
- ✅ Backward compatible (no breaking changes)

**For End Users:**
- ✅ Choice between immediate errors or interactive retry
- ✅ Better user experience for interactive workflows
- ✅ Faster error handling for automated processes

**For Operations:**
- ✅ Telemetry tracking for retry feature usage
- ✅ Monitoring and analytics capabilities
- ✅ Clear audit trail of retry behavior

---

## Notes and Best Practices

### Important Considerations

1. **Do NOT modify** SubCatalog request handlers - they remain unchanged
2. **Do NOT modify** `ExtensionResponseGenerator` - response generation logic remains unchanged
3. **Do NOT modify** validation logic or validators
4. **Focus only** on adding the parameter and conditional logic in Area classes
5. **Ensure** all MD files are updated consistently with the same pattern
6. **Verify** MD file names match the catalog request names (case-sensitive)
7. **Version number** must be updated in the main Extension class only
8. **Release notes** should clearly communicate backward compatibility

### Common Pitfalls to Avoid

❌ Using primitive `boolean` instead of `Boolean` (must be nullable)  
❌ Making the parameter required (must be `required = false`)  
❌ Forgetting to update telemetry calls  
❌ Not handling `null` case properly (use `Boolean.TRUE.equals(allowRetry)`)  
❌ Modifying SubCatalog handlers unnecessarily  
❌ Breaking backward compatibility  

### Code Quality Standards

- ✅ Follow existing code patterns and conventions
- ✅ Maintain consistent naming across all methods
- ✅ Include proper error handling
- ✅ Add comprehensive logging
- ✅ Write meaningful test cases
- ✅ Document all changes clearly

---

## Support and Questions

For questions or issues during implementation:

1. Review the Outlook3 extension implementation as a reference
2. Check the test files for examples
3. Consult the refactoring prompt documentation
4. Reach out to the development team for clarification

---

## Summary

This refactoring adds an optional "Allow Retry" parameter to all catalog requests across Krista extensions, providing fine-grained control over validation error retry behavior while maintaining full backward compatibility. The implementation is straightforward, well-tested, and follows established patterns.

**Estimated Effort**: 2-4 hours per extension (depending on number of catalog requests)

**Risk Level**: Low (backward compatible, well-tested pattern)

**Priority**: Medium (enhancement, not critical)

---

**Version**: 1.0  
**Last Updated**: October 2025  
**Author**: Krista Development Team  
**Reference Implementation**: Outlook3 Extension v3.0.16

