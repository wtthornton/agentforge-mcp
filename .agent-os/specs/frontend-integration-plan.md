# Frontend Integration Plan - Phase 2

## 🎯 Overview
This document outlines the frontend integration strategy for Phase 2 of AgentForge, focusing on API client implementation, state management, and frontend-backend communication.

## 📊 Current Status
- **Frontend Framework**: ✅ React 19 + TypeScript
- **Basic Components**: ✅ Pages and routing structure
- **UI Components**: ✅ Basic layout and error handling
- **Next Step**: 🔄 Implement API client and state management

## 🏗️ Architecture Overview

### Current Frontend Structure
```
frontend/
├── src/
│   ├── components/
│   │   ├── ErrorFallback.tsx ✅
│   │   └── Layout.tsx ✅
│   ├── pages/
│   │   ├── Dashboard.tsx ✅
│   │   ├── Projects.tsx ✅
│   │   ├── Agents.tsx ✅
│   │   └── Settings.tsx ✅
│   ├── App.tsx ✅
│   └── main.tsx ✅
```

### Target Architecture
```
frontend/
├── src/
│   ├── api/           🚀 TO BE IMPLEMENTED
│   │   ├── client.ts
│   │   ├── endpoints.ts
│   │   └── types.ts
│   ├── hooks/         🚀 TO BE IMPLEMENTED
│   │   ├── useApi.ts
│   │   ├── useProjects.ts
│   │   └── useUsers.ts
│   ├── store/         🚀 TO BE IMPLEMENTED
│   │   ├── context/
│   │   └── providers/
│   ├── components/    ✅ EXISTS
│   ├── pages/         ✅ EXISTS
│   └── utils/         🚀 TO BE IMPLEMENTED
```

## 🚀 Implementation Tasks

### 1. API Client Implementation

#### 1.1 Core API Client
**Objective**: Create robust API client for frontend-backend communication
**Implementation**:
- [ ] **Task 2.6.1**: Create API client for frontend
  - [ ] Implement base HTTP client with Axios
  - [ ] Add request/response interceptors
  - [ ] Implement error handling and retry logic
  - [ ] Add request timeout and cancellation
  - [ ] **Update lessons learned** - Capture insights from API client implementation

**Code Structure**:
```typescript
// src/api/client.ts
export class ApiClient {
  private axios: AxiosInstance;
  
  constructor(baseURL: string) {
    this.axios = axios.create({
      baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });
    
    this.setupInterceptors();
  }
  
  private setupInterceptors() {
    // Request interceptor for auth tokens
    // Response interceptor for error handling
    // Retry logic for failed requests
  }
}
```

#### 1.2 API Endpoints
**Objective**: Define typed API endpoints for all backend services
**Implementation**:
- [ ] **Task 2.6.2**: Implement error handling and retry logic
  - [ ] Define endpoint constants
  - [ ] Create typed request/response interfaces
  - [ ] Implement endpoint-specific error handling
  - [ ] Add request validation

**Code Structure**:
```typescript
// src/api/endpoints.ts
export const API_ENDPOINTS = {
  // Health endpoints
  HEALTH: '/health',
  HEALTH_READINESS: '/health/readiness',
  
  // User endpoints
  USERS: '/api/users',
  USER_BY_ID: (id: string) => `/api/users/${id}`,
  
  // Project endpoints
  PROJECTS: '/api/projects',
  PROJECT_BY_ID: (id: string) => `/api/projects/${id}`,
  PROJECT_ANALYSIS: (id: string) => `/api/projects/${id}/analysis`,
  
  // Analysis endpoints
  ANALYSES: '/api/analyses',
  ANALYSIS_BY_ID: (id: string) => `/api/analyses/${id}`,
  
  // Compliance endpoints
  COMPLIANCE: '/api/compliance',
  COMPLIANCE_CHECK: '/api/compliance/check',
} as const;
```

