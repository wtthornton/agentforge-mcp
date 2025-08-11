import React from 'react';
import { ButtonProps } from '@/types';
import { cn } from '@/utils/cn';

/**
 * Reusable Button component with multiple variants and sizes
 * 
 * @param props - Button properties
 * @param props.variant - Button style variant
 * @param props.size - Button size
 * @param props.disabled - Whether button is disabled
 * @param props.onClick - Click handler function
 * @param props.children - Button content
 * @param props.className - Additional CSS classes
 * @returns Button component
 */
export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  disabled = false,
  onClick,
  children,
  className,
  type = 'button',
  ...props
}) => {
  const baseClasses = 'inline-flex items-center justify-center rounded-lg font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50';
  
  const variantClasses = {
    primary: 'bg-primary-600 text-white hover:bg-primary-700 active:bg-primary-800',
    secondary: 'bg-secondary-100 text-secondary-900 hover:bg-secondary-200 active:bg-secondary-300',
    outline: 'border border-secondary-300 bg-transparent hover:bg-secondary-50 active:bg-secondary-100',
    ghost: 'bg-transparent hover:bg-secondary-100 active:bg-secondary-200'
  };
  
  const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base'
  };
  
  return (
    <button
      type={type}
      className={cn(
        baseClasses,
        variantClasses[variant],
        sizeClasses[size],
        className
      )}
      disabled={disabled}
      onClick={onClick}
      {...props}
    >
      {children}
    </button>
  );
};
