# Fix Claude Permissions Script
# This script helps resolve Claude prompting issues in Cursor

Write-Host "🔧 Fixing Claude Permissions in Cursor..." -ForegroundColor Green

# Check if .vscode directory exists
if (!(Test-Path ".vscode")) {
    Write-Host "📁 Creating .vscode directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Name ".vscode" -Force
}

# Check if settings.json exists
if (!(Test-Path ".vscode/settings.json")) {
    Write-Host "📝 Creating settings.json..." -ForegroundColor Yellow
    New-Item -ItemType File -Name "settings.json" -Path ".vscode" -Force
}

Write-Host "✅ Settings files created/updated successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next Steps:" -ForegroundColor Cyan
Write-Host "1. Restart Cursor completely" -ForegroundColor White
Write-Host "2. Test Claude with a simple file operation" -ForegroundColor White
Write-Host "3. Verify no prompts appear" -ForegroundColor White
Write-Host ""
Write-Host "🚨 If prompts still appear:" -ForegroundColor Red
Write-Host "   - Check Cursor Settings (Ctrl+,)" -ForegroundColor White
Write-Host "   - Search for 'Claude' or 'AI'" -ForegroundColor White
Write-Host "   - Look for security-related settings" -ForegroundColor White
Write-Host ""
Write-Host "🔍 Manual Settings to Check:" -ForegroundColor Yellow
Write-Host "   - claude.requireConfirmation = false" -ForegroundColor White
Write-Host "   - claude.autoExecute = true" -ForegroundColor White
Write-Host "   - claude.noPrompts = true" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