#### 1.3 Type Definitions
**Objective**: Create comprehensive TypeScript types for API communication
**Implementation**:
- [ ] **Task 2.6.3**: Add loading states and user feedback
  - [ ] Define request/response types
  - [ ] Create error type definitions
  - [ ] Define API response wrappers
  - [ ] Add validation schemas

**Code Structure**:
```typescript
// src/api/types.ts
export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
  timestamp: string;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, any>;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    size: number;
    total: number;
    totalPages: number;
  };
}
```

### 2. State Management Implementation

#### 2.1 TanStack Query Integration
**Objective**: Implement efficient data fetching and caching
**Implementation**:
- [ ] **Task 2.7.1**: Implement TanStack Query for data fetching
  - [ ] Set up QueryClient with configuration
  - [ ] Create custom query hooks
  - [ ] Implement optimistic updates
  - [ ] Add background refetching

**Code Structure**:
```typescript
// src/store/query-client.ts
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000,   // 10 minutes
      retry: 3,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    },
    mutations: {
      retry: 1,
    },
  },
});

// src/hooks/useProjects.ts
export const useProjects = () => {
  return useQuery({
    queryKey: ['projects'],
    queryFn: () => apiClient.get<Project[]>(API_ENDPOINTS.PROJECTS),
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

export const useProject = (id: string) => {
  return useQuery({
    queryKey: ['projects', id],
    queryFn: () => apiClient.get<Project>(API_ENDPOINTS.PROJECT_BY_ID(id)),
    enabled: !!id,
  });
};
```

#### 2.2 Context API Implementation
**Objective**: Implement global state management for app-wide data
**Implementation**:
- [ ] **Task 2.7.2**: Add Context API for global state
  - [ ] Create authentication context
  - [ ] Implement theme context
  - [ ] Add notification context
  - [ ] Create settings context

**Code Structure**:
```typescript
// src/store/context/AuthContext.tsx
interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  
  const login = async (credentials: LoginCredentials) => {
    // Implementation
  };
  
  const logout = () => {
    // Implementation
  };
  
  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};
```

#### 2.3 Optimistic Updates
**Objective**: Implement smooth user experience with optimistic updates
**Implementation**:
- [ ] **Task 2.7.3**: Implement optimistic updates
  - [ ] Add optimistic updates for CRUD operations
  - [ ] Implement rollback on failure
  - [ ] Add loading states for mutations
  - [ ] Create smooth transitions

**Code Structure**:
```typescript
// src/hooks/useProjectMutations.ts
export const useCreateProject = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (project: CreateProjectRequest) => 
      apiClient.post<Project>(API_ENDPOINTS.PROJECTS, project),
    onMutate: async (newProject) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['projects'] });
      
      // Snapshot previous value
      const previousProjects = queryClient.getQueryData(['projects']);
      
      // Optimistically update
      queryClient.setQueryData(['projects'], (old: Project[] = []) => [
        { ...newProject, id: 'temp-' + Date.now() },
        ...old,
      ]);
      
      return { previousProjects };
    },
    onError: (err, newProject, context) => {
      // Rollback on error
      if (context?.previousProjects) {
        queryClient.setQueryData(['projects'], context.previousProjects);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: ['projects'] });
    },
  });
};
```

### 3. Component Enhancement

#### 3.1 Loading States and Feedback
**Objective**: Implement comprehensive user feedback system
**Implementation**:
- [ ] **Task 2.6.4**: Add loading states and user feedback
  - [ ] Create loading spinners and skeletons
  - [ ] Implement progress indicators
  - [ ] Add success/error notifications
  - [ ] Create empty state components

**Code Structure**:
```typescript
// src/components/ui/LoadingSpinner.tsx
export const LoadingSpinner: React.FC<{ size?: 'sm' | 'md' | 'lg' }> = ({ size = 'md' }) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
  };
  
  return (
    <div className={`animate-spin rounded-full border-2 border-gray-300 border-t-blue-600 ${sizeClasses[size]}`} />
  );
};

// src/components/ui/Skeleton.tsx
export const Skeleton: React.FC<{ className?: string }> = ({ className }) => {
  return (
    <div className={`animate-pulse bg-gray-200 rounded ${className}`} />
  );
};
```

