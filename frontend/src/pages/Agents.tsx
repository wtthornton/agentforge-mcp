import React, { useState } from 'react';
import { 
  Plus, 
  Search, 
  Bot, 
  Play, 
  Pause, 
  Stop, 
  Trash2, 
  Edit,
  Eye,
  Activity,
  Cpu,
  Memory,
  Network,
  Calendar,
  User,
  Settings
} from 'lucide-react';

interface Agent {
  id: string;
  name: string;
  description: string;
  type: 'analysis' | 'monitoring' | 'automation' | 'ml';
  status: 'running' | 'stopped' | 'error' | 'idle';
  cpu: number;
  memory: number;
  network: number;
  createdAt: string;
  owner: string;
  lastActivity: string;
}

const Agents: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('all');
  const [statusFilter, setStatusFilter] = useState<string>('all');

  // Mock data - replace with real data from API
  const agents: Agent[] = [
    {
      id: '1',
      name: 'CodeAnalyzer',
      description: 'AI-powered code quality analysis agent',
      type: 'analysis',
      status: 'running',
      cpu: 45,
      memory: 68,
      network: 12,
      createdAt: '2024-01-15',
      owner: 'John Doe',
      lastActivity: '2 minutes ago'
    },
    {
      id: '2',
      name: 'SystemMonitor',
      description: 'Real-time system performance monitoring',
      type: 'monitoring',
      status: 'running',
      cpu: 23,
      memory: 34,
      network: 8,
      createdAt: '2024-01-10',
      owner: 'Jane Smith',
      lastActivity: '1 minute ago'
    },
    {
      id: '3',
      name: 'DataProcessor',
      description: 'Automated data processing and validation',
      type: 'automation',
      status: 'stopped',
      cpu: 0,
      memory: 12,
      network: 0,
      createdAt: '2024-01-12',
      owner: 'Mike Johnson',
      lastActivity: '1 hour ago'
    },
    {
      id: '4',
      name: 'MLTrainer',
      description: 'Machine learning model training agent',
      type: 'ml',
      status: 'error',
      cpu: 0,
      memory: 8,
      network: 0,
      createdAt: '2024-01-08',
      owner: 'Sarah Wilson',
      lastActivity: '3 hours ago'
    }
  ];

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'analysis':
        return 'bg-blue-100 text-blue-800';
      case 'monitoring':
        return 'bg-green-100 text-green-800';
      case 'automation':
        return 'bg-purple-100 text-purple-800';
      case 'ml':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'running':
        return 'bg-green-100 text-green-800';
      case 'stopped':
        return 'bg-gray-100 text-gray-800';
      case 'error':
        return 'bg-red-100 text-red-800';
      case 'idle':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'running':
        return <Play className="h-4 w-4" />;
      case 'stopped':
        return <Stop className="h-4 w-4" />;
      case 'error':
        return <Activity className="h-4 w-4" />;
      case 'idle':
        return <Pause className="h-4 w-4" />;
      default:
        return <Bot className="h-4 w-4" />;
    }
  };

  const filteredAgents = agents.filter(agent => {
    const matchesSearch = agent.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         agent.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = typeFilter === 'all' || agent.type === typeFilter;
    const matchesStatus = statusFilter === 'all' || agent.status === statusFilter;
    return matchesSearch && matchesType && matchesStatus;
  });

  const getResourceColor = (value: number) => {
    if (value < 50) return 'text-green-600';
    if (value < 80) return 'text-yellow-600';
    return 'text-red-600';
  };

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">AI Agents</h1>
          <p className="mt-1 text-sm text-gray-500">
            Manage and monitor your AI agents and automation workflows
          </p>
        </div>
        <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
          <Plus className="h-4 w-4 mr-2" />
          New Agent
        </button>
      </div>

      {/* Filters and search */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label htmlFor="search" className="sr-only">Search agents</label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                id="search"
                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                placeholder="Search agents..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
          <div>
            <label htmlFor="type-filter" className="sr-only">Filter by type</label>
            <select
              id="type-filter"
              className="block w-full px-3 py-2 border border-gray-300 rounded-md leading-5 bg-white focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
            >
              <option value="all">All Types</option>
              <option value="analysis">Analysis</option>
              <option value="monitoring">Monitoring</option>
              <option value="automation">Automation</option>
              <option value="ml">Machine Learning</option>
            </select>
          </div>
          <div>
            <label htmlFor="status-filter" className="sr-only">Filter by status</label>
            <select
              id="status-filter"
              className="block w-full px-3 py-2 border border-gray-300 rounded-md leading-5 bg-white focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Status</option>
              <option value="running">Running</option>
              <option value="stopped">Stopped</option>
              <option value="error">Error</option>
              <option value="idle">Idle</option>
            </select>
          </div>
        </div>
      </div>

      {/* Agents grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2 xl:grid-cols-3">
        {filteredAgents.map((agent) => (
          <div key={agent.id} className="bg-white shadow rounded-lg overflow-hidden">
            <div className="p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center space-x-2">
                    <Bot className="h-5 w-5 text-blue-500" />
                    <h3 className="text-lg font-medium text-gray-900 truncate">
                      {agent.name}
                    </h3>
                  </div>
                  <p className="mt-1 text-sm text-gray-500 line-clamp-2">
                    {agent.description}
                  </p>
                </div>
                <div className="ml-4 flex-shrink-0">
                  <button className="text-gray-400 hover:text-gray-600">
                    <Settings className="h-5 w-5" />
                  </button>
                </div>
              </div>

              <div className="mt-4 flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getTypeColor(agent.type)}`}>
                    {agent.type.charAt(0).toUpperCase() + agent.type.slice(1)}
                  </span>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(agent.status)}`}>
                    {getStatusIcon(agent.status)}
                    <span className="ml-1 capitalize">{agent.status}</span>
                  </span>
                </div>
              </div>

              {/* Resource usage */}
              <div className="mt-4 space-y-3">
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center">
                    <Cpu className="h-4 w-4 mr-2 text-gray-400" />
                    <span className="text-gray-600">CPU</span>
                  </div>
                  <span className={`font-medium ${getResourceColor(agent.cpu)}`}>
                    {agent.cpu}%
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center">
                    <Memory className="h-4 w-4 mr-2 text-gray-400" />
                    <span className="text-gray-600">Memory</span>
                  </div>
                  <span className={`font-medium ${getResourceColor(agent.memory)}`}>
                    {agent.memory}%
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center">
                    <Network className="h-4 w-4 mr-2 text-gray-400" />
                    <span className="text-gray-600">Network</span>
                  </div>
                  <span className={`font-medium ${getResourceColor(agent.network)}`}>
                    {agent.network} MB/s
                  </span>
                </div>
              </div>

              <div className="mt-4 flex items-center justify-between text-sm text-gray-500">
                <div className="flex items-center">
                  <User className="h-4 w-4 mr-1" />
                  {agent.owner}
                </div>
                <div className="flex items-center">
                  <Calendar className="h-4 w-4 mr-1" />
                  {new Date(agent.createdAt).toLocaleDateString()}
                </div>
              </div>

              <div className="mt-4 text-xs text-gray-500">
                Last activity: {agent.lastActivity}
              </div>

              {/* Action buttons */}
              <div className="mt-4 flex space-x-2">
                <button className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                  <Eye className="h-4 w-4 mr-1" />
                  View
                </button>
                <button className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                  <Edit className="h-4 w-4 mr-1" />
                  Edit
                </button>
                {agent.status === 'running' ? (
                  <button className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-yellow-300 text-sm font-medium rounded-md text-yellow-700 bg-yellow-50 hover:bg-yellow-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500">
                    <Pause className="h-4 w-4 mr-1" />
                    Pause
                  </button>
                ) : (
                  <button className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-green-300 text-sm font-medium rounded-md text-green-700 bg-green-50 hover:bg-green-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500">
                    <Play className="h-4 w-4 mr-1" />
                    Start
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {filteredAgents.length === 0 && (
        <div className="text-center py-12">
          <div className="mx-auto h-12 w-12 text-gray-400">
            <Bot className="h-12 w-12" />
          </div>
          <h3 className="mt-2 text-sm font-medium text-gray-900">No agents found</h3>
          <p className="mt-1 text-sm text-gray-500">
            {searchTerm || typeFilter !== 'all' || statusFilter !== 'all'
              ? 'Try adjusting your search or filter criteria.'
              : 'Get started by creating a new AI agent.'
            }
          </p>
          {!searchTerm && typeFilter === 'all' && statusFilter === 'all' && (
            <div className="mt-6">
              <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                <Plus className="h-4 w-4 mr-2" />
                New Agent
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Agents;
