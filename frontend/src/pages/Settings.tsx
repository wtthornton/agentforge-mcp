import React, { useState } from 'react'
import { Button } from '../components/ui/Button'
import { UserProfile } from '../components/auth/UserProfile'
import { 
  Settings as SettingsIcon, 
  Database, 
  Shield, 
  Bell, 
  Palette,
  Save,
  RotateCcw,
  User
} from 'lucide-react'

interface SettingsForm {
  projectName: string
  databaseUrl: string
  enableNotifications: boolean
  enableDarkMode: boolean
  complianceThreshold: number
  logLevel: string
}

const Settings: React.FC = () => {
  const [settings, setSettings] = useState<SettingsForm>({
    projectName: 'AgentForge',
    databaseUrl: 'postgresql://localhost:5432/agentforge',
    enableNotifications: true,
    enableDarkMode: false,
    complianceThreshold: 85,
    logLevel: 'INFO'
  })

  const [isSaving, setIsSaving] = useState(false)

  const handleSave = async () => {
    setIsSaving(true)
    try {
      // TODO: Implement settings save logic
      await new Promise(resolve => setTimeout(resolve, 1000))
      console.log('Settings saved:', settings)
    } catch (error) {
      console.error('Failed to save settings:', error)
    } finally {
      setIsSaving(false)
    }
  }

  const handleReset = () => {
    setSettings({
      projectName: 'AgentForge',
      databaseUrl: 'postgresql://localhost:5432/agentforge',
      enableNotifications: true,
      enableDarkMode: false,
      complianceThreshold: 85,
      logLevel: 'INFO'
    })
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-2">
        <SettingsIcon className="h-8 w-8 text-blue-600" />
        <div>
          <h1 className="text-3xl font-bold">Settings</h1>
          <p className="text-gray-600">
            Configure AgentForge preferences and system settings
          </p>
        </div>
      </div>

      <div className="grid gap-6">
        {/* Profile Settings */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center space-x-2">
              <User className="h-5 w-5" />
              <h3 className="text-lg font-semibold">Profile Settings</h3>
            </div>
            <p className="text-gray-600 text-sm mt-1">
              Manage your account information and preferences
            </p>
          </div>
          <div className="p-6">
            <UserProfile />
          </div>
        </div>

        {/* General Settings */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center space-x-2">
              <SettingsIcon className="h-5 w-5" />
              <h3 className="text-lg font-semibold">General Settings</h3>
            </div>
            <p className="text-gray-600 text-sm mt-1">
              Basic configuration for your AgentForge instance
            </p>
          </div>
          <div className="p-6 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="projectName" className="text-sm font-medium text-gray-700">Project Name</label>
                <input
                  id="projectName"
                  type="text"
                  value={settings.projectName}
                  onChange={(e) => setSettings({ ...settings, projectName: e.target.value })}
                  placeholder="Enter project name"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <div className="space-y-2">
                <label htmlFor="logLevel" className="text-sm font-medium text-gray-700">Log Level</label>
                <select
                  id="logLevel"
                  value={settings.logLevel}
                  onChange={(e) => setSettings({ ...settings, logLevel: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="DEBUG">DEBUG</option>
                  <option value="INFO">INFO</option>
                  <option value="WARN">WARN</option>
                  <option value="ERROR">ERROR</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Database Settings */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Database className="h-5 w-5" />
              <span>Database Configuration</span>
            </CardTitle>
            <CardDescription>
              Database connection and configuration settings
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="databaseUrl">Database URL</Label>
              <Input
                id="databaseUrl"
                value={settings.databaseUrl}
                onChange={(e) => setSettings({ ...settings, databaseUrl: e.target.value })}
                placeholder="postgresql://localhost:5432/agentforge"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="enableNotifications"
                checked={settings.enableNotifications}
                onCheckedChange={(checked) => setSettings({ ...settings, enableNotifications: checked })}
              />
              <Label htmlFor="enableNotifications">Enable database notifications</Label>
            </div>
          </CardContent>
        </Card>

        {/* Compliance Settings */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Shield className="h-5 w-5" />
              <span>Compliance & Standards</span>
            </CardTitle>
            <CardDescription>
              Configure compliance thresholds and enforcement rules
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="complianceThreshold">
                Compliance Threshold: {settings.complianceThreshold}%
              </Label>
              <input
                type="range"
                id="complianceThreshold"
                min="0"
                max="100"
                value={settings.complianceThreshold}
                onChange={(e) => setSettings({ ...settings, complianceThreshold: parseInt(e.target.value) })}
                className="w-full"
              />
              <div className="flex justify-between text-sm text-muted-foreground">
                <span>0%</span>
                <span>50%</span>
                <span>100%</span>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <Badge variant="secondary">Current: 100%</Badge>
              <Badge variant="outline">Target: {settings.complianceThreshold}%</Badge>
            </div>
          </CardContent>
        </Card>

        {/* Appearance Settings */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Palette className="h-5 w-5" />
              <span>Appearance</span>
            </CardTitle>
            <CardDescription>
              Customize the look and feel of AgentForge
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center space-x-2">
              <Switch
                id="enableDarkMode"
                checked={settings.enableDarkMode}
                onCheckedChange={(checked) => setSettings({ ...settings, enableDarkMode: checked })}
              />
              <Label htmlFor="enableDarkMode">Enable dark mode</Label>
            </div>
          </CardContent>
        </Card>

        {/* Notification Settings */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Bell className="h-5 w-5" />
              <span>Notifications</span>
            </CardTitle>
            <CardDescription>
              Configure notification preferences and alerts
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center space-x-2">
              <Switch
                id="enableNotifications"
                checked={settings.enableNotifications}
                onCheckedChange={(checked) => setSettings({ ...settings, enableNotifications: checked })}
              />
              <Label htmlFor="enableNotifications">Enable system notifications</Label>
            </div>
          </CardContent>
        </Card>
      </div>

      <Separator />

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        <Button
          variant="outline"
          onClick={handleReset}
          disabled={isSaving}
        >
          Reset to Defaults
        </Button>
        <Button
          variant="primary"
          onClick={handleSave}
          disabled={isSaving}
          size="md"
        >
          {isSaving ? 'Saving...' : 'Save Settings'}
        </Button>
      </div>
    </div>
  )
}

export default Settings
