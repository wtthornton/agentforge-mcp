#!/usr/bin/env node

import fs from 'fs';
import path from 'path';

console.log('🚀 Test gatherer starting...');

try {
    const projectRoot = process.cwd();
    console.log('Project root:', projectRoot);
    
    // Test file reading
    const tasksPath = path.join(projectRoot, '.agent-os/specs/TASKS.md');
    console.log('Tasks path:', tasksPath);
    
    if (fs.existsSync(tasksPath)) {
        console.log('✅ TASKS.md exists');
        const content = fs.readFileSync(tasksPath, 'utf8');
        console.log('Content length:', content.length);
    } else {
        console.log('❌ TASKS.md not found');
    }
    
    console.log('✅ Test completed successfully');
} catch (error) {
    console.error('❌ Error:', error.message);
    console.error('Stack:', error.stack);
}
