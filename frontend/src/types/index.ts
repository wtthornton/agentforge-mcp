// Comprehensive type definitions for AgentForge
export interface BaseComponentProps {
  className?: string;
  children?: React.ReactNode;
}

export interface ButtonProps extends BaseComponentProps {
  variant: 'primary' | 'secondary' | 'outline' | 'ghost';
  size: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
}

export interface CardProps extends BaseComponentProps {
  title?: string;
  subtitle?: string;
  padding?: 'sm' | 'md' | 'lg';
  shadow?: 'sm' | 'md' | 'lg';
}

export interface InputProps extends BaseComponentProps {
  type?: 'text' | 'email' | 'password' | 'number' | 'search';
  placeholder?: string;
  value?: string;
  onChange?: (value: string) => void;
  disabled?: boolean;
  required?: boolean;
  error?: string;
}

export interface BadgeProps extends BaseComponentProps {
  variant: 'primary' | 'secondary' | 'success' | 'warning' | 'error';
  size?: 'sm' | 'md' | 'lg';
}

// Navigation types
export interface NavigationItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: string;
  children?: NavigationItem[];
}

// API response types
export interface ApiResponse<T = any> {
  data: T;
  message: string;
  success: boolean;
  timestamp: string;
}

export interface PaginatedResponse<T> extends ApiResponse<T[]> {
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

// User and authentication types
export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  avatar?: string;
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  DEVELOPER = 'DEVELOPER',
  VIEWER = 'VIEWER'
}

// Project types
export interface Project {
  id: string;
  name: string;
  description: string;
  status: ProjectStatus;
  owner: User;
  collaborators: User[];
  createdAt: string;
  updatedAt: string;
  lastAnalysis?: string;
}

export enum ProjectStatus {
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED',
  DELETED = 'DELETED'
}

// Agent types
export interface Agent {
  id: string;
  name: string;
  type: AgentType;
  status: AgentStatus;
  project: Project;
  lastActivity: string;
  performance: {
    cpu: number;
    memory: number;
    uptime: number;
  };
}

export enum AgentType {
  ANALYZER = 'ANALYZER',
  MONITOR = 'MONITOR',
  REPORTER = 'REPORTER'
}

export enum AgentStatus {
  RUNNING = 'RUNNING',
  STOPPED = 'STOPPED',
  ERROR = 'ERROR',
  STARTING = 'STARTING'
}

// Analysis types
export interface Analysis {
  id: string;
  project: Project;
  type: 'code-quality' | 'security' | 'performance' | 'compliance';
  status: 'pending' | 'running' | 'completed' | 'failed';
  results: AnalysisResult[];
  startedAt: string;
  completedAt?: string;
  duration?: number;
}

export interface AnalysisResult {
  rule: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  message: string;
  line?: number;
  file?: string;
  suggestion?: string;
}

// Form types
export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'email' | 'password' | 'select' | 'textarea' | 'checkbox';
  required?: boolean;
  options?: { value: string; label: string }[];
  validation?: {
    minLength?: number;
    maxLength?: number;
    pattern?: string;
    custom?: (value: any) => string | undefined;
  };
}

// Table types
export interface TableColumn<T> {
  key: keyof T;
  header: string;
  sortable?: boolean;
  render?: (value: T[keyof T], row: T) => React.ReactNode;
  width?: string;
}

export interface TableProps<T> {
  data: T[];
  columns: TableColumn<T>[];
  sortable?: boolean;
  pagination?: boolean;
  searchable?: boolean;
  onRowClick?: (row: T) => void;
}

// Chart types
export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string;
    borderColor?: string;
  }[];
}

// Notification types
export interface Notification {
  id: string;
  type: 'success' | 'info' | 'warning' | 'error';
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
  action?: {
    label: string;
    onClick: () => void;
  };
}

// Export all types
export type {
  BaseComponentProps,
  ButtonProps,
  CardProps,
  InputProps,
  BadgeProps,
  NavigationItem,
  ApiResponse,
  PaginatedResponse,
  User,
  Project,
  Agent,
  Analysis,
  AnalysisResult,
  FormField,
  TableProps,
  ChartData,
  Notification
};
