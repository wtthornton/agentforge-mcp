import React from 'react';
import { 
  Activity, 
  Database, 
  Bot, 
  FolderOpen, 
  TrendingUp, 
  AlertCircle,
  CheckCircle,
  Clock
} from 'lucide-react';

const Dashboard: React.FC = () => {
  // Mock data - replace with real data from API
  const stats = [
    { name: 'Active Projects', value: '12', change: '+2', changeType: 'positive', icon: FolderOpen },
    { name: 'Running Agents', value: '8', change: '+1', changeType: 'positive', icon: Bot },
    { name: 'Database Connections', value: '24', change: '0', changeType: 'neutral', icon: Database },
    { name: 'System Health', value: '98%', change: '+1%', changeType: 'positive', icon: Activity },
  ];

  const recentActivities = [
    { id: 1, type: 'success', message: 'Project "AI Analysis" completed successfully', time: '2 minutes ago', icon: CheckCircle },
    { id: 2, type: 'info', message: 'Agent "DataProcessor" started processing', time: '5 minutes ago', icon: Clock },
    { id: 3, type: 'warning', message: 'High memory usage detected on server-02', time: '10 minutes ago', icon: AlertCircle },
    { id: 4, type: 'success', message: 'Database backup completed', time: '1 hour ago', icon: CheckCircle },
  ];

  const getStatusColor = (type: string) => {
    switch (type) {
      case 'success':
        return 'text-green-600 bg-green-100';
      case 'warning':
        return 'text-yellow-600 bg-yellow-100';
      case 'error':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-blue-600 bg-blue-100';
    }
  };

  const getChangeColor = (changeType: string) => {
    switch (changeType) {
      case 'positive':
        return 'text-green-600';
      case 'negative':
        return 'text-red-600';
      default:
        return 'text-gray-600';
    }
  };

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-1 text-sm text-gray-500">
          Overview of your AgentForge system and recent activities
        </p>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.name}
              className="relative overflow-hidden rounded-lg bg-white px-4 py-5 shadow sm:px-6"
            >
              <dt>
                <div className="absolute rounded-md bg-blue-500 p-3">
                  <Icon className="h-6 w-6 text-white" />
                </div>
                <p className="ml-16 truncate text-sm font-medium text-gray-500">
                  {stat.name}
                </p>
              </dt>
              <dd className="ml-16 flex items-baseline">
                <p className="text-2xl font-semibold text-gray-900">{stat.value}</p>
                <p className={`ml-2 flex items-baseline text-sm font-semibold ${getChangeColor(stat.changeType)}`}>
                  {stat.change}
                </p>
              </dd>
            </div>
          );
        })}
      </div>

      {/* Charts and metrics */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* System Performance Chart */}
        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">System Performance</h3>
            <TrendingUp className="h-5 w-5 text-blue-500" />
          </div>
          <div className="h-64 flex items-center justify-center bg-gray-50 rounded-lg">
            <p className="text-sm text-gray-500">Performance chart will be displayed here</p>
          </div>
        </div>

        {/* Recent Activities */}
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Recent Activities</h3>
          <div className="space-y-3">
            {recentActivities.map((activity) => {
              const Icon = activity.icon;
              return (
                <div key={activity.id} className="flex items-start space-x-3">
                  <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${getStatusColor(activity.type)}`}>
                    <Icon className="h-4 w-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900">{activity.message}</p>
                    <p className="text-xs text-gray-500">{activity.time}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <button className="inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
            <Bot className="h-4 w-4 mr-2" />
            Create Agent
          </button>
          <button className="inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500">
            <FolderOpen className="h-4 w-4 mr-2" />
            New Project
          </button>
          <button className="inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500">
            <Database className="h-4 w-4 mr-2" />
            Run Analysis
          </button>
          <button className="inline-flex items-center justify-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
            <Activity className="h-4 w-4 mr-2" />
            View Reports
          </button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