#### 3.2 Error Handling
**Objective**: Implement comprehensive error handling and user feedback
**Implementation**:
- [ ] **Task 2.6.2**: Implement error handling and retry logic
  - [ ] Create error boundary components
  - [ ] Implement error toast notifications
  - [ ] Add retry mechanisms
  - [ ] Create fallback UI components

**Code Structure**:
```typescript
// src/components/ui/ErrorBoundary.tsx
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  
  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }
  
  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }
  
  render() {
    if (this.state.hasError) {
      return <ErrorFallback error={this.state.error} />;
    }
    
    return this.props.children;
  }
}

// src/components/ui/ErrorToast.tsx
export const ErrorToast: React.FC<{ error: ApiError; onRetry?: () => void }> = ({ 
  error, 
  onRetry 
}) => {
  return (
    <div className="bg-red-50 border border-red-200 rounded-md p-4">
      <div className="flex">
        <div className="flex-shrink-0">
          <ExclamationTriangleIcon className="h-5 w-5 text-red-400" />
        </div>
        <div className="ml-3">
          <h3 className="text-sm font-medium text-red-800">
            {error.message}
          </h3>
          {onRetry && (
            <button
              onClick={onRetry}
              className="mt-2 text-sm text-red-600 hover:text-red-500"
            >
              Retry
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
```

### 4. Data Validation and Sanitization

#### 4.1 Input Validation
**Objective**: Implement comprehensive input validation
**Implementation**:
- [ ] **Task 2.6.4**: Add loading states and user feedback
  - [ ] Create validation schemas
  - [ ] Implement form validation
  - [ ] Add real-time validation feedback
  - [ ] Create validation error displays

**Code Structure**:
```typescript
// src/utils/validation.ts
import { z } from 'zod';

export const projectSchema = z.object({
  name: z.string().min(1, 'Project name is required').max(100, 'Project name too long'),
  description: z.string().max(500, 'Description too long').optional(),
  status: z.enum(['ACTIVE', 'INACTIVE', 'ARCHIVED']),
  technologyStack: z.array(z.string()).min(1, 'At least one technology required'),
});

export const userSchema = z.object({
  email: z.string().email('Invalid email address'),
  username: z.string().min(3, 'Username too short').max(50, 'Username too long'),
  role: z.enum(['DEVELOPER', 'ADMIN', 'MANAGER']),
});

// src/hooks/useFormValidation.ts
export const useFormValidation = <T>(schema: z.ZodSchema<T>) => {
  const [errors, setErrors] = useState<Record<string, string>>({});
  
  const validate = (data: unknown): data is T => {
    try {
      schema.parse(data);
      setErrors({});
      return true;
    } catch (error) {
      if (error instanceof z.ZodError) {
        const newErrors: Record<string, string> = {};
        error.errors.forEach((err) => {
          if (err.path) {
            newErrors[err.path.join('.')] = err.message;
          }
        });
        setErrors(newErrors);
      }
      return false;
    }
  };
  
  return { validate, errors, clearErrors: () => setErrors({}) };
};
```

#### 4.2 Data Sanitization
**Objective**: Implement data sanitization for security
**Implementation**:
- [ ] **Task 2.6.4**: Add loading states and user feedback
  - [ ] Create sanitization utilities
  - [ ] Implement XSS prevention
  - [ ] Add HTML encoding
  - [ ] Create safe rendering components

**Code Structure**:
```typescript
// src/utils/sanitization.ts
import DOMPurify from 'dompurify';

export const sanitizeHtml = (html: string): string => {
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a', 'p', 'br'],
    ALLOWED_ATTR: ['href', 'target'],
  });
};

export const sanitizeText = (text: string): string => {
  return text
    .replace(/[<>]/g, '')
    .trim();
};

// src/components/ui/SafeHtml.tsx
export const SafeHtml: React.FC<{ html: string; className?: string }> = ({ 
  html, 
  className 
}) => {
  const sanitizedHtml = useMemo(() => sanitizeHtml(html), [html]);
  
  return (
    <div 
      className={className}
      dangerouslySetInnerHTML={{ __html: sanitizedHtml }}
    />
  );
};
```

