/**
 * Common validation utilities for AgentForge
 * Provides reusable validation functions for forms and data
 */

/**
 * Validates email format
 * 
 * @param email - Email string to validate
 * @returns True if email is valid, false otherwise
 * 
 * @example
 * ```tsx
 * if (!isValidEmail(email)) {
 *   setError('Please enter a valid email address');
 * }
 * ```
 */
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Validates URL format
 * 
 * @param url - URL string to validate
 * @returns True if URL is valid, false otherwise
 * 
 * @example
 * ```tsx
 * if (!isValidUrl(website)) {
 *   setError('Please enter a valid website URL');
 * }
 * ```
 */
export const isValidUrl = (url: string): boolean => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * Validates password strength
 * 
 * @param password - Password string to validate
 * @returns Object with validation results and suggestions
 * 
 * @example
 * ```tsx
 * const validation = validatePassword(password);
 * if (!validation.isValid) {
 *   setError(validation.suggestions.join(', '));
 * }
 * ```
 */
export const validatePassword = (password: string) => {
  const minLength = 8;
  const hasUpperCase = /[A-Z]/.test(password);
  const hasLowerCase = /[a-z]/.test(password);
  const hasNumbers = /\d/.test(password);
  const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);
  
  const isValid = password.length >= minLength && hasUpperCase && hasLowerCase && hasNumbers && hasSpecialChar;
  
  const suggestions: string[] = [];
  if (password.length < minLength) suggestions.push(`At least ${minLength} characters`);
  if (!hasUpperCase) suggestions.push('At least one uppercase letter');
  if (!hasLowerCase) suggestions.push('At least one lowercase letter');
  if (!hasNumbers) suggestions.push('At least one number');
  if (!hasSpecialChar) suggestions.push('At least one special character');
  
  return {
    isValid,
    suggestions,
    score: [hasUpperCase, hasLowerCase, hasNumbers, hasSpecialChar, password.length >= minLength].filter(Boolean).length
  };
};

/**
 * Validates required fields
 * 
 * @param value - Value to check
 * @param fieldName - Name of the field for error message
 * @returns Error message if invalid, undefined if valid
 * 
 * @example
 * ```tsx
 * const error = validateRequired(name, 'Name');
 * if (error) setError(error);
 * ```
 */
export const validateRequired = (value: any, fieldName: string): string | undefined => {
  if (value === null || value === undefined || value === '') {
    return `${fieldName} is required`;
  }
  return undefined;
};

/**
 * Validates string length
 * 
 * @param value - String value to validate
 * @param fieldName - Name of the field for error message
 * @param minLength - Minimum required length
 * @param maxLength - Maximum allowed length
 * @returns Error message if invalid, undefined if valid
 * 
 * @example
 * ```tsx
 * const error = validateLength(description, 'Description', 10, 500);
 * if (error) setError(error);
 * ```
 */
export const validateLength = (
  value: string, 
  fieldName: string, 
  minLength?: number, 
  maxLength?: number
): string | undefined => {
  if (minLength && value.length < minLength) {
    return `${fieldName} must be at least ${minLength} characters`;
  }
  if (maxLength && value.length > maxLength) {
    return `${fieldName} must be no more than ${maxLength} characters`;
  }
  return undefined;
};

/**
 * Validates numeric range
 * 
 * @param value - Numeric value to validate
 * @param fieldName - Name of the field for error message
 * @param min - Minimum allowed value
 * @param max - Maximum allowed value
 * @returns Error message if invalid, undefined if valid
 * 
 * @example
 * ```tsx
 * const error = validateRange(age, 'Age', 0, 120);
 * if (error) setError(error);
 * ```
 */
export const validateRange = (
  value: number, 
  fieldName: string, 
  min?: number, 
  max?: number
): string | undefined => {
  if (min !== undefined && value < min) {
    return `${fieldName} must be at least ${min}`;
  }
  if (max !== undefined && value > max) {
    return `${fieldName} must be no more than ${max}`;
  }
  return undefined;
};

/**
 * Validates file size
 * 
 * @param file - File object to validate
 * @param maxSizeMB - Maximum file size in megabytes
 * @returns Error message if invalid, undefined if valid
 * 
 * @example
 * ```tsx
 * const error = validateFileSize(file, 10);
 * if (error) setError(error);
 * ```
 */
export const validateFileSize = (file: File, maxSizeMB: number): string | undefined => {
  const maxSizeBytes = maxSizeMB * 1024 * 1024;
  if (file.size > maxSizeBytes) {
    return `File size must be less than ${maxSizeMB}MB`;
  }
  return undefined;
};

/**
 * Validates file type
 * 
 * @param file - File object to validate
 * @param allowedTypes - Array of allowed MIME types
 * @returns Error message if invalid, undefined if valid
 * 
 * @example
 * ```tsx
 * const error = validateFileType(file, ['image/jpeg', 'image/png']);
 * if (error) setError(error);
 * ```
 */
export const validateFileType = (file: File, allowedTypes: string[]): string | undefined => {
  if (!allowedTypes.includes(file.type)) {
    return `File type must be one of: ${allowedTypes.join(', ')}`;
  }
  return undefined;
};

/**
 * Generic validation function that runs multiple validators
 * 
 * @param value - Value to validate
 * @param validators - Array of validation functions
 * @returns First error message found, or undefined if all validations pass
 * 
 * @example
 * ```tsx
 * const error = validate(value, [
 *   (v) => validateRequired(v, 'Field'),
 *   (v) => validateLength(v, 'Field', 3, 50)
 * ]);
 * if (error) setError(error);
 * ```
 */
export const validate = (
  value: any, 
  validators: ((value: any) => string | undefined)[]
): string | undefined => {
  for (const validator of validators) {
    const error = validator(value);
    if (error) return error;
  }
  return undefined;
};
