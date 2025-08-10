export interface Project {
  id: string;
  name: string;
  description?: string;
  path: string;
  technologyStack: string[];
  createdAt: Date;
  updatedAt: Date;
  lastAnalyzedAt?: Date;
  status: ProjectStatus;
  complianceScore?: number;
  totalFiles: number;
  totalDirectories: number;
  totalLinesOfCode: number;
}

export enum ProjectStatus {
  PENDING = 'pending',
  ANALYZING = 'analyzing',
  COMPLETED = 'completed',
  FAILED = 'failed',
  ARCHIVED = 'archived'
}

export interface ProjectAnalysis {
  id: string;
  projectId: string;
  analysisType: AnalysisType;
  startedAt: Date;
  completedAt?: Date;
  status: AnalysisStatus;
  results: any;
  errors?: string[];
  metadata: Record<string, any>;
}

export enum AnalysisType {
  STRUCTURE = 'structure',
  COMPLIANCE = 'compliance',
  SECURITY = 'security',
  QUALITY = 'quality',
  PERFORMANCE = 'performance'
}

export enum AnalysisStatus {
  PENDING = 'pending',
  RUNNING = 'running',
  COMPLETED = 'completed',
  FAILED = 'failed',
  CANCELLED = 'cancelled'
}

export interface ProjectCompliance {
  id: string;
  projectId: string;
  standardId: string;
  standardName: string;
  complianceLevel: ComplianceLevel;
  score: number;
  violations: ComplianceViolation[];
  lastCheckedAt: Date;
  nextCheckDue: Date;
}

export enum ComplianceLevel {
  COMPLIANT = 'compliant',
  PARTIALLY_COMPLIANT = 'partially_compliant',
  NON_COMPLIANT = 'non_compliant',
  UNKNOWN = 'unknown'
}

export interface ComplianceViolation {
  id: string;
  ruleId: string;
  ruleName: string;
  severity: ViolationSeverity;
  message: string;
  filePath?: string;
  lineNumber?: number;
  columnNumber?: number;
  context?: string;
  suggestedFix?: string;
}

export enum ViolationSeverity {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  CRITICAL = 'critical'
}
