#!/usr/bin/env node

/**
 * Quick Context Gatherer - Efficiently gathers full project context
 * This tool ensures we always have complete project understanding without wasting time
 */

import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';

class QuickContextGatherer {
    constructor() {
        this.projectRoot = process.cwd();
        this.context = {};
    }

    async gatherFullContext() {
        console.log('🚀 Gathering full project context efficiently...\n');
        
        const startTime = Date.now();
        
        // 1. Quick status check (fastest)
        this.context.status = this.getQuickStatus();
        
        // 2. Compliance overview (fast)
        this.context.compliance = this.getComplianceOverview();
        
        // 3. Implementation status (fast)
        this.context.implementation = this.getImplementationStatus();
        
        // 4. Current tasks (fast)
        this.context.tasks = this.getCurrentTasks();
        
        // 5. Project structure (fast)
        this.context.structure = this.getProjectStructure();
        
        // 6. Recent changes (fast)
        this.context.recent = this.getRecentChanges();
        
        const endTime = Date.now();
        const duration = endTime - startTime;
        
        console.log(`✅ Full context gathered in ${duration}ms\n`);
        
        // Save context for immediate use
        this.saveContext();
        
        // Display summary
        this.displayContextSummary();
        
        return this.context;
    }

    getQuickStatus() {
        try {
            const setupStatus = execSync('node .agent-os/scripts/setup.js status', { encoding: 'utf8' });
            return { setup: setupStatus.trim() };
        } catch (error) {
            return { setup: 'Status check failed', error: error.message };
        }
    }

    getComplianceOverview() {
        try {
            const compliance = execSync('node .agent-os/tools/compliance-checker.js --summary', { encoding: 'utf8' });
            return { summary: compliance.trim() };
        } catch (error) {
            return { summary: 'Compliance check failed', error: error.message };
        }
    }

    getImplementationStatus() {
        try {
            // Check specs folder first (correct .agent-os framework location)
            const tasksPath = path.join(this.projectRoot, '.agent-os/specs/2024-12-19-mcp-server-setup/TASKS.md');
            const roadmapPath = path.join(this.projectRoot, '.agent-os/product/ROADMAP.md');
            
            let content = '';
            let source = '';
            
            if (fs.existsSync(tasksPath)) {
                content = fs.readFileSync(tasksPath, 'utf8');
                source = 'TASKS.md';
            } else if (fs.existsSync(roadmapPath)) {
                content = fs.readFileSync(roadmapPath, 'utf8');
                source = 'ROADMAP.md';
            } else {
                // Fallback to old location
                const planPath = path.join(this.projectRoot, 'implementation/IMPLEMENTATION-PLAN.md');
                if (fs.existsSync(planPath)) {
                    content = fs.readFileSync(planPath, 'utf8');
                    source = 'IMPLEMENTATION-PLAN.md (legacy)';
                } else {
                    return { plan: 'No implementation plan found in .agent-os/specs/ or implementation/', source: 'none' };
                }
            }
            
            // Extract current focus section
            const currentFocusMatch = content.match(/## 🎯 Current Focus[\s\S]*?(?=##|$)/);
            const phaseProgressMatch = content.match(/## 📊 Progress Tracking[\s\S]*?(?=##|$)/);
            
            return { 
                plan: currentFocusMatch ? currentFocusMatch[0].trim() : 'Current focus section not found',
                progress: phaseProgressMatch ? phaseProgressMatch[0].trim() : 'Progress section not found',
                source: source
            };
        } catch (error) {
            return { plan: 'Failed to read implementation plan', error: error.message };
        }
    }

    getCurrentTasks() {
        try {
            const tasksPath = path.join(this.projectRoot, '.agent-os/internal/cursor-rules/tasks.mdc');
            if (fs.existsSync(tasksPath)) {
                const content = fs.readFileSync(tasksPath, 'utf8');
                return { content: content.trim() };
            }
            return { content: 'Tasks file not found' };
        } catch (error) {
            return { content: 'Failed to read tasks', error: error.message };
        }
    }

    getProjectStructure() {
        try {
            const backendPath = path.join(this.projectRoot, 'backend/src/main/java/com/agentforge');
            const frontendPath = path.join(this.projectRoot, 'frontend/src');
            
            const backend = fs.existsSync(backendPath) ? fs.readdirSync(backendPath) : [];
            const frontend = fs.existsSync(frontendPath) ? fs.readdirSync(frontendPath) : [];
            
            return { backend, frontend };
        } catch (error) {
            return { error: error.message };
        }
    }

    getRecentChanges() {
        try {
            const gitStatus = execSync('git status --porcelain', { encoding: 'utf8' });
            const recentCommits = execSync('git log --oneline -5', { encoding: 'utf8' });
            return { 
                status: gitStatus.trim() || 'No changes',
                commits: recentCommits.trim() || 'No commits'
            };
        } catch (error) {
            return { error: error.message };
        }
    }

    saveContext() {
        const contextPath = path.join(this.projectRoot, '.agent-os/reports/quick-context.json');
        const contextDir = path.dirname(contextPath);
        
        if (!fs.existsSync(contextDir)) {
            fs.mkdirSync(contextDir, { recursive: true });
        }
        
        fs.writeFileSync(contextPath, JSON.stringify(this.context, null, 2));
        console.log(`💾 Context saved to: ${contextPath}`);
    }

    displayContextSummary() {
        console.log('📊 PROJECT CONTEXT SUMMARY');
        console.log('==========================');
        
        // Implementation Status
        if (this.context.implementation.plan) {
            console.log(`\n🏗️  IMPLEMENTATION STATUS (${this.context.implementation.source}):`);
            console.log(this.context.implementation.plan);
            
            if (this.context.implementation.progress) {
                console.log(`\n📈 PROGRESS TRACKING:`);
                console.log(this.context.implementation.progress);
            }
        }
        
        // Compliance Status
        if (this.context.compliance.summary) {
            const compliance = this.context.compliance.summary;
            const scoreMatch = compliance.match(/Overall Score: (\d+)/);
            if (scoreMatch) {
                console.log(`\n📈 COMPLIANCE: ${scoreMatch[1]}%`);
            }
        }
        
        // Project Structure
        if (this.context.structure.backend && this.context.structure.frontend) {
            console.log(`\n🏗️  PROJECT STRUCTURE:`);
            console.log(`   Backend: ${this.context.structure.backend.length} components`);
            console.log(`   Frontend: ${this.context.structure.frontend.length} components`);
        }
        
        // Recent Changes
        if (this.context.recent.status && this.context.recent.status !== 'No changes') {
            console.log(`\n📝 RECENT CHANGES: ${this.context.recent.status.split('\n').length} modified files`);
        }
        
        console.log('\n🚀 Ready to continue with full context!');
    }
}

// Run if called directly
if (import.meta.url === `file://${process.argv[1]}`) {
    const gatherer = new QuickContextGatherer();
    gatherer.gatherFullContext().catch(console.error);
}

export default QuickContextGatherer;