## 🧪 Testing Strategy

### 1. Unit Testing
**Objective**: Test individual components and hooks
**Test Cases**:
- [ ] **TC-FRONTEND-001**: Component Rendering
  - Test component rendering with props
  - Test component state changes
  - Test component lifecycle methods

- [ ] **TC-FRONTEND-002**: Hook Testing
  - Test custom hook behavior
  - Test hook state changes
  - Test hook side effects

### 2. Integration Testing
**Objective**: Test component interactions and API integration
**Test Cases**:
- [ ] **TC-FRONTEND-003**: API Integration
  - Test API client functionality
  - Test data fetching and caching
  - Test error handling

- [ ] **TC-FRONTEND-004**: State Management
  - Test context providers
  - Test state updates
  - Test data flow

### 3. End-to-End Testing
**Objective**: Test complete user workflows
**Test Cases**:
- [ ] **TC-FRONTEND-005**: User Workflows
  - Test project creation workflow
  - Test project analysis workflow
  - Test compliance checking workflow

## 📊 Performance Targets

### Frontend Performance
- **Time to Interactive**: ≤1.8 seconds
- **Bundle Size**: ≤500KB (gzipped)
- **First Contentful Paint**: ≤1.2 seconds
- **Largest Contentful Paint**: ≤2.5 seconds

### API Performance
- **Data Fetching**: ≤200ms
- **Mutation Operations**: ≤500ms
- **Real-time Updates**: ≤100ms
- **Cache Hit Rate**: ≥90%

## 🔧 Implementation Timeline

### Week 1: API Client Foundation
- [ ] Day 1-2: Core API client implementation
- [ ] Day 3-4: Endpoint definitions and types
- [ ] Day 5: Basic error handling

### Week 2: State Management
- [ ] Day 1-2: TanStack Query setup
- [ ] Day 3-4: Context API implementation
- [ ] Day 5: Optimistic updates

### Week 3: Component Enhancement
- [ ] Day 1-2: Loading states and feedback
- [ ] Day 3-4: Error handling components
- [ ] Day 5: Component testing

### Week 4: Validation and Testing
- [ ] Day 1-2: Input validation implementation
- [ ] Day 3-4: Data sanitization
- [ ] Day 5: Integration testing

## 📚 Lessons Learned Integration

### Capture Points
- **API Integration**: API client optimization insights
- **State Management**: Performance optimization strategies
- **Error Handling**: User experience improvement patterns
- **Validation**: Security implementation techniques
- **Testing**: Component testing strategies

### Documentation Template
Use `.agent-os/templates/lessons-learned-template.md` for:
- Frontend integration insights
- Performance optimization lessons
- User experience improvement patterns
- Testing strategy improvements

## 🔄 Continuous Improvement

### Weekly Reviews
- Component performance analysis
- User experience feedback
- API integration optimization
- Testing coverage improvement

### Monthly Assessments
- Performance target validation
- User experience metrics
- Security vulnerability review
- Architecture optimization

### Quarterly Planning
- Frontend strategy updates
- New component development
- Performance target adjustment
- User experience enhancement

## 🎯 Success Criteria

### Phase 2 Success
- [ ] API client fully functional
- [ ] State management implemented
- [ ] All components enhanced
- [ ] Validation and sanitization working
- [ ] Performance targets met
- [ ] Test coverage ≥80%

### Quality Gates
- **Performance**: TTI ≤1.8s, bundle size ≤500KB
- **Functionality**: All API endpoints working
- **User Experience**: Smooth interactions and feedback
- **Security**: Input validation and sanitization
- **Testing**: Comprehensive test coverage

---

**Document Status**: Active
**Last Updated**: January 2025
**Next Review**: Weekly during implementation
**Responsible Team**: Frontend Development Team
**Stakeholder Approval**: Required for implementation
**Current Phase**: Phase 2 - Frontend Integration
