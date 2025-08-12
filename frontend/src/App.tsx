import React from 'react'
import { Routes, Route } from 'react-router-dom'
import { ErrorBoundary } from 'react-error-boundary'
import { AuthProvider } from './hooks/useAuth'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import Projects from './pages/Projects'
import Agents from './pages/Agents'
import Settings from './pages/Settings'
import { Login } from './pages/Login'
import ErrorFallback from './components/ErrorFallback'
import './App.css'

function App() {
  return (
    <ErrorBoundary FallbackComponent={ErrorFallback}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <Layout>
                  <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/projects" element={<Projects />} />
                    <Route 
                      path="/agents" 
                      element={
                        <ProtectedRoute requiredRole="DEVELOPER">
                          <Agents />
                        </ProtectedRoute>
                      } 
                    />
                    <Route path="/settings" element={<Settings />} />
                  </Routes>
                </Layout>
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </ErrorBoundary>
  )
}

export default App
