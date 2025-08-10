/**
 * MCP (Model Context Protocol) Types and Interfaces
 * Based on MCP specification for AI agent communication
 */

export interface MCPMessage {
  jsonrpc: '2.0';
  id: string | number;
  method?: string;
  params?: any;
  result?: any;
  error?: MCPError;
}

export interface MCPError {
  code: number;
  message: string;
  data?: any;
}

export interface MCPRequest extends MCPMessage {
  method: string;
  params?: any;
}

export interface MCPResponse extends MCPMessage {
  result?: any;
  error?: MCPError;
}

export interface MCPNotification extends MCPMessage {
  method: string;
  params?: any;
}

// MCP Function Types
export interface MCPContext {
  projectPath: string;
  projectStructure: ProjectStructure;
  standards: StandardsInfo;
  compliance: ComplianceStatus;
  metrics: ProjectMetrics;
}

export interface ProjectStructure {
  root: string;
  directories: string[];
  files: ProjectFile[];
  dependencies: DependencyInfo;
  configFiles: string[];
}

export interface ProjectFile {
  path: string;
  name: string;
  type: 'file' | 'directory';
  size?: number;
  lastModified?: Date;
  extension?: string;
}

export interface DependencyInfo {
  node: NodeDependencies;
  java?: JavaDependencies;
  python?: PythonDependencies;
  other: Record<string, any>;
}

export interface NodeDependencies {
  dependencies: Record<string, string>;
  devDependencies: Record<string, string>;
  scripts: Record<string, string>;
}

export interface JavaDependencies {
  groupId: string;
  artifactId: string;
  version: string;
  dependencies: Array<{
    groupId: string;
    artifactId: string;
    version: string;
  }>;
}

export interface PythonDependencies {
  requirements: string[];
  setupPy?: Record<string, any>;
  pyprojectToml?: Record<string, any>;
}

export interface StandardsInfo {
  version: string;
  standards: Standard[];
  categories: string[];
  lastUpdated: Date;
}

export interface Standard {
  id: string;
  name: string;
  category: string;
  description: string;
  rules: Rule[];
  priority: 'low' | 'medium' | 'high' | 'critical';
}

export interface Rule {
  id: string;
  name: string;
  description: string;
  type: 'lint' | 'security' | 'style' | 'performance' | 'architecture';
  severity: 'info' | 'warning' | 'error' | 'critical';
  pattern?: string;
  validator: string;
}

export interface ComplianceStatus {
  overall: 'compliant' | 'non-compliant' | 'partial';
  score: number;
  violations: ComplianceViolation[];
  lastChecked: Date;
  nextCheck?: Date;
}

export interface ComplianceViolation {
  ruleId: string;
  ruleName: string;
  severity: string;
  message: string;
  file?: string;
  line?: number;
  column?: number;
  context?: string;
}

export interface ProjectMetrics {
  codeQuality: CodeQualityMetrics;
  performance: PerformanceMetrics;
  security: SecurityMetrics;
  maintainability: MaintainabilityMetrics;
}

export interface CodeQualityMetrics {
  linesOfCode: number;
  cyclomaticComplexity: number;
  maintainabilityIndex: number;
  technicalDebt: number;
  codeCoverage?: number;
}

export interface PerformanceMetrics {
  buildTime: number;
  testTime: number;
  bundleSize?: number;
  loadTime?: number;
}

export interface SecurityMetrics {
  vulnerabilities: number;
  securityScore: number;
  lastScan: Date;
  criticalIssues: number;
}

export interface MaintainabilityMetrics {
  technicalDebtRatio: number;
  codeDuplication: number;
  documentationCoverage: number;
  testCoverage: number;
}

// MCP Function Parameters
export interface GetContextParams {
  projectPath?: string;
  includeStandards?: boolean;
  includeCompliance?: boolean;
  includeMetrics?: boolean;
}

export interface GetStandardsParams {
  category?: string;
  priority?: string;
  search?: string;
}

export interface ValidateComplianceParams {
  projectPath?: string;
  standards?: string[];
  strict?: boolean;
}

export interface AnalyzeProjectParams {
  projectPath?: string;
  depth?: number;
  includeHidden?: boolean;
}

export interface GetMetricsParams {
  projectPath?: string;
  metrics?: string[];
  timeRange?: string;
}

// MCP Function Results
export interface GetContextResult {
  context: MCPContext;
  timestamp: Date;
  version: string;
}

export interface GetStandardsResult {
  standards: Standard[];
  total: number;
  categories: string[];
  version: string;
}

export interface ValidateComplianceResult {
  status: ComplianceStatus;
  recommendations: string[];
  nextSteps: string[];
}

export interface AnalyzeProjectResult {
  analysis: ProjectStructure;
  insights: string[];
  recommendations: string[];
  timestamp: Date;
}

export interface GetMetricsResult {
  metrics: ProjectMetrics;
  trends: Record<string, any[]>;
  timestamp: Date;
}
